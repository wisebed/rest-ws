package eu.wisebed.restws.event;

import org.joda.time.DateTime;

public class DownstreamMessageEvent {

	private final String targetNodeUrn;

	private final DateTime timestamp;

	private final byte[] messageBytes;

	public DownstreamMessageEvent(final DateTime timestamp, final String targetNodeUrn, final byte[] messageBytes) {
		this.targetNodeUrn = targetNodeUrn;
		this.messageBytes = messageBytes;
		this.timestamp = timestamp;
	}

	public byte[] getMessageBytes() {
		return messageBytes;
	}

	public String getTargetNodeUrn() {
		return targetNodeUrn;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}
}
