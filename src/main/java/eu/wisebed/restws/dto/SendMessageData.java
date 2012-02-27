package eu.wisebed.restws.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class SendMessageData {

	public String sourceNodeUrn;

	public List<String> targetNodeUrns;

	public String bytesBase64;

	@Override
	public String toString() {
		return "SendMessageData{" +
				"sourceNodeUrn='" + sourceNodeUrn + '\'' +
				", targetNodeUrns=" + targetNodeUrns +
				", bytesBase64='" + bytesBase64 + '\'' +
				'}';
	}
}
