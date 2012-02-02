package eu.wisebed.restws;

import org.apache.log4j.Level;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import de.uniluebeck.itm.tr.util.Logging;

/**
 * This class must not be modified.
 * <p/>
 * Main class to bootstrap the HTTP server that runs the phone book REST
 * service.
 */
public class WisebedRestServer {

	static {
		Logging.setLoggingDefaults();
	}

	private static Logger log = LoggerFactory.getLogger(WisebedRestServer.class);

	public static void main(String[] args) throws Exception {

		final WisebedRestServerConfig config = parseCmdLineOptions(args);

		setLogLevel(config);

		log.debug("Starting up with the following configuration " + config);

		final Injector injector = Guice.createInjector(new WisebedRestServerModule(config));
		final WisebedRestServerService serverService = injector.getInstance(WisebedRestServerService.class);

		try {
			serverService.start().get();
		} catch (Exception e) {
			log.warn("Exception while starting server: " + e, e);
			System.exit(1);
		}

		Runtime.getRuntime().addShutdownHook(new Thread("ShutdownThread") {
			@Override
			public void run() {
				log.info("Received EXIT signal. Shutting down server...");
				try {
					serverService.stop().get();
				} catch (Exception e) {
					log.warn("Exception caught while shutting server: " + e, e);
				}
			}
		}
		);

	}

	private static void setLogLevel(final WisebedRestServerConfig config) {
		if (config.logLevel != null) {
			org.apache.log4j.Logger.getRootLogger().setLevel(config.logLevel);
		} else if (config.verbose) {
			org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
		}
	}

	private static WisebedRestServerConfig parseCmdLineOptions(final String[] args) {

		WisebedRestServerConfig options = new WisebedRestServerConfig();
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
