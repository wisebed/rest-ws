package eu.wisebed.restws.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.google.inject.Inject;

import eu.wisebed.api.rs.RS;
import eu.wisebed.restws.resources.SnaaResource.SecretAuthenticationKeyList;
import eu.wisebed.restws.util.InjectLogger;

@Path("/wisebed/" + Constants.WISEBED_API_VERSION + "/reservations/")
public class RsResource {
	@InjectLogger
	private Logger log;

	@Inject
	private RS rs;

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response listReservations(@CookieParam(Constants.COOKIE_WISEBED_SECRET_AUTHENTICATION_KEY) SecretAuthenticationKeyList secretAuthCookie) {

		log.debug("Cookie: {}", secretAuthCookie);

		return Response.ok("lala").build();
	}

	/**
	 * Deletes a single reservation. public void deleteReservation(List<SecretAuthenticationKey> authenticationData,
	 * List<SecretReservationKey> secretReservationKey)
	 * 
	 */

	/**
	 * Returns reservations for a time interval (including confidential information). public
	 * List<ConfidentialReservationData> getConfidentialReservations(List<SecretAuthenticationKey>
	 * secretAuthenticationKey, GetReservations period)
	 */

	/**
	 * Returns data about a single reservation (including confidential information). public
	 * List<ConfidentialReservationData> getReservation(List<SecretReservationKey> secretReservationKey)
	 * */

	/**
	 * Returns reservations for a time interval (excluding confidential information). public List<PublicReservationData>
	 * getReservations(XMLGregorianCalendar from, XMLGregorianCalendar to)
	 * */

	/**
	 * Reserves a set of nodes for an interval of time. public List<SecretReservationKey>
	 * makeReservation(List<SecretAuthenticationKey> authenticationData, ConfidentialReservationData reservation)
	 * */

}
