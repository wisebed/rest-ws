package eu.wisebed.restws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class WisebedResource {

	/*@Inject
	private EmployeeDB employeeDB;*/

	@GET
	public Response get() {
		// TODO implement
		return null;
	}

}
