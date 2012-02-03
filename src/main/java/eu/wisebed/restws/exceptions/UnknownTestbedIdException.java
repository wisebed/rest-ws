package eu.wisebed.restws.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UnknownTestbedIdException extends WebApplicationException {

	private static final long serialVersionUID = -3378302232916644015L;

	public UnknownTestbedIdException(final String testbedId) {
		super(Response
				.status(Response.Status.NOT_FOUND)
				.entity("Testbed ID \"" + testbedId + "\" is unknown!")
				.build()
		);
	}
}
