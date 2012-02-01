package eu.wisebed.restws.ws;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.uniluebeck.itm.tr.util.ExecutorUtils;
import eu.wisebed.restws.dto.WebSocketDownstreamMessage;
import eu.wisebed.restws.dto.WebSocketNotificationMessage;
import eu.wisebed.restws.dto.WebSocketUpstreamMessage;
import eu.wisebed.restws.proxy.WsnProxyManager;
import eu.wisebed.restws.util.Base64Helper;
import eu.wisebed.restws.util.InjectLogger;
import eu.wisebed.restws.util.JSONHelper;
import org.eclipse.jetty.websocket.WebSocket;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WsnWebSocket implements WebSocket, WebSocket.OnTextMessage {

	@InjectLogger
	private Logger log;

	private final WsnProxyManager wsnProxyManager;

	private final String experimentId;

	private Connection connection;

	private ScheduledExecutorService executor;

	private class AliveRunnable implements Runnable {

		private final String[] availableNodeUrns = {
				"urn:wisebed:uzl1:0x1234",
				"urn:wisebed:uzl1:0x2345",
				"urn:wisebed:uzl1:0x3456",
				"urn:wisebed:uzl1:0x4567",
				"urn:wisebed:uzl1:0x5678",
				"urn:wisebed:uzl1:0x6789",
				"urn:wisebed:uzl1:0x7890",
				"urn:wisebed:uzl1:0x8901",
				"urn:wisebed:uzl1:0x9012",
				"urn:wisebed:uzl1:0x0123"
		};

		private final String[] availablePayloads = {
				"Internet! Is that thing still around?",
				"Ah, beer, my one weakness. My Achille's heel, if you will.",
				"Okay, whatever to take my mind off my life.",
				"All right, brain. You don't like me and I don't like you, but let's just do this and I can get back to killing you with beer.",
				"All right, let's not panic. I'll make the money by selling one of my livers. I can get by with one.",
				"And how is education supposed to make me feel smarter? Besides, every time I learn something new, it pushes some old stuff out of my brain. Remember when I took that home winemaking course, and I forgot how to drive?",
				"Aw, Dad, you've done a lot of great things, but you're a very old man, and old people are useless.",
				"If something goes wrong at the plant, blame the guy who can't speak English.",
				"I don't apologize. I am sorry Lisa, that's the way I am.",
				"Bart, a woman is like beer. They look good, they smell good, and you'd step over your own mother just to get one!"
		};

		private final Random random = new Random();

		private final boolean node;

		private AliveRunnable(final boolean node) {
			this.node = node;
		}

		@Override
		public void run() {
			if (node) {
				sendUpstream(new DateTime(), getRandomNodeUrn(), getRandomPayload());
			} else {
				sendNotification(new DateTime(), getRandomPayload());
			}
		}

		private String getRandomPayload() {
			return availablePayloads[random.nextInt(availablePayloads.length)];
		}

		private String getRandomNodeUrn() {
			return availableNodeUrns[random.nextInt(availableNodeUrns.length)];
		}

	}

	private final Runnable nodeRunnable = new AliveRunnable(true);

	private final Runnable notificationRunnable = new AliveRunnable(false);

	private ScheduledFuture<?> nodeSchedule;

	private ScheduledFuture<?> notificationSchedule;

	@Inject
	public WsnWebSocket(final WsnProxyManager wsnProxyManager, @Assisted final String experimentId) {
		this.wsnProxyManager = wsnProxyManager;
		this.experimentId = experimentId;
	}

	@Override
	public void onMessage(final String data) {
		try {
			onMessage(JSONHelper.fromJSON(data, WebSocketDownstreamMessage.class));
		} catch (Exception e) {
			sendNotification(
					new DateTime(),
					"The following downstream message could not be parsed by the server: " + data + ". Exception: " + e
			);
		}
	}

	private void onMessage(final WebSocketDownstreamMessage message) {
		log.warn("TODO: send this message downstream: " + message);
	}

	private void sendUpstream(final DateTime dateTime, final String nodeUrn, final String payload) {
		String json = JSONHelper.toJSON(
				new WebSocketUpstreamMessage(
						dateTime.toString(ISODateTimeFormat.basicDateTimeNoMillis()),
						nodeUrn,
						Base64Helper.encode(payload)
				)
		);
		System.out.println(json);
		sendMessage(json);
	}

	private void sendNotification(final DateTime dateTime, final String notification) {
		sendMessage(
				JSONHelper.toJSON(
						new WebSocketNotificationMessage(
								dateTime.toString(ISODateTimeFormat.basicDateTimeNoMillis()),
								notification
						)
				)
		);
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
		this.nodeSchedule = this.executor.scheduleAtFixedRate(nodeRunnable, 2, 2, TimeUnit.SECONDS);
		this.notificationSchedule = this.executor.scheduleAtFixedRate(notificationRunnable, 5, 5, TimeUnit.SECONDS);
		log.info("Websocket connection opened: {}", connection);
	}

	@Override
	public void onClose(final int closeCode, final String message) {
		if (log.isInfoEnabled()) {
			log.info("Websocket connection closed with code {} and message \"{}\": {}",
					new Object[]{closeCode, message, connection}
			);
		}
		this.nodeSchedule.cancel(true);
		this.notificationSchedule.cancel(true);
		this.connection = null;
		ExecutorUtils.shutdown(executor, 1, TimeUnit.SECONDS);
	}
}
