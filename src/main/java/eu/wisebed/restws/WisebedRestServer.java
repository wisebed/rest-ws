package eu.wisebed.restws;

import org.apache.log4j.Level;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.servlet.GuiceFilter;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;

import de.uniluebeck.itm.tr.util.Logging;

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

		log.debug("Startup");

		String url = (args.length > 0) ? args[0] : "http://localhost:" + options.webServerPort;
		final GrizzlyWebServer ws = new GrizzlyWebServer(url);

		ServletAdapter sa = new ServletAdapter();
		ws.addGrizzlyAdapter(sa, null);
		sa.addServletListener(WisebedRestServerConfig.class.getName());
		sa.addFilter(new GuiceFilter(), "guiceFilter", null);
		ws.start();

		log.info("Started server on URL: {}", url);

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				log.info("Received EXIT signal. Shutting down Web server...");
				ws.stop();
			}
		}, "ShutdownThread"));

	}

	private static CommandLineOptions parseCmdLineOptions(final String[] args) {
		CommandLineOptions options = new CommandLineOptions();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
			if (options.help)
				printHelpAndExit(parser);
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
