package de.d2dev.fourseasons.network;

import java.util.regex.Pattern;

public class Network {
	
	public static final String IP_V4_REGEX = "\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b";
	// public static final String IP_V6_REGEX = "";
	
	public static boolean isIPv4Address(String s) {
		final Pattern p = Pattern.compile(IP_V4_REGEX);
		return p.matcher(s).matches();
	}
	
	public static boolean isIPv6Address(String s) {
		return false;
	}
	
	public static boolean isIPAddress(String s) {
		return Network.isIPv4Address(s) || Network.isIPv6Address(s);
	}
}
