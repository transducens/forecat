package org.forecat.shared.selection;

import java.util.List;
import java.util.Map;

import org.forecat.shared.translation.SourceSegment;

public class SelectionNoneShared extends SelectionShared {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4140355005655795685L;

	@Override
	public SelectionOutput useSegments(SelectionInput input,
			Map<String, List<SourceSegment>> segmentPairs, Map<String, Integer> segmentCounts) {
		SelectionOutput output = new SelectionOutput(segmentPairs.size());
		return output;
	}
}
