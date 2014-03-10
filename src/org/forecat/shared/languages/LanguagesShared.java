package org.forecat.shared.languages;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LanguagesShared {

	public static final Map<String, String> languageNames;

	static {
		Map<String, String> aMap = new HashMap<String, String>();

		aMap.put("ar", "Arabic");
		aMap.put("ast", "Asturian");
		aMap.put("bg", "Bulgarian");
		aMap.put("br", "Breton");
		aMap.put("ca", "Catalan");
		aMap.put("ca_valencia", "Valencian Catalan");
		aMap.put("cs", "Czech");
		aMap.put("cy", "Welsh");
		aMap.put("da", "Danish");
		aMap.put("de", "German");
		aMap.put("nl", "Dutch");
		aMap.put("el", "Greek");
		aMap.put("en", "English");
		aMap.put("en_US", "US English");
		aMap.put("eo", "Esperanto");
		aMap.put("es", "Spanish");
		aMap.put("et", "Estonian");
		aMap.put("eu", "Basque");
		aMap.put("fi", "Finnish");
		aMap.put("fr", "French");
		aMap.put("gl", "Galician");
		aMap.put("he", "Hebrew");
		aMap.put("hi", "Hindi");
		aMap.put("hu", "Hungarian");
		aMap.put("id", "Indonesian");
		aMap.put("is", "Icelandic");
		aMap.put("it", "Italian");
		aMap.put("ja", "Japanese");
		aMap.put("ko", "Korean");
		aMap.put("lv", "Latvian");
		aMap.put("lt", "Lithuanian");
		aMap.put("mk", "Macedonian");
		aMap.put("nb", "Norwegian Bokmål");
		aMap.put("nn", "Norwegian Nynorsk");
		aMap.put("no", "Norwegian");
		aMap.put("oc", "Occitan");
		aMap.put("oc_aran", "Aranese");
		aMap.put("pt", "Portuguese");
		aMap.put("pt_BR", "Brazilian Portuguese");
		aMap.put("pl", "Polish");
		aMap.put("ru", "Russian");
		aMap.put("ro", "Romanian");
		aMap.put("sk", "Slovak");
		aMap.put("sl", "Slovenian");
		aMap.put("sv", "Swedish");
		aMap.put("th", "Thai");
		aMap.put("tr", "Turkish");
		aMap.put("uk", "Ukranian");
		aMap.put("vi", "Vietnamese");
		aMap.put("zh-CHS", "Chinese Simplified");
		aMap.put("zh-CHT", "Chinese Traditional");
		aMap.put("eng", "English");
		aMap.put("kaz", "Kazakh");

		/*
		 * aMapDevelopment should be kept small and used during development to reduce response time
		 * because of a performance issue with GWT:
		 * http://stackoverflow.com/questions/4799501/why-gwt
		 * -devmode-serialization-is-hundred-times-slower
		 */
		Map<String, String> aMapDevelopment = new HashMap<String, String>();

		aMapDevelopment.put("ca", "Catalan");
		aMapDevelopment.put("el", "Greek");
		aMapDevelopment.put("en", "English");
		aMapDevelopment.put("eng", "English");
		aMapDevelopment.put("kaz", "Kazakh");
		aMapDevelopment.put("es", "Spanish");
		aMapDevelopment.put("esc", "Spanish"); // For corpus tests
		aMapDevelopment.put("eu", "Basque");
		aMapDevelopment.put("gl", "Galician");

		languageNames = Collections.unmodifiableMap(aMapDevelopment); // use in development mode
		// languageNames = Collections.unmodifiableMap(aMap); // use in production mode
	}

	public enum Engine {
		APERTIUM("apertium"), GOOGLE("google"), BING("bing"), CACHETRANS("cachetrans"), DICTIONARIUM(
				"dictionarium"), PHRASEUM("phraseum");

		private final String name;

		private Engine(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Checks whether the parameter is a valid engine name.
	 * 
	 * @param engine
	 * @return
	 */
	public static boolean engineNameValid(String engine) {
		for (Engine c : Engine.values()) {
			if (c.toString().equals(engine)) {
				return true;
			}
		}
		return false;
	}

	protected static void sortLanguagesList(List<LanguagesOutput> outputList) {
		Collections.sort(outputList, new Comparator<LanguagesOutput>() {
			@Override
			public int compare(LanguagesOutput o1, LanguagesOutput o2) {
				int i;
				i = o1.getSourceName().compareToIgnoreCase(o2.getSourceName());
				if (i != 0)
					return i;
				i = o1.getTargetName().compareToIgnoreCase(o2.getTargetName());
				if (i != 0)
					return i;
				return o1.getEngine().compareToIgnoreCase(o2.getEngine());
			}
		});
	}

}
