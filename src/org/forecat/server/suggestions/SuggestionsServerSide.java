package org.forecat.server.suggestions;

import java.util.List;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.SessionShared;
import org.forecat.shared.suggestions.SuggestionsBasic;
import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;

public class SuggestionsServerSide extends SuggestionsBasic {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2225923621505421822L;

	@Override
	public List<SuggestionsOutput> suggestionsService(SuggestionsInput input, SessionShared session)
			throws ForecatException {

		List<SuggestionsOutput> output = super.suggestionsService(input, session);
		return output;
	}

	// This code is left just to show how to use different regular expression libraries in
	// client/server sides:
	/*
	 * @Override protected boolean isPrefix(String possiblePrefix, String string) { return
	 * UtilsServerSide.isPrefix(possiblePrefix,string); }
	 */
}
