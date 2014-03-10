package org.forecat.shared.ranker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.suggestions.SuggestionsOutput;

public class RankerLongestShortestOnly extends RankerShared {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6208413797820197425L;
	RankerShared applyBefore = null;

	public class SuggestionsOutputLengthComparator implements Comparator<SuggestionsOutput> {
		@Override
		public int compare(SuggestionsOutput o1, SuggestionsOutput o2) {
			return o1.getSuggestionText().length() - o2.getSuggestionText().length();
		}
	}

	@Override
	public List<SuggestionsOutput> rankerService(RankerInput rankInp, List<SuggestionsOutput> input)
			throws ForecatException {
		ArrayList<SuggestionsOutput> outputSuggestionsList = new ArrayList<SuggestionsOutput>();
		// HashMap<Integer, SuggestionsOutput> longest = new HashMap<Integer, SuggestionsOutput>();
		// HashMap<Integer, SuggestionsOutput> shortest = new HashMap<Integer, SuggestionsOutput>();
		HashMap<Integer, List<SuggestionsOutput>> all = new HashMap<Integer, List<SuggestionsOutput>>();
		int biggestList = 0;

		// System.out.println("<<<<");
		for (SuggestionsOutput so : input) {
			// System.out.println("< " + so.getPosition() + " " + so.getSuggestionText() + " "
			// + so.getSuggestionFeasibility());
			if (all.get(so.getPosition()) == null) {
				// longest.put(so.getPosition(), so);
				// shortest.put(so.getPosition(), so);
				all.put(so.getPosition(), new ArrayList<SuggestionsOutput>());
				all.get(so.getPosition()).add(so);
			} else {
				all.get(so.getPosition()).add(so);
				// if (longest.get(so.getPosition()).getSuggestionText().length() < so
				// .getSuggestionText().length()) {
				// longest.put(so.getPosition(), so);
				// }
				// if (shortest.get(so.getPosition()).getSuggestionText().length() > so
				// .getSuggestionText().length()) {
				// shortest.put(so.getPosition(), so);
				// }
			}
			if (all.get(so.getPosition()).size() > biggestList) {
				biggestList = all.get(so.getPosition()).size();
			}
		}

		for (Integer i : all.keySet()) {
			Collections.sort(all.get(i), new SuggestionsOutputLengthComparator());
		}

		ArrayList<Integer> order = new ArrayList<Integer>();

		for (SuggestionsOutput so : input) {
			if (!order.contains(so.getPosition())) {
				order.add(so.getPosition());
			}
		}

		List<SuggestionsOutput> current;
		// for (Integer i : order) {
		// current = all.get(i);
		//
		// if (!outputSuggestionsList.contains(current.get(current.size() - 1))) {
		// outputSuggestionsList.add(current.get(current.size() - 1));
		// }
		// if (!outputSuggestionsList.contains(current.get(0))) {
		// outputSuggestionsList.add(current.get(0));
		// }
		// }
		int iteration = 0;

		while ((iteration <= biggestList / 2) && outputSuggestionsList.size() < maxSuggestions) {
			for (Integer i : order) {
				current = all.get(i);
				if (iteration <= current.size() / 2) {
					if (outputSuggestionsList.size() >= maxSuggestions)
						break;
					if (!outputSuggestionsList.contains(current.get(current.size()
							- (1 + iteration)))) {
						outputSuggestionsList.add(current.get(current.size() - (1 + iteration)));
					}
					if (outputSuggestionsList.size() >= maxSuggestions)
						break;
					if (!outputSuggestionsList.contains(current.get(iteration))) {
						outputSuggestionsList.add(current.get(iteration));
					}
				}
			}
			iteration++;
		}

		// for (Integer pos : longest.keySet()) {
		// if (!outputSuggestionsList.contains(longest.get(pos)))
		// outputSuggestionsList.add(longest.get(pos));
		// if (longest.get(pos) != shortest.get(pos)) {
		// if (!outputSuggestionsList.contains(shortest.get(pos)))
		// outputSuggestionsList.add(shortest.get(pos));
		// }
		// }
		// System.out.println(">>>> " + biggestList);
		// for (SuggestionsOutput so : outputSuggestionsList) {
		// System.out.println("> " + so.getPosition() + " " + so.getSuggestionText());
		// }

		return outputSuggestionsList;
	}
}
