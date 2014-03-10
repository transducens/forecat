package org.forecat.shared.selection;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.SessionShared;
import org.forecat.shared.translation.SourceSegment;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class SelectionShared implements IsSerializable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8541102368988858230L;

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
			throws ForecatException {

		@SuppressWarnings("unchecked")
		Map<String, List<SourceSegment>> segmentPairs = (Map<String, List<SourceSegment>>) session
				.getAttribute("segmentPairs");
		if (segmentPairs == null) {
			// TODO: throw exceptions in the browser?
			throw new ForecatException("segmentPairs could not be obtained from session");
		}

		@SuppressWarnings("unchecked")
		Map<String, Integer> segmentCounts = (Map<String, Integer>) session
				.getAttribute("segmentCounts");
		if (segmentCounts == null) {
			throw new ForecatException("segmentCounts could not be obtained from session");
		}

		SelectionOutput output = useSegments(input, segmentPairs, segmentCounts);
		return output;
	}

	public SelectionShared() {
		super();
	}

}