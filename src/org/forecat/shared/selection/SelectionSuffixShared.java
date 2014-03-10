package org.forecat.shared.selection;

import org.forecat.shared.utils.UtilsShared;

public class SelectionSuffixShared extends SelectionTextShared {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3209760121948806078L;

	@Override
	protected boolean match(String a, String b) {

		return (UtilsShared.isSuffix(a, b));

	}

}
