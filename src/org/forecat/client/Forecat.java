package org.forecat.client;

import java.util.logging.Logger;

import org.forecat.client.utils.JavaScriptJavaUtils;
import org.forecat.shared.SessionBrowserSideConsole;
import org.forecat.shared.ranker.RankerComposite;
import org.forecat.shared.ranker.RankerLongestFirst;
import org.forecat.shared.ranker.RankerLongestShortestFirst;
import org.forecat.shared.ranker.RankerLongestShortestFromPosition;
import org.forecat.shared.ranker.RankerPosition;
import org.forecat.shared.ranker.RankerShared;
import org.forecat.shared.ranker.RankerShortestFirst;
import org.forecat.shared.ranker.RankerShortestLongestFirst;
import org.forecat.shared.selection.SelectionPositionShared;
import org.forecat.shared.selection.SelectionPrefixShared;
import org.forecat.shared.suggestions.SuggestionsBasic;
import org.forecat.shared.suggestions.SuggestionsRanker;
import org.forecat.shared.suggestions.SuggestionsShared;
import org.forecat.shared.suggestions.SuggestionsTorchShared;

import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Forecat implements EntryPoint {

	public static SessionBrowserSideConsole browserSession;

	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad() {

		browserSession = new SessionBrowserSideConsole();
		if (com.google.gwt.user.client.Window.Location.getParameter("sugMethod") != null
				&& com.google.gwt.user.client.Window.Location.getParameter("sugMethod").equals(
						"lamp")) {
			SuggestionsTorchShared st = new SuggestionsTorchShared();
			browserSession.setAttribute("Suggestions", st);
			if (com.google.gwt.user.client.Window.Location.getParameter("width") != null) {
				try {
					st.setFrame(Integer.parseInt(com.google.gwt.user.client.Window.Location
							.getParameter("width")));
				} catch (Exception ex) {

				}
			}
		} else {
			browserSession.setAttribute("Suggestions", new SuggestionsBasic());
		}

		String sortMethod;
		if ((sortMethod = com.google.gwt.user.client.Window.Location.getParameter("sortMethod")) != null) {

			// Should implement RankerBrowserSideI
			RankerShared rb = null;

			if (sortMethod.equals("short")) {
				rb = new RankerShortestFirst();
			} else if (sortMethod.equals("long")) {
				rb = new RankerLongestFirst();
			} else if (sortMethod.equals("shortlong")) {
				rb = new RankerShortestLongestFirst();
			} else if (sortMethod.equals("longshort")) {
				rb = new RankerLongestShortestFirst();
			} else if (sortMethod.equals("pc")) {
				rb = new RankerComposite(new RankerPosition(), new RankerLongestShortestFromPosition());
			} else {
				rb = new RankerShortestFirst();
			}

			if (com.google.gwt.user.client.Window.Location.getParameter("maxSuggestions") != null) {
				try {
					RankerShared.setMaxSuggestions(Integer
							.parseInt(com.google.gwt.user.client.Window.Location
									.getParameter("maxSuggestions")));
				} catch (Exception ex) {

				}
			}

			browserSession.setAttribute("Ranker", rb);
			SuggestionsShared ss = (SuggestionsShared) browserSession.getAttribute("Suggestions");
			SuggestionsShared ss2 = new SuggestionsRanker(ss, rb);
			browserSession.setAttribute("Suggestions", ss2);
		}

		else {
			RankerShared rb = null;
			rb = new RankerLongestShortestFirst();
			RankerShared.setMaxSuggestions(4);
			SuggestionsShared ss = (SuggestionsShared) browserSession.getAttribute("Suggestions");
			SuggestionsShared ss2 = new SuggestionsRanker(ss, rb);
			browserSession.setAttribute("Suggestions", ss2);
			browserSession.setAttribute("Ranker", rb);
		}

		String selMethod;
		if ((selMethod = com.google.gwt.user.client.Window.Location.getParameter("selMethod")) != null) {
			if (selMethod.equals("position")) {
				browserSession.setAttribute("Selection", new SelectionPositionShared());
			} else {
				browserSession.setAttribute("Selection", new SelectionPrefixShared());
			}
		} else {
			browserSession.setAttribute("Selection", new SelectionPositionShared());
		}

		Logger log = Logger.getLogger(Forecat.class.getName());
		browserSession.setAttribute("Logger", log);

		String urlId;
		if ((urlId = com.google.gwt.user.client.Window.Location.getParameter("id")) == null) {
			urlId = "-1";
		}

		String line;
		if ((line = com.google.gwt.user.client.Window.Location.getParameter("line")) == null) {
			line = "-1";
		}

		browserSession.setAttribute("LogHeader", urlId + ":" + line);

		// Required: call 'javascriptInit' before returning from onModuleLoad
		javascriptInit();
	}

	// This method must be called in onModuleLoad!
	private void javascriptInit() {
		JavaScriptJavaUtils.exposeJavaMethodsToJavaScript();
		JavaScriptJavaUtils.init_js();
	}
}
