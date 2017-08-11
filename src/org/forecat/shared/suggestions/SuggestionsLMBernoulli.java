package org.forecat.shared.suggestions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.ranker.RankerScore;
import org.forecat.shared.ranker.RankerShared;
import org.forecat.shared.suggestions.LM.IRSTLMscorer;
import org.forecat.shared.translation.SourceSegment;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Each suggestion get their simplified Ngram Bernoulli language model score.
 * 
 * Ogawa, Atsunori, Kazuya Takeda, and Fumitada Itakura.
 * "Balancing acoustic and linguistic probabilities." Acoustics, Speech and Signal Processing, 1998.
 * Proceedings of the 1998 IEEE International Conference on. Vol. 1. IEEE, 1998.
 * 
 * @author Daniel Torregrosa
 * 
 */
public class SuggestionsLMBernoulli extends SuggestionsShared
		implements IsSerializable, Serializable {

	private static final long serialVersionUID = 8853225713906558623L;
	SuggestionsShared base;
	RankerShared ranker;

	public static int ngramOrder = 2;

	private int fact(int x) {
		int result = 1;
		while (x > 1) {
			result *= x;
			x--;
		}
		return result;
	}

	@Override
	public List<SuggestionsOutput> obtainSuggestions(SuggestionsInput input,
			Map<String, List<SourceSegment>> segmentPairs, Map<String, Integer> segmentCounts) {
		List<SuggestionsOutput> output = base.obtainSuggestions(input, segmentPairs, segmentCounts);

		String clippedTargetText = input.getFixedPrefix(), clippedAndSug, clippedAndSugWords[];
		int i, j = clippedTargetText.length() - 1, k, currentCount;
		int numberSpaces = 0;
		double logNFact = 0;
		int denom = 0;

		for (j = j > 0 ? j : 0; j > 0; j--) {
			if (clippedTargetText.charAt(j) == ' ') {
				numberSpaces++;

			}
			if (numberSpaces == 3)
				break;
		}

		j = j > 0 ? j + 1 : j;

		clippedTargetText = clippedTargetText.substring(j, clippedTargetText.length());

		try {
			output = ranker.rankerService(input, output);
		} catch (ForecatException e) {
			e.printStackTrace();
		}

		for (SuggestionsOutput so : output) {

			numberSpaces = 0;
			logNFact = 0;
			denom = 1;
			clippedAndSug = clippedTargetText + so.getSuggestionText();

			clippedAndSugWords = clippedAndSug.split(" ");

			logNFact = Math.log(fact(clippedAndSugWords.length));

			// for (i = 0; i < clippedAndSugWords.length - ngramOrder; i++) {
			// currentCount = 0;
			// for (j = 0; j < clippedAndSugWords.length - ngramOrder; j++) {
			// for (k = 0; k < ngramOrder; k++) {
			// if (!clippedAndSugWords[i + k].equals(clippedAndSugWords[j + k])) {
			// break;
			// }
			// }
			// if (k == ngramOrder) {
			// currentCount++;
			// }
			// }
			// denom *= fact(currentCount);
			// }

			HashMap<String, Integer> counts = new HashMap<String, Integer>();
			StringBuilder toAdd = new StringBuilder();
			for (i = 0; i < clippedAndSugWords.length - ngramOrder; i++) {
				toAdd.setLength(0);
				for (k = 0; k < ngramOrder; k++) {
					toAdd.append(clippedAndSugWords[i + k]);
					toAdd.append(" ");
				}
				if (counts.containsKey(toAdd.toString())) {
					counts.put(toAdd.toString(), counts.get(toAdd.toString()) + 1);
				} else {
					counts.put(toAdd.toString(), 1);
				}
			}

			for (Integer values : counts.values()) {
				denom *= values;
			}

			so.setSuggestionFeasibility(
					-(IRSTLMscorer.getPerplexity(clippedAndSug) + logNFact - Math.log(denom)));
			// System.out.println(">>>" + clippedTargetText + "- " + so.getSuggestionText() + " : "
			// + so.getSuggestionFeasibility() + " "
			// + (so.getSuggestionFeasibility() - logNFact + Math.log(denom)) + " " + logNFact
			// + " " + Math.log(denom));
		}

		return output;
	}

	protected SuggestionsLMBernoulli() {

	}

	public SuggestionsLMBernoulli(SuggestionsShared base, RankerShared ranker) {
		super();
		IRSTLMscorer.init();
		this.base = base;
		this.ranker = ranker;
	}

	public SuggestionsLMBernoulli(SuggestionsShared base) {
		super();
		IRSTLMscorer.init();
		this.base = base;
		this.ranker = new RankerScore();
	}

}
