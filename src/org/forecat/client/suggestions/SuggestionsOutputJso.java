package org.forecat.client.suggestions;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class SuggestionsOutputJso extends JavaScriptObject {

	protected SuggestionsOutputJso() {
	}

	public static final native SuggestionsOutputJso create(String suggestionText,
			double suggestionFeasibility, String id, int position) /*-{
		return {
			suggestionText : suggestionText,
			suggestionFeasibility : suggestionFeasibility,
			position : position,
			id : id
		};
	}-*/;

	public static native JsArray<SuggestionsOutputJso> createJavaScriptArray() /*-{
		return [];
	}-*/;

}
