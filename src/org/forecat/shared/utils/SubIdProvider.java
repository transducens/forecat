package org.forecat.shared.utils;

import java.util.ArrayList;

import org.forecat.shared.translation.SourceSegment;

public class SubIdProvider {

	public static class Pair {
		private final String l;
		private final SourceSegment r;

		public Pair(String l, SourceSegment r) {
			this.l = l;
			this.r = r;
		}
	}

	static ArrayList<Pair> subids = new ArrayList<Pair>();
	public static boolean isWorking = false;

	public static void addElement(String s, SourceSegment ss) {
		if (isWorking)
			subids.add(new Pair(s, ss));
	}

	public static int getSubId(String s, SourceSegment ss) {
		if (isWorking) {
			return subids.indexOf(new Pair(s, ss));
		}
		return 0;
	}

	public static void clear() {
		subids.clear();
	}
}
