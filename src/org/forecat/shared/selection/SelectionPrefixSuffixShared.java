package org.forecat.shared.selection;

import org.forecat.shared.utils.UtilsShared;

public class SelectionPrefixSuffixShared extends SelectionTextShared {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5737313556815907392L;

	@Override
	protected boolean match(String a, String b) {

		return UtilsShared.isPrefix(a, b) || UtilsShared.isSuffix(a, b);
	}

}
