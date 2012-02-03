package eu.wisebed.restws.resources;

import javax.ws.rs.core.Response;

public class ResourceHelper {

	public static String createSecretAuthenticationKeyCookieName(final String testbedId) {
		return Constants.COOKIE_SECRET_AUTH_KEY + "-" + testbedId;
	}
}
