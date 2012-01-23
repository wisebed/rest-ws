package eu.wisebed.restws;

import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * Configuration class to set up Google Guice-based dependency injection with the Jersey JAX-RS implementation.
 * For more information please check http://jersey.java.net/nonav/apidocs/latest/contribs/jersey-guice/com/sun/jersey/guice/spi/container/servlet/package-summary.html.
 */
public class WisebedRestServerModule extends JerseyServletModule {

	@Override
	protected void configureServlets() {
		bind(WisebedResource.class);
		serve("/*").with(GuiceContainer.class);
	}
}
