package org.miniforecat.selection;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.miniforecat.translation.SourceSegment;

public class SelectionPositionShared extends SelectionShared {

	@Override
	public SelectionOutput useSegments(SelectionInput input,
			Map<String, List<SourceSegment>> segmentPairs, Map<String, Integer> segmentCounts) {

		boolean unique = true;
		Iterator<Entry<String, List<SourceSegment>>> it = segmentPairs.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, List<SourceSegment>> e = it.next();

			if (input.getSelectionText().equals(e.getKey())) {
				if (e.getValue().size() > 1) {
					unique = false;
					break;
				}
			}
		}

		if (unique) {
			it = segmentPairs.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry<String, List<SourceSegment>> e = it.next();

				for (SourceSegment ss : e.getValue()) {
					if (input.getPosition() == ss.getPosition()) {
						ss.setUsed(true);
						segmentCounts.put(e.getKey(), segmentCounts.get(e.getKey()) - 1);
					}
				}
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
}
