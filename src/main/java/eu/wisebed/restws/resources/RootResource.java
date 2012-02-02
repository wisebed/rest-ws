package eu.wisebed.restws.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;

import eu.wisebed.restws.WisebedRestServerConfig;
import eu.wisebed.restws.dto.TestbedMap;

@Path("/" + Constants.WISEBED_API_VERSION + "/")
public class RootResource {

	@Inject
	private WisebedRestServerConfig config;

	@GET
	@Path("testbeds")
	@Produces({MediaType.APPLICATION_JSON})
	public TestbedMap getTestbedList() {
		return config.testbedMap;
	}
	
	
	@GET
	@Path("cookies")
	public Response getCookies() {

		int maxAge = 60 * 60 * 24;
		boolean secure = false;
		String comment = "";
		String domain = "";
		String value = "bla";
		String name = Constants.COOKIE_SECRET_AUTH_KEY;
		String path = "/";

		NewCookie cookie = new NewCookie(name, value, path, domain, comment, maxAge, secure);

		return Response.ok().cookie(cookie).build();
	}
}
