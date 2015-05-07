package org.omegat.plugins.autocomplete.forecat.adapter;

import java.util.ArrayList;
import java.util.List;

import org.miniforecat.exceptions.BboxcatException;
import org.miniforecat.SessionBrowserSideConsole;
import org.miniforecat.SessionShared;
import org.miniforecat.languages.LanguagesInput;
import org.miniforecat.languages.LanguagesOutput;
import org.miniforecat.selection.SelectionInput;
import org.miniforecat.selection.SelectionOutput;
import org.miniforecat.suggestions.SuggestionsInput;
import org.miniforecat.suggestions.SuggestionsOutput;
import org.miniforecat.suggestions.SuggestionsShared;
import org.miniforecat.translation.TranslationInput;
import org.miniforecat.translation.TranslationOutput;
import org.miniforecat.languages.LanguagesServerSide;
import org.miniforecat.selection.SelectionShared;
import org.miniforecat.selection.SelectionEqualsShared;
import org.miniforecat.suggestions.SuggestionsBasic;
import org.miniforecat.translation.TranslationServerSide;

/**
 * Interface with the simplified implementation of Forecat
 * @author Daniel Torregrosa
 *
 */
public class ForecatMiniInterface {

	private LanguagesServerSide languages;
	private TranslationServerSide translation;
	private SuggestionsShared suggestions;
	private SelectionShared selection;
	private SessionShared session;
	protected static ForecatMiniInterface iface;

	public static ForecatMiniInterface getForecatInterface() {
		return iface;
	}
	
	public ForecatMiniInterface() {
		languages = new LanguagesServerSide();
		session = new SessionBrowserSideConsole();
		suggestions = new SuggestionsBasic();
		translation = new TranslationServerSide();
		selection = new SelectionEqualsShared();
		iface = this;
	}

	public List<LanguagesOutput> getLanguages(
			ArrayList<LanguagesInput> inputLanguagesList) {
		List<LanguagesOutput> outputLanguagesList = null;
		try {
			outputLanguagesList = languages.languagesService(
					inputLanguagesList, session);
		} catch (BboxcatException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
		return outputLanguagesList;
	}

	public TranslationOutput translate(TranslationInput inputTranslation) {
		TranslationOutput outputTranslation = null;

		try {
			outputTranslation = translation.translationService(
					inputTranslation, session);
		} catch (BboxcatException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
		return outputTranslation;
	}

	public List<SuggestionsOutput> getSuggestions(
			SuggestionsInput inputSuggestions) {
		ArrayList<SuggestionsOutput> outputSuggestionsList = null;
		try {

			outputSuggestionsList = (ArrayList<SuggestionsOutput>) suggestions
					.suggestionsService(inputSuggestions, session);
		} catch (BboxcatException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return outputSuggestionsList;
	}

	public SelectionOutput select(SelectionInput inputSelection) {
		SelectionOutput outputSelection = null;
		try {
			outputSelection = selection.selectionService(inputSelection,
					session);
		} catch (BboxcatException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
		return outputSelection;
	}
}
