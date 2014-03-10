package org.forecat.shared.selection;

import org.forecat.shared.utils.UtilsShared;

public class SelectionContainsShared extends SelectionTextShared {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9152374031088737162L;

	@Override
	protected boolean match(String a, String b) {

		return b.contains(" " + a + " ") || UtilsShared.isPrefix(a, b)
				|| UtilsShared.isSuffix(a, b);
	}

}
