package eu.wisebed.restws;

import eu.wisebed.restws.pt.PacketTrackingWebSocketFactory;
import eu.wisebed.restws.pt.PacketTrackingWebSocketServlet;
import org.eclipse.jetty.servlet.DefaultServlet;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import eu.wisebed.restws.resources.CookieResource;
import eu.wisebed.restws.resources.ExperimentResource;
import eu.wisebed.restws.resources.RemoteExperimentConfigurationResource;
import eu.wisebed.restws.resources.RootResource;
import eu.wisebed.restws.resources.RsResource;
import eu.wisebed.restws.resources.SnaaResource;
import eu.wisebed.restws.ws.WsnWebSocketFactory;
import eu.wisebed.restws.ws.WsnWebSocketServlet;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import java.net.URL;

/**
 * Configuration class to set up Google Guice-based dependency injection with the Jersey JAX-RS implementation. For
 * more information please check
 * http://jersey.java.net/nonav/apidocs/latest/contribs/jersey-guice/com/sun/jersey/guice/spi/container/servlet/package-summary.html.
 */
public class WisebedRestServerServletModule extends JerseyServletModule {

	private final WisebedRestServerConfig config;

	public WisebedRestServerServletModule(final WisebedRestServerConfig config) {
		this.config = config;
	}

	@Override
	protected void configureServlets() {

		bind(RootResource.class);
		bind(CookieResource.class);
		bind(SnaaResource.class);
		bind(RsResource.class);
		bind(ExperimentResource.class);
		bind(RemoteExperimentConfigurationResource.class);

		install(new FactoryModuleBuilder().build(PacketTrackingWebSocketFactory.class));
		install(new FactoryModuleBuilder().build(WsnWebSocketFactory.class));

		bind(DefaultServlet.class).in(Singleton.class);
		bind(CrossOriginFilter.class).in(Singleton.class);

		serve("/ws/*").with(WsnWebSocketServlet.class);
		serve("/pt/ws/*").with(PacketTrackingWebSocketServlet.class);
		serve("/rest*").with(GuiceContainer.class, ImmutableMap.of(JSONConfiguration.FEATURE_POJO_MAPPING, "true"));

		filter("/*","/pt/*").through(CrossOriginFilter.class, ImmutableMap.of(
				"allowedOrigins", "http://*",
				"allowedMethods", "GET,POST,PUT,DELETE",
				"allowCredentials", "true"
		));

		String resourceBase;
		if (config.webAppRootDir != null) {
			resourceBase = config.webAppRootDir.getAbsolutePath();
		} else {
			final URL webAppBaseUrl = this.getClass().getClassLoader().getResource("webapp");
			if (webAppBaseUrl == null) {
				throw new IllegalArgumentException("Bundled web app directory was not found!");
			}
			resourceBase = webAppBaseUrl.toExternalForm();
		}

		serve("/*","/pt/*").with(DefaultServlet.class, ImmutableMap.of(
				"resourceBase", resourceBase,
				"maxCacheSize", "0"
		));
	}
}
