package eu.wisebed.restws;

import eu.wisebed.restws.util.Log4JLevelOptionHandler;
import org.apache.log4j.Level;
import org.kohsuke.args4j.Option;

public class WisebedRestServerConfig {

	@Option(name = "-n", aliases = {"--hostname"}, usage = "Hostname to start the web server on.", required = true)
	public String webServerHostname;

	@Option(name = "-p", aliases = {"--port"}, usage = "Port to start the web server on.", required = false)
	public int webServerPort = 8080;

	@Option(name = "-l", aliases = {"--logLevel"},
			usage = "Set logging level (valid values: TRACE, DEBUG, INFO, WARN, ERROR).", required = false,
			handler = Log4JLevelOptionHandler.class)
	public Level logLevel = null;

	@Option(name = "-v", aliases = {"--verbose"}, usage = "Verbose (DEBUG) logging output (default: INFO).",
			required = false)
	public boolean verbose = false;

	@Option(name = "-h", aliases = {"--help"}, usage = "This help message.", required = false)
	public boolean help = false;

	@Option(name = "--operationTimeoutMillis", usage = "The milliseconds after which all non-flash operations should time out.", required = false)
	public int operationTimeoutMillis = 10 * 1000;

	@Option(name = "--flashTimeoutMillis", usage = "The milliseconds after which a flash operation should time out.", required = false)
	public int flashTimeoutMillis = 2 * 60 * 1000;

	@Override
	public String toString() {
		return "{ webServerPort=" + webServerPort +
				", logLevel=" + logLevel +
				", verbose=" + verbose +
				'}';
	}
}
