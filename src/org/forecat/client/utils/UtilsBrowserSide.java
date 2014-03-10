package org.forecat.client.utils;

/**
 * Utility methods for the browser side of the application.
 */
public final class UtilsBrowserSide {

	/**
	 * Private constructor to avoid creation of objects of the class.
	 */
	private UtilsBrowserSide() {
	}

	// This code is left just to show how to use different regular expression libraries in
	// client/server sides.
	// This method is not currently used; see org.forecat.shared.UtilsShared.isPrefix() instead.
	/*
	 * public static boolean isPrefix(String possiblePrefix, String string) { RegExp regExp =
	 * RegExp.compile("^"+possiblePrefix); MatchResult matcher = regExp.exec(string); return
	 * (matcher != null); }
	 */
}
