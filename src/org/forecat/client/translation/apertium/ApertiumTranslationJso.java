package org.forecat.client.translation.apertium;

import com.google.gwt.core.client.JavaScriptObject;

public class ApertiumTranslationJso extends JavaScriptObject {

	protected ApertiumTranslationJso() {
	};

	public final native String getTranslatedText(int index) /*-{
		return this.responseData[index].responseData.translatedText;
	}-*/;

	public final native int getResponseStatus(int index) /*-{
		return this.responseData[index].responseStatus;
	}-*/;

	public final native String getResponseStatus() /*-{
		return this.responseStatus
	}-*/;
}
