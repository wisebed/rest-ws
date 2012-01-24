package eu.wisebed.restws;

import java.util.Set;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class MyResourceConfig extends DefaultResourceConfig {

	public MyResourceConfig() {
		super();
		doConfig();
	}

	public MyResourceConfig(Class<?>... classes) {
		super(classes);
		doConfig();
	}

	public MyResourceConfig(Set<Class<?>> classes) {
		super(classes);
		doConfig();
	}

	private void doConfig() {
		getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
	}
}
