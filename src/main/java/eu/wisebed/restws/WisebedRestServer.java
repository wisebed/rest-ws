package eu.wisebed.restws;

import com.google.inject.servlet.GuiceFilter;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class must not be modified.
 * <p/>
 * Main class to bootstrap the HTTP server that runs the phone book REST service.
 */
public class WisebedRestServer {

	private static final Logger log = LoggerFactory.getLogger(WisebedRestServer.class);

	public static void main(String[] args) throws Exception {

		String url = (args.length > 0) ? args[0] : "http://localhost:8080";

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
		}, "ShutdownThread"
		)
		);

	}

}
