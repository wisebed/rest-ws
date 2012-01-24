package eu.wisebed.restws.resources;

import static eu.wisebed.restws.util.JaxbHelper.convertToJSON;
import static eu.wisebed.restws.util.JaxbHelper.convertToXML;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;

import eu.wisebed.api.snaa.AuthenticationTriple;
import eu.wisebed.api.snaa.SecretAuthenticationKey;
import eu.wisebed.restws.util.InjectLogger;

@Path("/wisebed/" + Constants.WISEBED_API_VERSION + "/snaa/")
public class SnaaResource {
	@InjectLogger
	private Logger log;

	@XmlRootElement
	public static class LoginData {
		public List<AuthenticationTriple> authenticationData;
	}

	@XmlRootElement
	public static class LoginResult {
		public List<SecretAuthenticationKey> secretAuthenticationKeys;

		public LoginResult() {
		}

		public LoginResult(List<SecretAuthenticationKey> secretAuthenticationKeys) {
			this.secretAuthenticationKeys = secretAuthenticationKeys;
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
	public LoginResult login(LoginData loginData) {
		List<SecretAuthenticationKey> secretAuthKeys = new LinkedList<>();

		SecretAuthenticationKey sak1 = new SecretAuthenticationKey();
		sak1.setSecretAuthenticationKey("verysecret");
		sak1.setUrnPrefix("urn");
		sak1.setUsername("user");

		SecretAuthenticationKey sak2 = new SecretAuthenticationKey();
		sak2.setSecretAuthenticationKey("verysecret");
		sak2.setUrnPrefix("urn");
		sak2.setUsername("user");

		secretAuthKeys.add(sak1);
		secretAuthKeys.add(sak2);

		LoginResult loginResult = new LoginResult(secretAuthKeys);
		log.debug("XML : received {}, sending {}", convertToXML(loginData), convertToXML(loginResult));
		log.debug("JSON: received {}, sending {}", convertToJSON(loginData), convertToJSON(loginResult));
		return loginResult;

	}

}
