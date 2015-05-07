package org.omegat.plugins.autocomplete.forecat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.EditorTextArea3;
import org.omegat.gui.editor.autocompleter.AbstractAutoCompleterView;
import org.omegat.gui.editor.autocompleter.AutoCompleter;
import org.omegat.gui.editor.autocompleter.AutoCompleterItem;
import org.omegat.plugins.autocomplete.forecat.adapter.ForecatMiniInterface;
import org.omegat.plugins.autocomplete.forecat.gui.ForecatAutoCompleteView;
import org.omegat.plugins.autocomplete.forecat.gui.ForecatAutocomplete;
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
	private static Method setVisibleMethod = null;
	private static AutoCompleter ac = null;

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
		try {
			setVisibleMethod.invoke(ac, true);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return ("Number of segments: " + outputTranslation.getNumberSegments() + "\n");

	}

	@SuppressWarnings("unchecked")
	public static void addForecatView() {
		Field textEditorField = null, editorField = null, autoCompleterField = null, viewsField = null, currentViewField = null, autoCompleterViewField = null;
		Method activateViewMethod = null;
		EditorController edc = null;
		EditorTextArea3 eda = null;
		ArrayList<AbstractAutoCompleterView> views = null;

		try {
			editorField = Core.class.getDeclaredField("editor");
			textEditorField = EditorController.class.getDeclaredField("editor");
			autoCompleterField = EditorTextArea3.class
					.getDeclaredField("autoCompleter");
			viewsField = AutoCompleter.class.getDeclaredField("views");
			currentViewField = AutoCompleter.class
					.getDeclaredField("currentView");
			activateViewMethod = AutoCompleter.class
					.getDeclaredMethod("activateView");
			autoCompleterViewField = AbstractAutoCompleterView.class
					.getDeclaredField("completer");
			setVisibleMethod = AutoCompleter.class.getDeclaredMethod(
					"setVisible", boolean.class);
		} catch (NoSuchFieldException | SecurityException
				| NoSuchMethodException nsfe) {
			nsfe.printStackTrace(System.err);
			System.exit(-1);
		}

		editorField.setAccessible(true);
		textEditorField.setAccessible(true);
		autoCompleterField.setAccessible(true);
		viewsField.setAccessible(true);
		currentViewField.setAccessible(true);
		activateViewMethod.setAccessible(true);
		autoCompleterViewField.setAccessible(true);
		setVisibleMethod.setAccessible(true);

		try {
			edc = (EditorController) editorField.get(null);
			eda = (EditorTextArea3) textEditorField.get(edc);
			ac = (AutoCompleter) autoCompleterField.get(eda);
			ac = new ForecatAutocomplete(eda, ac);
			autoCompleterField.set(eda, ac);
			views = (ArrayList<AbstractAutoCompleterView>) viewsField.get(ac);
			for (AbstractAutoCompleterView aacv : views) {
				autoCompleterViewField.set(aacv, ac);
			}
			currentViewField.set(ac, 0);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		views.add(0, new ForecatAutoCompleteView(ac));

		try {
			activateViewMethod.invoke(ac);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static EditorTextArea3 getEditorTextArea() {
		EditorController controller = (EditorController) Core.getEditor();

		// Getting the field
		Field editor = null;
		EditorTextArea3 tarea = null;
		try {
			editor = EditorController.class.getDeclaredField("editor");
		} catch (NoSuchFieldException nsfe) {
			nsfe.printStackTrace(System.err);
			System.exit(-1);
		}
		// Setting it accessible
		editor.setAccessible(true);
		try {
			tarea = (EditorTextArea3) editor.get(controller);
		} catch (IllegalAccessException iae) {
			iae.printStackTrace(System.err);
			System.exit(-1);
		}
		// Returning the object
		return tarea;
	}
}
