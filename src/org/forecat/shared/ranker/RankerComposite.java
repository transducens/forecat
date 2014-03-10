package org.forecat.shared.ranker;

import java.util.ArrayList;
import java.util.List;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.suggestions.SuggestionsOutput;

public class RankerComposite extends RankerShared {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1229745996571442964L;
	RankerShared applyBefore = null;
	RankerShared applyAfter = null;

	protected RankerComposite() {

	}

	public RankerComposite(RankerShared before, RankerShared after) {
		applyBefore = before;
		applyAfter = after;
	}

	@Override
	public List<SuggestionsOutput> rankerService(RankerInput rankInp, List<SuggestionsOutput> input)
			throws ForecatException {
		ArrayList<SuggestionsOutput> outputSuggestionsList = new ArrayList<SuggestionsOutput>();

		// Only the last ranker limits the number of suggestions to be shown
		int holdmaxSuggestions = maxSuggestions;
		maxSuggestions = Integer.MAX_VALUE;
		input = applyBefore.rankerService(rankInp, input);
		maxSuggestions = holdmaxSuggestions;
		input = applyAfter.rankerService(rankInp, input);

		for (SuggestionsOutput so : input) {
			outputSuggestionsList.add(so);
		}

		return outputSuggestionsList;
	}
}
