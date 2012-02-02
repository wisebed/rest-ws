package eu.wisebed.restws.ws;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@ThreadSafe
public class WsnWebSocketServlet extends WebSocketServlet {

	private static final long serialVersionUID = 160812781199698413L;

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

		String experimentUrl = splitPath[2];

		return wsnWebSocketFactory.create(experimentUrl);
	}
}
