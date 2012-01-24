package eu.wisebed.restws;

import com.google.inject.matcher.Matchers;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import eu.wisebed.restws.resources.ExperimentResource;
import eu.wisebed.restws.resources.RsResource;
import eu.wisebed.restws.resources.SessionManagementResource;
import eu.wisebed.restws.resources.SnaaResource;
import eu.wisebed.restws.util.Log4JTypeListener;

/**
 * Configuration class to set up Google Guice-based dependency injection with the Jersey JAX-RS implementation. For more
 * information please check
 * http://jersey.java.net/nonav/apidocs/latest/contribs/jersey-guice/com/sun/jersey/guice/spi/container
 * /servlet/package-summary.html.
 */
public class WisebedRestServerModule extends JerseyServletModule {

	@Override
	protected void configureServlets() {

		bind(WisebedResource.class);
		bind(SnaaResource.class);
		bind(RsResource.class);
		bind(SessionManagementResource.class);
		bind(ExperimentResource.class);

		bindListener(Matchers.any(), new Log4JTypeListener());

		serve("/*").with(GuiceContainer.class);
	}
}
