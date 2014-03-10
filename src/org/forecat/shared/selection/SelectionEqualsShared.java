package org.forecat.shared.selection;

public class SelectionEqualsShared extends SelectionTextShared {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5521433718767576548L;

	@Override
	protected boolean match(String a, String b) {
		return a.equals(b);
	}

}
