package eu.wisebed.restws.proxy;

public class UnknownTestbedIdException extends Exception {

	private static final long serialVersionUID = -3378302232916644015L;

	private final String testbedId;

	public UnknownTestbedIdException(final String testbedId) {
		this.testbedId = testbedId;
	}

	public String getTestbedId() {
		return testbedId;
	}
}
