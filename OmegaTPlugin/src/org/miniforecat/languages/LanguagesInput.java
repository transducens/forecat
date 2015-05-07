package org.miniforecat.languages;

import java.util.List;

public class LanguagesInput {

	String engine;
	String key;

	protected LanguagesInput() {
	}

	public LanguagesInput(String engine, String key) {
		this.engine = engine;
		this.key = key;
	}

	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public static int searchEngine(List<LanguagesInput> languagesInput, String engine) {
		for (int i = 0, n = languagesInput.size(); i < n; ++i) {
			if (languagesInput.get(i).getEngine().equals(engine)) {
				return i;
			}
		}
		return -1;
	};
}
