package org.forecat.client.suggestions;

import com.google.gwt.core.client.JavaScriptObject;

public class SuggestionsInputJso extends JavaScriptObject {

	protected SuggestionsInputJso() {
	}

	public final native String getTargetText() /*-{
		return this.targetText;
	}-*/;

	public final native int getPrefixStart() /*-{
		return this.prefixStart;
	}-*/;

	public final native String getPrefixText() /*-{
		return this.prefixText;
	}-*/;

	public final native int getPosition() /*-{
		return this.position;
	}-*/;

}
