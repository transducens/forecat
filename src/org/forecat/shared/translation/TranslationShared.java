package org.forecat.shared.translation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.forecat.shared.utils.PropertiesShared;
import org.forecat.shared.utils.SubIdProvider;
import org.forecat.shared.utils.UtilsShared;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class TranslationShared implements IsSerializable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7564451857631940674L;

	public static List<SourceSegment> sliceIntoSegments(String[] words, int maxSegmentLength,
			int minSegmentLength, int currentId) {

		int wordCount = words.length;
		int partialId = 0;

		if (wordCount * maxSegmentLength > PropertiesShared.maxSegments) {
			wordCount = (int) Math.floor(PropertiesShared.maxSegments / maxSegmentLength);
		}

		List<SourceSegment> sourceSegments = new ArrayList<SourceSegment>();

		int numchars = 0;

		for (int i = 0; i < wordCount; ++i) {
			String sourceText = "";
			String delim = "";
			for (int j = 0; j < maxSegmentLength && i + j < words.length; ++j) {
				sourceText += delim + words[i + j];
				delim = " ";
				if (j >= (minSegmentLength - 1)) {
					System.err.println(sourceText + " " + i + " " + currentId + partialId + " "
							+ numchars);
					sourceSegments.add(new SourceSegment(sourceText, i, false, currentId
							+ partialId, numchars));
					partialId++;
				}
			}
			numchars += words[i].length() + 1;
		}
		return sourceSegments;
	}

	protected static String[] slice(String text) {
		return text.split("\\s+");
	}

	protected static void addSegments(Map<String, List<SourceSegment>> segmentPairs,
			Map<String, Integer> segmentCounts, String targetText, String engine,
			SourceSegment sourceSegment) {
		// sourceSegment does not contain any engines yet
		if (!targetText.isEmpty()) {
			addSegment(segmentPairs, segmentCounts, targetText, engine, sourceSegment);
			String targetTextLowercase = UtilsShared.uncapitalizeFirstLetter(targetText);
			addSegment(segmentPairs, segmentCounts, targetTextLowercase, engine, new SourceSegment(
					sourceSegment));
		}
	}

	protected static void addSegment(Map<String, List<SourceSegment>> segmentPairs,
			Map<String, Integer> segmentCounts, String targetText, String engine,
			SourceSegment sourceSegment) {
		SourceSegment s = null;

		SubIdProvider.addElement(targetText, sourceSegment);

		if (!segmentPairs.containsKey(targetText)) {
			segmentPairs.put(targetText, new ArrayList<SourceSegment>());
			segmentCounts.put(targetText, 0);
		} else {
			s = SourceSegment.searchByTextAndPosition(segmentPairs.get(targetText),
					sourceSegment.getSourceSegmentText(), sourceSegment.getPosition());
		}
		if (s != null) {
			// TODO: make SourceSegment.engineList a HashSet to avoid duplicated engines, although
			// this only makes sense
			// if a particular engine returns more than one translation for the same SourceSegment
			// object
			s.addEngine(engine);
		} else {
			sourceSegment.addEngine(engine);
			sourceSegment.setUsed(false);
			segmentPairs.get(targetText).add(sourceSegment);
		}
		segmentCounts.put(targetText, segmentCounts.get(targetText) + 1);
	}

}
