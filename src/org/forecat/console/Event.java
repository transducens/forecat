package org.forecat.console;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.util.Pair;
import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.translation.SourceSegment;

public class Event {

	static int nextid = 0;
	int id;
	String sugUsed = "";
	String idused = "";
	int sentence;
	int curr;
	int word;
	char key;
	ArrayList<String> sug;
	ArrayList<String> potSug;
	ArrayList<SuggestionsOutput> sugOutputs;

	double precision;
	double recall;
	public SuggestionsInput rankinp;
	public SuggestionsOutput usedSuggestion;

	public static String target;
	public static String source;

	private double[][] pressures;
	private ArrayList<Pair<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, Double>> alignments;
	private double[] posPressure, negPressure;

	private static SummaryStatistics absoluteDiff = new SummaryStatistics(),
			relativeDiff = new SummaryStatistics();
	private static int numEvents = 0;

	public double getPrecision() {
		return precision;
	}

	public char getChar() {
		return key;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public boolean hasSuggestions() {
		return !sug.isEmpty();
	}

	public boolean usedSuggestion() {
		return !sugUsed.equals("");
	}

	public int getId() {
		return id;
	}

	public int getSentence() {
		return sentence;
	}

	public Event(char k, int s, int curr, int word) {
		key = k;
		id = nextid;
		nextid++;
		sug = new ArrayList<String>();
		potSug = new ArrayList<String>();
		sugOutputs = new ArrayList<SuggestionsOutput>();
		sentence = s;
		this.curr = curr;
		this.word = word;
		recall = 0;
		precision = 0;
	}

	public void addSuggestion(String id, String score, SuggestionsOutput so) {
		sug.add(id + ":" + score);
		sugOutputs.add(so);
	}

	public void addPotentialSuggestion(String id) {
		potSug.add(id);
	}

	public void useSuggestion(String id, String sug, SuggestionsOutput us) {
		sugUsed = sug;
		idused = id;
		usedSuggestion = us;
	}

	@Override
	public String toString() {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		sb.append(sentence);
		sb.append("|");
		sb.append(curr);
		sb.append("|");
		sb.append(word);
		sb.append("|");
		sb.append(key);
		sb.append("|{");
		for (String i : sug) {
			if (!first)
				sb.append(";");
			else
				first = false;
			sb.append(i);
		}
		sb.append("}");
		sb.append("|{");
		first = true;
		for (String i : potSug) {
			if (!first)
				sb.append(";");
			else
				first = false;
			sb.append(i);
		}
		sb.append("}");
		sb.append("|");
		sb.append(precision);
		sb.append("|");
		sb.append(recall);
		if (idused != "") {
			sb.append("|");
			sb.append(idused);
			sb.append("|");
			sb.append(sugUsed);
		}
		return sb.toString();
	}

	public void calcPressure() {

		computeAlignments(target, source.split(" ").length, target.split(" "),
				(Map<String, List<SourceSegment>>) Main.session.getAttribute("segmentPairs"), curr);

		posPressure = new double[pressures.length];
		negPressure = new double[pressures.length];

		for (Pair<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, Double> pair : alignments) {
			Pair<Integer, Integer> topleftCoord = pair.getKey().getKey();
			Pair<Integer, Integer> bottomrightCoord = pair.getKey().getValue();
			Integer startX, startY, endX, endY;
			Double weight;

			startX = topleftCoord.getKey();
			endX = bottomrightCoord.getKey();
			startY = topleftCoord.getValue();
			endY = bottomrightCoord.getValue();
			weight = pair.getValue();

			if (rankinp.getPosition() >= startY && rankinp.getPosition() < endY) {
				for (int x = startX; x < endX && x < pressures.length; x++) {
					posPressure[x] += weight * (endY - startY);
				}
			} else {
				for (int x = startX; x < endX && x < pressures.length; x++) {
					negPressure[x] -= weight * (endY - startY);
				}
			}
		}
	}

	public void computeAlignments(String target, int sourceWords, String[] targetSplit,
			Map<String, List<SourceSegment>> segmentPairs, int currentChar) {
		int targetWords = targetSplit.length;
		pressures = new double[sourceWords][targetWords];
		alignments = new ArrayList<Pair<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, Double>>();
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
				pressures[xx][yy] = 0;
			}
		}
		for (Entry<String, List<SourceSegment>> entry : segmentPairs.entrySet()) {

			sugSplit = entry.getKey().split(" ");
			for (twaux = 0; twaux < tw; twaux++) {

				// Comparar palabra a palabra la sugerencia con lo que se pretende escribir
				int sugSplitLength = sugSplit.length;
				for (tsug = 0; tsug < sugSplitLength && tsug + twaux < tw; tsug++) {
					if (!sugSplit[tsug].equals(targetSplit[twaux + tsug])) {
						// if (print)
						// System.out.println("NO FIT :" + entry.getKey());
						break;
					}
				}
				if (!completeWord && tsug + twaux == tw && tsug < sugSplitLength) {
					// Comprobar si la sugerencia puede encajar al final de lo tecleado
					if (sugSplit[tsug].startsWith(lastWordPrefix)
							&& (sugSplitLength + twaux <= targetWords))
						// if (print)
						// System.out.println("END FIT :" + entry.getKey());
						tsug = sugSplitLength;
				}
				if (tsug == sugSplitLength) {
					// if (print)
					// System.out.println("FIT : " + entry.getKey());
					for (SourceSegment ss : entry.getValue()) {
						sugSourceSplit = ss.getSourceSegmentText().split(" ");
						startingSugPos = ss.getPosition();

						int sugSourceSplitLength = sugSourceSplit.length;

						alignments
								.add(new Pair<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, Double>(
										new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(
												new Pair<Integer, Integer>(startingSugPos, twaux),
												new Pair<Integer, Integer>(
														sugSourceSplitLength + startingSugPos,
														sugSplitLength + twaux)),
										(1) / ((double) sugSplitLength * sugSourceSplitLength
												* entry.getValue().size())));

						for (int yaux = twaux; yaux < sugSplitLength + twaux; yaux++) {
							for (int xaux = startingSugPos; xaux < sugSourceSplitLength
									+ startingSugPos; xaux++) {
								pressures[xaux][yaux] += (1) / ((double) sugSplitLength
										* sugSourceSplitLength * entry.getValue().size());
							}

						}
					}
				}
			}
		}
	}

	public void toWeka() {
		float[] features = new float[79];
		String sugType;
		// Suggestion-independent features

		// Target length in characters
		features[0] = rankinp.getFixedPrefixCharLength();
		// Target length in words
		features[1] = rankinp.getFixedPrefixWordLength();
		// Target length ratio in chars
		features[2] = ((float) rankinp.getFixedPrefixCharLength())
				/ ((float) rankinp.getSourceCharLength());
		// Target length ratio in words
		features[3] = ((float) rankinp.getFixedPrefixWordLength())
				/ ((float) rankinp.getSourceWordLength());

		calcPressure();

		for (SuggestionsOutput so : sugOutputs) {
			fillFeatures(features, so);
			numEvents++;
			try {
				for (int i = 0; i < features.length; i++) {
					Main.featuresTempFile.write(features[i] + ",");
				}

				sugType = "loser";
				if (usedSuggestion() && so.getId() == usedSuggestion.getId()) {
					sugType = "winning";
				} else {
					String soId = so.getId().split("\\.")[0]; // Potential suggestions are segments,
																// that have no subid
					for (String pot : potSug) {
						if (pot.equals(soId)) {
							sugType = "viable";
							break;
						}
					}
				}

				Main.featuresTempFile.write(sugType + "\n");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void fillFeatures(float[] features, SuggestionsOutput so) {
		features[4] = so.getCharPosition();// Suggestion origin in chars
		features[5] = so.getWordPosition();// Suggestion origin in words
		// Suggestion origin ratio in chars
		features[6] = ((float) so.getCharPosition()) / ((float) rankinp.getSourceCharLength());
		// Suggestion origin ratio in words
		features[7] = ((float) so.getWordPosition()) / ((float) rankinp.getSourceWordLength());
		// Suggestion length in chars
		features[8] = so.getSuggestionCharLength();
		// Suggestion length in words
		features[9] = so.getSuggestionWordLength();
		// Length of the source text of the suggestion in chars
		features[10] = so.getOriginalCharLength();
		// Length of the source text of the suggestion in words
		features[11] = so.getOriginalWordLength();
		// Distance source->target in chars
		features[12] = features[0] - features[4];
		// Distance source->target in words
		features[13] = features[1] - features[5];
		// Sourcepos/sourcelen - targetpos/targetlen in chars
		// As we don not know targetlen, we use sourcelen
		features[14] = ((1 + features[0]) / (1 + rankinp.getSourceCharLength()))
				- ((1 + features[4]) / (1 + rankinp.getSourceCharLength()));
		// Sourcepos/sourcelen - targetpos/targetlen in words
		features[15] = ((1 + features[1]) / (1 + rankinp.getSourceWordLength()))
				- ((1 + features[5]) / (1 + rankinp.getSourceCharLength()));
		// Sourcepos/targetpos in chars
		features[16] = (1 + features[0]) / (1 + features[4]);
		// Sourcepos/targetpos in words
		features[17] = (1 + features[1]) / (1 + features[5]);

		// TODO
		// features[ 18 ] = //Positive pressure
		// features[ 19 ] = //Negative pressure
		features[18] = features[19] = 0;

		for (int i = 0; i < so.getSuggestionWordLength()
				&& so.getWordPosition() + i < pressures.length; i++) {
			features[18] += posPressure[so.getWordPosition() + i];
		}

		for (int i = 0; i < so.getSuggestionWordLength()
				&& so.getWordPosition() + i < pressures.length; i++) {
			features[19] += negPressure[so.getWordPosition() + i];
		}

		// features[ 20 ] = // The suggestion starts with a
		// features[ 21 ] = // b
		// features[ 22 ] = // c
		// features[ 23 ] = // d
		// features[ 24 ] = // e
		// features[ 25 ] = // f
		// features[ 26 ] = // g
		// features[ 27 ] = // h
		// features[ 28 ] = // i
		// features[ 29 ] = // j
		// features[ 30 ] = // k
		// features[ 31 ] = // l
		// features[ 32 ] = // m
		// features[ 33 ] = // n
		// features[ 34 ] = // o
		// features[ 35 ] = // p
		// features[ 36 ] = // q
		// features[ 37 ] = // r
		// features[ 38 ] = // s
		// features[ 39 ] = // t
		// features[ 40 ] = // u
		// features[ 41 ] = // v
		// features[ 42 ] = // w
		// features[ 43 ] = // x
		// features[ 44 ] = // y
		// features[ 45 ] = // z
		// features[ 46 ] = // other

		features[20] = features[21] = features[22] = features[23] = features[24] = features[25] = features[26] = features[27] = features[28] = features[29] = features[30] = features[31] = features[32] = features[33] = features[34] = features[35] = features[36] = features[37] = features[38] = features[39] = features[40] = features[41] = features[42] = features[43] = features[44] = features[45] = features[46] = 0;
		char starting = Character.toLowerCase(so.getSuggestionText().charAt(0));
		if (starting >= 'a' && starting <= 'z') {
			features[starting - 'a' + 20] = 1;
		} else {
			features[46] = 1;
		}

		// features[ 47 ] = //=== NL word N = f[13] < 4 | F = f[13] >= 4
		// features[ 48 ] = // FL S = f[9] <= 2 | L = f[9] > 2
		// features[ 49 ] = // NS
		// features[ 50 ] = //=== FS

		features[47] = features[48] = features[49] = features[50] = 0;
		if (features[13] < 4) { // N
			if (features[9] <= 2) { // S
				features[49] = 1;
			} else {
				features[47] = 1; // L
			}
		} else { // F
			if (features[9] <= 2) { // S
				features[50] = 1;
			} else {
				features[48] = 1; // L
			}
		}

		// features[ 51 ] = //=== NL word N = f[12] < 20 | F = f[12] >= 20
		// Assuming 1 word ~= 5 chars
		// features[ 52 ] = // FL S = f[8] <= 10 | L = f[8] > 10
		// features[ 53 ] = // NS
		// features[ 54 ] = //=== FS

		features[51] = features[52] = features[53] = features[54] = 0;
		if (features[12] < 20) { // N
			if (features[8] <= 10) { // S
				features[53] = 1;
			} else {
				features[51] = 1; // L
			}
		} else { // F
			if (features[8] <= 10) { // S
				features[54] = 1;
			} else {
				features[52] = 1; // L
			}
		}

		// features[ 55 ] = //=== NL word N = f[17] < 1.2 | F = f[17] >= 1.2
		// features[ 56 ] = // FL S = f[9] <= 2 | L = f[9] > 2
		// features[ 57 ] = // NS
		// features[ 58 ] = //=== FS

		features[55] = features[56] = features[57] = features[58] = 0;
		if (features[17] < 1.2) { // N
			if (features[9] <= 2) { // S
				features[57] = 1;
			} else {
				features[55] = 1; // L
			}
		} else { // F
			if (features[9] <= 2) { // S
				features[58] = 1;
			} else {
				features[56] = 1; // L
			}
		}

		// features[ 59 ] = //=== NL word N = f[16] < 1.2 | F = f[16] >= 1.2
		// features[ 60 ] = // FL S = f[8] <= 10 | L = f[8] > 10
		// features[ 61 ] = // NS
		// features[ 62 ] = //=== FS

		features[59] = features[60] = features[61] = features[62] = 0;
		if (features[16] < 1.2) { // N
			if (features[8] <= 10) { // S
				features[61] = 1;
			} else {
				features[60] = 1; // L
			}
		} else { // F
			if (features[8] <= 10) { // S
				features[62] = 1;
			} else {
				features[59] = 1; // L
			}
		}

		// features[ 63 ] = //=== H diffAvg - 0.5 diffDev <= f[13] <=
		// diffAvg + 0.5 diffDev
		// features[ 64 ] = // D diffAvg - 1 diffDev <= f[13] <= diffAvg + 1
		// diffDev
		// features[ 65 ] = // DD diffAvg - 2 diffDev <= f[13] <= diffAvg +
		// 2 diffDev
		// features[ 66 ] = //=== M else

		features[63] = features[64] = features[65] = features[66] = 0;

		// Process later in a script; averages and stdevs not available until a full pass is done
		// if (((diffAvg - .5 * diffDev) <= features[13])
		// && (features[13] <= (diffAvg + .5 * diffDev))) {
		// features[63] = 1;
		// } else if (((diffAvg - 1 * diffDev) <= features[13])
		// && (features[13] <= (diffAvg + 1 * diffDev))) {
		// features[64] = 1;
		// } else if (((diffAvg - 2 * diffDev) <= features[13])
		// && (features[13] <= (diffAvg + 3 * diffDev))) {
		// features[65] = 1;
		// } else {
		// features[66] = 1;
		// }

		// features[ 67 ] = //=== H ratioAvg - 0.5 ratioDev <= f[17] <=
		// ratioAvg + 0.5 ratioDev
		// features[ 68 ] = // D ratioAvg - 1 ratioDev <= f[17] <= ratioAvg
		// + 1 ratioDev
		// features[ 69 ] = // DD ratioAvg - 2 ratioDev <= f[17] <= ratioAvg
		// + 2 ratioDev
		// features[ 70 ] = //=== M else

		features[67] = features[68] = features[69] = features[70] = 0;

		// Process later in a script; averages and stdevs not available until a full pass is done
		// if (((ratioAvg - .5 * ratioDev) <= features[17])
		// && (features[17] <= (ratioAvg + .5 * ratioDev))) {
		// features[67] = 1;
		// } else if (((ratioAvg - 1 * ratioDev) <= features[17])
		// && (features[17] <= (ratioAvg + 1 * ratioDev))) {
		// features[68] = 1;
		// } else if (((ratioAvg - 2 * ratioDev) <= features[17])
		// && (features[17] <= (ratioAvg + 3 * ratioDev))) {
		// features[69] = 1;
		// } else {
		// features[70] = 1;
		// }

		// features[ 71 ] = (f[13] - diffAvg) / diffDev
		// features[71] = (features[13] - diffAvg) / diffDev;
		features[71] = features[13];
		absoluteDiff.addValue(features[13]);
		// features[ 72 ] = (f[17] - ratioAvg) / ratioDev
		// features[72] = (features[17] - ratioAvg) / ratioDev;
		features[72] = features[17];
		relativeDiff.addValue(features[17]);

		// features[ 73 ] = FROM USED
		features[73] = rankinp.getFromused() ? 1.0f : 0.0f;

		// features[ 74 ] = B Before the previously used suggestion
		// features[ 75 ] = JB Just before...
		// features[ 76 ] = O Overlapping with...
		// features[ 77 ] = JA Just after ...
		// features[ 78 ] = A After ...

		features[74] = features[75] = features[76] = features[77] = features[78] = 0;
		if (features[5] + features[8] < rankinp.getLastUsedStart()) {
			features[74] = 1;
		} else if (features[5] + features[8] == rankinp.getLastUsedStart()) {
			features[75] = 1;
		} else if (features[5] == (rankinp.getLastUsedEnd() + 1)) {
			features[77] = 1;
		} else if (features[5] > (rankinp.getLastUsedEnd() + 1)) {
			features[78] = 1;
		} else {
			features[76] = 1;
		}
	}

	// Uses the temporary file to create the actual files
	public static void wrapUp() {
		double diffAvg = absoluteDiff.getMean(), diffDev = absoluteDiff.getStandardDeviation(),
				ratioAvg = relativeDiff.getMean(), ratioDev = relativeDiff.getStandardDeviation();

		BufferedReader fr;
		String line;
		String[] features;
		boolean first = false;
		double f13, f17;
		try {
			if (Main.useFannStyle) {
				if (Main.featuresFileWinning != null) {
					Main.featuresFileWinning.write("" + numEvents + " 79 1\n");
				}
				if (Main.featuresFileViable != null) {
					Main.featuresFileViable.write("" + numEvents + " 79 1\n");
				}
			} else {
				if (Main.featuresFileWinning != null) {
					Main.featuresFileWinning.write(WekaHeader);
					Main.featuresFileWinning.write("@attribute winning {W,N} \n");
					Main.featuresFileWinning.write("@data\n\n");
				}
				if (Main.featuresFileViable != null) {
					Main.featuresFileViable.write(WekaHeader);
					Main.featuresFileWinning.write("@attribute viable {V,N} \n");
					Main.featuresFileWinning.write("@data\n\n");
				}
			}

			Main.featuresTempFile.flush();
			fr = new BufferedReader(new FileReader(Main.featuresTempF));
			while ((line = fr.readLine()) != null) {
				features = line.split(",");
				f13 = Double.parseDouble(features[13]);
				f17 = Double.parseDouble(features[17]);
				if (((diffAvg - .5 * diffDev) <= f13) && (f13 <= (diffAvg + .5 * diffDev))) {
					features[63] = "1";
				} else if (((diffAvg - 1 * diffDev) <= f13) && (f13 <= (diffAvg + 1 * diffDev))) {
					features[64] = "1";
				} else if (((diffAvg - 2 * diffDev) <= f13) && (f13 <= (diffAvg + 3 * diffDev))) {
					features[65] = "1";
				} else {
					features[66] = "1";
				}
				if (((ratioAvg - .5 * ratioDev) <= f17) && (f17 <= (ratioAvg + .5 * ratioDev))) {
					features[67] = "1";
				} else if (((ratioAvg - 1 * ratioDev) <= f17)
						&& (f17 <= (ratioAvg + 1 * ratioDev))) {
					features[68] = "1";
				} else if (((ratioAvg - 2 * ratioDev) <= f17)
						&& (f17 <= (ratioAvg + 3 * ratioDev))) {
					features[69] = "1";
				} else {
					features[70] = "1";
				}

				features[71] = "" + ((f13 - diffAvg) / diffDev);
				features[72] = "" + ((f17 - ratioAvg) / ratioDev);

				if (Main.featuresFileWinning != null) {
					first = true;
					for (int i = 0; i < features.length - 1; i++) {
						if (first) {
							Main.featuresFileWinning.write(features[i]);
							first = false;
						} else {
							Main.featuresFileWinning.write("," + features[i]);
						}
					}
					if (Main.useFannStyle) {
						if ("winning".equals(features[79])) {
							Main.featuresFileWinning.write("\n1\n");
						} else {
							Main.featuresFileWinning.write("\n0\n");
						}
					} else {
						if ("winning".equals(features[79])) {
							Main.featuresFileWinning.write(",W\n");
						} else {
							Main.featuresFileWinning.write(",N\n");
						}
					}
				}
				if (Main.featuresFileViable != null) {
					first = true;
					for (int i = 0; i < features.length - 1; i++) {
						if (first) {
							Main.featuresFileViable.write(features[i]);
							first = false;
						} else {
							Main.featuresFileViable.write("," + features[i]);
						}
					}
					if (Main.useFannStyle) {
						if ("winning".equals(features[80]) || "viable".equals(features[80])) {
							Main.featuresFileViable.write("\n1\n");
						} else {
							Main.featuresFileViable.write("\n0\n");
						}
					} else {
						if ("winning".equals(features[80]) || "viable".equals(features[80])) {
							Main.featuresFileViable.write(",V\n");
						} else {
							Main.featuresFileViable.write(",N\n");
						}
					}
				}

			}
			if (Main.featuresFileWinning != null) {
				Main.featuresFileWinning.close();
			}
			if (Main.featuresFileViable != null) {
				Main.featuresFileViable.close();
			}
			Main.featuresTempFile.close();
			Main.featuresTempF.delete();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static final String WekaHeader = "@attribute cNumber numeric\n@attribute wNumber numeric\n@attribute normCNumbernumeric\n@attribute normWNumber numeric\n@attribute oCNumber numeric\n@attribute owNumbernumeric\n@attribute normOrigCNumber numeric\n@attribute normOrigwNumber numeric\n@attributesugLengthC numeric\n@attribute sugLengthW numeric\n@attribute origWordLengthCnumeric\n@attribute origWordLengthW numeric\n@attribute charDiff numeric\n@attribute wordDiffnumeric\n@attribute charRatioDiff numeric\n@attribute wordRatioDiff numeric\n@attributecharRatio numeric\n@attribute wordRatio numeric\n@attribute pr numeric\n@attribute pnnumeric\n@attribute first_letter=a numeric\n@attribute first_letter=b numeric\n@attributefirst_letter=c numeric\n@attribute first_letter=d numeric\n@attribute first_letter=enumeric\n@attribute first_letter=f numeric\n@attribute first_letter=g numeric\n@attributefirst_letter=h numeric\n@attribute first_letter=i numeric\n@attribute first_letter=jnumeric\n@attribute first_letter=k numeric\n@attribute first_letter=l numeric\n@attributefirst_letter=m numeric\n@attribute first_letter=n numeric\n@attribute first_letter=onumeric\n@attribute first_letter=p numeric\n@attribute first_letter=q numeric\n@attributefirst_letter=r numeric\n@attribute first_letter=s numeric\n@attribute first_letter=tnumeric\n@attribute first_letter=u numeric\n@attribute first_letter=v numeric\n@attributefirst_letter=w numeric\n@attribute first_letter=x numeric\n@attribute first_letter=ynumeric\n@attribute first_letter=z numeric\n@attribute first_letter=other numeric\n@attributeword_diff_length_class=NL numeric\n@attribute word_diff_length_class=FL numeric\n@attributeword_diff_length_class=NS numeric\n@attribute word_diff_length_class=FS numeric\n@attributechar_diff_length_class=NL numeric\n@attribute char_diff_length_class=FL numeric\n@attributechar_diff_length_class=NS numeric\n@attribute char_diff_length_class=FS numeric\n@attributeword_ratio_length_class=NL numeric\n@attribute word_ratio_length_class=FL numeric\n@attributeword_ratio_length_class=NS numeric\n@attribute word_ratio_length_class=FS numeric\n@attributechar_ratio_length_class=NL numeric\n@attribute char_ratio_length_class=FL numeric\n@attributechar_ratio_length_class=NS numeric\n@attribute char_ratio_length_class=FS numeric\n@attributedistribution_diff_C=H numeric\n@attribute distribution_diff_C=D numeric\n@attributedistribution_diff_C=DD numeric\n@attribute distribution_diff_C=M numeric\n@attributedistribution_ratio_C=H numeric\n@attribute distribution_ratio_C=D numeric\n@attributedistribution_ratio_C=DD numeric\n@attribute distribution_ratio_C=M numeric\n@attributedistribution_diffN numeric\n@attribute distribution_ratioN numeric\n@attributefromUsed=FROM_TYPING numeric\n@attribute overlapping=B numeric\n@attribute overlapping=JBnumeric\n@attribute overlapping=O numeric\n@attribute overlapping=JA numeric\n@attributeoverlapping=A numeric";
}
