package eu.wisebed.restws.pt;

import java.util.concurrent.ScheduledExecutorService;

public interface PacketTrackingWebSocketFactory {

	public PacketTrackingWebSocket create(ScheduledExecutorService scheduler);

}
