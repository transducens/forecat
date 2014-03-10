package org.forecat.client.translation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.client.translation.apertium.ApertiumTranslationJso;
import org.forecat.shared.SessionShared;
import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesOutput;
import org.forecat.shared.languages.LanguagesShared.Engine;
import org.forecat.shared.translation.SourceSegment;
import org.forecat.shared.translation.TranslationInput;
import org.forecat.shared.translation.TranslationOutput;
import org.forecat.shared.translation.TranslationShared;

import com.google.gwt.http.client.URL;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;

public class TranslationBrowserSide extends TranslationShared implements IsSerializable,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6713094838797983412L;

	public void translationService(TranslationInput input,
			final AsyncCallback<TranslationOutput> callback, final SessionShared session)
			throws ForecatException {
		int currentId = 1;
		Object aux = session.getAttribute("SuggestionId");
		if (aux != null) {
			currentId = (Integer) aux;
		}

		session.setAttribute("translation", input);

		@SuppressWarnings("unchecked")
		List<LanguagesInput> languagesInput = (List<LanguagesInput>) session
				.getAttribute("engines");
		if (languagesInput == null) {
			throw new ForecatException("engines could not be obtained from session");
		}

		@SuppressWarnings("unchecked")
		List<LanguagesOutput> languagesOutput = (List<LanguagesOutput>) session
				.getAttribute("languages");
		if (languagesOutput == null) {
			throw new ForecatException("languages could not be obtained from session");
		}

		final Map<String, List<SourceSegment>> segmentPairs = new HashMap<String, List<SourceSegment>>();
		final Map<String, Integer> segmentCounts = new HashMap<String, Integer>();

		session.setAttribute("segmentPairs", segmentPairs);
		session.setAttribute("segmentCounts", segmentCounts);

		String[] words = slice(input.getSourceText());
		List<SourceSegment> sourceSegments = sliceIntoSegments(words, input.getMaxSegmentLenth(),
				input.getMinSegmentLenth(), currentId);

		session.setAttribute("SuggestionId", sourceSegments.size() + currentId);

		// Call the first engine:
		translateApertiumAPI(sourceSegments, input.getSourceCode(), input.getTargetCode(),
				segmentPairs, segmentCounts, languagesInput, languagesOutput, words.length,
				callback);
	}

	private void translateApertiumAPI(final List<SourceSegment> sourceSegments,
			final String sourceCode, final String targetCode,
			final Map<String, List<SourceSegment>> segmentPairs,
			final Map<String, Integer> segmentCounts, final List<LanguagesInput> languagesInput,
			final List<LanguagesOutput> languagesOutput, final int totalSegments,
			final AsyncCallback<TranslationOutput> callback) {

		int pos;

		// Use this engine it if was in Languages input and the engine supports the requested
		// language pair:
		if (((pos = LanguagesInput.searchEngine(languagesInput, Engine.APERTIUM.toString())) != -1)
				&& (LanguagesOutput.engineTranslatesLanguagePair(languagesOutput,
						Engine.APERTIUM.toString(), sourceCode, targetCode))) {

			String key = languagesInput.get(pos).getKey();
			// TODO: createQuery is engine dependent!
			String queryString = TranslationBrowserSide.createApertiumQuery(sourceSegments,
					sourceCode, targetCode, key);

			JsonpRequestBuilder jsonp = new JsonpRequestBuilder();

			// TODO: ensure batch API is being used; the output of the Languages service
			// must be stored in the session for getting this
			String url = "http://api.apertium.org/json/translate?" + queryString;

			jsonp.requestObject(url, new AsyncCallback<ApertiumTranslationJso>() {
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				@Override
				public void onSuccess(ApertiumTranslationJso result) {
					for (int i = 0, n = sourceSegments.size(); i < n; ++i) {
						String targetText = result.getTranslatedText(i);
						if (result.getResponseStatus(i) == 200) {
							addSegments(segmentPairs, segmentCounts, targetText,
									Engine.APERTIUM.toString(), sourceSegments.get(i));
						}
					}
					translateGoogleAPI(sourceSegments, sourceCode, targetCode, segmentPairs,
							segmentCounts, languagesInput, languagesOutput, totalSegments, callback);
				}
			});
		} else {
			translateGoogleAPI(sourceSegments, sourceCode, targetCode, segmentPairs, segmentCounts,
					languagesInput, languagesOutput, totalSegments, callback);
		}
	}

	// Each function calls the next one; the final one simply returns
	private void translateGoogleAPI(final List<SourceSegment> sourceSegments,
			final String sourceCode, final String targetCode,
			final Map<String, List<SourceSegment>> segmentPairs,
			final Map<String, Integer> segmentCounts, final List<LanguagesInput> languagesInput,
			final List<LanguagesOutput> languagesOutput, final int totalSegments,
			final AsyncCallback<TranslationOutput> callback) {

		int pos;

		// Use this engine it if was in Languages input and the engine supports the requested
		// language pair:
		if (((pos = LanguagesInput.searchEngine(languagesInput, Engine.GOOGLE.toString())) != -1)
				&& (LanguagesOutput.engineTranslatesLanguagePair(languagesOutput,
						Engine.APERTIUM.toString(), sourceCode, targetCode))) {

			@SuppressWarnings("unused")
			String key = languagesInput.get(pos).getKey();

			for (int i = 0, n = sourceSegments.size(); i < n; ++i) {
				String targetText = "Google API call not implemented";
				addSegments(segmentPairs, segmentCounts, targetText, Engine.GOOGLE.toString(),
						sourceSegments.get(i));
			}
		}
		translateBingAPI(sourceSegments, sourceCode, targetCode, segmentPairs, segmentCounts,
				languagesInput, languagesOutput, totalSegments, callback);

	}

	// Each function calls the next one; the final one simply returns
	private void translateBingAPI(final List<SourceSegment> sourceSegments,
			final String sourceCode, final String targetCode,
			final Map<String, List<SourceSegment>> segmentPairs,
			final Map<String, Integer> segmentCounts, final List<LanguagesInput> languagesInput,
			final List<LanguagesOutput> languagesOutput, final int totalSegments,
			final AsyncCallback<TranslationOutput> callback) {
		//
		// TranslationServerSide tss = new TranslationServerSide();
		//
		// try {
		// tss.translateBingAPI(sourceSegments, sourceCode, targetCode, segmentPairs,
		// segmentCounts, languagesInput, languagesOutput);
		// } catch (ForecatException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		TranslationOutput output = new TranslationOutput(segmentCounts.size(), totalSegments);
		callback.onSuccess(output);
	}

	public static String createApertiumQuery(List<SourceSegment> sourceSegments, String sourceCode,
			String targetCode, String key) {
		String s = "markUnknown=no&langpair=" + sourceCode + "%7C" + targetCode + "&key=";
		Iterator<SourceSegment> it = sourceSegments.iterator();
		// Put text to translate in the end in case the query string is truncated by the browser
		// TODO: make multiple requests to the server
		while (it.hasNext()) {
			s += "&q=" + URL.encode(it.next().getSourceSegmentText());
		}
		return s;
	}
}
