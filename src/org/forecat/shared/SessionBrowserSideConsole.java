package org.forecat.shared;

import java.util.HashMap;
import java.util.Map;

// TODO: implement this class as a singleton
public class SessionBrowserSideConsole implements SessionShared {
	private final Map<String, Object> map;

	public SessionBrowserSideConsole() {
		map = new HashMap<String, Object>();
	}

	@Override
	public void setAttribute(String key, Object value) {
		map.put(key, value);
	}

	@Override
	public Object getAttribute(String key) {
		return map.get(key);
	}

	@Override
	public String getId() {
		return "Local";
	}
}
