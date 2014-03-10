package org.forecat.client.selection;

import com.google.gwt.core.client.JavaScriptObject;

public class SelectionOutputJso extends JavaScriptObject {

	protected SelectionOutputJso() {
	}

	public static final native SelectionOutputJso create(int numberSegments) /*-{
		return {
			numberSegments : numberSegments
		};
	}-*/;
}
