package org.forecat.shared.suggestions;

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

public class SuggestionsTorchShared extends SuggestionsBasic {

	@Override
	public List<SuggestionsOutput> obtainSuggestions(SuggestionsInput input,
			Map<String, List<SourceSegment>> segmentPairs, Map<String, Integer> segmentCounts) {
		// SuggestionsOutput includes a compareTo method.
		SortedSet<SuggestionsOutput> preoutput = new TreeSet<SuggestionsOutput>();
		Iterator<Entry<String, List<SourceSegment>>> it = segmentPairs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<SourceSegment>> e = it.next();
			if (UtilsShared.isPrefix(input.getPrefixText(), e.getKey())
					&& segmentCounts.get(e.getKey()) > 0) {
				for (SourceSegment ss : segmentPairs.get(e.getKey())) {
					if (!ss.isUsed()) {
						if (input.getPrefixStart() >= (ss.getPosition() - frame)
								&& input.getPrefixStart() <= (ss.getPosition() + frame)) {
							// Simple estimation of feasibility: ratio of lengths
							preoutput.add(new SuggestionsOutput(e.getKey(), e.getKey().length(), ss
									.getId() + "." + SubIdProvider.getSubId(e.getKey(), ss), ss
									.getPosition()));
						}
					}
				}
			}
		}

		// Return as a list.
		List<SuggestionsOutput> output = new ArrayList<SuggestionsOutput>(preoutput);

		return output;
	}
}
