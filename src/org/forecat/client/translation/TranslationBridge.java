package org.forecat.client.translation;

import org.forecat.client.Forecat;
import org.forecat.client.ForecatService;
import org.forecat.client.ForecatServiceAsync;
import org.forecat.client.exceptions.ForecatException;
import org.forecat.client.utils.JavaScriptJavaUtils;
import org.forecat.client.utils.PropertiesBrowserSide;
import org.forecat.server.utils.PropertiesServer;
import org.forecat.shared.translation.TranslationInput;
import org.forecat.shared.translation.TranslationMixedOutput;
import org.forecat.shared.translation.TranslationOutput;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class TranslationBridge {

	final static void translationService(final TranslationInputJso inputJso,
			final JavaScriptObject successCallback, final JavaScriptObject failureCallback) {

		TranslationInput input = new TranslationInput(inputJso);

		AsyncCallback<TranslationOutput> callback = new AsyncCallback<TranslationOutput>() {
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
			public void onSuccess(TranslationOutput result) {
				TranslationOutputJso outputJso = TranslationOutputJso.create(
						result.getNumberSegments(), result.getMaxNumberSegments());
				JavaScriptJavaUtils.call1_js(successCallback, outputJso);
			}
		};

		AsyncCallback<TranslationMixedOutput> callbackMixed = new AsyncCallback<TranslationMixedOutput>() {
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
			public void onSuccess(TranslationMixedOutput result) {
				Forecat.browserSession.setAttribute("segmentPairs", result.getSegmentPairs());
				Forecat.browserSession.setAttribute("segmentCounts", result.getSegmentCounts());
				TranslationOutputJso outputJso = TranslationOutputJso.create(result
						.getTranslationOutput().getNumberSegments(), result.getTranslationOutput()
						.getMaxNumberSegments());
				JavaScriptJavaUtils.call1_js(successCallback, outputJso);
			}
		};

		if (PropertiesBrowserSide.executionMode == PropertiesBrowserSide.ExecutionModes.BROWSER_MODE) {
			TranslationBrowserSide tr = new TranslationBrowserSide();
			try {
				tr.translationService(input, callback, Forecat.browserSession);
			} catch (ForecatException e) {
				callback.onFailure(e);
			}
		} else if (PropertiesBrowserSide.executionMode == PropertiesBrowserSide.ExecutionModes.MIXED_MODE) {
			Forecat.browserSession.setAttribute("translation", input);
			ForecatServiceAsync forecatService = GWT.create(ForecatService.class);

			if (org.forecat.server.utils.PropertiesServer.useUrl) {
				ServiceDefTarget serviceDefTarget = (ServiceDefTarget) forecatService;
				serviceDefTarget.setServiceEntryPoint(PropertiesServer.serverurl);
			}

			forecatService.translationMixedRPCServer(input, callbackMixed);
		} else if (PropertiesBrowserSide.executionMode == PropertiesBrowserSide.ExecutionModes.SERVER_MODE) {
			// Create a remote service proxy to talk to the server-side service.
			ForecatServiceAsync forecatService = GWT.create(ForecatService.class);

			if (org.forecat.server.utils.PropertiesServer.useUrl) {
				ServiceDefTarget serviceDefTarget = (ServiceDefTarget) forecatService;
				serviceDefTarget.setServiceEntryPoint(PropertiesServer.serverurl);
			}

			forecatService.translationRPCServer(input, callback);
		}

	}
}
