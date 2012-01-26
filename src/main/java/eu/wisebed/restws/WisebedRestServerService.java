package eu.wisebed.restws;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.servlet.GuiceFilter;
import eu.wisebed.restws.servlet.InvalidRequestServlet;
import eu.wisebed.restws.util.InjectLogger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

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

			ServletContextHandler handler = new ServletContextHandler();
			handler.setContextPath("/");
			handler.addServlet(new ServletHolder(new InvalidRequestServlet()), "/*");

			FilterHolder guiceFilterHolder = new FilterHolder(guiceFilter);
			handler.addFilter(guiceFilterHolder, "/*", EnumSet.allOf(DispatcherType.class));

			server.setHandler(handler);
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
