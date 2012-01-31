package eu.wisebed.restws.proxy;

public class NodeStatus {

	public int statusCode;

	public String message;

	public State state;

	public NodeStatus(final State state, final int statusCode, final String message) {
		this.message = message;
		this.state = state;
		this.statusCode = statusCode;
	}
}