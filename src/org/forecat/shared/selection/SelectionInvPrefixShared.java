package org.forecat.shared.selection;

import org.forecat.shared.utils.UtilsShared;

public class SelectionInvPrefixShared extends SelectionTextShared {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1478609136296971391L;

	@Override
	protected boolean match(String a, String b) {
		return UtilsShared.isPrefix(b, a);
	}

}
