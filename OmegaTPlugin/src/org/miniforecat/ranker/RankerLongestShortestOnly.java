package org.miniforecat.ranker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.miniforecat.exceptions.BboxcatException;
import org.miniforecat.suggestions.SuggestionsOutput;

public class RankerLongestShortestOnly extends RankerShared {

	RankerShared applyBefore = null;

	public class SuggestionsOutputLengthComparator implements Comparator<SuggestionsOutput> {
		@Override
		public int compare(SuggestionsOutput o1, SuggestionsOutput o2) {
			return o1.getSuggestionText().length() - o2.getSuggestionText().length();
		}
	}

	@Override
	public List<SuggestionsOutput> rankerService(RankerInput rankInp, List<SuggestionsOutput> input)
			throws BboxcatException {
		ArrayList<SuggestionsOutput> outputSuggestionsList = new ArrayList<SuggestionsOutput>();
		HashMap<Integer, List<SuggestionsOutput>> all = new HashMap<Integer, List<SuggestionsOutput>>();
		int biggestList = 0;

		for (SuggestionsOutput so : input) {
			if (all.get(so.getPosition()) == null) {
				all.put(so.getPosition(), new ArrayList<SuggestionsOutput>());
				all.get(so.getPosition()).add(so);
			} else {
				all.get(so.getPosition()).add(so);
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

		return outputSuggestionsList;
	}
}
