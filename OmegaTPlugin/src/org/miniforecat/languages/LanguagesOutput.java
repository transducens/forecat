package org.miniforecat.languages;

import java.util.List;

public class LanguagesOutput {
	String engine;
	String sourceName;
	String sourceCode;
	String targetName;
	String targetCode;

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
