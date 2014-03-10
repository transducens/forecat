package org.forecat.server.utils;

public class UtilsServerSide {

	private UtilsServerSide() {
	}

	// This code is left just to show how to use different regular expression libraries in
	// client/server sides.
	// This method is not currently used; see org.forecat.shared.UtilsShared.isPrefix() instead.
	// We do not use GWT RegEXp here because we want to make server-side GWT-free.
	/*
	 * public static boolean isPrefix(String possiblePrefix, String string) { Pattern pattern=
	 * Pattern.compile(possiblePrefix); Matcher matcher= pattern.matcher(string); return
	 * matcher.lookingAt(); }
	 */
}
