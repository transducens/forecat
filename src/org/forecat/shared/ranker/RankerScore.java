package org.forecat.shared.ranker;

import java.util.ArrayList;
import java.util.List;

import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.utils.Quicksort;

/**
 * Chooses the suggestions based in their score
 * 
 * @author Daniel Torregrosa
 * 
 */

public class RankerScore extends RankerShared {

	private static final long serialVersionUID = -7278773616524632991L;

	@Override
	public List<SuggestionsOutput> rankerService(SuggestionsInput rankinput,
			List<SuggestionsOutput> input) {

		ArrayList<SuggestionsOutput> outputSuggestionsList = new ArrayList<SuggestionsOutput>();
		ArrayList<Integer> sortList = new ArrayList<Integer>();

		for (int index = 0; index < input.size(); index++) {
			sortList.add(index);
		}

		Quicksort q = new Quicksort();
		q.sort(sortList, input);

		for (int index = 0; index < maxSuggestions && index < input.size(); index++) {
			outputSuggestionsList.add(input.get(sortList.get(index)));
		}

		return outputSuggestionsList;
	}
}
