package org.miniforecat.ranker;

import java.util.ArrayList;
import java.util.List;

import org.miniforecat.exceptions.BboxcatException;
import org.miniforecat.suggestions.SuggestionsOutput;

public class RankerPressureHeuristic extends RankerPressureBasic {

	@Override
	public List<SuggestionsOutput> rankerService(RankerInput rankinp, List<SuggestionsOutput> input)
			throws BboxcatException {
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
			Pair<Integer, Integer> originCoord = pair.getKey().getKey();
			Pair<Integer, Integer> targetCoord = pair.getKey().getValue();

			startX = originCoord.getKey();
			endX = targetCoord.getKey();
			startY = originCoord.getValue();
			endY = targetCoord.getValue();
			weight = pair.getValue();

			// System.out.println(weight + " " + startX + " " + endX + " " + startY + " " + endY);
			if (rankinp.getPosition() >= startY && rankinp.getPosition() < endY) {
				for (int x = startX; x < endX && x < pressures.length; x++) {
					pressureLine[x] += weight * (endY - startY);
				}
			} else {
				for (int x = startX; x < endX && x < pressures.length; x++) {
					pressureLine[x] -= weight * (endY - startY);
				}
			}
		}

		for (index = 0; index < input.size(); index++) {
			sortList.add(index);
			so = input.get(index);
			acumPressure = 0.0;

			for (int i = 0; i < so.getNumberWords() && so.getPosition() + i < pressures.length; i++) {
				acumPressure += pressureLine[so.getPosition() + i];
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
