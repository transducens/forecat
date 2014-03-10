package org.forecat.client.selection;

import org.forecat.client.Forecat;
import org.forecat.client.ForecatService;
import org.forecat.client.ForecatServiceAsync;
import org.forecat.client.exceptions.ForecatException;
import org.forecat.client.utils.JavaScriptJavaUtils;
import org.forecat.client.utils.PropertiesBrowserSide;
import org.forecat.server.utils.PropertiesServer;
import org.forecat.shared.selection.SelectionInput;
import org.forecat.shared.selection.SelectionOutput;
import org.forecat.shared.selection.SelectionShared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class SelectionBridge {

	final static void selectionService(final SelectionInputJso inputJso,
			final JavaScriptObject successCallback, final JavaScriptObject failureCallback) {

		SelectionInput input = new SelectionInput(inputJso);

		AsyncCallback<SelectionOutput> callback = new AsyncCallback<SelectionOutput>() {
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
			public void onSuccess(SelectionOutput result) {
				SelectionOutputJso output = SelectionOutputJso.create(result.getNumberSegments());
				JavaScriptJavaUtils.call1_js(successCallback, output);
			}
		};

		if (PropertiesBrowserSide.executionMode == PropertiesBrowserSide.ExecutionModes.BROWSER_MODE
				|| PropertiesBrowserSide.executionMode == PropertiesBrowserSide.ExecutionModes.MIXED_MODE) {
			SelectionShared se = (SelectionShared) Forecat.browserSession.getAttribute("Selection");

			// new SelectionPreffixBrowserSide();

			try {
				SelectionOutput result = se.selectionService(input, Forecat.browserSession);
				callback.onSuccess(result);
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

			forecatService.selectionRPCServer(input, callback);
		}

	}

}
