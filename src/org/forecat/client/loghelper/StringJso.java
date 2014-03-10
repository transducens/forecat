package org.forecat.client.loghelper;

import com.google.gwt.core.client.JavaScriptObject;

public class StringJso extends JavaScriptObject {

	protected StringJso() {
	}

	public final native String getText() /*-{
		return this.text;
	}-*/;
}
