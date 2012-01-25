package eu.wisebed.restws.resources;

import com.google.inject.Singleton;

import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Singleton
@ThreadSafe
@Path("/")
public class RootResource {

	/*@Inject
	private EmployeeDB employeeDB;*/

	@GET
	public Response get() {
		// TODO implement
		return Response.ok("Hello, World!").build();
	}

}
