package org.forecat.server.utils;

public final class PropertiesServer {

	private PropertiesServer() {
	}

	public enum ApertiumLocations {
		LOCAL_APERTIUM, NET_APERTIUM
	};

	public static final String serverurl = "http://127.0.0.1:8888/forecat/forecat";
	public static final boolean useUrl = true;
}
