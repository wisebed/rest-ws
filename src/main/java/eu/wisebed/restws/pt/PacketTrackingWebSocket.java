package eu.wisebed.restws.pt;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import eu.wisebed.restws.util.InjectLogger;
import org.eclipse.jetty.websocket.WebSocket;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Throwables.propagate;

public class PacketTrackingWebSocket implements WebSocket, WebSocket.OnTextMessage {

	@InjectLogger
	private Logger log;

	/**
	 * +------+-----------+---------+---------+--------+<br/>
	 * | TYPE | DIRECTION | SRC_MAC | DST_MAC | PACKET |<br/>
	 * +------+-----------+---------+---------+--------+<br/>
	 * <p/>
	 * TYPE: (1 Byte) 32 for MAC Pakets, 16 for IPv6 Pakete<br/>
	 * DIRECTION: (1 Byte) 'i' für incoming, 'o' for outgoing<br/>
	 * SRC_MAC: (8 Byte) Source MAC Address<br/>
	 * DST_MAC: (8 Byte) Destination MAC Address<br/>
	 * PACKET: (max. 1280 Byte) RAW IPv6 packet, i.e. (IPv6 Header | Transport Header | Application Header / Payload ) or
	 * MAC Payload
	 */
	private final Runnable packetGeneratorRunnable = new Runnable() {
		@Override
		public void run() {
			try {

				connection.sendMessage("Hello, World!");
			} catch (IOException e) {
				connection.close(500, "Internal Server Error");
				throw propagate(e);
			}
		}
	};

	private final ScheduledExecutorService scheduler;

	private Connection connection;

	private ScheduledFuture<?> schedule;

	@Inject
	public PacketTrackingWebSocket(@Assisted final ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public void onMessage(final String data) {
		// nothing to do
	}

	@Override
	public void onOpen(final Connection connection) {
		this.connection = connection;
		this.schedule = this.scheduler.scheduleAtFixedRate(packetGeneratorRunnable, 1, 2, TimeUnit.SECONDS);
	}

	@Override
	public void onClose(final int closeCode, final String message) {
		this.schedule.cancel(true);
		this.connection = null;
	}
}
