package eu.wisebed.restws.proxy;

public class UnknownTestbedIdException extends Exception {

	private final String testbedId;

	public UnknownTestbedIdException(final String testbedId) {
		this.testbedId = testbedId;
	}

	public String getTestbedId() {
		return testbedId;
	}
}
