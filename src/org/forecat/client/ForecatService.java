package org.forecat.client;

import java.util.List;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesOutput;
import org.forecat.shared.selection.SelectionInput;
import org.forecat.shared.selection.SelectionOutput;
import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.translation.TranslationInput;
import org.forecat.shared.translation.TranslationMixedOutput;
import org.forecat.shared.translation.TranslationOutput;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("forecat")
public interface ForecatService extends RemoteService {
	// TODO: Do these methods really throw these exceptions?
	List<LanguagesOutput> languagesRPCServer(List<LanguagesInput> input) throws ForecatException;

	TranslationOutput translationRPCServer(TranslationInput input) throws ForecatException;

	List<SuggestionsOutput> suggestionsRPCServer(SuggestionsInput input) throws ForecatException;

	SelectionOutput selectionRPCServer(SelectionInput input) throws ForecatException;

	TranslationMixedOutput translationMixedRPCServer(TranslationInput input)
			throws ForecatException;

}
