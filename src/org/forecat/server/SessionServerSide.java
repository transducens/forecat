package org.forecat.server;

import javax.servlet.http.HttpSession;

import org.forecat.shared.SessionShared;

public class SessionServerSide implements SessionShared {

	HttpSession session = null;

	public SessionServerSide() {
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

	public boolean isInitialized() {
		return session != null;
	}

	@Override
	public void setAttribute(String key, Object value) {
		if (isInitialized()) {
			session.setAttribute(key, value);
		}
	}

	@Override
	public Object getAttribute(String key) {
		if (isInitialized()) {
			return session.getAttribute(key);
		} else {
			return null;
		}
	}

	@Override
	public String getId() {
		return session.getId();
	}
}
