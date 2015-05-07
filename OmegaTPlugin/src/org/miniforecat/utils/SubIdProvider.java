package org.miniforecat.utils;

import java.util.ArrayList;

import org.miniforecat.translation.SourceSegment;

public class SubIdProvider {

	public static class Pair {
		public Pair(String l, SourceSegment r) {
		}
	}

	static ArrayList<Pair> subids = new ArrayList<Pair>();
	public static boolean isWorking = false;


	public static int getSubId(String s, SourceSegment ss) {
		if (isWorking) {
			return subids.indexOf(new Pair(s, ss));
		}
		return 0;
	}

	public static void clear() {
		subids.clear();
	}

	public static void addElement(String s, SourceSegment ss) {
		subids.add(new Pair(s, ss));
	}
}
