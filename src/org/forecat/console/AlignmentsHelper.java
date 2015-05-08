package org.forecat.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.util.Pair;
import org.forecat.shared.ranker.RankerPressureBasic;
import org.forecat.shared.translation.SourceSegment;

public class AlignmentsHelper {

	/**
	 * Computes the alignments as described in Miquel Esplà-Gomis, Felipe Sánchez-Martínez, Mikel L
	 * Forcada. A Simple Approach to Use Bilingual Information Sources for Word Alignment. En
	 * Procesamiento del Lenguaje Natural, 49 (XXVIII Conferència de la Sociedad Española de
	 * Procesamiento del Lenguaje Natural, 5-7.9.2012, Castelló de la Plana), p. 93–100.
	 * 
	 * @param target
	 *            Target sentence
	 * @param sourceWords
	 *            Number of words at source
	 * @param targetSplit
	 *            Target split at word level
	 * @param segmentPairs
	 *            Suggestions
	 * @param currentChar
	 *            Position of the character being typed
	 */
	public static void computeAlignments(String target, int sourceWords, String[] targetSplit,
			Map<String, List<SourceSegment>> segmentPairs, int currentChar) {
		int targetWords = targetSplit.length;
		double[][] presures = new double[sourceWords][targetWords];
		ArrayList<Pair<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, Double>> alignments = new ArrayList<Pair<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, Double>>();
		String[] sugSplit;
		String[] sugSourceSplit;
		String[] typedSplit;
		String lastWordPrefix = "";
		int startingSugPos;
		int tsug;
		int tw;
		int twaux;
		boolean completeWord = false;

		typedSplit = target.substring(0, currentChar).split(" ");

		tw = typedSplit.length;

		completeWord = (currentChar == target.length() || target.charAt(currentChar) == ' ');
		lastWordPrefix = typedSplit[tw - 1];

		for (int xx = 0; xx < sourceWords; xx++) {
			for (int yy = 0; yy < targetWords; yy++) {
				presures[xx][yy] = 0;
			}
		}
		for (Entry<String, List<SourceSegment>> entry : segmentPairs.entrySet()) {

			sugSplit = entry.getKey().split(" ");
			for (twaux = 0; twaux < tw; twaux++) {

				// Comparar palabra a palabra la sugerencia con lo que se pretende escribir
				int sugSplitLength = sugSplit.length;
				for (tsug = 0; tsug < sugSplitLength && tsug + twaux < tw; tsug++) {
					if (!sugSplit[tsug].equals(targetSplit[twaux + tsug])) {
						break;
					}
				}
				if (!completeWord && tsug + twaux == tw && tsug < sugSplitLength) {
					// Comprobar si la sugerencia puede encajar al final de lo tecleado
					if (sugSplit[tsug].startsWith(lastWordPrefix)
							&& (sugSplitLength + twaux <= targetWords))
						tsug = sugSplitLength;
				}
				if (tsug == sugSplitLength) {

					for (SourceSegment ss : entry.getValue()) {
						sugSourceSplit = ss.getSourceSegmentText().split(" ");
						startingSugPos = ss.getPosition();

						int sugSourceSplitLength = sugSourceSplit.length;

						alignments
								.add(new Pair<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, Double>(
										new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(
												new Pair<Integer, Integer>(startingSugPos, twaux),
												new Pair<Integer, Integer>(sugSourceSplitLength
														+ startingSugPos, sugSplitLength + twaux)),
										(1) / ((double) sugSplitLength * sugSourceSplitLength * entry
												.getValue().size())));

						for (int yaux = twaux; yaux < sugSplitLength + twaux; yaux++) {
							for (int xaux = startingSugPos; xaux < sugSourceSplitLength
									+ startingSugPos; xaux++) {
								presures[xaux][yaux] += (1) / ((double) sugSplitLength
										* sugSourceSplitLength * entry.getValue().size());
							}

						}
					}
				}
			}
		}

		RankerPressureBasic.setPressures(presures);
		RankerPressureBasic.setAlignments(alignments);
	}

	/**
	 * Smooths the pressures using pagerank
	 * 
	 * @param target
	 *            Target sentence
	 * @param sourceWords
	 *            Number of words at source
	 * @param targetWords
	 *            Number of words at target
	 */
	public static void smoothPressures(String target, int sourceWords, int targetWords) {
		double[][] presures = RankerPressureBasic.getPressures();
		double[][] newPresures = new double[sourceWords][targetWords];
		for (int it = 0; it < 20; it++) {

			// if (it % 1 == 0 && Main.outputGrow) {
			// TestOutput.addOutput("\n#$--- :" + numsentence + ":" + target);
			// for (int xx = 0; xx < sourceWords; xx++) {
			// TestOutput.addOutput("\n#$-- :" + numsentence + ":" + it + ":\t");
			// for (int yy = 0; yy < targetWords; yy++) {
			// TestOutput.addOutput(presures[xx][yy] + "\t");
			// }
			// }
			// TestOutput.addOutput("\n");
			// }

			for (int xx = 0; xx < sourceWords; xx++) {
				for (int yy = 0; yy < targetWords; yy++) {
					newPresures[xx][yy] = Main.dampenFactor;
					for (int xxx = -1; xxx <= 1; xxx++) {
						if (xx + xxx < 0 || xx + xxx >= sourceWords)
							continue;
						for (int yyy = -1; yyy <= 1; yyy++) {
							if (yy + yyy < 0 || yy + yyy >= targetWords)
								continue;
							if (xxx == 0 && yyy == 0)
								continue;
							if (xx + xxx == 0 || xx + xxx == sourceWords - 1) {
								if (yy + yyy == 0 || yy + yyy == targetWords - 1) {
									newPresures[xx][yy] += presures[xx + xxx][yy + yyy]
											* Main.dampenFactorInv / 3;
								} else {
									newPresures[xx][yy] += presures[xx + xxx][yy + yyy]
											* Main.dampenFactorInv / 5;
								}
							} else {
								if (yy + yyy == 0 || yy + yyy == targetWords - 1) {
									newPresures[xx][yy] += presures[xx + xxx][yy + yyy]
											* Main.dampenFactorInv / 5;
								} else {
									newPresures[xx][yy] += presures[xx + xxx][yy + yyy]
											* Main.dampenFactorInv / 9;
								}
							}
						}
					}
				}
			}

			presures = newPresures;
			newPresures = new double[sourceWords][targetWords];
		}
	}

}
