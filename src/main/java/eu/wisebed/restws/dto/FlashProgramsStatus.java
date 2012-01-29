package eu.wisebed.restws.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class FlashProgramsStatus {

	@XmlElement(name = "status")
	public Map<String, Integer> statusList;
}
