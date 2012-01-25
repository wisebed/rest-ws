package eu.wisebed.restws.resources;

import com.google.inject.Inject;
import eu.wisebed.restws.WsnInstanceCache;
import eu.wisebed.restws.util.InjectLogger;
import org.eclipse.jetty.websocket.WebSocket;
import org.slf4j.Logger;

import java.io.IOException;

public class WsnWebSocket implements WebSocket, WebSocket.OnTextMessage {

	@InjectLogger
	private Logger log;

	private final WsnInstanceCache wsnInstanceCache;

	private Connection connection;

	@Inject
	public WsnWebSocket(final WsnInstanceCache wsnInstanceCache) {
		this.wsnInstanceCache = wsnInstanceCache;
	}

	@Override
	public void onMessage(final String data) {
		try {
			connection.sendMessage(data);
		} catch (IOException e) {
			log.error("IOException while sending message over websocket connection " + connection);
		}
	}

	@Override
	public void onOpen(final Connection connection) {
		this.connection = connection;
		log.info("Websocket connection opened: {}", connection);
	}

	@Override
	public void onClose(final int closeCode, final String message) {
		if (log.isInfoEnabled()) {
			log.info("Websocket connection closed with code {} and message \"{}\": {}",
					new Object[]{closeCode, message, connection}
			);
		}
		this.connection = null;
	}
}
