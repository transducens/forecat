package org.forecat.shared.ranker;

import java.io.Serializable;
import java.util.List;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.suggestions.SuggestionsOutput;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class RankerShared implements IsSerializable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7693233597371066385L;
	protected static int maxSuggestions = Integer.MAX_VALUE;

	public static void setMaxSuggestions(int value) {
		maxSuggestions = value;
	}

	public abstract List<SuggestionsOutput> rankerService(RankerInput rankinp,
			List<SuggestionsOutput> input) throws ForecatException;

}
