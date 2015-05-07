package org.miniforecat.utils;


public class UtilsShared {

	private UtilsShared() {
	}

	public static String join(String[] list, String delim) {
		StringBuilder sb = new StringBuilder();
		String loopDelim = "";

		for (String s : list) {
			sb.append(loopDelim);
			sb.append(s);
			loopDelim = delim;
		}
		return sb.toString();
	}

	public static String uncapitalizeFirstLetter(String string) {
		return string.substring(0, 1).toLowerCase() + string.substring(1);
	}

	public static boolean isPrefix(String possiblePrefix, String string) {
		int i = 0;

		while (i < possiblePrefix.length() && i < string.length()) {
			if (possiblePrefix.charAt(i) != string.charAt(i)) {
				return false;
			}
			++i;
		}
		return i == possiblePrefix.length();
	}

	public static boolean isSuffix(String possibleSuffix, String string) {
		return possibleSuffix.endsWith(string);
	}

}
