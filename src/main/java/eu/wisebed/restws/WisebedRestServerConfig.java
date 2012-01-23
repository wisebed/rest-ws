package eu.wisebed.restws;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * This class must not be modified.
 * <p/>
 * Configuration class to set up Google Guice-based dependency injection with the Jersey JAX-RS implementation.
 * For more information please check http://jersey.java.net/nonav/apidocs/latest/contribs/jersey-guice/com/sun/jersey/guice/spi/container/servlet/package-summary.html.
 */
public class WisebedRestServerConfig extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new WisebedRestServerModule());
	}
}