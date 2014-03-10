package org.forecat.client.languages.apertium;

import com.google.gwt.core.client.JavaScriptObject;

public class ApertiumPairsJso extends JavaScriptObject {
	protected ApertiumPairsJso() {
	}

	public final native String getSourceLanguageCode(int index) /*-{
		return this.responseData[index].sourceLanguage;
	}-*/;

	public final native String getTargetLanguageCode(int index) /*-{
		return this.responseData[index].targetLanguage;
	}-*/;

	public final native int size() /*-{
		return this.responseData.length;
	}-*/;

}
