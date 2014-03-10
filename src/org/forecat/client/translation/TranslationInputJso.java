package org.forecat.client.translation;

import com.google.gwt.core.client.JavaScriptObject;

public class TranslationInputJso extends JavaScriptObject {

	protected TranslationInputJso() {
	}

	public final native String getSourceText() /*-{
		return this.sourceText;
	}-*/;

	public final native String getSourceCode() /*-{
		return this.sourceCode;
	}-*/;

	public final native String getTargetCode() /*-{
		return this.targetCode;
	}-*/;

	public final native int getMaxSegmentLength() /*-{
		return this.maxSegmentLength;
	}-*/;

	public final native int getMinSegmentLength() /*-{
		return this.minSegmentLength;
	}-*/;

}
