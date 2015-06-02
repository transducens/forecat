package org.omegat.plugins.autocomplete.forecat;

import java.util.ArrayList;
import java.util.List;

import org.miniforecat.languages.LanguagesInput;
import org.miniforecat.languages.LanguagesOutput;
import org.miniforecat.selection.SelectionInput;
import org.miniforecat.translation.TranslationInput;
import org.miniforecat.translation.TranslationOutput;
import org.miniforecat.translation.cachetrans.Cachetrans;
import org.omegat.core.Core;
import org.omegat.core.machinetranslators.BaseTranslate;
import org.omegat.gui.editor.autocompleter.AutoCompleterItem;
import org.omegat.plugins.autocomplete.forecat.adapter.ForecatMiniInterface;
import org.omegat.plugins.autocomplete.forecat.gui.ForecatAutoCompleteView;
import org.omegat.util.Language;

/**
 * Main class of the OmegaT plugin. 
 * Extends BaseTranslate for creating the translation segments whenever the user proceeds to the next sentence. 
 * @author Daniel Torregrosa
 *
 */
public class ForecatPTS extends BaseTranslate {

	private ForecatMiniInterface iface;
	private static ForecatPTS self;

	public static void useSuggestion(AutoCompleterItem item) {
		if (item != null)
			self.iface.select(new SelectionInput(item.extras[0], 0));
	}

	/**
	 * Initialize and add the plugin
	 */
	public ForecatPTS() {
		self = this;
		iface = new ForecatMiniInterface();
		addForecatView();

		ArrayList<LanguagesInput> inputLanguagesList = new ArrayList<LanguagesInput>();
		List<LanguagesOutput> outputLanguagesList = null;
		LanguagesInput languagesInput = new LanguagesInput("cachetrans", "");
		Cachetrans.setConfigFile("resources/en-es.ini");
		Cachetrans.setUseApertium(true);
		inputLanguagesList.add(languagesInput);
		outputLanguagesList = iface.getLanguages(inputLanguagesList);

		StringBuilder sb = new StringBuilder();
		for (LanguagesOutput s : outputLanguagesList) {
			sb.append(s.getSourceName());
			sb.append(" to ");
			sb.append(s.getTargetName());
			sb.append("; ");
		}
		System.out.println("Languages: " + sb.toString());
	}

	@Override
	protected String getPreferenceName() {
		return "Forecat_pts";
	}

	public String getName() {
		return "forecat_pts";
	}

	@Override
	protected String translate(Language sLang, Language tLang, String text) {
		TranslationInput inputTranslation = new TranslationInput(text, "en",
				"es", 4, 1);
		TranslationOutput outputTranslation = null;
		outputTranslation = ForecatMiniInterface.getForecatInterface()
				.translate(inputTranslation);
//		Core.getEditor().getAutoCompleter().updatePopup(true);

		return ("Number of segments: " + outputTranslation.getNumberSegments() + "\n");

	}

	public static void addForecatView() {
		Core.getEditor().getAutoCompleter().addView(new ForecatAutoCompleteView());
	}
}
