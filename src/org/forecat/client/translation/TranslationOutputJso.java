package org.forecat.client.translation;

import com.google.gwt.core.client.JavaScriptObject;

public class TranslationOutputJso extends JavaScriptObject {

	protected TranslationOutputJso() {
	}

	public static final native TranslationOutputJso create(int numberSegments, int maxNumberSegments) /*-{
		return {
			numberSegments : numberSegments,
			maxNumberSegments : maxNumberSegments
		};
	}-*/;

}
