package eu.wisebed.restws.dto;

import eu.wisebed.restws.jobs.JobNodeStatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class OperationStatusMap {

	public Map<String, JobNodeStatus> operationStatus;

}
