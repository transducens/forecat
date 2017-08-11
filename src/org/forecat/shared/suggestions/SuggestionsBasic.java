package org.forecat.shared.suggestions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.forecat.shared.translation.SourceSegment;
import org.forecat.shared.utils.SubIdProvider;
import org.forecat.shared.utils.UtilsShared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SuggestionsBasic extends SuggestionsShared implements IsSerializable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1267521779646327709L;

	@Override
	public List<SuggestionsOutput> obtainSuggestions(SuggestionsInput input,
			Map<String, List<SourceSegment>> segmentPairs, Map<String, Integer> segmentCounts) {

		// SuggestionsOutput includes a compareTo method.
		SortedSet<SuggestionsOutput> preoutput = new TreeSet<SuggestionsOutput>();
		int closerPositionWords, closerDifference, closerPositionChars, i;

		Iterator<Entry<String, List<SourceSegment>>> it = segmentPairs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<SourceSegment>> e = it.next();

			// Search for the closest source segment that could have generated this suggestion
			closerPositionWords = -1;
			closerDifference = 0;
			closerPositionChars = 0;
			String originText = "";
			String closerId = "";
			for (SourceSegment ss : segmentPairs.get(e.getKey())) {
				if (closerPositionWords == -1) {
					closerPositionWords = ss.getPosition();
					closerDifference = Math.abs(ss.getPosition() - input.getPosition());
					closerId = ss.getId() + "." + SubIdProvider.getSubId(e.getKey(), ss);
					originText = ss.getSourceSegmentText();
				} else if (Math.abs(ss.getPosition() - input.getPosition()) < closerDifference) {
					closerPositionWords = ss.getPosition();
					closerDifference = Math.abs(ss.getPosition() - input.getPosition());
					closerId = ss.getId() + "." + SubIdProvider.getSubId(e.getKey(), ss);
					originText = ss.getSourceSegmentText();
				}
			}

			i = 0;
			for (String s : input.getSourceText().split(" ")) {
				if (i == closerPositionWords)
					break;
				i++;
				closerPositionChars += s.length() + 1; // Word + a space
			}
			if (closerPositionChars > 0)
				closerPositionChars--; // Discount the last space

			if (UtilsShared.isPrefix(input.getLastWordPrefix(), e.getKey())
					&& segmentCounts.get(e.getKey()) > 0) {
				// Simple estimation of feasibility: ratio of lengths
				preoutput.add(new SuggestionsOutput(e.getKey(), originText, e.getKey().length(),
						closerId, closerPositionWords, closerPositionChars));
			}
		}

		// Return as a list.
		List<SuggestionsOutput> output = new ArrayList<SuggestionsOutput>(preoutput);

		return output;
	}

	// This code is left just to show how to use different regular expression libraries in
	// client/server sides:
	/*
	 * protected abstract boolean isPrefix(String possiblePrefix, String string);
	 */
}
