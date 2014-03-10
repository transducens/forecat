package org.forecat.shared;

public interface SessionShared {
	public void setAttribute(String key, Object value);

	public Object getAttribute(String key);

	public String getId();
}
