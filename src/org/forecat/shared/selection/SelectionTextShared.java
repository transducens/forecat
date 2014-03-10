package org.forecat.shared.selection;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.forecat.shared.translation.SourceSegment;

public abstract class SelectionTextShared extends SelectionShared {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6215706244537848796L;

	@Override
	public SelectionOutput useSegments(SelectionInput input,
			Map<String, List<SourceSegment>> segmentPairs, Map<String, Integer> segmentCounts) {

		Iterator<Entry<String, List<SourceSegment>>> it = segmentPairs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<SourceSegment>> e = it.next();

			if (match(input.getSelectionText(), e.getKey()) && segmentCounts.get(e.getKey()) > 0) {
				useFirstUnusedSegment(e.getValue());
				segmentCounts.put(e.getKey(), segmentCounts.get(e.getKey()) - 1);
			} else if (match(e.getKey(), input.getSelectionText())
					&& segmentCounts.get(e.getKey()) > 0) {
				useFirstUnusedSegment(e.getValue());
				segmentCounts.put(e.getKey(), segmentCounts.get(e.getKey()) - 1);
			}
		}

		int liveSegments = 0;
		for (Map.Entry<String, Integer> entry : segmentCounts.entrySet()) {
			if (entry.getValue() > 0) {
				++liveSegments;
			}
		}
		SelectionOutput output = new SelectionOutput(liveSegments);
		return output;
	}

	protected abstract boolean match(String a, String b);

	// This code is left just to show how to use different regular expression libraries in
	// client/server sides:
	/*
	 * protected abstract boolean isPrefix(String possiblePrefix, String string);
	 */
}
