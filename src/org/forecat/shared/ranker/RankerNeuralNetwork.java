package org.forecat.shared.ranker;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.utils.Quicksort;

import com.googlecode.fannj.Fann;

public class RankerNeuralNetwork extends RankerPressureBasic {

	private static final long serialVersionUID = -7251804261090384718L;
	private static Fann ann;
	private static String annFile;

	private static float diffAvg = 0;
	private static float diffDev = 1;
	private static float ratioAvg = 0;
	private static float ratioDev = 1;

	public RankerNeuralNetwork() {
		ann = new Fann(getAnnFile());
	}

	public static void setDiffAvg(float da) {
		diffAvg = da;
	}

	public static void setDiffDev(float dd) {
		diffDev = dd;
	}

	public static void setRatioAvg(float ra) {
		ratioAvg = ra;
	}

	public static void setRatioDev(float rd) {
		ratioDev = rd;
	}

	public void calcPressure(double[] pos, double[] neg, SuggestionsInput rankinp) {
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
					pos[x] += weight * (endY - startY);
				}
			} else {
				for (int x = startX; x < endX && x < pressures.length; x++) {
					neg[x] -= weight * (endY - startY);
				}
			}
		}
	}

	/*
	 * CURRENT HEADER
	 * 
	 * 1@attribute cNumber numeric
	 * 
	 * 2@attribute wNumber numeric
	 * 
	 * 3@attribute normCNumber numeric
	 * 
	 * 4@attribute normWNumber numeric
	 * 
	 * 5@attribute oCNumber numeric
	 * 
	 * 6@attribute owNumber numeric
	 * 
	 * 7@attribute normOrigCNumber numeric
	 * 
	 * 8@attribute normOrigwNumber numeric
	 * 
	 * 9@attribute sugLengthC numeric
	 * 
	 * 10@attribute sugLengthW numeric
	 * 
	 * 11@attribute origWordLengthC numeric
	 * 
	 * 12@attribute origWordLengthW numeric
	 * 
	 * 13@attribute charDiff numeric
	 * 
	 * 14@attribute wordDiff numeric
	 * 
	 * 15@attribute charRatioDiff numeric
	 * 
	 * 16@attribute wordRatioDiff numeric
	 * 
	 * 17@attribute charRatio numeric
	 * 
	 * 18@attribute wordRatio numeric
	 * 
	 * 19@attribute pr numeric
	 * 
	 * 20@attribute pn numeric
	 * 
	 * 21@attribute first_letter=a numeric
	 * 
	 * 22@attribute first_letter=b numeric
	 * 
	 * 23@attribute first_letter=c numeric
	 * 
	 * 24@attribute first_letter=d numeric
	 * 
	 * 25@attribute first_letter=e numeric
	 * 
	 * 26@attribute first_letter=f numeric
	 * 
	 * 27@attribute first_letter=g numeric
	 * 
	 * 28@attribute first_letter=h numeric
	 * 
	 * 29@attribute first_letter=i numeric
	 * 
	 * 30@attribute first_letter=j numeric
	 * 
	 * 31@attribute first_letter=k numeric
	 * 
	 * 32@attribute first_letter=l numeric
	 * 
	 * 33@attribute first_letter=m numeric
	 * 
	 * 34@attribute first_letter=n numeric
	 * 
	 * 35@attribute first_letter=o numeric
	 * 
	 * 36@attribute first_letter=p numeric
	 * 
	 * 37@attribute first_letter=q numeric
	 * 
	 * 38@attribute first_letter=r numeric
	 * 
	 * 39@attribute first_letter=s numeric
	 * 
	 * 40@attribute first_letter=t numeric
	 * 
	 * 41@attribute first_letter=u numeric
	 * 
	 * 42@attribute first_letter=v numeric
	 * 
	 * 43@attribute first_letter=w numeric
	 * 
	 * 44@attribute first_letter=x numeric
	 * 
	 * 45@attribute first_letter=y numeric
	 * 
	 * 46@attribute first_letter=z numeric
	 * 
	 * 47@attribute first_letter=other numeric
	 * 
	 * 48@attribute word_diff_length_class=NL numeric
	 * 
	 * 49@attribute word_diff_length_class=FL numeric
	 * 
	 * 50@attribute word_diff_length_class=NS numeric
	 * 
	 * 51@attribute word_diff_length_class=FS numeric
	 * 
	 * 52@attribute char_diff_length_class=NL numeric
	 * 
	 * 53@attribute char_diff_length_class=FL numeric
	 * 
	 * 54@attribute char_diff_length_class=NS numeric
	 * 
	 * 55@attribute char_diff_length_class=FS numeric
	 * 
	 * 56@attribute word_ratio_length_class=NL numeric
	 * 
	 * 57@attribute word_ratio_length_class=FL numeric
	 * 
	 * 58@attribute word_ratio_length_class=NS numeric
	 * 
	 * 59@attribute word_ratio_length_class=FS numeric
	 * 
	 * 60@attribute char_ratio_length_class=NL numeric
	 * 
	 * 61@attribute char_ratio_length_class=FL numeric
	 * 
	 * 62@attribute char_ratio_length_class=NS numeric
	 * 
	 * 63@attribute char_ratio_length_class=FS numeric
	 * 
	 * 64@attribute distribution_diff_C=H numeric
	 * 
	 * 65@attribute distribution_diff_C=D numeric
	 * 
	 * 66@attribute distribution_diff_C=DD numeric
	 * 
	 * 67@attribute distribution_diff_C=M numeric
	 * 
	 * 68@attribute distribution_ratio_C=H numeric
	 * 
	 * 69@attribute distribution_ratio_C=D numeric
	 * 
	 * 70@attribute distribution_ratio_C=DD numeric
	 * 
	 * 71@attribute distribution_ratio_C=M numeric
	 * 
	 * 72@attribute distribution_diffN numeric
	 * 
	 * 73@attribute distribution_ratioN numeric
	 * 
	 * 74@attribute fromUsed=FROM_TYPING numeric
	 * 
	 * 75@attribute overlapping=B numeric
	 * 
	 * 76@attribute overlapping=JB numeric
	 * 
	 * 77@attribute overlapping=O numeric
	 * 
	 * 78@attribute overlapping=JA numeric
	 * 
	 * 79@attribute overlapping=A numeric
	 * 
	 * 
	 */

	@Override
	public List<SuggestionsOutput> rankerService(SuggestionsInput rankinp,
			List<SuggestionsOutput> input) throws ForecatException {
		ArrayList<SuggestionsOutput> outputSuggestionsList = new ArrayList<SuggestionsOutput>();
		ArrayList<Integer> sortList = new ArrayList<Integer>();
		int index = 0;
		float nnScore;
		float[] features = new float[79];
		double[] posPressure = new double[pressures.length];
		double[] negPressure = new double[pressures.length];

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

		calcPressure(posPressure, negPressure, rankinp);

		for (SuggestionsOutput so : input) {
			sortList.add(index);
			index++;

			fillFeatures(rankinp, features, posPressure, negPressure, so);

			// for (int i = 0; i < features.length; i++) {
			// System.out.println(i + "=" + features[i] + "\t");
			// }
			// System.out.println();

			nnScore = ann.run(features)[0];
			so.setSuggestionFeasibility(nnScore);
			System.out.println(so.getSuggestionText() + " " + nnScore);
		}

		Quicksort q = new Quicksort();
		q.sort(sortList, input);

		for (index = 0; index < maxSuggestions && index < input.size(); index++) {
			outputSuggestionsList.add(input.get(sortList.get(sortList.size() - index - 1)));
		}

		return outputSuggestionsList;
	}

	public void printEvents(SuggestionsInput rankinp, List<SuggestionsOutput> input)
			throws ForecatException {
		float[] features = new float[79];
		double[] posPressure = new double[pressures.length];
		double[] negPressure = new double[pressures.length];

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

		calcPressure(posPressure, negPressure, rankinp);

		for (SuggestionsOutput so : input) {
			fillFeatures(rankinp, features, posPressure, negPressure, so);

			for (int i = 0; i < features.length; i++) {
				System.out.print(features[i] + ",");
			}
			System.out.println();
		}
	}

	private void fillFeatures(SuggestionsInput rankinp, float[] features, double[] posPressure,
			double[] negPressure, SuggestionsOutput so) {
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
		// features[ 18 ] = //Possitive pressure
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

		if (((diffAvg - .5 * diffDev) <= features[13])
				&& (features[13] <= (diffAvg + .5 * diffDev))) {
			features[63] = 1;
		} else if (((diffAvg - 1 * diffDev) <= features[13])
				&& (features[13] <= (diffAvg + 1 * diffDev))) {
			features[64] = 1;
		} else if (((diffAvg - 2 * diffDev) <= features[13])
				&& (features[13] <= (diffAvg + 3 * diffDev))) {
			features[65] = 1;
		} else {
			features[66] = 1;
		}

		// features[ 67 ] = //=== H ratioAvg - 0.5 ratioDev <= f[17] <=
		// ratioAvg + 0.5 ratioDev
		// features[ 68 ] = // D ratioAvg - 1 ratioDev <= f[17] <= ratioAvg
		// + 1 ratioDev
		// features[ 69 ] = // DD ratioAvg - 2 ratioDev <= f[17] <= ratioAvg
		// + 2 ratioDev
		// features[ 70 ] = //=== M else

		features[67] = features[68] = features[69] = features[70] = 0;

		if (((ratioAvg - .5 * ratioDev) <= features[17])
				&& (features[17] <= (ratioAvg + .5 * ratioDev))) {
			features[67] = 1;
		} else if (((ratioAvg - 1 * ratioDev) <= features[17])
				&& (features[17] <= (ratioAvg + 1 * ratioDev))) {
			features[68] = 1;
		} else if (((ratioAvg - 2 * ratioDev) <= features[17])
				&& (features[17] <= (ratioAvg + 3 * ratioDev))) {
			features[69] = 1;
		} else {
			features[70] = 1;
		}

		// features[ 71 ] = (f[13] - diffAvg) / diffDev
		features[71] = (features[13] - diffAvg) / diffDev;
		// features[ 72 ] = (f[17] - ratioAvg) / ratioDev
		features[72] = (features[17] - ratioAvg) / ratioDev;

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

	public static String getAnnFile() {
		return annFile;
	}

	public static void setAnnFile(String annFile) {
		RankerNeuralNetwork.annFile = annFile;
	}

}
