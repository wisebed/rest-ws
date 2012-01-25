package eu.wisebed.restws.resources.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FlashProgramsRequest {

	public static class FlashTask {
		public List<String> nodeUrns;
		public String programBase64;
	}

	public List<FlashTask> flashTasks;

}
