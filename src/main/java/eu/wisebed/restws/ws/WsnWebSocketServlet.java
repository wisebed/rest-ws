package eu.wisebed.restws.ws;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

@Singleton
@ThreadSafe
public class WsnWebSocketServlet extends WebSocketServlet {

	@Inject
	private WsnWebSocketFactory wsnWebSocketFactory;

	@Override
	public WebSocket doWebSocketConnect(final HttpServletRequest request, final String protocol) {

		String uriString = request.getRequestURI();
		URI requestUri;
		try {
			requestUri = new URI(uriString);
		} catch (URISyntaxException e) {
			return null;
		}

		String path = requestUri.getPath().startsWith("/") ? requestUri.getPath().substring(1) : requestUri.getPath();
		String[] splitPath = path.split("/");

		if (splitPath.length < 1 || !"ws".equals(splitPath[0]) || !"experiments".equals(splitPath[1])) {
			return null;
		}

		String experimentId = splitPath[2];

		return wsnWebSocketFactory.create(experimentId);
	}
}
