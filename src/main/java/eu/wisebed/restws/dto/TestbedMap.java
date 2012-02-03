package eu.wisebed.restws.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class TestbedMap {

	public static class Testbed {

		public String name;

		public String[] urnPrefixes;

		public String sessionManagementEndpointUrl;
	}

	public Map<String, Testbed> testbedMap;

}
