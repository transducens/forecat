package org.miniforecat.ranker;

import java.util.List;

import org.miniforecat.exceptions.BboxcatException;
import org.miniforecat.suggestions.SuggestionsOutput;

public abstract class RankerShared{

	protected static int maxSuggestions = Integer.MAX_VALUE;

	public static void setMaxSuggestions(int value) {
		maxSuggestions = value;
	}

	public abstract List<SuggestionsOutput> rankerService(RankerInput rankinp,
			List<SuggestionsOutput> input) throws BboxcatException;

}
