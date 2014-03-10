package org.forecat.client.languages;

import java.util.ArrayList;
import java.util.List;

import org.forecat.client.Forecat;
import org.forecat.client.ForecatService;
import org.forecat.client.ForecatServiceAsync;
import org.forecat.client.exceptions.ForecatException;
import org.forecat.client.utils.JavaScriptJavaUtils;
import org.forecat.client.utils.PropertiesBrowserSide;
import org.forecat.server.utils.PropertiesServer;
import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesOutput;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class LanguagesBridge {

	// Parameters must be final if we want to use them in the callback
	final static void languagesService(final JsArray<LanguagesInputJso> inputJsArray,
			final JavaScriptObject successCallback, final JavaScriptObject failureCallback) {

		List<LanguagesInput> inputList = new ArrayList<LanguagesInput>();

		for (int i = 0, n = inputJsArray.length(); i < n; ++i) {
			inputList.add(new LanguagesInput(inputJsArray.get(i)));
		}

		AsyncCallback<List<LanguagesOutput>> callback = new AsyncCallback<List<LanguagesOutput>>() {
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
			public void onSuccess(List<LanguagesOutput> result) {
				JsArray<LanguagesOutputJso> outputJsArray = LanguagesOutputJso
						.createJavaScriptArray();
				for (int i = 0, n = result.size(); i < n; ++i) {
					LanguagesOutput output = result.get(i);
					outputJsArray.set(i, LanguagesOutputJso.create(output.getEngine(),
							output.getSourceName(), output.getSourceCode(), output.getTargetName(),
							output.getTargetCode()));
				}
				JavaScriptJavaUtils.call1_js(successCallback, outputJsArray);
			}
		};

		if (PropertiesBrowserSide.executionMode == PropertiesBrowserSide.ExecutionModes.BROWSER_MODE) {
			LanguagesBrowserSide la = new LanguagesBrowserSide();
			la.languagesService(inputList, callback, Forecat.browserSession);
		} else if (PropertiesBrowserSide.executionMode == PropertiesBrowserSide.ExecutionModes.MIXED_MODE) {
			Forecat.browserSession.setAttribute("engines", inputList);
			ForecatServiceAsync forecatService = GWT.create(ForecatService.class);

			if (org.forecat.server.utils.PropertiesServer.useUrl) {
				ServiceDefTarget serviceDefTarget = (ServiceDefTarget) forecatService;
				serviceDefTarget.setServiceEntryPoint(PropertiesServer.serverurl);
			}

			forecatService.languagesRPCServer(inputList, callback);
		} else if (PropertiesBrowserSide.executionMode == PropertiesBrowserSide.ExecutionModes.SERVER_MODE) {
			// Create a remote service proxy to talk to the server-side service.
			ForecatServiceAsync forecatService = GWT.create(ForecatService.class);

			if (org.forecat.server.utils.PropertiesServer.useUrl) {
				ServiceDefTarget serviceDefTarget = (ServiceDefTarget) forecatService;
				serviceDefTarget.setServiceEntryPoint(PropertiesServer.serverurl);
			}

			forecatService.languagesRPCServer(inputList, callback);
		}

	}

}
