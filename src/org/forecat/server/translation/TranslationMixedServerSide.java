package org.forecat.server.translation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.SessionShared;
import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesOutput;
import org.forecat.shared.translation.SourceSegment;
import org.forecat.shared.translation.TranslationInput;
import org.forecat.shared.translation.TranslationMixedOutput;
import org.forecat.shared.translation.TranslationOutput;

public class TranslationMixedServerSide extends TranslationServerSide {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1066776610320926520L;

	public TranslationMixedOutput translationMixedService(TranslationInput input,
			SessionShared session) throws ForecatException {

		int currentId = 1;
		Object aux = session.getAttribute("SuggestionId");
		if (aux != null) {
			currentId = (Integer) aux;
		}

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

		String[] words = slice(input.getSourceText());
		List<SourceSegment> sourceSegments = sliceIntoSegments(words, input.getMaxSegmentLenth(),
				input.getMinSegmentLenth(), currentId);

		session.setAttribute("SuggestionId", sourceSegments.size() + currentId);

		translateApertiumAPY(sourceSegments, input.getSourceCode(), input.getTargetCode(),
				segmentPairs, segmentCounts, languagesInput, languagesOutput);
		// translateApertiumLocalInstallation(sourceSegments, input.getSourceCode(),
		// input.getTargetCode(), segmentPairs, segmentCounts, languagesInput, languagesOutput);
		translateBingAPI(sourceSegments, input.getSourceCode(), input.getTargetCode(),
				segmentPairs, segmentCounts, languagesInput, languagesOutput);

		TranslationMixedOutput output = new TranslationMixedOutput(segmentPairs, segmentCounts,
				new TranslationOutput(segmentPairs.size(), words.length));
		return output;
	}

}
