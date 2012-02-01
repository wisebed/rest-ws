package eu.wisebed.restws.resources;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import de.uniluebeck.itm.tr.util.Tuple;
import eu.wisebed.api.rs.*;
import eu.wisebed.api.snaa.SecretAuthenticationKey;
import eu.wisebed.restws.dto.ConfidentialReservationDataList;
import eu.wisebed.restws.dto.PublicReservationDataList;
import eu.wisebed.restws.dto.SnaaSecretAuthenticationKeyList;
import eu.wisebed.restws.dto.SecretReservationKeyListRs;
import eu.wisebed.restws.util.InjectLogger;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

import static eu.wisebed.restws.util.JSONHelper.toJSON;

@Path("/" + Constants.WISEBED_API_VERSION + "/reservations/")
public class RsResource {

	@InjectLogger
	private Logger log;

	@Inject
	private RS rs;

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response listReservations(
			@CookieParam(Constants.COOKIE_SECRET_AUTH_KEY) SnaaSecretAuthenticationKeyList snaaSecretAuthCookie,
			@QueryParam("from") String from, @QueryParam("to") String to) {

		log.debug("Cookie (secret authentication key): {}", snaaSecretAuthCookie);

		try {
			Tuple<XMLGregorianCalendar, XMLGregorianCalendar> duration = convertToDuration(from, to);
			XMLGregorianCalendar fromDate = duration.getFirst();
			XMLGregorianCalendar toDate = duration.getSecond();

			if (snaaSecretAuthCookie == null) {

				List<PublicReservationData> reservations = rs.getReservations(fromDate, toDate);
				PublicReservationDataList list = new PublicReservationDataList(reservations);
				String jsonResponse = toJSON(list);

				log.debug("Listing public reservations from {} until {}: {}",
						new Object[]{fromDate, toDate, jsonResponse}
				);

				return Response.ok(jsonResponse).build();

			} else {
				GetReservations gr = new GetReservations();
				gr.setFrom(fromDate);
				gr.setTo(toDate);

				List<ConfidentialReservationData> reservations = rs.getConfidentialReservations(
						copySnaaToRs(snaaSecretAuthCookie.secretAuthenticationKeys), gr
				);
				String jsonResponse = toJSON(new ConfidentialReservationDataList(reservations));

				log.debug("Listing confidential reservations from {} until {}: {}",
						new Object[]{fromDate, toDate, jsonResponse}
				);
				return Response.ok(jsonResponse).build();
			}

		} catch (IllegalArgumentException e) {
			return returnError("Wrong input, please encode from and to as XMLGregorianCalendar", e, Status.BAD_REQUEST);
		} catch (RSExceptionException e) {
			return returnError("Error while loading data from the reservation system", e, Status.BAD_REQUEST);
		}

	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.TEXT_PLAIN})
	public Response makeReservation(
			@CookieParam(Constants.COOKIE_SECRET_AUTH_KEY) SnaaSecretAuthenticationKeyList snaaSecretAuthCookie,
			PublicReservationData request) {

		if (snaaSecretAuthCookie == null) {
			return returnError("Not logged in", new Exception("Not logged in"), Status.FORBIDDEN);
		}

		try {

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
	 */
	@DELETE
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.TEXT_PLAIN})
	public Response deleteReservation(
			@CookieParam(Constants.COOKIE_SECRET_AUTH_KEY) SnaaSecretAuthenticationKeyList snaaSecretAuthCookie,
			SecretReservationKeyListRs secretReservationKeys) {

		log.debug("Cookie (secret authentication key): {}", snaaSecretAuthCookie);

		if (snaaSecretAuthCookie != null) {
			try {
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
	 */
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response getReservation(SecretReservationKeyListRs secretReservationKeys) {

		try {

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

}
