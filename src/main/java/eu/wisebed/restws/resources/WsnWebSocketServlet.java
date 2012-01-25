package eu.wisebed.restws.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

@Singleton
@ThreadSafe
public class WsnWebSocketServlet extends WebSocketServlet {

	@Inject
	private Provider<WsnWebSocket> wsnWebSocketProvider;

	@Override
	public WebSocket doWebSocketConnect(final HttpServletRequest request, final String protocol) {
		return wsnWebSocketProvider.get();
	}
}
