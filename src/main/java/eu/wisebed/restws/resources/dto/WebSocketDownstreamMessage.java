package eu.wisebed.restws.resources.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class WebSocketDownstreamMessage {

	@XmlElement(name = "bytesBase64")
	public String bytesBase64;

	@XmlElement(name = "nodeUrns")
	public List<String> nodeUrns;

	@Override
	public String toString() {
		return "WebSocketDownstreamMessage{" +
				"bytesBase64='" + bytesBase64 + '\'' +
				", nodeUrns=" + nodeUrns +
				'}';
	}
}
