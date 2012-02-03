package eu.wisebed.restws.resources;

public class ResourceHelper {

	public static String createSecretAuthenticationKeyCookieName(final String testbedId) {
		return Constants.COOKIE_SECRET_AUTH_KEY + "-" + testbedId;
	}
}
