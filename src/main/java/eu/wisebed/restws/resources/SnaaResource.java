package eu.wisebed.restws.resources;

import com.google.inject.Inject;
import eu.wisebed.api.snaa.*;
import eu.wisebed.restws.dto.LoginData;
import eu.wisebed.restws.dto.SnaaSecretAuthenticationKeyList;
import eu.wisebed.restws.util.Base64Helper;
import eu.wisebed.restws.util.InjectLogger;
import org.slf4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import static eu.wisebed.restws.util.JSONHelper.toJSON;

public class SnaaResource {

	@InjectLogger
	private Logger log;

	@Inject
	private SNAA snaa;

	@Context
	private UriInfo uriInfo;

	/**
	 * loginData example: <code>
	 * {
	 * "authenticationData":
	 * [
	 * {"password":"pass1", "urnPrefix":"urnprefix1", "username":"user1"},
	 * {"password":"pass2", "urnPrefix":"urnprefix2", "username":"user2"}
	 * ]
	 * }
	 * </code>
	 * <p/>
	 * loginResult example: <code>
	 * {
	 * "secretAuthenticationKeys":
	 * [
	 * {"username":"user","secretAuthenticationKey":"verysecret","urnPrefix":"urn"},
	 * {"username":"user","secretAuthenticationKey":"verysecret","urnPrefix":"urn"}
	 * ]
	 * }
	 * </code>
	 *
	 * @param loginData
	 *
	 * @return
	 */
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/" + Constants.WISEBED_API_VERSION + "/{testbedId}/login")
	public Response login(@PathParam("testbedId") final String testbedId, final LoginData loginData) {

		List<SecretAuthenticationKey> secretAuthenticationKeys;

		try {

			secretAuthenticationKeys = snaa.authenticate(loginData.authenticationData);
			SnaaSecretAuthenticationKeyList loginResult = new SnaaSecretAuthenticationKeyList(secretAuthenticationKeys);
			String jsonResponse = toJSON(loginResult);

			NewCookie sakCookie = toCookie(loginResult);

			log.debug("Received {}, returning {}", toJSON(loginData), jsonResponse);
			return Response.ok(jsonResponse).cookie(sakCookie).build();

		} catch (AuthenticationExceptionException e) {
			return returnLoginError(e);
		} catch (SNAAExceptionException e) {
			return returnLoginError(e);
		}

	}

	private NewCookie toCookie(SnaaSecretAuthenticationKeyList loginData) {

		int maxAge = 60 * 60 * 24;
		boolean secure = false;
		String comment = "";
		String domain = "";
		String value = Base64Helper.encode(toJSON(loginData));
		String name = Constants.COOKIE_SECRET_AUTH_KEY;
		String path = "/";

		return new NewCookie(name, value, path, domain, comment, maxAge, secure);
	}

	private Response returnLoginError(Exception e) {
		log.debug("Login failed :" + e, e);
		String errorMessage = String.format("Login failed: %s (%s)", e, e.getMessage());
		return Response.status(Status.FORBIDDEN).entity(errorMessage).build();
	}

}
