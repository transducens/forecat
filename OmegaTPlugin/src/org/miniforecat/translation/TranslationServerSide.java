package org.miniforecat.translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miniforecat.exceptions.BboxcatException;
import org.miniforecat.translation.cachetrans.Cachetrans;
import org.miniforecat.SessionShared;
import org.miniforecat.translation.SourceSegment;
import org.miniforecat.translation.TranslationInput;
import org.miniforecat.translation.TranslationOutput;
import org.miniforecat.utils.PropertiesShared;
import org.miniforecat.utils.SubIdProvider;
import org.miniforecat.utils.UtilsShared;

public class TranslationServerSide {

	public TranslationOutput translationService(
			TranslationInput inputTranslation, SessionShared session)
			throws BboxcatException {

		final Map<String, List<SourceSegment>> segmentPairs = new HashMap<String, List<SourceSegment>>();
		final Map<String, Integer> segmentCounts = new HashMap<String, Integer>();
		int currentId = 1;
		Object aux = session.getAttribute("SuggestionId");
		if (aux != null) {
			currentId = (Integer) aux;
		}
		List<SourceSegment> sourceSegments = sliceIntoSegments(
				slice(inputTranslation.getSourceText()),
				inputTranslation.getMaxSegmentLenth(),
				inputTranslation.getMinSegmentLenth(), currentId);
		List<String> targetSegments = Cachetrans.getTranslation(
				inputTranslation.getSourceCode(),
				inputTranslation.getTargetCode(), sourceSegments);
		session.setAttribute("segmentPairs", segmentPairs);
		session.setAttribute("segmentCounts", segmentCounts);

		for (int i = 0, n = sourceSegments.size(); i < n; ++i) {
			String targetText = targetSegments.get(i);
			addSegments(segmentPairs, segmentCounts, targetText,
					"cachetrans", sourceSegments.get(i));
		}
		return new TranslationOutput(sourceSegments.size(), sourceSegments.size());
	}

	protected static String[] slice(String text) {
		return text.split("\\s+");
	}

	public static List<SourceSegment> sliceIntoSegments(String[] words,
			int maxSegmentLength, int minSegmentLength, int currentId) {

		int wordCount = words.length;
		int partialId = 0;

		if (wordCount * maxSegmentLength > PropertiesShared.maxSegments) {
			wordCount = (int) Math.floor(PropertiesShared.maxSegments
					/ maxSegmentLength);
		}

		List<SourceSegment> sourceSegments = new ArrayList<SourceSegment>();

		int numchars = 0;

		for (int i = 0; i < wordCount; ++i) {
			String sourceText = "";
			String delim = "";
			for (int j = 0; j < maxSegmentLength; ++j) {
				if (i + j < words.length) {
					sourceText += delim + words[i + j];
					delim = " ";
					if (j >= (minSegmentLength - 1)) {
						sourceSegments.add(new SourceSegment(sourceText, i,
								false, currentId + partialId, numchars));
						partialId++;
					}
				} else {
					break;
				}
			}
			numchars += words[i].length() + 1;
		}
		return sourceSegments;
	}

	protected static void addSegments(
			Map<String, List<SourceSegment>> segmentPairs,
			Map<String, Integer> segmentCounts, String targetText,
			String engine, SourceSegment sourceSegment) {
		// sourceSegment does not contain any engines yet
		if (!targetText.isEmpty()) {
			addSegment(segmentPairs, segmentCounts, targetText, engine,
					sourceSegment);
			String targetTextLowercase = UtilsShared
					.uncapitalizeFirstLetter(targetText);
			addSegment(segmentPairs, segmentCounts, targetTextLowercase,
					engine, new SourceSegment(sourceSegment));
		}
	}

	protected static void addSegment(
			Map<String, List<SourceSegment>> segmentPairs,
			Map<String, Integer> segmentCounts, String targetText,
			String engine, SourceSegment sourceSegment) {
		SourceSegment s = null;

		SubIdProvider.addElement(targetText, sourceSegment);

		if (!segmentPairs.containsKey(targetText)) {
			segmentPairs.put(targetText, new ArrayList<SourceSegment>());
			segmentCounts.put(targetText, 0);
		} else {
			s = SourceSegment.searchByTextAndPosition(
					segmentPairs.get(targetText),
					sourceSegment.getSourceSegmentText(),
					sourceSegment.getPosition());
		}
		if (s != null) {
			s.addEngine(engine);
		} else {
			sourceSegment.addEngine(engine);
			sourceSegment.setUsed(false);
			segmentPairs.get(targetText).add(sourceSegment);
		}
		segmentCounts.put(targetText, segmentCounts.get(targetText) + 1);
	}
}
