package eu.wisebed.restws.resources;

import static eu.wisebed.restws.resources.ResourceHelper.getSnaaSecretAuthCookie;
import static eu.wisebed.restws.util.JSONHelper.toJSON;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import de.uniluebeck.itm.tr.util.Tuple;
import eu.wisebed.api.rs.AuthorizationExceptionException;
import eu.wisebed.api.rs.ConfidentialReservationData;
import eu.wisebed.api.rs.Data;
import eu.wisebed.api.rs.GetReservations;
import eu.wisebed.api.rs.PublicReservationData;
import eu.wisebed.api.rs.RS;
import eu.wisebed.api.rs.RSExceptionException;
import eu.wisebed.api.rs.ReservervationConflictExceptionException;
import eu.wisebed.api.rs.ReservervationNotFoundExceptionException;
import eu.wisebed.api.rs.SecretReservationKey;
import eu.wisebed.api.snaa.SecretAuthenticationKey;
import eu.wisebed.restws.dto.ConfidentialReservationDataList;
import eu.wisebed.restws.dto.PublicReservationDataList;
import eu.wisebed.restws.dto.SecretReservationKeyListRs;
import eu.wisebed.restws.dto.SnaaSecretAuthenticationKeyList;
import eu.wisebed.restws.proxy.WebServiceEndpointManager;
import eu.wisebed.restws.util.InjectLogger;

@Path("/" + Constants.WISEBED_API_VERSION + "/{testbedId}/reservations")
public class RsResource {

	@InjectLogger
	private Logger log;

	@Inject
	private WebServiceEndpointManager endpointManager;

	@Context
	private HttpHeaders httpHeaders;

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response listReservations(@PathParam("testbedId") final String testbedId,
									 @QueryParam("from") final String from,
									 @QueryParam("to") final String to,
									 @QueryParam("userOnly") @DefaultValue("false") final boolean userOnly) {

		try {

			Object response = userOnly ?
					getConfidentialReservations(testbedId, getSnaaSecretAuthCookie(httpHeaders, testbedId), from, to) :
					getPublicReservations(testbedId, from, to);

			return Response.ok(toJSON(response)).build();

		} catch (IllegalArgumentException e) {
			return returnError("Wrong input, please encode from and to as XMLGregorianCalendar", e, Status.BAD_REQUEST);
		} catch (RSExceptionException e) {
			return returnError("Error while loading data from the reservation system", e, Status.BAD_REQUEST);
		}

	}

	private PublicReservationDataList getPublicReservations(final String testbedId, final String from, final String to)
			throws RSExceptionException {
		RS rs = endpointManager.getRsEndpoint(testbedId);

		Tuple<XMLGregorianCalendar, XMLGregorianCalendar> duration = convertToDuration(from, to);
		XMLGregorianCalendar fromDate = duration.getFirst();
		XMLGregorianCalendar toDate = duration.getSecond();

		List<PublicReservationData> reservations = rs.getReservations(fromDate, toDate);
		return new PublicReservationDataList(reservations);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response makeReservation(@PathParam("testbedId") final String testbedId,
									PublicReservationData request) {

		SnaaSecretAuthenticationKeyList snaaSecretAuthCookie = getSnaaSecretAuthCookie(httpHeaders, testbedId);

		try {

			RS rs = endpointManager.getRsEndpoint(testbedId);

			ConfidentialReservationData confidentialReservation =
					createFrom(snaaSecretAuthCookie.secretAuthenticationKeys, request);

			List<SecretReservationKey> reservation =
					rs.makeReservation(copySnaaToRs(snaaSecretAuthCookie.secretAuthenticationKeys),
							confidentialReservation
					);

			String jsonResponse = toJSON(new SecretReservationKeyListRs(reservation));
			log.debug("Made reservation: {}", jsonResponse);
			return Response.ok(jsonResponse).build();

		} catch (AuthorizationExceptionException e) {
			return returnError("Authorization problem occured", e, Status.UNAUTHORIZED);
		} catch (RSExceptionException e) {
			return returnError("Error in the reservation system", e, Status.INTERNAL_SERVER_ERROR);
		} catch (ReservervationConflictExceptionException e) {
			return returnError("Another reservation is in conflict with yours", e, Status.BAD_REQUEST);
		}

	}

	/**
	 * Deletes a single reservation. public void deleteReservation(List<SecretAuthenticationKey> authenticationData,
	 * List<SecretReservationKey> secretReservationKey)
	 *
	 * @param testbedId
	 * @param secretReservationKeys
	 *
	 * @return
	 */
	@DELETE
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.TEXT_PLAIN})
	public Response deleteReservation(@PathParam("testbedId") final String testbedId,
									  SecretReservationKeyListRs secretReservationKeys) {

		SnaaSecretAuthenticationKeyList snaaSecretAuthCookie = getSnaaSecretAuthCookie(httpHeaders, testbedId);

		log.debug("Cookie (secret authentication key): {}", snaaSecretAuthCookie);

		if (snaaSecretAuthCookie != null) {
			try {

				RS rs = endpointManager.getRsEndpoint(testbedId);
				rs.deleteReservation(copySnaaToRs(snaaSecretAuthCookie.secretAuthenticationKeys),
						secretReservationKeys.reservations
				);
				return Response.ok("Ok, deleted reservation").build();

			} catch (RSExceptionException e) {
				return returnError("Error while communicating with the reservation server", e,
						Status.INTERNAL_SERVER_ERROR
				);
			} catch (ReservervationNotFoundExceptionException e) {
				return returnError("Reservation not found", e, Status.BAD_REQUEST);
			}
		}
		return returnError("Not logged in", new Exception("Not logged in"), Status.FORBIDDEN);
	}

	/**
	 * Returns data about a single reservation (including confidential information).
	 * <p/>
	 * WISEBED API function:
	 * <p/>
	 * public List<ConfidentialReservationData> getReservation(List<SecretReservationKey> secretReservationKey)
	 *
	 * @param testbedId
	 * @param secretReservationKeys
	 *
	 * @return
	 */
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response getReservation(@PathParam("testbedId") final String testbedId,
								   SecretReservationKeyListRs secretReservationKeys) {

		try {

			RS rs = endpointManager.getRsEndpoint(testbedId);

			List<ConfidentialReservationData> reservation = rs.getReservation(secretReservationKeys.reservations);
			String jsonResponse = toJSON(new ConfidentialReservationDataList(reservation));
			log.debug("Get reservation data for {}: {}", toJSON(secretReservationKeys), jsonResponse);
			return Response.ok(jsonResponse).build();

		} catch (RSExceptionException e) {
			return returnError("Error while communicating with the reservation server", e, Status.INTERNAL_SERVER_ERROR
			);
		} catch (ReservervationNotFoundExceptionException e) {
			return returnError("Reservation not found", e, Status.BAD_REQUEST);
		}
	}

	private Response returnError(String msg, Exception e, Status status) {
		log.debug(msg + " :" + e, e);
		String errorMessage = String.format("%s: %s (%s)", msg, e, e.getMessage());
		return Response.status(status).entity(errorMessage).build();
	}

	private ConfidentialReservationData createFrom(List<SecretAuthenticationKey> secretAuthenticationKeys,
												   PublicReservationData reservation) {

		ConfidentialReservationData confidentialReservation = new ConfidentialReservationData();
		for (SecretAuthenticationKey key : secretAuthenticationKeys) {
			Data data = new Data();
			data.setUrnPrefix(key.getUrnPrefix());
			data.setUsername(key.getUsername());
			data.setSecretReservationKey(key.getSecretAuthenticationKey());
			confidentialReservation.getData().add(data);
		}

		confidentialReservation.getNodeURNs().addAll(reservation.getNodeURNs());
		confidentialReservation.setFrom(reservation.getFrom());
		confidentialReservation.setTo(reservation.getTo());
		confidentialReservation.setUserData(reservation.getUserData());

		return confidentialReservation;
	}

	private Tuple<XMLGregorianCalendar, XMLGregorianCalendar> convertToDuration(String from, String to)
			throws IllegalArgumentException {

		try {

			if (from == null || "".equals(from)) {
				from = DatatypeFactory.newInstance().newXMLGregorianCalendar(new DateTime().toGregorianCalendar())
						.toString();
			}

			if (to == null || "".equals(to)) {
				to = DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(new DateTime().plusDays(1).toGregorianCalendar()).toString();
			}

			XMLGregorianCalendar fromDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(from);
			XMLGregorianCalendar toDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(to);

			return new Tuple<XMLGregorianCalendar, XMLGregorianCalendar>(fromDate, toDate);

		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException("Unable to create a DataType factory: " + e, e);
		}

	}

	private static List<eu.wisebed.api.rs.SecretAuthenticationKey> copySnaaToRs(
			List<SecretAuthenticationKey> snaaKeys) {

		List<eu.wisebed.api.rs.SecretAuthenticationKey> secretAuthKeys =
				Lists.newArrayListWithCapacity(snaaKeys.size());

		for (SecretAuthenticationKey snaaKey : snaaKeys) {

			eu.wisebed.api.rs.SecretAuthenticationKey key = new eu.wisebed.api.rs.SecretAuthenticationKey();

			key.setSecretAuthenticationKey(snaaKey.getSecretAuthenticationKey());
			key.setUrnPrefix(snaaKey.getUrnPrefix());
			key.setUsername(snaaKey.getUsername());

			secretAuthKeys.add(key);
		}

		return secretAuthKeys;
	}

	private ConfidentialReservationDataList getConfidentialReservations(final String testbedId,
																		final SnaaSecretAuthenticationKeyList snaaSecretAuthenticationKeyList,
																		final String from, final String to)
			throws RSExceptionException {
		RS rs = endpointManager.getRsEndpoint(testbedId);

		Tuple<XMLGregorianCalendar, XMLGregorianCalendar> duration = convertToDuration(from, to);
		XMLGregorianCalendar fromDate = duration.getFirst();
		XMLGregorianCalendar toDate = duration.getSecond();

		GetReservations getReservations = new GetReservations();
		getReservations.setFrom(fromDate);
		getReservations.setTo(toDate);

		List<eu.wisebed.api.rs.SecretAuthenticationKey> rsSAKs =
				copySnaaToRs(snaaSecretAuthenticationKeyList.secretAuthenticationKeys);

		return new ConfidentialReservationDataList(rs.getConfidentialReservations(rsSAKs, getReservations));
	}
}
