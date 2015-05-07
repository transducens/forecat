package org.miniforecat.translation;

public class TranslationInput {

	String sourceText;
	String sourceCode;
	String targetCode;
	int maxSegmentLength;
	int minSegmentLength;

	protected TranslationInput() {
	}

	public TranslationInput(String sourceText, String sourceCode, String targetCode,
			int maxSegmentLength, int minSegmentLength) {
		this.sourceText = sourceText;
		this.sourceCode = sourceCode;
		this.targetCode = targetCode;
		this.maxSegmentLength = maxSegmentLength;
		this.minSegmentLength = minSegmentLength;
	}

	public String getSourceText() {
		return sourceText;
	}

	public void setSourceText(String sourceText) {
		this.sourceText = sourceText;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public String getTargetCode() {
		return targetCode;
	}

	public void setTargetCode(String targetCode) {
		this.targetCode = targetCode;
	}

	public int getMaxSegmentLenth() {
		return maxSegmentLength;
	}

	public void setMaxSegmentLength(int maxSegmentLength) {
		this.maxSegmentLength = maxSegmentLength;
	}

	public int getMinSegmentLenth() {
		return minSegmentLength;
	}

	public void setMinSegmentLength(int maxSegmentLength) {
		this.minSegmentLength = maxSegmentLength;
	}
}
