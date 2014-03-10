package org.forecat.shared.selection;

import org.forecat.shared.utils.UtilsShared;

public class SelectionPrefixShared extends SelectionTextShared {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9089528113544331622L;

	@Override
	protected boolean match(String a, String b) {

		return UtilsShared.isPrefix(a, b);
	}

}
