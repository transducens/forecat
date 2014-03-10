package org.forecat.shared.translation;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class TranslationMixedOutput implements Serializable, IsSerializable {

	protected Map<String, List<SourceSegment>> segmentPairs;
	protected Map<String, Integer> segmentCounts;
	protected TranslationOutput translationOutput;

	/**
	 * From Serializable documentation
	 * (http://docs.oracle.com/javase/1.5.0/docs/api/java/io/Serializable.html): "To allow subtypes
	 * of non-serializable classes to be serialized, the subtype may assume responsibility for
	 * saving and restoring the state of the supertype's public, protected, and (if accessible)
	 * package fields. The subtype may assume this responsibility only if the class it extends has
	 * an accessible no-arg constructor to initialize the class's state. It is an error to declare a
	 * class Serializable if this is not the case. The error will be detected at runtime."
	 */
	protected TranslationMixedOutput() {
	}

	public TranslationMixedOutput(Map<String, List<SourceSegment>> segmentPairs,
			Map<String, Integer> segmentCounts, TranslationOutput translationOutput) {
		this.segmentPairs = segmentPairs;
		this.segmentCounts = segmentCounts;
		this.translationOutput = translationOutput;
	}

	public Map<String, List<SourceSegment>> getSegmentPairs() {
		return segmentPairs;
	}

	public void setSegmentPairs(Map<String, List<SourceSegment>> segmentPairs) {
		this.segmentPairs = segmentPairs;
	}

	public Map<String, Integer> getSegmentCounts() {
		return segmentCounts;
	}

	public void setSegmentCounts(Map<String, Integer> segmentCounts) {
		this.segmentCounts = segmentCounts;
	}

	public TranslationOutput getTranslationOutput() {
		return translationOutput;
	}

	public void setTranslationOutput(TranslationOutput translationOutput) {
		this.translationOutput = translationOutput;
	}

}
