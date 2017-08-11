package org.forecat.client.translation.apertium;

import com.google.gwt.core.client.JavaScriptObject;

public class ApertiumAPYTranslationJso extends JavaScriptObject {

	protected ApertiumAPYTranslationJso() {
	};

	public final native String getTranslatedText() /*-{
		return this.responseData.translatedText;
	}-*/;

	public final native int getResponseStatus() /*-{
		return this.responseStatus;
	}-*/;

}
