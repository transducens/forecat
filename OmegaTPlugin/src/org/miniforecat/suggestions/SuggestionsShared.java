package org.miniforecat.suggestions;

import java.util.List;
import java.util.Map;

import org.miniforecat.exceptions.BboxcatException;
import org.miniforecat.SessionShared;
import org.miniforecat.suggestions.SuggestionsInput;
import org.miniforecat.suggestions.SuggestionsOutput;
import org.miniforecat.translation.SourceSegment;

public abstract class SuggestionsShared {

	protected int frame = 0;

	public abstract List<SuggestionsOutput> obtainSuggestions(SuggestionsInput input, Map<String, List<SourceSegment>> segmentPairs, Map<String, Integer> segmentCounts);

	public SuggestionsShared() {
		super();
	}

	public void setFrame(int f) {
		frame = f;
	}

	public List<SuggestionsOutput> suggestionsService(SuggestionsInput input, SessionShared session) throws BboxcatException {
	
		session.setAttribute("targetText", input);
	
		@SuppressWarnings("unchecked")
		Map<String, List<SourceSegment>> segmentPairs = (Map<String, List<SourceSegment>>) session
				.getAttribute("segmentPairs");
		if (segmentPairs == null) {
			throw new BboxcatException("segmentPairs could not be obtained from session");
		}
		@SuppressWarnings("unchecked")
		Map<String, Integer> segmentCounts = (Map<String, Integer>) session
				.getAttribute("segmentCounts");
		if (segmentCounts == null) {
			throw new BboxcatException("segmentCounts could not be obtained from session");
		}
	
		List<SuggestionsOutput> output = obtainSuggestions(input, segmentPairs, segmentCounts);
		session.setAttribute("suggestions", output);
		return output;
	}
}
