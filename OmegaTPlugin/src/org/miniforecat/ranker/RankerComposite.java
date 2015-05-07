package org.miniforecat.ranker;

import java.util.ArrayList;
import java.util.List;

import org.miniforecat.exceptions.BboxcatException;
import org.miniforecat.suggestions.SuggestionsOutput;

public class RankerComposite extends RankerShared {

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
			throws BboxcatException {
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
