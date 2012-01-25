package eu.wisebed.restws.resources.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NodeUrnList {
	public List<String> nodeUrns;
}
