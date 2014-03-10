package org.forecat.client.utils;

import com.google.gwt.core.client.JavaScriptObject;

public class JavaScriptJavaUtils {

	private JavaScriptJavaUtils() {
	}

	public static final native void init_js() /*-{
		$wnd.init();
	}-*/;

	public static final native void call_js(String functionName) /*-{
		$wnd[functionName]();
	}-*/;

	public static final native void call0_js(JavaScriptObject f) /*-{
		f();
	}-*/;

	public static final native void call1_js(JavaScriptObject f, JavaScriptObject arg1) /*-{
		f(arg1);
	}-*/;

	public static final native void call1_js(JavaScriptObject f, String s) /*-{
		f(s);
	}-*/;

	public static final native void call2_js(JavaScriptObject f, JavaScriptObject arg1,
			JavaScriptObject arg2) /*-{
		f(arg1, arg2);
	}-*/;

	// Make Java methods accessible from JavaScript:
	public static final native void exposeJavaMethodsToJavaScript() /*-{
		$wnd.languagesService_java = $entry(@org.forecat.client.languages.LanguagesBridge::languagesService(Lcom/google/gwt/core/client/JsArray;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;));
		$wnd.translationService_java = $entry(@org.forecat.client.translation.TranslationBridge::translationService(Lorg/forecat/client/translation/TranslationInputJso;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;));
		$wnd.suggestionsService_java= $entry(@org.forecat.client.suggestions.SuggestionsBridge::suggestionsService(Lorg/forecat/client/suggestions/SuggestionsInputJso;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;));
		$wnd.selectionService_java= $entry(@org.forecat.client.selection.SelectionBridge::selectionService(Lorg/forecat/client/selection/SelectionInputJso;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;));
//		$wnd.rankingService_java= $entry(@org.forecat.client.ranker.RankerBridge::rankerService(Lcom/google/gwt/core/client/JsArray;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;));
		$wnd.logService_java= $entry(@org.forecat.client.loghelper.LogHelper::log(Lorg/forecat/client/loghelper/StringJso;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;));
	}-*/;

}
