package org.forecat.console;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public class TestOutput {

	private static ArrayList<Integer> keyPress = new ArrayList<Integer>();
	private static ArrayList<Integer> suggestions_offered = new ArrayList<Integer>();
	private static ArrayList<Integer> suggestions_used = new ArrayList<Integer>();
	private static ArrayList<Integer> sentence_length = new ArrayList<Integer>();

	private static HashMap<Integer, HashMap<Integer, Integer>> sourceTargetCorrespondenceSelected = new HashMap<Integer, HashMap<Integer, Integer>>();
	private static int maxSourceSelected = -1, maxTargetSelected = -1;

	private static HashMap<Character, HashMap<Integer, Integer>> sourceTargetCorrespondence = new HashMap<Character, HashMap<Integer, Integer>>();
	private static TreeSet<Character> allchars = new TreeSet<Character>();
	private static int maxPosition = -1;

	private static int allOk = 0;
	private static int allNok = 0;
	private static int allSugOk = 0;
	private static int allSugNok = 0;

	private static int charsRead = 0;
	private static int charsAccepted = 0;
	private static ArrayList<Integer> charsReadBySentence = new ArrayList<Integer>();
	private static ArrayList<Integer> charsAcceptedBySentence = new ArrayList<Integer>();

	private static ArrayList<Double> precs = new ArrayList<Double>();
	private static ArrayList<Double> recs = new ArrayList<Double>();

	private static ArrayList<Integer> coverages = new ArrayList<Integer>();
	private static ArrayList<Integer> numsuggestions = new ArrayList<Integer>();
	private static ArrayList<ArrayList<Integer>> coveragesByLength = new ArrayList<ArrayList<Integer>>();
	private static ArrayList<ArrayList<Integer>> numSuggestionsByLength = new ArrayList<ArrayList<Integer>>();

	private static FileWriter out = null;

	private static ArrayList<Integer> suggestionPosition = new ArrayList<Integer>();
	private static ArrayList<Event> events = new ArrayList<Event>();

	private static int shownPopupsForASR = 0;
	private static int usedSuggestionsForASR = 0;

	public static void addCharsRead(int i) {
		charsRead += i;
		charsReadBySentence.add(i);
	}

	public static void addCharsAccepted(int i) {
		charsAccepted += i;
		charsAcceptedBySentence.add(i);
	}

	public static void setOut(FileWriter o) {
		out = o;
	}

	public static void addAllOk(int i) {
		allOk += i;
	}

	public static void addAllNok(int i) {
		allNok += i;
	}

	public static void addAllSugOk(int i) {
		allSugOk += i;
	}

	public static void addAllSugNok(int i) {
		allSugNok += i;
	}

	public static void addPrecision(double i) {
		precs.add(i);
	}

	public static void addRecall(double i) {
		recs.add(i);
	}

	public static void addSuggestionPosition(int pos) {
		suggestionPosition.add(pos);
	}

	public static void addCoverages(int coverage, int numsuggestion, Integer[] coverageByLenght,
			Integer[] numSuggestionByLength) {
		coverages.add(coverage);
		numsuggestions.add(numsuggestion);

		ArrayList<Integer> aux = new ArrayList<Integer>();
		for (int i = 0; i < coverageByLenght.length; i++) {
			aux.add(coverageByLenght[i]);
		}

		coveragesByLength.add(aux);

		aux = new ArrayList<Integer>();
		for (int i = 0; i < numSuggestionByLength.length; i++) {
			aux.add(numSuggestionByLength[i]);
		}

		numSuggestionsByLength.add(aux);
	}

	public static void addSentence(int lenght, int key, int offered, int used) {
		sentence_length.add(lenght);
		keyPress.add(key);
		suggestions_offered.add(offered);
		suggestions_used.add(used);
	}

	public static void addEvent(Event event) {
		events.add(event);
	}

	public static void addMatch(int source, int target) {

		if (!sourceTargetCorrespondenceSelected.containsKey(source)) {
			sourceTargetCorrespondenceSelected.put(source, new HashMap<Integer, Integer>());
			maxSourceSelected = Math.max(source, maxSourceSelected);
		}
		if (!sourceTargetCorrespondenceSelected.get(source).containsKey(target)) {
			sourceTargetCorrespondenceSelected.get(source).put(target, 1);
			maxTargetSelected = Math.max(target, maxTargetSelected);
		} else {
			sourceTargetCorrespondenceSelected.get(source).put(target,
					sourceTargetCorrespondenceSelected.get(source).get(target) + 1);
			System.out.println(" -> " + sourceTargetCorrespondenceSelected.get(source).get(target));
		}
	}

	public static void addOutput(String s) {
		try {
			out.write(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void addCorrespondence(char letter, int position) {
		if (!sourceTargetCorrespondence.containsKey(letter)) {
			sourceTargetCorrespondence.put(letter, new HashMap<Integer, Integer>());
			allchars.add(letter);
		}
		if (!sourceTargetCorrespondence.get(letter).containsKey(position)) {
			sourceTargetCorrespondence.get(letter).put(position, 1);
			maxPosition = Math.max(maxPosition, position);
		} else {
			sourceTargetCorrespondence.get(letter).put(position,
					sourceTargetCorrespondence.get(letter).get(position) + 1);
			System.out.println(" -> " + sourceTargetCorrespondence.get(letter).get(position));
		}
	}

	public static void getPlot() {

		try {
			out.write("# 1:Longitud\n");
			out.write("# 2:Pulsaciones\n");
			out.write("# 3:Sugerencias ofrecidas\n");
			out.write("# 4:Sugerencias usadas\n");

			for (int i = 0; i < keyPress.size(); i++) {
				out.write("" + sentence_length.get(i));
				out.write("\t");
				out.write("" + keyPress.get(i));
				out.write("\t");
				out.write("" + suggestions_offered.get(i));
				out.write("\t");
				out.write("" + suggestions_used.get(i));
				out.write("\n");
			}

			out.write("## Correlacion 1:2 -> ");
			out.write(((Double) getPearsonCorrelation(sentence_length, keyPress)).toString());
			out.write("\n## Correlacion 1:4 -> ");
			out.write(
					((Double) getPearsonCorrelation(sentence_length, suggestions_used)).toString());
			out.write("\n");

			SummaryStatistics ss = new SummaryStatistics();
			TDistribution td = new TDistribution(Math.max(2, keyPress.size() - 1));

			int press = 0;
			int length = 0;

			for (int i = 0; i < keyPress.size(); i++) {
				ss.addValue(((double) keyPress.get(i)) / ((double) sentence_length.get(i)));
				press += keyPress.get(i);
				length += sentence_length.get(i);
				out.write("\n##= ");
				out.write("" + keyPress.get(i));
				out.write(":");
				out.write("" + sentence_length.get(i));
			}

			out.write("\n## Mejora media | ");
			out.write(((Double) ((double) press / (double) length)).toString());
			out.write("\n## Press / length | ");
			out.write("" + press);
			out.write(" ");
			out.write("" + length);
			out.write(" ");
			out.write("\n## Mejora por frase | ");
			out.write(((Double) ss.getMean()).toString());
			out.write(" |  ");
			out.write(((Double) (ss.getMean() * td.inverseCumulativeProbability(0.975D)
					* ss.getStandardDeviation() / Math.sqrt(keyPress.size()))).toString());
			out.write("\n");

			System.out.println("MEJORA MEDIA: " + ((double) press / (double) length) + " " + press
					+ " " + length);

			ss.clear();
			for (int i = 0; i < suggestions_offered.size(); i++) {
				ss.addValue(suggestions_offered.get(i));
			}

			out.write("## Media sugerencias ofrecidas | ");
			out.write(((Double) ss.getMean()).toString());
			out.write(" |  ");
			out.write(((Double) (ss.getMean() * td.inverseCumulativeProbability(0.975D)
					* ss.getStandardDeviation() / Math.sqrt(suggestions_offered.size())))
							.toString());
			out.write("\n");

			ss.clear();
			for (int i = 0; i < keyPress.size(); i++) {
				ss.addValue(suggestions_used.get(i));
			}

			out.write("## Media sugerencias usadas | ");
			out.write(((Double) ss.getMean()).toString());
			out.write(" |  ");
			out.write(((Double) (ss.getMean() * td.inverseCumulativeProbability(0.975D)
					* ss.getStandardDeviation() / Math.sqrt(keyPress.size()))).toString());
			out.write("\n");

			ss.clear();
			for (int i = 0; i < suggestions_used.size(); i++) {
				if (suggestions_offered.get(i) == 0) {
					ss.addValue(0);
				} else {
					ss.addValue(((double) suggestions_used.get(i))
							/ ((double) suggestions_offered.get(i) + 1));
				}
			}

			out.write("## Sugerencias usadas / Sugerencias ofrecidas | ");
			out.write(((Double) ss.getMean()).toString());
			out.write(" |  ");
			out.write(((Double) (ss.getMean() * td.inverseCumulativeProbability(0.975D)
					* ss.getStandardDeviation() / Math.sqrt(suggestions_used.size()))).toString());
			out.write("\n");

			{
				int totalUsed = 0;
				int totalOffered = 0;
				for (int i = 0; i < suggestions_used.size(); i++) {

					totalUsed += suggestions_used.get(i);
					totalOffered += suggestions_offered.get(i);

				}

				out.write("## ASR COBERTURA | ");

				out.write(((Double) (((double) totalUsed) / ((double) totalOffered))).toString());
				out.write("\n");

			}

			out.write("## ASR | ");
			out.write(((double) usedSuggestionsForASR / (double) shownPopupsForASR) + "\n");

			ss.clear();
			for (int i = 0; i < precs.size(); i++) {
				ss.addValue((precs.get(i)));
			}

			out.write("## Precision | ");
			out.write(((Double) ss.getMean()).toString());
			out.write(" |  ");
			out.write(((Double) (ss.getMean() * td.inverseCumulativeProbability(0.975D)
					* ss.getStandardDeviation() / Math.sqrt(precs.size()))).toString());
			out.write("\n");

			ss.clear();
			for (int i = 0; i < recs.size(); i++) {
				ss.addValue((recs.get(i)));
			}

			out.write("## Cobertura | ");
			out.write(((Double) ss.getMean()).toString());
			out.write(" |  ");
			out.write(((Double) (ss.getMean() * td.inverseCumulativeProbability(0.975D)
					* ss.getStandardDeviation() / Math.sqrt(recs.size()))).toString());
			out.write("\n");

			ss.clear();
			for (int i = 0; i < suggestionPosition.size(); i++) {
				ss.addValue((suggestionPosition.get(i)));
			}

			out.write("## Suggestion Position | ");
			out.write(((Double) ss.getMean()).toString());
			out.write(" |  ");
			out.write(((Double) (ss.getMean() * td.inverseCumulativeProbability(0.975D)
					* ss.getStandardDeviation() / Math.sqrt(recs.size()))).toString());
			out.write("\n");

			double precision = (double) allSugOk / (double) (allSugOk + allSugNok);
			double recall = (double) allSugOk / (double) allOk;

			out.write("## Precision2 | ");
			out.write(precision + "\n");
			out.write("## Cobertura2 | ");
			out.write(recall + "\n");

			// ss.clear();
			// int sentence = 1;
			// int used = 0;
			// int withSug = 0;
			//
			// for (Event e : events) {
			// if (e.hasSuggestions())
			// withSug++;
			// if (e.usedSuggestion())
			// used++;
			// if (e.getSentence() != sentence) {
			// sentence = e.getSentence();
			// if (withSug == 0) {
			// ss.addValue(0);
			// } else {
			// ss.addValue((used) / ((double) withSug + 1));
			// }
			// used = 0;
			// withSug = 0;
			// }
			// }
			//
			// out.write("## ASGPorFrase | ");
			// out.write(((Double) ss.getMean()).toString());
			// out.write(" | ");
			// out.write(((Double) (ss.getMean() * td.inverseCumulativeProbability(0.975D)
			// * ss.getStandardDeviation() / Math.sqrt(sentence - 1))).toString());
			// out.write("\n");

			out.write("#C# |all| ");
			for (int i = 0; i < coverages.size(); i++) {
				out.write(((double) coverages.get(i)) / ((double) numsuggestions.get(i)) + " ");
			}
			out.write("\n");
			for (int j = 0; j < coveragesByLength.get(0).size(); j++) {
				out.write("#C# |" + j + "| ");
				for (int i = 0; i < coveragesByLength.size(); i++) {
					out.write(((double) coveragesByLength.get(i).get(j))
							/ ((double) numSuggestionsByLength.get(i).get(j)) + " ");
				}
				out.write("\n");
			}
			out.write("\n");

			ss.clear();
			out.write("### - \t");
			for (int i = 0; i < maxTargetSelected; i++) {
				out.write(i + "\t");
			}

			out.write("\n");
			ArrayList<Integer> spairs = new ArrayList<Integer>(), tpairs = new ArrayList<Integer>();
			SimpleRegression sr = new SimpleRegression();

			StringBuilder pairs = new StringBuilder();
			HashMap<Integer, Integer> row;
			for (int i = 0; i < maxSourceSelected; i++) {
				out.write("### " + i + "\t");
				if (sourceTargetCorrespondenceSelected.containsKey(i)) {
					row = sourceTargetCorrespondenceSelected.get(i);
					for (int j = 0; j < maxTargetSelected; j++) {
						if (row.containsKey(j)) {
							out.write(row.get(j) + "\t");
							for (int k = 0; k < row.get(j); k++) {
								spairs.add(i);
								tpairs.add(j);
								sr.addData(i, j);
								pairs.append("#P# " + i + " " + j + "\n");
							}
						} else {
							out.write("-\t");
						}
					}
				} else {
					for (int j = 0; j < maxTargetSelected; j++) {
						out.write("-\t");
					}
				}
				out.write("\n");
			}

			out.write(pairs.toString());

			out.write("\n## Regresión posición origen/destino | " + sr.getIntercept() + " | "
					+ sr.getInterceptStdErr() + "|" + sr.getSlope() + " | " + sr.getSlopeStdErr());

			out.write("\n## Correlación posición origen/destino | "
					+ getPearsonCorrelation(spairs, tpairs) + "\n\n");

			out.write("#### - \t");
			for (int i = 0; i < maxPosition; i++) {
				out.write(i + "\t");
			}

			out.write("\n");

			out.write("## Read/accepted ratio ");

			out.write(((Double) (((double) charsAccepted) / charsRead)).toString());
			out.write("\n");

			out.write("## Read chars ");
			out.write(((Integer) charsRead).toString());
			out.write("\n");

			int numSentence = 1;

			for (Integer i : charsReadBySentence) {
				out.write("## Read chars ");
				out.write(((Integer) numSentence).toString());
				out.write(":");
				out.write(i.toString());
				out.write("\n");
				numSentence++;
			}

			out.write("## Accepted chars ");
			out.write((((Integer) charsAccepted).toString()));
			out.write("\n");
			numSentence = 1;

			for (Integer i : charsAcceptedBySentence) {
				out.write("## Accepted chars ");
				out.write(((Integer) numSentence).toString());
				out.write(":");
				out.write(i.toString());
				out.write("\n");
				numSentence++;
			}

			for (char i : allchars) {
				out.write("#### " + i + "\t");
				if (sourceTargetCorrespondence.containsKey(i)) {
					row = sourceTargetCorrespondence.get(i);
					for (int j = 0; j < maxPosition; j++) {
						if (row.containsKey(j)) {
							out.write(row.get(j) + "\t");
						} else {
							out.write("-\t");
						}
					}
				} else {
					for (int j = 0; j < maxPosition; j++) {
						out.write("-\t");
					}
				}
				out.write("\n");
			}

			Event.wrapUp();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static double getPearsonCorrelation(ArrayList<Integer> scores1,
			ArrayList<Integer> scores2) {
		PearsonsCorrelation pc = new PearsonsCorrelation();
		double[] sc1 = new double[scores1.size()];
		double[] sc2 = new double[scores2.size()];

		for (int i = 0; i < scores1.size(); i++)
			sc1[i] = scores1.get(i);

		for (int i = 0; i < scores2.size(); i++)
			sc2[i] = scores2.get(i);

		try {
			return pc.correlation(sc1, sc2);
		} catch (Exception ex) {
			return 0;
		}
	}

	public static void flush() {
		for (Event event : events) {
			if ((Main.featuresFileWinning != null || Main.featuresFileViable != null)
					&& event.rankinp != null) {
				eventToWeka(event);
			}
			if (event.hasSuggestions()) {
				shownPopupsForASR++;
			}
			if (event.usedSuggestion()) {
				usedSuggestionsForASR++;
			}
			try {
				out.write("#%% " + event.toString() + "\n");
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		events.clear();
	}

	// public static void featuresHeader() {
	// try {
	// Main.feauresFile.write(
	// "@attribute cNumber numeric\n@attribute wNumber numeric\n@attribute normCNumber
	// numeric\n@attribute normWNumber numeric\n@attribute oCNumber numeric\n@attribute owNumber
	// numeric\n@attribute normOrigCNumber numeric\n@attribute normOrigwNumber numeric\n@attribute
	// sugLengthC numeric\n@attribute sugLengthW numeric\n@attribute origWordLengthC
	// numeric\n@attribute origWordLengthW numeric\n@attribute charDiff numeric\n@attribute wordDiff
	// numeric\n@attribute charRatioDiff numeric\n@attribute wordRatioDiff numeric\n@attribute
	// charRatio numeric\n@attribute wordRatio numeric\n@attribute pr numeric\n@attribute pn
	// numeric\n@attribute first_letter=a numeric\n@attribute first_letter=b numeric\n@attribute
	// first_letter=c numeric\n@attribute first_letter=d numeric\n@attribute first_letter=e
	// numeric\n@attribute first_letter=f numeric\n@attribute first_letter=g numeric\n@attribute
	// first_letter=h numeric\n@attribute first_letter=i numeric\n@attribute first_letter=j
	// numeric\n@attribute first_letter=k numeric\n@attribute first_letter=l numeric\n@attribute
	// first_letter=m numeric\n@attribute first_letter=n numeric\n@attribute first_letter=o
	// numeric\n@attribute first_letter=p numeric\n@attribute first_letter=q numeric\n@attribute
	// first_letter=r numeric\n@attribute first_letter=s numeric\n@attribute first_letter=t
	// numeric\n@attribute first_letter=u numeric\n@attribute first_letter=v numeric\n@attribute
	// first_letter=w numeric\n@attribute first_letter=x numeric\n@attribute first_letter=y
	// numeric\n@attribute first_letter=z numeric\n@attribute first_letter=other numeric\n@attribute
	// word_diff_length_class=NL numeric\n@attribute word_diff_length_class=FL numeric\n@attribute
	// word_diff_length_class=NS numeric\n@attribute word_diff_length_class=FS numeric\n@attribute
	// char_diff_length_class=NL numeric\n@attribute char_diff_length_class=FL numeric\n@attribute
	// char_diff_length_class=NS numeric\n@attribute char_diff_length_class=FS numeric\n@attribute
	// word_ratio_length_class=NL numeric\n@attribute word_ratio_length_class=FL numeric\n@attribute
	// word_ratio_length_class=NS numeric\n@attribute word_ratio_length_class=FS numeric\n@attribute
	// char_ratio_length_class=NL numeric\n@attribute char_ratio_length_class=FL numeric\n@attribute
	// char_ratio_length_class=NS numeric\n@attribute char_ratio_length_class=FS numeric\n@attribute
	// distribution_diff_C=H numeric\n@attribute distribution_diff_C=D numeric\n@attribute
	// distribution_diff_C=DD numeric\n@attribute distribution_diff_C=M numeric\n@attribute
	// distribution_ratio_C=H numeric\n@attribute distribution_ratio_C=D numeric\n@attribute
	// distribution_ratio_C=DD numeric\n@attribute distribution_ratio_C=M numeric\n@attribute
	// distribution_diffN numeric\n@attribute distribution_ratioN numeric\n@attribute
	// fromUsed=FROM_TYPING numeric\n@attribute overlapping=B numeric\n@attribute overlapping=JB
	// numeric\n@attribute overlapping=O numeric\n@attribute overlapping=JA numeric\n@attribute
	// overlapping=A numeric");
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	public static void eventToWeka(Event event) {
		event.toWeka();
	}
}
