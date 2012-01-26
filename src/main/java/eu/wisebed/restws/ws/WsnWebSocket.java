package eu.wisebed.restws.ws;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.uniluebeck.itm.tr.util.ExecutorUtils;
import eu.wisebed.restws.WsnInstanceCache;
import eu.wisebed.restws.util.InjectLogger;
import org.eclipse.jetty.websocket.WebSocket;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WsnWebSocket implements WebSocket, WebSocket.OnTextMessage {

	@InjectLogger
	private Logger log;

	private final WsnInstanceCache wsnInstanceCache;

	private final String experimentId;

	private Connection connection;

	private ScheduledExecutorService executor;

	private final Runnable aliveRunnable = new Runnable() {
		@Override
		public void run() {
			sendMessage("I'm alive");
		}
	};

	private ScheduledFuture<?> aliveSchedule;

	@Inject
	public WsnWebSocket(final WsnInstanceCache wsnInstanceCache, @Assisted final String experimentId) {
		this.wsnInstanceCache = wsnInstanceCache;
		this.experimentId = experimentId;
	}

	@Override
	public void onMessage(final String data) {
		sendMessage(data);
	}

	private void sendMessage(final String data) {
		try {
			connection.sendMessage(data);
		} catch (IOException e) {
			log.error("IOException while sending message over websocket connection " + connection);
		}
	}

	@Override
	public void onOpen(final Connection connection) {
		this.connection = connection;
		this.executor = Executors.newScheduledThreadPool(1);
		this.aliveSchedule = this.executor.scheduleAtFixedRate(aliveRunnable, 2, 2, TimeUnit.SECONDS);
		log.info("Websocket connection opened: {}", connection);
	}

	@Override
	public void onClose(final int closeCode, final String message) {
		if (log.isInfoEnabled()) {
			log.info("Websocket connection closed with code {} and message \"{}\": {}",
					new Object[]{closeCode, message, connection}
			);
		}
		this.aliveSchedule.cancel(true);
		this.connection = null;
		ExecutorUtils.shutdown(executor, 1, TimeUnit.SECONDS);
	}
}
