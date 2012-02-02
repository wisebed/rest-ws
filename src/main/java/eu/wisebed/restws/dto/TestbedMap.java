package eu.wisebed.restws.dto;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TestbedMap {

	public static class Testbed {

		public String name;

		public String[] urnPrefixes;

		public String sessionManagementEndpointUrl;
	}

	public Map<String, Testbed> testbedMap;

}
