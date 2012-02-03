package eu.wisebed.restws;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;
import eu.wisebed.restws.servlet.InvalidRequestServlet;
import eu.wisebed.restws.util.InjectLogger;
import org.eclipse.jetty.http.spi.JettyHttpServerProvider;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Singleton
public class WisebedRestServerService extends AbstractService {

	@InjectLogger
	private Logger log;

	private final WisebedRestServerConfig config;

	private final GuiceFilter guiceFilter;

	private Server server;

	@Inject
	public WisebedRestServerService(final WisebedRestServerConfig config, final GuiceFilter guiceFilter) {
		this.config = config;
		this.guiceFilter = guiceFilter;
	}

	@Override
	protected void doStart() {

		try {

			server = new Server(config.webServerPort);

			FilterHolder guiceFilterHolder = new FilterHolder(guiceFilter);

			ServletContextHandler guiceContextHandler = new ServletContextHandler();
			guiceContextHandler.setContextPath("/");
			guiceContextHandler.addFilter(guiceFilterHolder, "/*", EnumSet.allOf(DispatcherType.class));
			guiceContextHandler.addServlet(DefaultServlet.class, "/");

			// set up JAX-WS support for Jetty
			System.setProperty(
					"com.sun.net.httpserver.HttpServerProvider",
					JettyHttpServerProvider.class.getCanonicalName()
			);
			JettyHttpServerProvider.setServer(server);

			ContextHandlerCollection contexts = new ContextHandlerCollection();
			contexts.setHandlers(new Handler[]{ guiceContextHandler });

			server.setHandler(contexts);
			server.start();

			log.info("Started server on port {}", config.webServerPort);

			notifyStarted();

		} catch (Exception e) {

			log.error("Failed to start server on port {} due to the following error: " + e, e);
			notifyFailed(e);
		}
	}

	@Override
	protected void doStop() {

		try {
			server.stop();


			notifyStopped();
		} catch (Exception e) {

			notifyFailed(e);
		}
	}
}
