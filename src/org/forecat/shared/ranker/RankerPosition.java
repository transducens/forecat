package org.forecat.shared.ranker;

import java.util.ArrayList;
import java.util.List;

import org.forecat.shared.suggestions.SuggestionsOutput;

public class RankerPosition extends RankerShared {

	/**
	 * 
	 */
	private static final long serialVersionUID = 185266502718735714L;

	@Override
	public List<SuggestionsOutput> rankerService(RankerInput rankInp, List<SuggestionsOutput> input) {
		ArrayList<SuggestionsOutput> outputSuggestionsList = new ArrayList<SuggestionsOutput>();
		ArrayList<Integer> sortList = new ArrayList<Integer>();
		SuggestionsOutput so;

		for (int index = 0; index < input.size(); index++) {
			sortList.add(index);
			so = input.get(index);
			so.setSuggestionFeasibility(Math.abs(so.getPosition() - rankInp.getPosition()));
			// System.out.println("+" + so.getSuggestionText() + " " + so.getPosition() + " "
			// + rankInp.getPosition());
		}
		Quicksort q = new Quicksort();
		q.sort(sortList, input);

		for (int index = 0; index < maxSuggestions && index < input.size(); index++) {
			outputSuggestionsList.add(input.get(sortList.get(index)));
		}

		return outputSuggestionsList;
	}
}
