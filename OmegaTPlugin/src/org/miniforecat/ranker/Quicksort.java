package org.miniforecat.ranker;

import java.util.List;

import org.miniforecat.suggestions.SuggestionsOutput;

public class Quicksort {
	List<Integer> indices;
	List<SuggestionsOutput> sug;
	private int number;

	public Quicksort() {
	};

	public void sort(List<Integer> indices, List<SuggestionsOutput> sug) {
		// Check for empty or null array

		if (indices.size() == 0)
			return;

		this.indices = indices;
		this.sug = sug;
		number = indices.size();
		quicksort(0, number - 1);
	}

	private void quicksort(int low, int high) {
		int i = low, j = high;
		// Get the pivot element from the middle of the list
		double pivot = sug.get(indices.get(low + (high - low) / 2)).getSuggestionFeasibility();

		// Divide into two lists
		while (i <= j) {
			// If the current value from the left list is smaller then the pivot
			// element then get the next element from the left list
			while (sug.get(indices.get(i)).getSuggestionFeasibility() < pivot) {
				i++;
			}
			// If the current value from the right list is larger then the pivot
			// element then get the next element from the right list
			while (sug.get(indices.get(j)).getSuggestionFeasibility() > pivot) {
				j--;
			}

			// If we have found a values in the left list which is larger then
			// the pivot element and if we have found a value in the right list
			// which is smaller then the pivot element then we exchange the
			// values.
			// As we are done we can increase i and j
			if (i <= j) {
				exchange(i, j);
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j)
			quicksort(low, j);
		if (i < high)
			quicksort(i, high);
	}

	private void exchange(int i, int j) {
		int temp = indices.get(i);
		indices.set(i, indices.get(j));
		indices.set(j, temp);
	}
}
