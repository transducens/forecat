package org.forecat.client.loghelper;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.forecat.client.Forecat;
import org.forecat.client.exceptions.ForecatException;
import org.forecat.client.utils.JavaScriptJavaUtils;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LogHelper {

	final static void log(final StringJso inputJso, final JavaScriptObject successCallback,
			final JavaScriptObject failureCallback, final JavaScriptObject context) {

		StringNoJso text = new StringNoJso(inputJso);

		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

			@Override
			public void onSuccess(Boolean result) {
				JavaScriptJavaUtils.call0_js(successCallback);
			}

			@Override
			public void onFailure(Throwable caught) {
				String details = caught.getMessage();
				if (caught instanceof ForecatException) {
					details = "languagesService: " + details;
				}
				details = "Error: " + details;
				JavaScriptJavaUtils.call1_js(failureCallback, details);

			}
		};

		try {
			Logger log = (Logger) Forecat.browserSession.getAttribute("Logger");
			String header = (String) Forecat.browserSession.getAttribute("LogHeader");
			String lines[] = text.getText().split("\\n");
			for (String line : lines) {
				log.log(Level.INFO, "#FORECAT# " + header + ": " + line);
			}
			callback.onSuccess(true);
		} catch (Exception caught) {
			callback.onFailure(caught);
		}
	}
}
