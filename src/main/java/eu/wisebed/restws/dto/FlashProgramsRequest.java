package eu.wisebed.restws.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
