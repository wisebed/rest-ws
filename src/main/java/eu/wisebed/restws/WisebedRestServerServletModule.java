package eu.wisebed.restws;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import eu.wisebed.restws.resources.*;

/**
 * Configuration class to set up Google Guice-based dependency injection with the Jersey JAX-RS implementation. For
 * more information please check
 * http://jersey.java.net/nonav/apidocs/latest/contribs/jersey-guice/com/sun/jersey/guice/spi/container/servlet/package-summary.html.
 */
public class WisebedRestServerServletModule extends ServletModule {

	@Override
	protected void configureServlets() {

		bind(RootResource.class);
		bind(SnaaResource.class);
		bind(RsResource.class);
		bind(ExperimentResource.class);

		install(new FactoryModuleBuilder().build(WsnWebSocketFactory.class));

		serve("/ws/*").with(WsnWebSocketServlet.class);
		serve("/*").with(GuiceContainer.class, ImmutableMap.of(JSONConfiguration.FEATURE_POJO_MAPPING, "true"));
	}
}
