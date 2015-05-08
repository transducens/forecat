package org.forecat.shared.ranker;

import java.util.ArrayList;
import java.util.List;

import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.utils.Quicksort;

/**
 * Chooses the longest suggestions
 * 
 * @author Daniel Torregrosa
 * 
 */

public class RankerLongestFirst extends RankerShared {

	private static final long serialVersionUID = 896290267380126773L;

	@Override
	public List<SuggestionsOutput> rankerService(RankerInput rankinput,
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

		for (int index = 0; index < maxSuggestions && index < input.size(); index++) {
			outputSuggestionsList.add(input.get(sortList.get(sortList.size() - index - 1)));
		}

		return outputSuggestionsList;
	}

}
