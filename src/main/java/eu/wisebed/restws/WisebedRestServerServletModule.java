package eu.wisebed.restws;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import eu.wisebed.restws.resources.ExperimentResource;
import eu.wisebed.restws.resources.RootResource;
import eu.wisebed.restws.resources.RsResource;
import eu.wisebed.restws.resources.SnaaResource;
import eu.wisebed.restws.ws.WsnWebSocketFactory;
import eu.wisebed.restws.ws.WsnWebSocketServlet;
import org.eclipse.jetty.servlet.DefaultServlet;

/**
 * Configuration class to set up Google Guice-based dependency injection with the Jersey JAX-RS implementation. For
 * more information please check
 * http://jersey.java.net/nonav/apidocs/latest/contribs/jersey-guice/com/sun/jersey/guice/spi/container/servlet/package-summary.html.
 */
public class WisebedRestServerServletModule extends JerseyServletModule {

	@Override
	protected void configureServlets() {

		bind(RootResource.class);
		bind(SnaaResource.class);
		bind(RsResource.class);
		bind(ExperimentResource.class);

		install(new FactoryModuleBuilder().build(WsnWebSocketFactory.class));

		bind(DefaultServlet.class).in(Singleton.class);

		serve("/ws/*").with(WsnWebSocketServlet.class);
		serve("/rest*").with(GuiceContainer.class, ImmutableMap.of(JSONConfiguration.FEATURE_POJO_MAPPING, "true"));
		serve("/*").with(DefaultServlet.class, ImmutableMap.of(
				"resourceBase", this.getClass().getClassLoader().getResource("webapp").toExternalForm(),
				"maxCacheSize", "0"
		));
	}
}
