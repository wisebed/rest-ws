package eu.wisebed.restws.resources;

import static eu.wisebed.restws.util.JaxbHelper.convertToJSON;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import eu.wisebed.api.rs.ConfidentialReservationData;
import eu.wisebed.api.rs.GetReservations;
import eu.wisebed.api.rs.PublicReservationData;
import eu.wisebed.api.rs.RS;
import eu.wisebed.api.rs.RSExceptionException;
import eu.wisebed.api.rs.ReservervationNotFoundExceptionException;
import eu.wisebed.api.rs.SecretReservationKey;
import eu.wisebed.api.snaa.SecretAuthenticationKey;
import eu.wisebed.restws.resources.SnaaResource.SecretAuthenticationKeyList;
import eu.wisebed.restws.util.InjectLogger;

@Path("/wisebed/" + Constants.WISEBED_API_VERSION + "/reservations/")
public class RsResource {

	@InjectLogger
	private Logger log;

	@Inject
	private RS rs;

	@XmlRootElement
	public static class PublicReservationDataList {
		List<PublicReservationData> reservations;

		public PublicReservationDataList(List<PublicReservationData> reservations) {
			this.reservations = reservations;
		}

	}

	@XmlRootElement
	public static class ConfidentialReservationDataList {
		List<ConfidentialReservationData> reservations;

		public ConfidentialReservationDataList(List<ConfidentialReservationData> reservations) {
			this.reservations = reservations;
		}

	}

	@XmlRootElement
	public static class SecretReservationKeyList {
		List<SecretReservationKey> reservations;

		public SecretReservationKeyList(List<SecretReservationKey> reservations) {
			this.reservations = reservations;
		}

	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response listReservations(@CookieParam(Constants.COOKIE_WISEBED_SECRET_AUTHENTICATION_KEY) SecretAuthenticationKeyList secretAuthCookie,
			@QueryParam("from") String from, @QueryParam("to") String to) {

		log.debug("Cookie (secret authentication key): {}", secretAuthCookie);

		
		try {
			if( from == null || "".equals(from))
				from = DatatypeFactory.newInstance().newXMLGregorianCalendar(new DateTime().toGregorianCalendar()).toString();
			
			if( to == null || "".equals(to))
				to = DatatypeFactory.newInstance().newXMLGregorianCalendar(new DateTime().plusDays(1).toGregorianCalendar()).toString(); 
			
			XMLGregorianCalendar fromDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(from);
			XMLGregorianCalendar toDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(to);

			if (secretAuthCookie == null) {

				List<PublicReservationData> reservations = rs.getReservations(fromDate, toDate);
				String jsonResponse = convertToJSON(new PublicReservationDataList(reservations));

				log.debug("Listing public reservations from {} until {}: {}", new Object[] { from, to, jsonResponse });
				return Response.ok(jsonResponse).build();

			} else {
				GetReservations gr = new GetReservations();
				gr.setFrom(fromDate);
				gr.setTo(toDate);
				List<ConfidentialReservationData> reservations = rs.getConfidentialReservations(
						copySnaaToRs(secretAuthCookie.secretAuthenticationKeys), gr);
				String jsonResponse = convertToJSON(new ConfidentialReservationDataList(reservations));

				log.debug("Listing confidential reservations from {} until {}: {}", new Object[] { from, to, jsonResponse });
				return Response.ok(jsonResponse).build();
			}

		} catch (DatatypeConfigurationException e) {
			return returnError("Wrong input, please encode from and to as XMLGregorianCalendar", e, Status.BAD_REQUEST);
		} catch (RSExceptionException e) {
			return returnError("Error while loading data from the reservation system", e, Status.BAD_REQUEST);
		}

	}

	/**
	 * Deletes a single reservation. public void deleteReservation(List<SecretAuthenticationKey> authenticationData,
	 * List<SecretReservationKey> secretReservationKey)
	 * 
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_PLAIN })
	public Response deleteReservation(@CookieParam(Constants.COOKIE_WISEBED_SECRET_AUTHENTICATION_KEY) SecretAuthenticationKeyList secretAuthCookie,
			SecretReservationKeyList secretReservationKeys) {

		log.debug("Cookie (secret authentication key): {}", secretAuthCookie);

		if (secretAuthCookie != null) {
			try {

				rs.deleteReservation(copySnaaToRs(secretAuthCookie.secretAuthenticationKeys), secretReservationKeys.reservations);
				return Response.ok("Ok, deleted reservation").build();

			} catch (RSExceptionException e) {
				return returnError("Error while communicating with the reservation server", e, Status.INTERNAL_SERVER_ERROR);
			} catch (ReservervationNotFoundExceptionException e) {
				return returnError("Reservation not found", e, Status.BAD_REQUEST);
			}
		}
		return returnError("Not logged in", new Exception("Not logged in"), Status.FORBIDDEN);
	}

	private List<eu.wisebed.api.rs.SecretAuthenticationKey> copySnaaToRs(List<SecretAuthenticationKey> snaaKeys) {
		List<eu.wisebed.api.rs.SecretAuthenticationKey> secretAuthKeys = Lists.newArrayListWithCapacity(snaaKeys.size());

		for (SecretAuthenticationKey snaaKey : snaaKeys) {

			eu.wisebed.api.rs.SecretAuthenticationKey key = new eu.wisebed.api.rs.SecretAuthenticationKey();

			key.setSecretAuthenticationKey(snaaKey.getSecretAuthenticationKey());
			key.setUrnPrefix(snaaKey.getUrnPrefix());
			key.setUsername(snaaKey.getUsername());

			secretAuthKeys.add(key);
		}

		return secretAuthKeys;
	}

	private Response returnError(String msg, Exception e, Status status) {
		log.debug(msg + " :" + e, e);
		String errorMessage = String.format("%s: %s (%s)", msg, e, e.getMessage());
		return Response.status(status).entity(errorMessage).build();
	}

	// TODO
	/**
	 * Returns data about a single reservation (including confidential information). public
	 * List<ConfidentialReservationData> getReservation(List<SecretReservationKey> secretReservationKey)
	 * */

}
