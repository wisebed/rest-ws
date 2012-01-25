package eu.wisebed.restws;

import eu.wisebed.restws.util.Log4JLevelOptionHandler;
import org.apache.log4j.Level;
import org.kohsuke.args4j.Option;

public class CommandLineOptions {

	@Option(name = "-p", aliases = {"--port"}, usage = "Port to start the web server on.", required = false)
	public int webServerPort = 8080;

	@Option(name = "-w", aliases = {"--wsport"}, usage = "Port to listen for WebSocket connections", required = false)
	public int webSocketPort = 8081;

	@Option(name = "-l", aliases = {"--logLevel"},
			usage = "Set logging level (valid values: TRACE, DEBUG, INFO, WARN, ERROR).", required = false,
			handler = Log4JLevelOptionHandler.class)
	public Level logLevel = null;

	@Option(name = "-v", aliases = {"--verbose"}, usage = "Verbose (DEBUG) logging output (default: INFO).",
			required = false)
	public boolean verbose = false;

	@Option(name = "-h", aliases = {"--help"}, usage = "This help message.", required = false)
	public boolean help = false;

	@Override
	public String toString() {
		return "{ webServerPort=" + webServerPort +
				", webSocketPort=" + webSocketPort +
				", logLevel=" + logLevel +
				", verbose=" + verbose +
				'}';
	}
}
