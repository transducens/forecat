package org.forecat.client;

import java.util.List;

import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesOutput;
import org.forecat.shared.selection.SelectionInput;
import org.forecat.shared.selection.SelectionOutput;
import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.translation.TranslationInput;
import org.forecat.shared.translation.TranslationMixedOutput;
import org.forecat.shared.translation.TranslationOutput;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface ForecatServiceAsync {
	void languagesRPCServer(List<LanguagesInput> inputList,
			AsyncCallback<List<LanguagesOutput>> callback);

	void translationRPCServer(TranslationInput inputList, AsyncCallback<TranslationOutput> callback);

	void suggestionsRPCServer(SuggestionsInput input,
			AsyncCallback<List<SuggestionsOutput>> callback);

	void selectionRPCServer(SelectionInput input, AsyncCallback<SelectionOutput> callback);

	void translationMixedRPCServer(TranslationInput inputList,
			AsyncCallback<TranslationMixedOutput> callback);

	// void rankRPCServer(RankerInput rankInp, List<SuggestionsOutput> input,
	// AsyncCallback<List<SuggestionsOutput>> callback);
}
