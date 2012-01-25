package eu.wisebed.restws.resources.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SendMessageData extends NodeUrnList {
	public String bytesBase64;
	public String sourceNodeUrn;
}
