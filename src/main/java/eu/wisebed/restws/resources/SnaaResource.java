package eu.wisebed.restws.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.wisebed.api.snaa.*;
import eu.wisebed.restws.util.Base64Helper;
import eu.wisebed.restws.util.InjectLogger;
import eu.wisebed.restws.util.JaxbHelper;
import org.slf4j.Logger;

import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static eu.wisebed.restws.util.JaxbHelper.convertToJSON;

@Singleton
@ThreadSafe
@Path("/wisebed/" + Constants.WISEBED_API_VERSION + "/login")
public class SnaaResource {

	@InjectLogger
	private Logger log;

	@Inject
	private SNAA snaa;

	@Context
	private UriInfo uriInfo;

	@XmlRootElement
	public static class LoginData {

		public List<AuthenticationTriple> authenticationData;
	}

	@XmlRootElement
	public static class SecretAuthenticationKeyList {

		public List<SecretAuthenticationKey> secretAuthenticationKeys;

		public SecretAuthenticationKeyList() {
		}

		public SecretAuthenticationKeyList(String json) {
			try {
				this.secretAuthenticationKeys = JaxbHelper.fromJSON(Base64Helper.decode(json),
						SecretAuthenticationKeyList.class
				).secretAuthenticationKeys;
			} catch (Exception e) {
				this.secretAuthenticationKeys = null;
			}
		}

		public SecretAuthenticationKeyList(List<SecretAuthenticationKey> secretAuthenticationKeys) {
			this.secretAuthenticationKeys = secretAuthenticationKeys;
		}

	}

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
	 * @param authenticationData
	 *
	 * @return
	 */
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public Response login(LoginData loginData) {

		List<SecretAuthenticationKey> secretAuthenticationKeys;
		try {

			secretAuthenticationKeys = snaa.authenticate(loginData.authenticationData);
			SecretAuthenticationKeyList loginResult = new SecretAuthenticationKeyList(secretAuthenticationKeys);
			String jsonResponse = convertToJSON(loginResult);

			NewCookie sakCookie = toCookie(loginResult);

			log.debug("Received {}, returning {}", convertToJSON(loginData), jsonResponse);
			return Response.ok(jsonResponse).cookie(sakCookie).build();

		} catch (AuthenticationExceptionException e) {
			return returnLoginError(e);
		} catch (SNAAExceptionException e) {
			return returnLoginError(e);
		}

	}

	@GET
	public Response test() throws AuthenticationExceptionException, SNAAExceptionException {
		List<SecretAuthenticationKey> secretAuthenticationKeys = snaa.authenticate(null);
		SecretAuthenticationKeyList loginResult = new SecretAuthenticationKeyList(secretAuthenticationKeys);
		String jsonResponse = convertToJSON(loginResult);

		log.debug("Received {}, returning {}", "{}", jsonResponse);
		return Response.ok(jsonResponse).cookie(toCookie(loginResult)).build();

	}

	private NewCookie toCookie(SecretAuthenticationKeyList loginData) {
		return new NewCookie(Constants.COOKIE_SECRET_AUTH_KEY, Base64Helper.encode(convertToJSON(loginData)), "/",
				uriInfo
						.getRequestUri().getHost(), "", 60 * 60 * 24, false
		);
	}

	private Response returnLoginError(Exception e) {
		log.debug("Login failed :" + e, e);
		String errorMessage = String.format("Login failed: %s (%s)", e, e.getMessage());
		return Response.status(Status.FORBIDDEN).entity(errorMessage).build();
	}

}
