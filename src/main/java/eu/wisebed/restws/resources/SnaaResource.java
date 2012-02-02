package eu.wisebed.restws.resources;

import static eu.wisebed.restws.resources.ResourceHelper.createSecretAuthenticationKeyCookieName;
import static eu.wisebed.restws.resources.ResourceHelper.createUnknownTestbedIdResponse;
import static eu.wisebed.restws.util.JSONHelper.toJSON;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

import com.google.inject.Inject;

import eu.wisebed.api.snaa.AuthenticationExceptionException;
import eu.wisebed.api.snaa.SNAA;
import eu.wisebed.api.snaa.SNAAExceptionException;
import eu.wisebed.api.snaa.SecretAuthenticationKey;
import eu.wisebed.restws.dto.LoginData;
import eu.wisebed.restws.dto.SnaaSecretAuthenticationKeyList;
import eu.wisebed.restws.proxy.UnknownTestbedIdException;
import eu.wisebed.restws.proxy.WebServiceEndpointManager;
import eu.wisebed.restws.util.Base64Helper;
import eu.wisebed.restws.util.InjectLogger;

public class SnaaResource {

	@InjectLogger
	private Logger log;

	@Inject
	private WebServiceEndpointManager endpointManager;

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
	 * @param testbedId
	 * 		the ID of the testbed
	 * @param loginData
	 * 		login data
	 *
	 * @return a response
	 */
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/" + Constants.WISEBED_API_VERSION + "/{testbedId}/login")
	public Response login(@PathParam("testbedId") final String testbedId, final LoginData loginData) {

		List<SecretAuthenticationKey> secretAuthenticationKeys;

		try {

			SNAA snaa = endpointManager.getSnaaEndpoint(testbedId);

			secretAuthenticationKeys = snaa.authenticate(loginData.authenticationData);
			SnaaSecretAuthenticationKeyList loginResult = new SnaaSecretAuthenticationKeyList(secretAuthenticationKeys);
			String jsonResponse = toJSON(loginResult);

			NewCookie sakCookie = createCookie(testbedId, loginResult);

			log.debug("Received {}, returning {}", toJSON(loginData), jsonResponse);
			return Response.ok(jsonResponse).cookie(sakCookie).build();

		} catch (AuthenticationExceptionException e) {
			return createLoginErrorResponse(e);
		} catch (SNAAExceptionException e) {
			return createLoginErrorResponse(e);
		} catch (UnknownTestbedIdException e) {
			return createUnknownTestbedIdResponse(testbedId);
		}

	}

	private NewCookie createCookie(final String testbedId, SnaaSecretAuthenticationKeyList loginData) {

		int maxAge = 60 * 60 * 24;
		boolean secure = false;
		String comment = "";
		String domain = "";
		String value = Base64Helper.encode(toJSON(loginData));
		String name = createSecretAuthenticationKeyCookieName(testbedId);
		String path = "/";

		return new NewCookie(name, value, path, domain, comment, maxAge, secure);
	}

	private Response createLoginErrorResponse(Exception e) {
		log.debug("Login failed :" + e, e);
		String errorMessage = String.format("Login failed: %s (%s)", e, e.getMessage());
		return Response.status(Status.FORBIDDEN).entity(errorMessage).build();
	}

}
