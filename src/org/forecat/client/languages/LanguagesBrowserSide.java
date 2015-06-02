package org.forecat.client.languages;

import java.util.ArrayList;
import java.util.List;

import org.forecat.client.languages.apertium.ApertiumPairsJso;
import org.forecat.shared.SessionShared;
import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesOutput;
import org.forecat.shared.languages.LanguagesShared;

import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LanguagesBrowserSide extends LanguagesShared {

	public void languagesService(List<LanguagesInput> inputList,
			final AsyncCallback<List<LanguagesOutput>> callback, SessionShared session) {

		session.setAttribute("engines", inputList);

		List<LanguagesOutput> outputList = new ArrayList<LanguagesOutput>();
		session.setAttribute("languages", outputList);

		// Call the first in the chain; it is important to be clear about the order
		obtainLanguagesApertiumAPI(inputList, outputList, callback);
	}

	private void obtainLanguagesApertiumAPI(final List<LanguagesInput> languagesInput,
			final List<LanguagesOutput> outputList,
			final AsyncCallback<List<LanguagesOutput>> callback) {

		int pos;

		if ((pos = LanguagesInput.searchEngine(languagesInput, Engine.APERTIUM.toString())) != -1) {

			String key = languagesInput.get(pos).getKey();

			JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
			String queryString = "key=" + key;
			String url = "http://api.apertium.org/json/listPairs?" + queryString;

			jsonp.requestObject(url, new AsyncCallback<ApertiumPairsJso>() {
				@Override
				public void onFailure(Throwable caught) {
					// TODO: call the next engine or throw the exception?
					callback.onFailure(caught);
				}

				@Override
				public void onSuccess(ApertiumPairsJso result) {
					for (int i = 0, n = result.size(); i < n; ++i) {
						String sourceCode = result.getSourceLanguageCode(i);
						String targetCode = result.getTargetLanguageCode(i);
						// Only add pairs whose both languages are in
						// LanguageAllSides.languageNames:
						if (LanguagesShared.languageNames.containsKey(sourceCode)
								&& LanguagesShared.languageNames.containsKey(targetCode)) {
							String sourceName = LanguagesShared.languageNames.get(sourceCode);
							String targetName = LanguagesShared.languageNames.get(targetCode);
							LanguagesOutput output = new LanguagesOutput(
									Engine.APERTIUM.toString(), sourceName, sourceCode, targetName,
									targetCode);
							outputList.add(output);
						}
					}
					// Call the next engine in the engine:
					obtainLanguageGoogleAPI(languagesInput, outputList, callback);
				}
			});
		} else {
			// Call the next engine in the chain:
			obtainLanguageGoogleAPI(languagesInput, outputList, callback);
		}
	}

	private void obtainLanguageGoogleAPI(List<LanguagesInput> languagesInput,
			List<LanguagesOutput> outputList, AsyncCallback<List<LanguagesOutput>> callback) {

		// int pos;

		if ((/* pos = */LanguagesInput.searchEngine(languagesInput, Engine.GOOGLE.toString())) != -1) {
			// String key = languagesInput.get(pos).getKey();

			outputList.add(new LanguagesOutput(Engine.GOOGLE.toString(), "Google Source", "go-sl",
					"Google Target", "go-tl"));
		}
		// End of chain:
		sortLanguagesList(outputList);
		callback.onSuccess(outputList);

	}

}
