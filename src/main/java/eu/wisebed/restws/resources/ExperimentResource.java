package eu.wisebed.restws.resources;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.concurrent.Future;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.sun.grizzly.util.buf.Base64Utils;

import de.uniluebeck.itm.wisebed.cmdlineclient.BeanShellHelper;
import de.uniluebeck.itm.wisebed.cmdlineclient.jobs.JobResult;
import de.uniluebeck.itm.wisebed.cmdlineclient.jobs.JobResult.Result;
import de.uniluebeck.itm.wisebed.cmdlineclient.wrapper.IWsnAsyncWrapper;
import eu.wisebed.api.common.Message;
import eu.wisebed.api.sm.ExperimentNotRunningException_Exception;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.sm.UnknownReservationIdException_Exception;
import eu.wisebed.api.wsn.Program;
import eu.wisebed.api.wsn.ProgramMetaData;
import eu.wisebed.api.wsn.WSN;
import eu.wisebed.restws.WsnInstanceCache;
import eu.wisebed.restws.resources.dto.FlashProgramsRequest;
import eu.wisebed.restws.resources.dto.FlashProgramsRequest.FlashTask;
import eu.wisebed.restws.resources.dto.FlashProgramsStatus;
import eu.wisebed.restws.resources.dto.NodeUrnList;
import eu.wisebed.restws.resources.dto.NodeUrnSuccessMap;
import eu.wisebed.restws.resources.dto.SecretReservationKeyListRs;
import eu.wisebed.restws.resources.dto.SendMessageData;
import eu.wisebed.restws.resources.dto.TwoNodeUrns;
import eu.wisebed.restws.util.Base64Helper;
import eu.wisebed.restws.util.InjectLogger;
import eu.wisebed.restws.util.JaxbHelper;
import eu.wisebed.wiseml.Wiseml;

/**
 * TODO: The following WISEBED functions are not implemented yet:
 * 
 * List<String> getFilters();
 * 
 * List<ChannelHandlerDescription>
 * 
 * getSupportedChannelHandlers();
 * 
 * String getVersion();
 * 
 * String setChannelPipeline(List<String> nodes, List<ChannelHandlerConfiguration> channelHandlerConfigurations);
 * 
 * String setVirtualLink(String sourceNode, String targetNode, String remoteServiceInstance, List<String> parameters,
 * List<String> filters);
 */
@Path("/wisebed/" + Constants.WISEBED_API_VERSION + "/experiments/")
public class ExperimentResource {

	@InjectLogger
	private Logger log;

	@Inject
	private SessionManagement sessions;

	@Inject
	WsnInstanceCache wsnCache;

	@GET
	@Path("network")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getNetworkJson() {
		String wisemlString = sessions.getNetwork();
		try {

			JAXBContext jaxbContext = JAXBContext.newInstance(Wiseml.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Wiseml wiseml = (Wiseml) unmarshaller.unmarshal(new StringReader(wisemlString));
			return Response.ok(JaxbHelper.convertToJSON(wiseml)).build();

		} catch (JAXBException e) {
			return returnError("Unable to retrieve WiseML", e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("network")
	@Produces({ MediaType.APPLICATION_XML })
	public Response getNetworkXml() {
		String wisemlString = sessions.getNetwork();
		return Response.ok(wisemlString).build();
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_PLAIN })
	public Response getInstance(SecretReservationKeyListRs reservationKey) {

		try {

			// TODO Hier muss noch eine Controller-URL übergeben werden
			String experimentInstanceUrl = sessions.getInstance(BeanShellHelper.copyRsToWsn(reservationKey.reservations), null);
			return Response.ok().location(new URI(Base64Helper.encode(experimentInstanceUrl) + "/")).build();

		} catch (ExperimentNotRunningException_Exception e) {
			return returnError("Experiment not running (yet)", e, Status.BAD_REQUEST);
		} catch (UnknownReservationIdException_Exception e) {
			return returnError("Reservation not known to the system", e, Status.BAD_REQUEST);
		} catch (URISyntaxException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * <code>
	 	{
			[
				{"nodeurns" : ["urn:...:0x1234", "urn:...:0x2345", ...], "image" : base64-string },
				{"nodeurns" : ["urn:...:0x1234", "urn:...:0x2345", ...], "image" : base64-string }
			]
		}
	 * </code>
	 * 
	 * @param experimentUrlBase64
	 * @param flashData
	 * @return
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Path("{experimenturl}/flash")
	public Response flashPrograms(@PathParam("experimenturl") String experimentUrlBase64, FlashProgramsRequest flashData) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		// Convert input to the strange flashPrograms format
		LinkedList<Program> programs = new LinkedList<Program>();
		LinkedList<String> nodeUrns = new LinkedList<String>();
		LinkedList<Integer> programIndices = new LinkedList<Integer>();

		for (FlashTask task : flashData.flashTasks) {
			// First, add the program to the list of programs
			Program program = new Program();
			program.setProgram(Base64Utils.decode(task.programBase64));
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

			WSN wsn = wsnCache.get(experimentUrl);
			String taskid = wsn.flashPrograms(nodeUrns, programIndices, programs);
			return Response.ok().location(new URI(Base64Helper.encode(taskid) + "/")).build();

		} catch (URISyntaxException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return returnError(String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * Response looks like: <code>
	 	{
	 		"status" : 
	 		[
	 			"urn:wisebed:...." : 100,
	 			"urn:wisebed:...." : -1,
	 		]
	 	}
	 * </code>
	 * 
	 * @param experimentUrl
	 * @param requestIdBase64
	 * @param flashData
	 * @return
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{experimenturl}/flash/{requestIdBase64}")
	public Response flashProgramsStatus(@PathParam("experimenturl") String experimentUrl, @PathParam("requestIdBase64") String requestIdBase64,
			FlashProgramsRequest flashData) {

		FlashProgramsStatus status = new FlashProgramsStatus();
		// TODO Generate real status or return error
		return Response.ok(JaxbHelper.convertToJSON(status)).build();

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{experimenturl}/resetNodes")
	public Response resetNodes(@PathParam("experimenturl") String experimentUrlBase64, NodeUrnList nodeUrns) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		try {

			IWsnAsyncWrapper wsnAsync = wsnCache.getAyncWrapper(experimentUrl);
			Future<JobResult> future = wsnAsync.resetNodes(nodeUrns.nodeUrns, Constants.TIMEOUT, Constants.TIMEOUT_UNIT);
			return Response.ok(JaxbHelper.convertToJSON(convert(future.get()))).build();

		} catch (URISyntaxException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return returnError(String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR);
		}

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{experimenturl}/areNodesAlive")
	public Response areNodesAlive(@PathParam("experimenturl") String experimentUrlBase64, NodeUrnList nodeUrns) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		try {

			IWsnAsyncWrapper wsnAsync = wsnCache.getAyncWrapper(experimentUrl);
			Future<JobResult> future = wsnAsync.areNodesAlive(nodeUrns.nodeUrns, Constants.TIMEOUT, Constants.TIMEOUT_UNIT);
			return Response.ok(JaxbHelper.convertToJSON(convert(future.get()))).build();

		} catch (URISyntaxException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return returnError(String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR);
		}

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{experimenturl}/send")
	public Response send(@PathParam("experimenturl") String experimentUrlBase64, SendMessageData data) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		try {
			Message message = new Message();
			message.setBinaryData(Base64Utils.decode(data.bytesBase64));
			message.setSourceNodeId(data.sourceNodeUrn);
			message.setTimestamp(DatatypeFactory.newInstance().newXMLGregorianCalendar());

			IWsnAsyncWrapper wsnAsync = wsnCache.getAyncWrapper(experimentUrl);
			Future<JobResult> future = wsnAsync.send(data.nodeUrns, message, Constants.TIMEOUT, Constants.TIMEOUT_UNIT);
			return Response.ok(JaxbHelper.convertToJSON(convert(future.get()))).build();

		} catch (URISyntaxException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return returnError(String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR);
		}

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{experimenturl}/destroyVirtualLink")
	public Response destroyVirtualLink(@PathParam("experimenturl") String experimentUrlBase64, TwoNodeUrns nodeUrns) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		try {

			IWsnAsyncWrapper wsnAsync = wsnCache.getAyncWrapper(experimentUrl);
			Future<JobResult> future = wsnAsync.destroyVirtualLink(nodeUrns.from, nodeUrns.to, Constants.TIMEOUT, Constants.TIMEOUT_UNIT);
			return Response.ok(JaxbHelper.convertToJSON(convert(future.get()))).build();

		} catch (URISyntaxException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return returnError(String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR);
		}

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{experimenturl}/disableNode")
	public Response disableNode(@PathParam("experimenturl") String experimentUrlBase64, String nodeUrn) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		try {

			IWsnAsyncWrapper wsnAsync = wsnCache.getAyncWrapper(experimentUrl);
			Future<JobResult> future = wsnAsync.disableNode(nodeUrn, Constants.TIMEOUT, Constants.TIMEOUT_UNIT);
			return Response.ok(JaxbHelper.convertToJSON(convert(future.get()))).build();

		} catch (URISyntaxException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return returnError(String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR);
		}

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{experimenturl}/enableNode")
	public Response enableNode(@PathParam("experimenturl") String experimentUrlBase64, String nodeUrn) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		try {

			IWsnAsyncWrapper wsnAsync = wsnCache.getAyncWrapper(experimentUrl);
			Future<JobResult> future = wsnAsync.enableNode(nodeUrn, Constants.TIMEOUT, Constants.TIMEOUT_UNIT);
			return Response.ok(JaxbHelper.convertToJSON(convert(future.get()))).build();

		} catch (URISyntaxException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return returnError(String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR);
		}

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{experimenturl}/disablePhysicalLink")
	public Response disablePhysicalLink(@PathParam("experimenturl") String experimentUrlBase64, TwoNodeUrns nodeUrns) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		try {

			IWsnAsyncWrapper wsnAsync = wsnCache.getAyncWrapper(experimentUrl);
			Future<JobResult> future = wsnAsync.disablePhysicalLink(nodeUrns.from, nodeUrns.to, Constants.TIMEOUT, Constants.TIMEOUT_UNIT);
			return Response.ok(JaxbHelper.convertToJSON(convert(future.get()))).build();

		} catch (URISyntaxException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return returnError(String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR);
		}

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{experimenturl}/enablePhysicalLink")
	public Response enablePhysicalLink(@PathParam("experimenturl") String experimentUrlBase64, TwoNodeUrns nodeUrns) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		try {

			IWsnAsyncWrapper wsnAsync = wsnCache.getAyncWrapper(experimentUrl);
			Future<JobResult> future = wsnAsync.enablePhysicalLink(nodeUrns.from, nodeUrns.to, Constants.TIMEOUT, Constants.TIMEOUT_UNIT);
			return Response.ok(JaxbHelper.convertToJSON(convert(future.get()))).build();

		} catch (URISyntaxException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return returnError(String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR);
		}

	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{experimenturl}/network")
	public Response getExperimentNetworkJson(@PathParam("experimenturl") String experimentUrlBase64) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		try {
			IWsnAsyncWrapper wsnAsync = wsnCache.getAyncWrapper(experimentUrl);
			String wisemlString = wsnAsync.getNetwork().get();

			JAXBContext jaxbContext = JAXBContext.newInstance(Wiseml.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Wiseml wiseml = (Wiseml) unmarshaller.unmarshal(new StringReader(wisemlString));

			return Response.ok(JaxbHelper.convertToJSON(wiseml)).build();

		} catch (JAXBException e) {
			return returnError("Unable to retrieve WiseML", e, Status.INTERNAL_SERVER_ERROR);
		} catch (URISyntaxException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return returnError(String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("{experimenturl}/network")
	@Produces({ MediaType.APPLICATION_XML })
	public Response getExperimentNetworkXml(@PathParam("experimenturl") String experimentUrlBase64) {
		String experimentUrl = Base64Helper.decode(experimentUrlBase64);

		try {
			IWsnAsyncWrapper wsnAsync = wsnCache.getAyncWrapper(experimentUrl);
			String wisemlString = wsnAsync.getNetwork().get();
			return Response.ok(wisemlString).build();

		} catch (JAXBException e) {
			return returnError("Unable to retrieve WiseML", e, Status.INTERNAL_SERVER_ERROR);
		} catch (URISyntaxException e) {
			return returnError("Internal error on URL generation", e, Status.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return returnError(String.format("No such experiment: %s (decoded: %s)", experimentUrlBase64, experimentUrl), e,
					Status.INTERNAL_SERVER_ERROR);
		}

	}

	private NodeUrnSuccessMap convert(JobResult result) {
		NodeUrnSuccessMap nodeUrnSuccessMap = new NodeUrnSuccessMap();

		for (String nodeUrn : result.getResults().keySet()) {
			Result r = result.getResults().get(nodeUrn);
			nodeUrnSuccessMap.nodeUrnSuccessMap.put(nodeUrn, r.success);
		}

		return nodeUrnSuccessMap;
	}

	private Response returnError(String msg, Exception e, Status status) {
		log.debug(msg + " :" + e, e);
		String errorMessage = String.format("%s: %s (%s)", msg, e, e.getMessage());
		return Response.status(status).entity(errorMessage).build();
	}

}
