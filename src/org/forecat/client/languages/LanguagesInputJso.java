package org.forecat.client.languages;

import com.google.gwt.core.client.JavaScriptObject;

public class LanguagesInputJso extends JavaScriptObject {

	protected LanguagesInputJso() {
	}

	public final native String getEngine() /*-{
		return this.engine;
	}-*/;

	public final native String getKey() /*-{
		return this.key;
	}-*/;

}
