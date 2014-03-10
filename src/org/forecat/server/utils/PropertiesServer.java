package org.forecat.server.utils;

public final class PropertiesServer {

	private PropertiesServer() {
	}

	public enum ApertiumLocations {
		LOCAL_APERTIUM, NET_APERTIUM
	};

	/**
	 * Installation of Apertium to use. This is only used in server code.
	 */
	// public static final ApertiumLocations apertiumLocation = ApertiumLocations.NET_APERTIUM;
	public static ApertiumLocations apertiumLocation = ApertiumLocations.LOCAL_APERTIUM;

	public static final String serverurl = "http://127.0.0.1:8888/forecat/forecat";
	public static final boolean useUrl = true;
}
