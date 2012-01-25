package eu.wisebed.restws;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import de.uniluebeck.itm.tr.util.Logging;
import eu.wisebed.restws.ws.WebSocketServerModule;
import eu.wisebed.restws.ws.WebSocketServerService;
import org.apache.log4j.Level;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * This class must not be modified.
 * <p/>
 * Main class to bootstrap the HTTP server that runs the phone book REST service.
 */
public class WisebedRestServer {

	private static Logger log;

	static {
		Logging.setLoggingDefaults();
	}

	public static void main(String[] args) throws Exception {
		log = LoggerFactory.getLogger(WisebedRestServer.class);
		final CommandLineOptions options = parseCmdLineOptions(args);

		if (options.logLevel != null) {
			org.apache.log4j.Logger.getRootLogger().setLevel(options.logLevel);
		} else if (options.verbose) {
			org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
		}

		log.debug("Startup with the following configuration " + options);

		final Injector injector = Guice.createInjector(
				new WisebedRestServerModule(),
				new WebSocketServerModule(options)
		);

		final Server server = new Server(options.webServerPort);

		ServletContextHandler handler = new ServletContextHandler();
		handler.setContextPath("/");

		FilterHolder guiceFilter = new FilterHolder(injector.getInstance(GuiceFilter.class));
		handler.addFilter(guiceFilter, "/*", EnumSet.allOf(DispatcherType.class));

		server.setHandler(handler);
		server.start();

		/*final GrizzlyWebServer ws = new GrizzlyWebServer(options.webServerPort);

		ServletAdapter sa = new ServletAdapter();
		ws.addGrizzlyAdapter(sa, null);
		sa.addServletListener(WisebedRestServerServletListener.class.getName());
		sa.addFilter(new GuiceFilter(), "guiceFilter", null);
		ws.start();*/

		log.info("Started REST resources on port {}", options.webServerPort);

		final WebSocketServerService webSocketServerService = injector.getInstance(WebSocketServerService.class);

		try {
			webSocketServerService.start().get();
			log.info("Started WebSocket service on port " + options.webSocketPort);
		} catch (Exception e) {
			log.error("Exception while starting WebSocketServerService: " + e, e);
			System.exit(1);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				log.info("Received EXIT signal. Shutting down Web server...");
				try {
					server.stop();
				} catch (Exception e) {
					log.warn("Exception caught while shutting down Jetty during application shutdown: " + e, e);
				}
				webSocketServerService.stopAndWait();
			}
		}, "ShutdownThread"
		)
		);

	}

	private static CommandLineOptions parseCmdLineOptions(final String[] args) {
		CommandLineOptions options = new CommandLineOptions();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
			if (options.help) {
				printHelpAndExit(parser);
			}
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			printHelpAndExit(parser);
		}

		return options;
	}

	private static void printHelpAndExit(CmdLineParser parser) {
		System.err.print("Usage: java " + WisebedRestServer.class.getCanonicalName());
		parser.printSingleLineUsage(System.err);
		System.err.println();
		parser.printUsage(System.err);
		System.exit(1);
	}
}
