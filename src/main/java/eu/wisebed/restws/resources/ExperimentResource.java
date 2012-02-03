package eu.wisebed.restws.resources;

import com.google.inject.Inject;
import com.sun.jersey.core.util.Base64;
import eu.wisebed.api.common.Message;
import eu.wisebed.api.rs.*;
import eu.wisebed.api.sm.ExperimentNotRunningException_Exception;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.sm.UnknownReservationIdException_Exception;
import eu.wisebed.api.wsn.Program;
import eu.wisebed.api.wsn.ProgramMetaData;
import eu.wisebed.restws.WisebedRestServerConfig;
import eu.wisebed.restws.dto.*;
import eu.wisebed.restws.dto.FlashProgramsRequest.FlashTask;
import eu.wisebed.restws.jobs.Job;
import eu.wisebed.restws.jobs.JobNodeStatus;
import eu.wisebed.restws.proxy.WebServiceEndpointManager;
import eu.wisebed.restws.proxy.WsnProxy;
import eu.wisebed.restws.proxy.WsnProxyManager;
import eu.wisebed.restws.util.Base64Helper;
import eu.wisebed.restws.util.InjectLogger;
import eu.wisebed.restws.util.JSONHelper;
import eu.wisebed.wiseml.Wiseml;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * TODO: The following WISEBED functions are not implemented yet:
 * <p/>
 * List<String> getFilters();<br/>
 * List<ChannelHandlerDescription> getSupportedChannelHandlers();<br/>
 * String getVersion();<br/>
 * String setChannelPipeline(List<String> nodes, List<ChannelHandlerConfiguration> channelHandlerConfigurations);<br/>
 * String setVirtualLink(String sourceNode, String targetNode, String remoteServiceInstance, List<String>
 * parameters,<br/>
 * List<String> filters);
 */
@Path("/" + Constants.WISEBED_API_VERSION + "/{testbedId}/experiments/")
public class ExperimentResource {

	@InjectLogger
	private Logger log;

	@Inject
	private WebServiceEndpointManager endpointManager;

	@Inject
	private WsnProxyManager wsnProxyManager;

	@Context
	private UriInfo uriInfo;

	@Inject
	private WisebedRestServerConfig config;

	@GET
	@Path("network")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getNetworkJson(@PathParam("testbedId") final String testbedId) {


		try {

			SessionManagement sessions = endpointManager.getSmEndpoint(testbedId);
			String wisemlString = sessions.getNetwork();

			JAXBContext jaxbContext = JAXBContext.newInstance(Wiseml.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Wiseml wiseml = (Wiseml) unmarshaller.unmarshal(new StringReader(wisemlString));

			String jsonString = JSONHelper.toJSON(wiseml);
			log.trace("Returning JSON representation of WiseML: {}", jsonString);
			return Response.ok(jsonString).build();

		} catch (JAXBException e) {
			return returnError("Unable to retrieve WiseML", e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("network")
	@Produces({MediaType.APPLICATION_XML})
	public Response getNetworkXml(@PathParam("testbedId") final String testbedId) {

		SessionManagement sessions = endpointManager.getSmEndpoint(testbedId);
		String wisemlString = sessions.getNetwork();
		log.trace("Returning WiseML: {}", wisemlString);
		return Response.ok(wisemlString).build();
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.TEXT_PLAIN})
	public Response getInstance(@PathParam("testbedId") final String testbedId,
								SecretReservationKeyListRs reservationKey) {

		DateTime now = DateTime.now();
		DateTime earliestFrom = new DateTime();
		DateTime latestUntil = new DateTime();

		try {

			RS rs = endpointManager.getRsEndpoint(testbedId);
			List<ConfidentialReservationData> reservation = rs.getReservation(reservationKey.reservations);

			for (ConfidentialReservationData data : reservation) {

				DateTime from = new DateTime(data.getFrom().toGregorianCalendar());
				if (from.isBefore(earliestFrom)) {
					earliestFrom = from;
				}

				DateTime until = new DateTime(data.getTo().toGregorianCalendar());
				if (until.isAfter(latestUntil)) {
					latestUntil = until;
				}
			}

			if (earliestFrom.isAfter(now)) {
				return Response
						.status(Status.BAD_REQUEST)
						.entity("The reservation time span lies in the future! Please try to reconnect after "
								+ earliestFrom.toString(ISODateTimeFormat.basicDateTimeNoMillis())
								+ "."
						)
						.build();
			}

			if (latestUntil.isBefore(now)) {
				return Response
						.status(Status.BAD_REQUEST)
						.entity("The reservation time span lies in the past! It ended on "
								+ latestUntil.toString(ISODateTimeFormat.basicDateTimeNoMillis())
								+ ". You can not connect to the experiment after it has ended."
						)
						.build();
			}

		} catch (RSExceptionException e) {

			return Response.serverError().entity(e).build();

		} catch (ReservervationNotFoundExceptionException e) {

			return Response
					.status(Status.NOT_FOUND)
					.entity("No reservation with the given secret reservation keys could be found!")
					.build();
		}

		try {

			SessionManagement sessions = endpointManager.getSmEndpoint(testbedId);
			String experimentWsnInstanceUrl = sessions.getInstance(
					copyRsToWsn(reservationKey.reservations),
					"NONE"
			);

			wsnProxyManager.create(experimentWsnInstanceUrl, latestUntil);
			String controllerEndpointUrl = wsnProxyManager.getControllerEndpointUrl(experimentWsnInstanceUrl);
			WsnProxy wsnProxy = wsnProxyManager.get(experimentWsnInstanceUrl);
			if (wsnProxy == null) {
				throw new RuntimeException("This should not happen ever :(");
			}
			wsnProxy.addController(controllerEndpointUrl).get();

			URI location = UriBuilder
					.fromUri(uriInfo.getRequestUri())
					.path("{experimentUrlBase64}")
					.build(Base64Helper.encode(experimentWsnInstanceUrl));

			log.debug("Returning instance URL {}", location.toString());

			return Response.ok().location(location).build();

		} catch (ExperimentNotRunningException_Exception e) {
			return returnError("Experiment not running", e, Status.BAD_REQUEST);
		} catch (UnknownReservationIdException_Exception e) {
			return returnError("Reservation not known to the system", e, Status.BAD_REQUEST);
		} catch (InterruptedException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		} catch (ExecutionException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	private static List<eu.wisebed.api.sm.SecretReservationKey> copyRsToWsn(List<SecretReservationKey> keys) {
		List<eu.wisebed.api.sm.SecretReservationKey> newKeys = new ArrayList<eu.wisebed.api.sm.SecretReservationKey>();
		for (SecretReservationKey key : keys) {
			eu.wisebed.api.sm.SecretReservationKey newKey =
					new eu.wisebed.api.sm.SecretReservationKey();
			newKey.setSecretReservationKey(key.getSecretReservationKey());
			newKey.setUrnPrefix(key.getUrnPrefix());
			newKeys.add(newKey);
		}
		return newKeys;
	}

	/**
	 * <code>
	 * {
	 * [
	 * {"nodeUrns" : ["urn:...:0x1234", "urn:...:0x2345", ...], "imageBase64" : base64-string },
	 * {"nodeUrns" : ["urn:...:0x1234", "urn:...:0x2345", ...], "imageBase64" : base64-string }
	 * ]
	 * }
	 * </code>
	 *
	 * @param experimentUrlBase64
	 * 		the base64-encoded URL of the experiment
	 * @param flashData
	 * 		the data to flash onto the nodes
	 *
	 * @return a response
	 */
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("{experimentUrlBase64}/flash")
	public Response flashPrograms(@PathParam("experimentUrlBase64") final String experimentUrlBase64,
								  final FlashProgramsRequest flashData) {

		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		log.debug("Flash request received");

		// Convert input to the strange flashPrograms format
		LinkedList<Program> programs = new LinkedList<Program>();
		LinkedList<String> nodeUrns = new LinkedList<String>();
		LinkedList<Integer> programIndices = new LinkedList<Integer>();

		for (FlashTask task : flashData.flashTasks) {
			// First, add the program to the list of programs
			Program program = new Program();
			program.setProgram(extractByteArrayFromDataURL(task.imageBase64));
			ProgramMetaData metaData = new ProgramMetaData();
			program.setMetaData(metaData);
			programs.addLast(program);
			int programIndex = programs.size() - 1;

			// Then add the node URNs and the program index
			for (String urn : task.nodeUrns) {
				nodeUrns.addLast(urn);
				programIndices.addLast(programIndex);
			}
		}

		// Invoke the call and redirect the caller
		try {

			WsnProxy wsn = wsnProxyManager.get(experimentUrl);

			if (wsn == null) {
				return createExperimentNotFoundResponse(experimentUrlBase64);
			}

			String requestId = wsn.flashPrograms(nodeUrns, programIndices, programs,
					config.flashTimeoutMillis, TimeUnit.MILLISECONDS
			);

			URI location = UriBuilder
					.fromUri(uriInfo.getRequestUri())
					.path("{requestId}")
					.build(Base64Helper.encode(requestId));

			return Response.ok().location(location).build();

		} catch (Exception e) {
			return returnError(
					String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR
			);
		}

	}

	private Response createExperimentNotFoundResponse(final String experimentUrlBase64) {
		return Response
				.status(Status.BAD_REQUEST)
				.entity("An experiment with the experimentUrl "
						+ experimentUrlBase64 +
						" has not been found! Did you POST to /experiments before?"
				).build();
	}

	private byte[] extractByteArrayFromDataURL(String dataURL) {
		// data:[<mediatype>][;base64]
		int commaPos = dataURL.indexOf(',');
		String header = dataURL.substring(0, commaPos);
		if (!header.endsWith(";base64")) {
			throw new RuntimeException("Data URLs are only supported with base64 encoding!");
		}
		return Base64.decode(dataURL.substring(commaPos + 1).getBytes());
	}

	/**
	 * Response looks like: <code>
	 * {
	 * "status" :
	 * [
	 * "urn:wisebed:...." : 100,
	 * "urn:wisebed:...." : -1,
	 * ]
	 * }
	 * </code>
	 *
	 * @param experimentUrlBase64
	 * 		the base64-encoded URL of the experiment
	 * @param requestIdBase64
	 * 		the base64-encoded requestId of the flash operation
	 *
	 * @return the current state of the flash operation
	 */
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{experimentUrlBase64}/flash/{requestIdBase64}")
	public Response flashProgramsStatus(@PathParam("experimentUrlBase64") final String experimentUrlBase64,
										@PathParam("requestIdBase64") final String requestIdBase64) {

		String experimentUrl = Base64Helper.decode(experimentUrlBase64);
		String requestId = Base64Helper.decode(requestIdBase64);

		WsnProxy wsnProxy = wsnProxyManager.get(experimentUrl);
		if (wsnProxy == null) {
			return createExperimentNotFoundResponse(experimentUrlBase64);
		}

		Job job = wsnProxyManager.getJob(experimentUrl, requestId);
		if (job == null) {
			return Response.status(Status.NOT_FOUND).entity("No job with requestId " + requestId + " found!").build();
		}

		NodeUrnStatusMap nodeUrnStatusMap = buildNodeUrnStatusMap(job.getJobNodeStates());

		return Response.ok(JSONHelper.toJSON(nodeUrnStatusMap)).build();
	}

	private NodeUrnStatusMap buildNodeUrnStatusMap(final Map<String, JobNodeStatus> jobNodeStates) {
		NodeUrnStatusMap nodeUrnStatusMap = new NodeUrnStatusMap();
		for (Map.Entry<String, JobNodeStatus> entry : jobNodeStates.entrySet()) {
			nodeUrnStatusMap.map.put(entry.getKey(), entry.getValue());
		}
		return nodeUrnStatusMap;
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{experimentUrlBase64}/resetNodes")
	public Response resetNodes(@PathParam("experimentUrlBase64") String experimentUrlBase64, NodeUrnList nodeUrns) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);
		log.debug("Received request to reset nodes: {}", nodeUrns);

		try {

			WsnProxy wsnProxy = wsnProxyManager.get(experimentUrl);
			if (wsnProxy == null) {
				return createExperimentNotFoundResponse(experimentUrlBase64);
			}

			Job job = wsnProxy.resetNodes(
					nodeUrns.nodeUrns,
					config.operationTimeoutMillis,
					TimeUnit.MILLISECONDS
			).get();
			NodeUrnStatusMap nodeUrnStatusMap = buildNodeUrnStatusMap(job.getJobNodeStates());
			return Response.ok(JSONHelper.toJSON(nodeUrnStatusMap)).build();

		} catch (Exception e) {
			return returnError(
					String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR
			);
		}

	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{experimentUrlBase64}/areNodesAlive")
	public Response areNodesAlive(@PathParam("experimentUrlBase64") String experimentUrlBase64, NodeUrnList nodeUrns) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);
		log.debug("Received request to check for alive nodes: {}", nodeUrns);

		try {

			WsnProxy wsnProxy = wsnProxyManager.get(experimentUrl);
			if (wsnProxy == null) {
				return createExperimentNotFoundResponse(experimentUrlBase64);
			}
			Job job = wsnProxy.areNodesAlive(
					nodeUrns.nodeUrns,
					config.operationTimeoutMillis,
					TimeUnit.MILLISECONDS
			).get();
			NodeUrnStatusMap nodeUrnStatusMap = buildNodeUrnStatusMap(job.getJobNodeStates());
			return Response.ok(JSONHelper.toJSON(nodeUrnStatusMap)).build();

		} catch (Exception e) {
			return returnError(
					String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR
			);
		}

	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{experimentUrlBase64}/send")
	public Response send(@PathParam("experimentUrlBase64") String experimentUrlBase64, SendMessageData data) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);
		log.debug("Received request to send data:  {}", data);

		try {
			Message message = new Message();
			message.setBinaryData(Base64.decode(data.bytesBase64));
			message.setSourceNodeId(data.sourceNodeUrn);
			message.setTimestamp(DatatypeFactory.newInstance().newXMLGregorianCalendar());

			WsnProxy wsnProxy = wsnProxyManager.get(experimentUrl);
			if (wsnProxy == null) {
				return createExperimentNotFoundResponse(experimentUrlBase64);
			}
			Job job = wsnProxy.send(data.nodeUrns, message, config.operationTimeoutMillis, TimeUnit.MILLISECONDS).get();
			NodeUrnStatusMap nodeUrnStatusMap = buildNodeUrnStatusMap(job.getJobNodeStates());
			return Response.ok(JSONHelper.toJSON(nodeUrnStatusMap)).build();

		} catch (Exception e) {
			return returnError(
					String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR
			);
		}

	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{experimentUrlBase64}/destroyVirtualLink")
	public Response destroyVirtualLink(@PathParam("experimentUrlBase64") String experimentUrlBase64,
									   TwoNodeUrns nodeUrns) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);
		log.debug("Received request to destroy virtual link:  {}->{}", nodeUrns.from, nodeUrns.to);

		try {

			WsnProxy wsnProxy = wsnProxyManager.get(experimentUrl);
			if (wsnProxy == null) {
				return createExperimentNotFoundResponse(experimentUrlBase64);
			}
			Job job = wsnProxy.destroyVirtualLink(
					nodeUrns.from,
					nodeUrns.to,
					config.operationTimeoutMillis,
					TimeUnit.MILLISECONDS
			).get();
			NodeUrnStatusMap nodeUrnStatusMap = buildNodeUrnStatusMap(job.getJobNodeStates());
			return Response.ok(JSONHelper.toJSON(nodeUrnStatusMap)).build();

		} catch (Exception e) {
			return returnError(
					String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR
			);
		}

	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{experimentUrlBase64}/disableNode")
	public Response disableNode(@PathParam("experimentUrlBase64") String experimentUrlBase64, String nodeUrn) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);
		log.debug("Received request to disable node:  {}", nodeUrn);

		try {

			WsnProxy wsnProxy = wsnProxyManager.get(experimentUrl);
			if (wsnProxy == null) {
				return createExperimentNotFoundResponse(experimentUrlBase64);
			}
			Job job = wsnProxy.disableNode(
					nodeUrn,
					config.operationTimeoutMillis,
					TimeUnit.MILLISECONDS
			).get();
			NodeUrnStatusMap nodeUrnStatusMap = buildNodeUrnStatusMap(job.getJobNodeStates());
			return Response.ok(JSONHelper.toJSON(nodeUrnStatusMap)).build();

		} catch (Exception e) {
			return returnError(
					String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR
			);
		}

	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{experimentUrlBase64}/enableNode")
	public Response enableNode(@PathParam("experimentUrlBase64") String experimentUrlBase64, String nodeUrn) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);
		log.debug("Received request to enable node:  {}", nodeUrn);

		try {

			WsnProxy wsnProxy = wsnProxyManager.get(experimentUrl);
			if (wsnProxy == null) {
				return createExperimentNotFoundResponse(experimentUrlBase64);
			}

			Job job = wsnProxy.enableNode(
					nodeUrn,
					config.operationTimeoutMillis,
					TimeUnit.MILLISECONDS
			).get();
			NodeUrnStatusMap nodeUrnStatusMap = buildNodeUrnStatusMap(job.getJobNodeStates());
			return Response.ok(JSONHelper.toJSON(nodeUrnStatusMap)).build();

		} catch (Exception e) {
			return returnError(
					String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR
			);
		}

	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{experimentUrlBase64}/disablePhysicalLink")
	public Response disablePhysicalLink(@PathParam("experimentUrlBase64") String experimentUrlBase64,
										TwoNodeUrns nodeUrns) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);
		log.debug("Received request to disable physical link:  {}->{}", nodeUrns.from, nodeUrns.to);

		try {

			WsnProxy wsnProxy = wsnProxyManager.get(experimentUrl);
			if (wsnProxy == null) {
				return createExperimentNotFoundResponse(experimentUrlBase64);
			}
			Job job = wsnProxy.disablePhysicalLink(
					nodeUrns.from,
					nodeUrns.to,
					config.operationTimeoutMillis,
					TimeUnit.MILLISECONDS
			).get();
			NodeUrnStatusMap nodeUrnStatusMap = buildNodeUrnStatusMap(job.getJobNodeStates());
			return Response.ok(JSONHelper.toJSON(nodeUrnStatusMap)).build();

		} catch (Exception e) {
			return returnError(
					String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR
			);
		}

	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{experimentUrlBase64}/enablePhysicalLink")
	public Response enablePhysicalLink(@PathParam("experimentUrlBase64") String experimentUrlBase64,
									   TwoNodeUrns nodeUrns) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);
		log.debug("Received request to enable physical link:  {}->{}", nodeUrns.from, nodeUrns.to);

		try {

			WsnProxy wsnProxy = wsnProxyManager.get(experimentUrl);
			if (wsnProxy == null) {
				return createExperimentNotFoundResponse(experimentUrlBase64);
			}

			Job job = wsnProxy.enablePhysicalLink(
					nodeUrns.from,
					nodeUrns.to,
					config.operationTimeoutMillis,
					TimeUnit.MILLISECONDS
			).get();
			NodeUrnStatusMap nodeUrnStatusMap = buildNodeUrnStatusMap(job.getJobNodeStates());
			return Response.ok(JSONHelper.toJSON(nodeUrnStatusMap)).build();

		} catch (Exception e) {
			return returnError(
					String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR
			);
		}

	}

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{experimentUrlBase64}/network")
	public Response getExperimentNetworkJson(@PathParam("experimentUrlBase64") String experimentUrlBase64) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		try {

			WsnProxy wsnProxy = wsnProxyManager.get(experimentUrl);
			if (wsnProxy == null) {
				return createExperimentNotFoundResponse(experimentUrlBase64);
			}
			String wisemlString = wsnProxy.getNetwork().get();

			JAXBContext jaxbContext = JAXBContext.newInstance(Wiseml.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Wiseml wiseml = (Wiseml) unmarshaller.unmarshal(new StringReader(wisemlString));

			String json = JSONHelper.toJSON(wiseml);
			log.debug("Returning network for experiment {} as json: {}", experimentUrl, json);
			return Response.ok(json).build();

		} catch (JAXBException e) {
			return returnError("Unable to retrieve WiseML", e, Status.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return returnError(
					String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR
			);
		}
	}

	@GET
	@Path("{experimentUrlBase64}/network")
	@Produces({MediaType.APPLICATION_XML})
	public Response getExperimentNetworkXml(@PathParam("experimentUrlBase64") String experimentUrlBase64) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		try {

			WsnProxy wsnProxy = wsnProxyManager.get(experimentUrl);
			if (wsnProxy == null) {
				return createExperimentNotFoundResponse(experimentUrlBase64);
			}
			String wisemlString = wsnProxy.getNetwork().get();
			log.debug("Returning network for experiment {} as xml: {}", experimentUrl, wisemlString);
			return Response.ok(wisemlString).build();

		} catch (Exception e) {
			return returnError(
					String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR
			);
		}

	}

	private Response returnError(String msg, Exception e, Status status) {
		log.debug(msg + " :" + e, e);
		String errorMessage = String.format("%s: %s (%s)", msg, e, e.getMessage());
		return Response.status(status).entity(errorMessage).build();
	}

}
