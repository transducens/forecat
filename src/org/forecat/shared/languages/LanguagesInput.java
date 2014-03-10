package org.forecat.shared.languages;

import java.io.Serializable;
import java.util.List;

import org.forecat.client.languages.LanguagesInputJso;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class LanguagesInput implements Serializable, IsSerializable {

	String engine;
	String key;

	/**
	 * From Serializable documentation
	 * (http://docs.oracle.com/javase/1.5.0/docs/api/java/io/Serializable.html): "To allow subtypes
	 * of non-serializable classes to be serialized, the subtype may assume responsibility for
	 * saving and restoring the state of the supertype's public, protected, and (if accessible)
	 * package fields. The subtype may assume this responsibility only if the class it extends has
	 * an accessible no-arg constructor to initialize the class's state. It is an error to declare a
	 * class Serializable if this is not the case. The error will be detected at runtime."
	 */
	protected LanguagesInput() {
	}

	public LanguagesInput(String engine, String key) {
		this.engine = engine;
		this.key = key;
	}

	public LanguagesInput(LanguagesInputJso jso) {
		this.engine = jso.getEngine();
		this.key = jso.getKey();
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
