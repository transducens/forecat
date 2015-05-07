package org.miniforecat.selection;

public class SelectionEqualsShared extends SelectionTextShared {

	@Override
	protected boolean match(String a, String b) {
		return a.equals(b);
	}

}
