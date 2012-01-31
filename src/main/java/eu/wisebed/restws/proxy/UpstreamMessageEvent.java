package eu.wisebed.restws.proxy;

import org.joda.time.DateTime;

public class UpstreamMessageEvent {

	private final String sourceNodeUrn;

	private final DateTime timestamp;

	private final byte[] messageBytes;

	public UpstreamMessageEvent(final DateTime timestamp, final String sourceNodeUrn, final byte[] messageBytes) {
		this.timestamp = timestamp;
		this.sourceNodeUrn = sourceNodeUrn;
		this.messageBytes = messageBytes;
	}

	public byte[] getMessageBytes() {
		return messageBytes;
	}

	public String getSourceNodeUrn() {
		return sourceNodeUrn;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}
}
