package org.miniforecat.suggestions;

import java.util.List;
import java.util.Map;

import org.miniforecat.exceptions.BboxcatException;
import org.miniforecat.ranker.RankerInput;
import org.miniforecat.ranker.RankerShared;
import org.miniforecat.suggestions.SuggestionsInput;
import org.miniforecat.suggestions.SuggestionsOutput;
import org.miniforecat.suggestions.SuggestionsShared;
import org.miniforecat.translation.SourceSegment;

public class SuggestionsRanker extends SuggestionsShared {

	SuggestionsShared base;
	RankerShared ranker;

	@Override
	public List<SuggestionsOutput> obtainSuggestions(SuggestionsInput input,
			Map<String, List<SourceSegment>> segmentPairs, Map<String, Integer> segmentCounts) {
		RankerInput rankerInput = new RankerInput(input.getPosition());
		List<SuggestionsOutput> output = base.obtainSuggestions(input, segmentPairs, segmentCounts);
		try {
			output = ranker.rankerService(rankerInput, output);
		} catch (BboxcatException e) {
			e.printStackTrace();
		}
		return output;
	}

	protected SuggestionsRanker() {

	}

	public SuggestionsRanker(SuggestionsShared base, RankerShared ranker) {
		super();
		this.base = base;
		this.ranker = ranker;
	}

}
