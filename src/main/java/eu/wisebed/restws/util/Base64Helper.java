package eu.wisebed.restws.util;

import com.sun.jersey.core.util.Base64;

public class Base64Helper {

	public static String decode(String input) {
		return new String(Base64.decode(input));
	}

	public static String encode(String input) {
		return new String(Base64.encode(input));
	}

	public static String encode(byte[] input) {
		return new String(Base64.encode(input));
	}

}
