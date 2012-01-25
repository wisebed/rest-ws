package eu.wisebed.restws.resources;

public interface WsnWebSocketFactory {

	WsnWebSocket create(String experimentId);
}
