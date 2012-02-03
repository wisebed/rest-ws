package eu.wisebed.restws.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class FlashProgramsRequest {

	public static class FlashTask {

		@XmlElement(name = "nodeUrns")
		public List<String> nodeUrns;

		@XmlElement(name = "image")
		public String imageBase64;
	}

	@XmlElement(name = "configurations")
	public List<FlashTask> flashTasks;

}
