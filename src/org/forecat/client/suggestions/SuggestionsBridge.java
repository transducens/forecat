package org.forecat.client.suggestions;

import java.util.List;

import org.forecat.client.Forecat;
import org.forecat.client.ForecatService;
import org.forecat.client.ForecatServiceAsync;
import org.forecat.client.exceptions.ForecatException;
import org.forecat.client.utils.JavaScriptJavaUtils;
import org.forecat.client.utils.PropertiesBrowserSide;
import org.forecat.server.utils.PropertiesServer;
import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.suggestions.SuggestionsShared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class SuggestionsBridge {

	final static void suggestionsService(final SuggestionsInputJso inputJso,
			final JavaScriptObject successCallback, final JavaScriptObject failureCallback,
			final JavaScriptObject context) {

		SuggestionsInput input = new SuggestionsInput(inputJso);

		AsyncCallback<List<SuggestionsOutput>> callback = new AsyncCallback<List<SuggestionsOutput>>() {
			@Override
			public void onFailure(Throwable caught) {
				String details = caught.getMessage();
				if (caught instanceof ForecatException) {
					details = "languagesService: " + details;
				}
				details = "Error: " + details;
				JavaScriptJavaUtils.call1_js(failureCallback, details);
			}

			@Override
			public void onSuccess(List<SuggestionsOutput> result) {
				JsArray<SuggestionsOutputJso> outputJsArray = SuggestionsOutputJso
						.createJavaScriptArray();
				for (int i = 0, n = result.size(); i < n; ++i) {
					SuggestionsOutput output = result.get(i);
					outputJsArray.set(i,
							SuggestionsOutputJso.create(output.getSuggestionText(),
									output.getSuggestionFeasibility(), output.getId(),
									output.getWordPosition()));
				}
				JavaScriptJavaUtils.call2_js(successCallback, outputJsArray, context);
			}
		};

		if (PropertiesBrowserSide.executionMode == PropertiesBrowserSide.ExecutionModes.BROWSER_MODE
				|| PropertiesBrowserSide.executionMode == PropertiesBrowserSide.ExecutionModes.MIXED_MODE) {
			SuggestionsShared su = (SuggestionsShared) Forecat.browserSession
					.getAttribute("Suggestions");
			// new SuggestionsBrowserSide();
			try {
				List<SuggestionsOutput> out = su.suggestionsService(input, Forecat.browserSession);

				callback.onSuccess(out);
			} catch (ForecatException e) {
				callback.onFailure(e);
			}
		} else if (PropertiesBrowserSide.executionMode == PropertiesBrowserSide.ExecutionModes.SERVER_MODE) {
			// Create a remote service proxy to talk to the server-side service.
			ForecatServiceAsync forecatService = GWT.create(ForecatService.class);

			if (org.forecat.server.utils.PropertiesServer.useUrl) {
				ServiceDefTarget serviceDefTarget = (ServiceDefTarget) forecatService;
				serviceDefTarget.setServiceEntryPoint(PropertiesServer.serverurl);
			}

			forecatService.suggestionsRPCServer(input, callback);
		}

	}
}
