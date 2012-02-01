package eu.wisebed.restws;

import eu.wisebed.restws.util.Log4JLevelOptionHandler;
import org.apache.log4j.Level;
import org.kohsuke.args4j.Option;

public class WisebedRestServerConfig {

	@Option(name = "--hostname", usage = "Hostname to start the web server on.", required = true)
	public String webServerHostname;

	@Option(name = "--port", usage = "Port to start the web server on.")
	public int webServerPort = 8080;

	@Option(name = "--sessionManagementEndpointUrl", usage = "The endpoint URL of the testbeds Session Management API",
			required = true)
	public String sessionManagementEndpointUrl;

	@Option(name = "--servedUrnPrefixes",
			usage = "A comma-separated list of testbed URN prefixes served by this server "
					+ "(e.g. \"urn:wisebed:uzl1:,urn:wisebed:uzl2:\")",
			required = true)
	public String servedUrnPrefixes;

	@Option(name = "--logLevel",
			usage = "Set logging level (valid values: TRACE, DEBUG, INFO, WARN, ERROR).",
			handler = Log4JLevelOptionHandler.class)
	public Level logLevel = null;

	@Option(name = "--verbose", usage = "Verbose (DEBUG) logging output (default: INFO).")
	public boolean verbose = false;

	@Option(name = "--help", usage = "This help message.")
	public boolean help = false;

	@Option(name = "--operationTimeoutMillis",
			usage = "The milliseconds after which all non-flash operations should time out.")
	public int operationTimeoutMillis = 10 * 1000;

	@Option(name = "--flashTimeoutMillis", usage = "The milliseconds after which a flash operation should time out.")
	public int flashTimeoutMillis = 2 * 60 * 1000;

	@Override
	public String toString() {
		return "{ webServerPort=" + webServerPort +
				", logLevel=" + logLevel +
				", verbose=" + verbose +
				'}';
	}
}
