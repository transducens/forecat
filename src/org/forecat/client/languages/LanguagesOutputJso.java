package org.forecat.client.languages;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class LanguagesOutputJso extends JavaScriptObject {

	protected LanguagesOutputJso() {
	}

	public static native LanguagesOutputJso create(String engine, String sourceName,
			String sourceCode, String targetName, String targetCode) /*-{
		return {
			engine : engine,
			sourceName : sourceName,
			sourceCode : sourceCode,
			targetName : targetName,
			targetCode : targetCode
		};
	}-*/;

	public static native JsArray<LanguagesOutputJso> createJavaScriptArray() /*-{
		return [];
	}-*/;

}
