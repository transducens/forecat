package org.miniforecat.selection;

import java.util.List;
import java.util.Map;

import org.miniforecat.exceptions.BboxcatException;
import org.miniforecat.SessionShared;
import org.miniforecat.selection.SelectionInput;
import org.miniforecat.selection.SelectionOutput;
import org.miniforecat.translation.SourceSegment;

public abstract class SelectionShared {

	public static void useFirstUnusedSegment(List<SourceSegment> origins) {
		for (int i = 0, n = origins.size(); i < n; ++i) {
			if (!origins.get(i).isUsed()) {
				origins.get(i).setUsed(true);
				break;
			}
		}
	}

	public abstract SelectionOutput useSegments(SelectionInput input,
			Map<String, List<SourceSegment>> segmentPairs, Map<String, Integer> segmentCounts);

	public SelectionOutput selectionService(SelectionInput input, SessionShared session)
			throws BboxcatException {

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

		SelectionOutput output = useSegments(input, segmentPairs, segmentCounts);
		return output;
	}

	public SelectionShared() {
		super();
	}

}