package org.forecat.shared.suggestions;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.ranker.RankerShared;
import org.forecat.shared.translation.SourceSegment;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SuggestionsRanker extends SuggestionsShared implements IsSerializable, Serializable {

	SuggestionsShared base;
	RankerShared ranker;

	/**
	 * 
	 */
	private static final long serialVersionUID = -1080886143337665973L;

	@Override
	public List<SuggestionsOutput> obtainSuggestions(SuggestionsInput input,
			Map<String, List<SourceSegment>> segmentPairs, Map<String, Integer> segmentCounts) {
		List<SuggestionsOutput> output = base.obtainSuggestions(input, segmentPairs, segmentCounts);
		try {
			output = ranker.rankerService(input, output);
		} catch (ForecatException e) {
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
