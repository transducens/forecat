package org.forecat.shared.ranker;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.utils.Quicksort;

/**
 * Chooses the suggestions according to the light aligning model described in
 * 
 * Miquel Esplà-Gomis, Felipe Sánchez-Martínez, Mikel L Forcada. A Simple Approach to Use Bilingual
 * Information Sources for Word Alignment. En Procesamiento del Lenguaje Natural, 49 (XXVIII
 * Conferència de la Sociedad Española de Procesamiento del Lenguaje Natural, 5-7.9.2012, Castelló
 * de la Plana), p. 93–100.
 * 
 * with a new heuristic that penalizes (potentially) already aligned suggestions
 * 
 * @author Daniel Torregrosa
 * 
 */
public class RankerPressureNegativeEvidence extends RankerPressureBasic {

	private static final long serialVersionUID = 1882826662861238513L;

	@Override
	public List<SuggestionsOutput> rankerService(SuggestionsInput rankinp,
			List<SuggestionsOutput> input) throws ForecatException {
		ArrayList<SuggestionsOutput> outputSuggestionsList = new ArrayList<SuggestionsOutput>();
		ArrayList<Integer> sortList = new ArrayList<Integer>();
		SuggestionsOutput so;
		double[] pressureLine = new double[pressures.length];
		double acumPressure = 0;
		int index;
		Integer startX, startY, endX, endY;
		Double weight;

		for (int i = 0; i < pressureLine.length; i++) {
			pressureLine[i] = 0;
		}

		for (Pair<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, Double> pair : alignments) {
			Pair<Integer, Integer> topleftCoord = pair.getKey().getKey();
			Pair<Integer, Integer> bottomrightCoord = pair.getKey().getValue();

			startX = topleftCoord.getKey();
			endX = bottomrightCoord.getKey();
			startY = topleftCoord.getValue();
			endY = bottomrightCoord.getValue();
			weight = pair.getValue();

			// System.out.println(weight + " " + startX + " " + endX + " " + startY + " " + endY);
			if (rankinp.getPosition() >= startY && rankinp.getPosition() < endY) {
				// for (int x = startX; x < endX && x < pressures.length; x++) {
				// pressureLine[x] += weight * (endY - startY);
				// }
			} else {
				for (int x = startX; x < endX && x < pressures.length; x++) {
					pressureLine[x] -= weight * (endY - startY);
				}
			}
		}

		// if (alignments.size() > 0) {
		// System.out.println("&&& " + alignments.size());
		// System.out.print("&&&&& ");
		// for (int i = 0; i < pressureLine.length; i++) {
		// System.out.print(" " + pressureLine[i]);
		// }
		// System.out.println();
		// }
		for (index = 0; index < input.size(); index++) {
			sortList.add(index);
			so = input.get(index);
			acumPressure = 0.0;

			for (int i = 0; i < so.getSuggestionWordLength()
					&& so.getWordPosition() + i < pressures.length; i++) {
				acumPressure -= pressureLine[so.getWordPosition() + i];
			}

			so.setSuggestionFeasibility(acumPressure);
		}
		Quicksort q = new Quicksort();
		q.sort(sortList, input);

		for (index = 0; index < maxSuggestions && index < input.size(); index++) {
			outputSuggestionsList.add(input.get(sortList.get(sortList.size() - index - 1)));
		}

		return outputSuggestionsList;
	}
}
