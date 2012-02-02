package eu.wisebed.restws.dto;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import eu.wisebed.restws.jobs.JobNodeStatus;

@XmlRootElement
public class NodeUrnStatusMap {

	public Map<String, JobNodeStatus> map;
}
