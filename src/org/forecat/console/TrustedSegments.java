package org.forecat.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.forecat.shared.translation.SourceSegment;

public class TrustedSegments {
	static HashMap<String, Set<String>> possibles = new HashMap<String, Set<String>>(); // Target ->
																						// [Source1,
																						// Source2...]
	static HashMap<String, Integer> count = new HashMap<String, Integer>();

	public static void addTranslationMemory(String target, Map<String, List<SourceSegment>> segments) {
		for (String targetSegment : segments.keySet()) {
			for (SourceSegment seg : segments.get(targetSegment)) {
				if (target.contains(targetSegment)) {
					if (!possibles.containsKey(targetSegment)) {
						possibles.put(targetSegment, new HashSet<String>());
					}

					possibles.get(targetSegment).add(seg.getSourceSegmentText());

					if (!count.containsKey(targetSegment)) {
						count.put(targetSegment, 1);
					} else {
						count.put(targetSegment, count.get(targetSegment) + 1);
					}
				}
			}
		}
	}

	public static Map<String, List<SourceSegment>> getOk(Map<String, List<SourceSegment>> segments) {
		Map<String, List<SourceSegment>> ret = new HashMap<String, List<SourceSegment>>();

		for (String targetSegment : segments.keySet()) {
			if (possibles.containsKey(targetSegment)) {
				ArrayList<SourceSegment> compatible = new ArrayList<SourceSegment>();
				for (SourceSegment seg : segments.get(targetSegment)) {
					compatible.add(seg);
				}
				if (compatible.size() > 0) {
					ret.put(targetSegment, compatible);
				}
			}
		}

		return ret;
	}

	// static Map<String, List<SourceSegment>> getStrictOk(Map<String, List<SourceSegment>>
	// segments) {
	// Map<String, List<SourceSegment>> ret = new HashMap<String, List<SourceSegment>>();
	//
	// for (String targetSegment : segments.keySet()) {
	// if (possibles.containsKey(targetSegment)) {
	// ArrayList<SourceSegment> compatible = new ArrayList<SourceSegment>();
	// for (SourceSegment seg : segments.get(targetSegment)) {
	// if (possibles.get(targetSegment).contains(seg.getTargetSegmentText())) {
	// compatible.add(seg);
	// }
	// }
	// if (compatible.size() > 0) {
	// ret.put(targetSegment, compatible);
	// }
	// }
	// }
	//
	// return ret;
	// }

	public static int getSize() {
		return possibles.size();
	}

	static int NGRAM = 3;

	// TODO: Unfinished
	// static int getScore(String fixed, String sug) {
	// int score = 0;
	// String[] fixWords = fixed.split(" ");
	// String[] sugWords = sug.split(" ");
	// String cur = "";
	//
	// int endFix = fixWords.length - 1;
	//
	// if (fixWords.length > 1) {
	// cur = fixWords[endFix] + " " + sugWords[0];
	// if (sugWords.length > 1) {
	// cur = fixWords[endFix] + " " + sugWords[0] + " " + sugWords[1];
	// }
	// }
	// if (fixWords.length > 2) {
	// cur = fixWords[endFix - 1] + " " + fixWords[endFix] + " " + sugWords[0];
	// }
	//
	// return score;
	// }

	public static int getScoreTimes(String sug) {
		if (count.containsKey(sug))
			return count.get(sug);
		return 0;
	}

	public static String text() {
		String ret = "";
		// for (String s : count.keySet()) {
		// ret += "#T# " + s + ":" + count.get(s) + "\n";
		// }
		// for (String s : possibles.keySet()) {
		// ret += "#TT# " + s + ": [";
		//
		// for (String s2 : possibles.get(s)) {
		// ret += s2 + " , ";
		// }
		//
		// ret += "] \n";
		// }
		return ret;
	}
}
