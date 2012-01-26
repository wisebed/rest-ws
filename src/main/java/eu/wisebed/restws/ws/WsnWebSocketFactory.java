package eu.wisebed.restws.ws;

public interface WsnWebSocketFactory {

	WsnWebSocket create(String experimentId);
}
