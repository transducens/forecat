package org.forecat.client.selection;

import com.google.gwt.core.client.JavaScriptObject;

public class SelectionInputJso extends JavaScriptObject {

	protected SelectionInputJso() {
	}

	public final native String getSelectionText() /*-{
		return this.selectionText;
	}-*/;

	public final native int getPosition() /*-{
		return this.position;
	}-*/;

	public final native String getId() /*-{
		return this.id;
	}-*/;

}
