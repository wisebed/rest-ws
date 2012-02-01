package eu.wisebed.restws.resources;

import com.google.inject.Inject;
import eu.wisebed.restws.WisebedRestServerConfig;
import eu.wisebed.restws.dto.TestbedMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
}
