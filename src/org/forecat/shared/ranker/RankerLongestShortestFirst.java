package org.forecat.shared.ranker;

import java.util.ArrayList;
import java.util.List;

import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.utils.Quicksort;

/**
 * Alternates between the longest and shortest suggestions
 * 
 * @author Daniel Torregrosa
 * 
 */
public class RankerLongestShortestFirst extends RankerShared {

	private static final long serialVersionUID = 2151574862257494336L;

	@Override
	public List<SuggestionsOutput> rankerService(SuggestionsInput rankInp,
			List<SuggestionsOutput> input) {
		ArrayList<SuggestionsOutput> outputSuggestionsList = new ArrayList<SuggestionsOutput>();
		ArrayList<Integer> sortList = new ArrayList<Integer>();
		SuggestionsOutput so;

		for (int index = 0; index < input.size(); index++) {
			sortList.add(index);
			so = input.get(index);
			so.setSuggestionFeasibility(so.getSuggestionText().length());
		}
		Quicksort q = new Quicksort();
		q.sort(sortList, input);

		boolean first = true;
		for (int index = 0; index < maxSuggestions && index < input.size(); index++) {
			if (first) {
				outputSuggestionsList.add(input.get(sortList.get(sortList.size() - index / 2 - 1)));
			} else {
				outputSuggestionsList.add(input.get(sortList.get(index / 2)));
			}
			first = !first;
		}

		return outputSuggestionsList;
	}
}
