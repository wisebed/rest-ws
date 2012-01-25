package eu.wisebed.restws.resources.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SendMessageData extends NodeUrnList {

	public String bytesBase64;

	public String sourceNodeUrn;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SendMessageData [bytesBase64 bytes=");
		builder.append(bytesBase64 != null ? bytesBase64.length() : "0");
		builder.append(", sourceNodeUrn=");
		builder.append(sourceNodeUrn);
		builder.append("]");
		return builder.toString();
	}
}
