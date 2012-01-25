package eu.wisebed.restws.resources.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class FlashProgramsRequest {

	public static class FlashTask {

		public List<String> nodeUrns;

		public String programBase64;
	}

	public List<FlashTask> flashTasks;

}
