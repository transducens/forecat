package org.forecat.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.forecat.shared.SessionBrowserSideConsole;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.translation.SourceSegment;
import org.forecat.shared.utils.UtilsShared;

public class TestHelper {

	/**
	 * Gets precision and recall for each event, and adds it to TestOutput
	 * 
	 * @param offers
	 *            Offered suggestions
	 * @param session
	 *            Current session
	 * @param prefix
	 *            Current typed prefix
	 * @param target
	 *            Target sentence
	 * @param event
	 *            Information of the action taken
	 */
	static void getPrecAndRecall(ArrayList<SuggestionsOutput> offers,
			SessionBrowserSideConsole session, String prefix, String target, Event event) {
		@SuppressWarnings("unchecked")
		Map<String, List<SourceSegment>> segmentPairs = (Map<String, List<SourceSegment>>) session
				.getAttribute("segmentPairs");

		int ok = 0;
		int nok = 0;
		int sugOk = 0;
		int sugNok = 0;
		boolean isOk = false;

		for (Entry<String, List<SourceSegment>> e : segmentPairs.entrySet()) {
			isOk = false;

			if (UtilsShared.isPrefix(prefix, e.getKey())) {
				if (UtilsShared.isPrefix(e.getKey(), target)
						&& (e.getKey().length() == target.length() || Character.isWhitespace(target
								.charAt(e.getKey().length())))) {
					for (SourceSegment sg : e.getValue()) {
						event.addPotentialSuggestion(sg.getId());
					}
					ok++;
					isOk = true;
				} else {
					nok++;
				}

				for (SuggestionsOutput so : offers) {
					if (so.getSuggestionText().equals(e.getKey())) {
						if (isOk) {
							sugOk++;
						} else {
							sugNok++;
						}
					}
				}
			}
		}

		double precision = (double) sugOk / (double) (sugOk + sugNok);
		double recall = (double) sugOk / (double) ok;

		if (ok == 0) {
			if ((sugOk + sugNok) == 0) {
				precision = 1;
				recall = 1;
			} else {
				precision = 0;
				recall = 0;
			}
		} else {
			if ((sugOk + sugNok) == 0) {
				precision = 0;
				recall = 0;
			}
		}
		if (event.getChar() != ' ') {
			TestOutput.addAllOk(ok);
			TestOutput.addAllNok(nok);
			TestOutput.addAllSugOk(sugOk);
			TestOutput.addAllSugNok(sugNok);
			TestOutput.addPrecision(precision);
			TestOutput.addRecall(recall);
		}

		event.setPrecision(precision);
		event.setRecall(recall);
	}

	/**
	 * Computes the coverage % of each sentence
	 * 
	 * @param session
	 *            Current session
	 * @param target
	 *            Target sentence
	 */
	static void getSegmentsCoverage(SessionBrowserSideConsole session, String target) {

		@SuppressWarnings("unchecked")
		Map<String, List<SourceSegment>> segmentPairs = (Map<String, List<SourceSegment>>) session
				.getAttribute("segmentPairs");
		Integer bySegmentOk[] = new Integer[Main.segmentLength];
		Integer bySegmentTotal[] = new Integer[Main.segmentLength];
		int ok = 0;
		int total = 0;
		int index = 0;

		for (int i = 0; i < Main.segmentLength; i++) {
			bySegmentOk[i] = 0;
			bySegmentTotal[i] = 0;
		}

		for (Entry<String, List<SourceSegment>> e : segmentPairs.entrySet()) {
			for (SourceSegment s : e.getValue()) {
				index = s.getSourceSegmentText().split(" ").length - 1;
				total++;
				bySegmentTotal[index]++;

				if (target.contains(s.getSourceSegmentText())) {
					ok++;
					bySegmentOk[index]++;
				}
			}
		}

		TestOutput.addCoverages(ok, total, bySegmentOk, bySegmentTotal);
	}

}
