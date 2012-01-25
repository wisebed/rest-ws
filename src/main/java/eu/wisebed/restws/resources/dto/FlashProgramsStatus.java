package eu.wisebed.restws.resources.dto;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FlashProgramsStatus {
	public Map<String, Integer> statusList;
}
