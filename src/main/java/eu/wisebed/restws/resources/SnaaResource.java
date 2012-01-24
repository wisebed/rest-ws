package eu.wisebed.restws.resources;

import static eu.wisebed.restws.util.JaxbHelper.convertToJSON;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;

import com.google.inject.Inject;

import eu.wisebed.api.snaa.AuthenticationExceptionException;
import eu.wisebed.api.snaa.AuthenticationTriple;
import eu.wisebed.api.snaa.SNAA;
import eu.wisebed.api.snaa.SNAAExceptionException;
import eu.wisebed.api.snaa.SecretAuthenticationKey;
import eu.wisebed.restws.util.InjectLogger;
import eu.wisebed.restws.util.JaxbHelper;

@Path("/wisebed/" + Constants.WISEBED_API_VERSION + "/snaa/")
public class SnaaResource {

	@InjectLogger
	private Logger log;

	@Inject
	private SNAA snaa;

	@XmlRootElement
	public static class LoginData {
		public List<AuthenticationTriple> authenticationData;
	}

	@XmlRootElement
	public static class SecretAuthenticationKeyList {
		public List<SecretAuthenticationKey> secretAuthenticationKeys;

		public SecretAuthenticationKeyList() {
		}

		public SecretAuthenticationKeyList(List<SecretAuthenticationKey> secretAuthenticationKeys) {
			this.secretAuthenticationKeys = secretAuthenticationKeys;
		}

		public static SecretAuthenticationKeyList fromString(String json) {
			try {
				return JaxbHelper.fromJSON(json, SecretAuthenticationKeyList.class);
			} catch (Exception e) {
				return null;
			}
		}
	}

	/**
	 * loginData example: <code>
		{
		"authenticationData":
		[
		{"password":"pass1", "urnPrefix":"urnprefix1", "username":"user1"},
		{"password":"pass2", "urnPrefix":"urnprefix2", "username":"user2"}
		]
		}
	 * </code>
	 * 
	 * loginResult example: <code>
	 	{	
	 		"secretAuthenticationKeys":
	 		[
	 			{"username":"user","secretAuthenticationKey":"verysecret","urnPrefix":"urn"},
	 			{"username":"user","secretAuthenticationKey":"verysecret","urnPrefix":"urn"}
	 		]
	 	}
	 * </code>
	 * 
	 * @param authenticationData
	 * @return
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@Path("login")
	public Response login(LoginData loginData) {

		List<SecretAuthenticationKey> secretAuthenticationKeys;
		try {

			secretAuthenticationKeys = snaa.authenticate(loginData.authenticationData);
			SecretAuthenticationKeyList loginResult = new SecretAuthenticationKeyList(secretAuthenticationKeys);
			String jsonResponse = convertToJSON(loginResult);

			NewCookie sakCookie = new NewCookie(Constants.COOKIE_WISEBED_SECRET_AUTHENTICATION_KEY, jsonResponse);

			log.debug("Received {}, returning {}", convertToJSON(loginData), jsonResponse);
			return Response.ok(jsonResponse).cookie(sakCookie).build();

		} catch (AuthenticationExceptionException e) {
			return returnLoginError(e);
		} catch (SNAAExceptionException e) {
			return returnLoginError(e);
		}

	}

	private Response returnLoginError(Exception e) {
		log.debug("Login failed :" + e, e);
		String errorMessage = String.format("Login failed: %s (%s)", e, e.getMessage());
		return Response.status(Status.FORBIDDEN).entity(errorMessage).build();
	}

}
