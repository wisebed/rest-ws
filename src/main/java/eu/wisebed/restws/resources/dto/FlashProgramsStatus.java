package eu.wisebed.restws.resources.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class FlashProgramsStatus {

	public Map<String, Integer> statusList;
}
