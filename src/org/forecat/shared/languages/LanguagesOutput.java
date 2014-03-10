package org.forecat.shared.languages;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class LanguagesOutput implements Serializable, IsSerializable {

	String engine;
	String sourceName;
	String sourceCode;
	String targetName;
	String targetCode;

	/**
	 * From Serializable documentation
	 * (http://docs.oracle.com/javase/1.5.0/docs/api/java/io/Serializable.html): "To allow subtypes
	 * of non-serializable classes to be serialized, the subtype may assume responsibility for
	 * saving and restoring the state of the supertype's public, protected, and (if accessible)
	 * package fields. The subtype may assume this responsibility only if the class it extends has
	 * an accessible no-arg constructor to initialize the class's state. It is an error to declare a
	 * class Serializable if this is not the case. The error will be detected at runtime."
	 */
	protected LanguagesOutput() {
	}

	public LanguagesOutput(String engine, String sourceName, String sourceCode, String targetName,
			String targetCode) {
		this.engine = engine;
		this.sourceName = sourceName;
		this.sourceCode = sourceCode;
		this.targetName = targetName;
		this.targetCode = targetCode;
	}

	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public final String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getTargetCode() {
		return targetCode;
	}

	public final void setTargetCode(String targetCode) {
		this.targetCode = targetCode;
	}

	public static boolean engineTranslatesLanguagePair(List<LanguagesOutput> list, String engine,
			String sourceCode, String targetCode) {
		for (LanguagesOutput i : list) {
			if (i.getEngine().equals(engine) && i.getSourceCode().equals(sourceCode)
					&& i.getTargetCode().equals(targetCode)) {
				return true;
			}
		}
		return false;
	}

}
