package org.forecat.shared.suggestions;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.ranker.RankerInput;
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
public class SuggestionsLMSimpleBernoulli extends SuggestionsShared implements IsSerializable,
		Serializable {

	SuggestionsShared base;
	RankerShared ranker;

	private static final long serialVersionUID = -1080886143337665973L;

	@Override
	public List<SuggestionsOutput> obtainSuggestions(SuggestionsInput input,
			Map<String, List<SourceSegment>> segmentPairs, Map<String, Integer> segmentCounts) {
		RankerInput rankerInput = new RankerInput(input.getPosition());
		List<SuggestionsOutput> output = base.obtainSuggestions(input, segmentPairs, segmentCounts);

		String clippedTargetText = input.getTargetText(), clippedAndSug;
		int j = clippedTargetText.length() - 1;
		int numberSpaces = 0;
		double logNFact = 0;

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
			output = ranker.rankerService(rankerInput, output);
		} catch (ForecatException e) {
			e.printStackTrace();
		}

		for (SuggestionsOutput so : output) {

			numberSpaces = 0;
			logNFact = 0;
			clippedAndSug = clippedTargetText + so.getSuggestionText();
			for (j = clippedAndSug.length() - 1; j > 0; j--) {
				if (clippedAndSug.charAt(j) == ' ') {
					numberSpaces++;
					logNFact += Math.log(numberSpaces);
				}
			}

			so.setSuggestionFeasibility(IRSTLMscorer.getPerplexity(clippedAndSug) + logNFact);
			// System.out.println(">>>" + clippedTargetText + "- " + so.getSuggestionText() + " : "
			// + so.getSuggestionFeasibility() + " "
			// + (so.getSuggestionFeasibility() - logNFact) + " " + logNFact);
		}
		// System.out.println("*");

		return output;
	}

	protected SuggestionsLMSimpleBernoulli() {

	}

	public SuggestionsLMSimpleBernoulli(SuggestionsShared base, RankerShared ranker) {
		super();
		IRSTLMscorer.init();
		this.base = base;
		this.ranker = ranker;
	}

	public SuggestionsLMSimpleBernoulli(SuggestionsShared base) {
		super();
		IRSTLMscorer.init();
		this.base = base;
		this.ranker = new RankerScore();
	}

}
