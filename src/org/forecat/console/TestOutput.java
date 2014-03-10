package org.forecat.console;

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

	private static ArrayList<Double> precs = new ArrayList<Double>();
	private static ArrayList<Double> recs = new ArrayList<Double>();
	private static ArrayList<Event> events = new ArrayList<Event>();

	private static ArrayList<Integer> coverages = new ArrayList<Integer>();
	private static ArrayList<Integer> numsuggestions = new ArrayList<Integer>();
	private static ArrayList<ArrayList<Integer>> coveragesByLength = new ArrayList<ArrayList<Integer>>();
	private static ArrayList<ArrayList<Integer>> numSuggestionsByLength = new ArrayList<ArrayList<Integer>>();

	private static StringBuilder out = new StringBuilder();

	private static ArrayList<Integer> suggestionPosition = new ArrayList<Integer>();

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

		System.out.println(" MATCH " + source + " " + target);
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
		out.append(s);
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

	public static String getPlot() {

		out.append("# 1:Longitud\n");
		out.append("# 2:Pulsaciones\n");
		out.append("# 3:Sugerencias ofrecidas\n");
		out.append("# 4:Sugerencias usadas\n");

		for (int i = 0; i < keyPress.size(); i++) {
			out.append(sentence_length.get(i));
			out.append("\t");
			out.append(keyPress.get(i));
			out.append("\t");
			out.append(suggestions_offered.get(i));
			out.append("\t");
			out.append(suggestions_used.get(i));
			out.append("\n");
		}

		out.append("## Correlacion 1:2 -> ");
		out.append(getPearsonCorrelation(sentence_length, keyPress));
		out.append("\n## Correlacion 1:4 -> ");
		out.append(getPearsonCorrelation(sentence_length, suggestions_used));
		out.append("\n");

		SummaryStatistics ss = new SummaryStatistics();
		TDistribution td = new TDistribution(keyPress.size() - 1);

		int press = 0;
		int length = 0;

		for (int i = 0; i < keyPress.size(); i++) {
			ss.addValue(((double) keyPress.get(i)) / ((double) sentence_length.get(i)));
			press += keyPress.get(i);
			length += sentence_length.get(i);
		}

		out.append("\n## Mejora media | ");
		out.append(((double) press / (double) length));
		out.append("\n## Mejora por frase | ");
		out.append(ss.getMean());
		out.append(" |  ");
		out.append(ss.getMean() * td.inverseCumulativeProbability(0.975D)
				* ss.getStandardDeviation() / Math.sqrt(keyPress.size()));
		out.append("\n");

		System.out.println("MEJORA MEDIA: " + ss.getMean());

		ss.clear();
		for (int i = 0; i < suggestions_offered.size(); i++) {
			ss.addValue(suggestions_offered.get(i));
		}

		out.append("## Media sugerencias ofrecidas | ");
		out.append(ss.getMean());
		out.append(" |  ");
		out.append(ss.getMean() * td.inverseCumulativeProbability(0.975D)
				* ss.getStandardDeviation() / Math.sqrt(suggestions_offered.size()));
		out.append("\n");

		ss.clear();
		for (int i = 0; i < keyPress.size(); i++) {
			ss.addValue(suggestions_used.get(i));
		}

		out.append("## Media sugerencias usadas | ");
		out.append(ss.getMean());
		out.append(" |  ");
		out.append(ss.getMean() * td.inverseCumulativeProbability(0.975D)
				* ss.getStandardDeviation() / Math.sqrt(keyPress.size()));
		out.append("\n");

		ss.clear();
		for (int i = 0; i < suggestions_used.size(); i++) {
			if (suggestions_offered.get(i) == 0) {
				ss.addValue(0);
			} else {
				ss.addValue(((double) suggestions_used.get(i))
						/ ((double) suggestions_offered.get(i) + 1));
			}
		}

		out.append("## Sugerencias usadas / Sugerencias ofrecidas | ");
		out.append(ss.getMean());
		out.append(" |  ");
		out.append(ss.getMean() * td.inverseCumulativeProbability(0.975D)
				* ss.getStandardDeviation() / Math.sqrt(suggestions_used.size()));
		out.append("\n");

		{
			int withSug = 0;
			int used = 0;
			for (Event e : events) {
				if (e.getChar() == ' ')
					continue;
				if (e.hasSuggestions())
					withSug++;
				if (e.usedSuggestion())
					used++;
			}
			out.append("## ASG | ");
			out.append(((double) used / (double) withSug) + "\n");
		}

		ss.clear();
		for (int i = 0; i < precs.size(); i++) {
			ss.addValue((precs.get(i)));
		}

		out.append("## Precision | ");
		out.append(ss.getMean());
		out.append(" |  ");
		out.append(ss.getMean() * td.inverseCumulativeProbability(0.975D)
				* ss.getStandardDeviation() / Math.sqrt(precs.size()));
		out.append("\n");

		ss.clear();
		for (int i = 0; i < recs.size(); i++) {
			ss.addValue((recs.get(i)));
		}

		out.append("## Cobertura | ");
		out.append(ss.getMean());
		out.append(" |  ");
		out.append(ss.getMean() * td.inverseCumulativeProbability(0.975D)
				* ss.getStandardDeviation() / Math.sqrt(recs.size()));
		out.append("\n");

		ss.clear();
		for (int i = 0; i < suggestionPosition.size(); i++) {
			ss.addValue((suggestionPosition.get(i)));
		}

		out.append("## Suggestion Position | ");
		out.append(ss.getMean());
		out.append(" |  ");
		out.append(ss.getMean() * td.inverseCumulativeProbability(0.975D)
				* ss.getStandardDeviation() / Math.sqrt(recs.size()));
		out.append("\n");

		double precision = (double) allSugOk / (double) (allSugOk + allSugNok);
		double recall = (double) allSugOk / (double) allOk;

		out.append("## Precision2 | ");
		out.append(precision + "\n");
		out.append("## Cobertura2 | ");
		out.append(recall + "\n");

		ss.clear();
		int sentence = 1;
		int used = 0;
		int withSug = 0;

		for (Event e : events) {
			if (e.getChar() == ' ')
				continue;
			if (e.hasSuggestions())
				withSug++;
			if (e.usedSuggestion())
				used++;
			if (e.getSentence() != sentence) {
				sentence = e.getSentence();
				if (withSug == 0) {
					ss.addValue(0);
				} else {
					ss.addValue((used) / ((double) withSug + 1));
				}
				used = 0;
				withSug = 0;
			}
		}

		out.append("## ASGPorFrase | ");
		out.append(ss.getMean());
		out.append(" |  ");
		out.append(ss.getMean() * td.inverseCumulativeProbability(0.975D)
				* ss.getStandardDeviation() / Math.sqrt(sentence - 1));
		out.append("\n");

		out.append("#C# |all| ");
		for (int i = 0; i < coverages.size(); i++) {
			out.append(((double) coverages.get(i)) / ((double) numsuggestions.get(i)) + " ");
		}
		out.append("\n");
		for (int j = 0; j < coveragesByLength.get(0).size(); j++) {
			out.append("#C# |" + j + "| ");
			for (int i = 0; i < coveragesByLength.size(); i++) {
				out.append(((double) coveragesByLength.get(i).get(j))
						/ ((double) numSuggestionsByLength.get(i).get(j)) + " ");
			}
			out.append("\n");
		}
		out.append("\n");

		ss.clear();
		out.append("### - \t");
		for (int i = 0; i < maxTargetSelected; i++) {
			out.append(i + "\t");
		}

		out.append("\n");
		ArrayList<Integer> spairs = new ArrayList<Integer>(), tpairs = new ArrayList<Integer>();
		SimpleRegression sr = new SimpleRegression();

		StringBuilder pairs = new StringBuilder();
		HashMap<Integer, Integer> row;
		for (int i = 0; i < maxSourceSelected; i++) {
			out.append("### " + i + "\t");
			if (sourceTargetCorrespondenceSelected.containsKey(i)) {
				row = sourceTargetCorrespondenceSelected.get(i);
				for (int j = 0; j < maxTargetSelected; j++) {
					if (row.containsKey(j)) {
						out.append(row.get(j) + "\t");
						for (int k = 0; k < row.get(j); k++) {
							spairs.add(i);
							tpairs.add(j);
							sr.addData(i, j);
							pairs.append("#P# " + i + " " + j + "\n");
						}
					} else {
						out.append("-\t");
					}
				}
			} else {
				for (int j = 0; j < maxTargetSelected; j++) {
					out.append("-\t");
				}
			}
			out.append("\n");
		}

		out.append(pairs.toString());

		out.append("\n## Regresión posición origen/destino | " + sr.getIntercept() + " | "
				+ sr.getInterceptStdErr() + "|" + sr.getSlope() + " | " + sr.getSlopeStdErr());

		out.append("\n## Correlación posición origen/destino | "
				+ getPearsonCorrelation(spairs, tpairs) + "\n\n");

		out.append("#### - \t");
		for (int i = 0; i < maxPosition; i++) {
			out.append(i + "\t");
		}

		out.append("\n");

		for (char i : allchars) {
			out.append("#### " + i + "\t");
			if (sourceTargetCorrespondence.containsKey(i)) {
				row = sourceTargetCorrespondence.get(i);
				for (int j = 0; j < maxPosition; j++) {
					if (row.containsKey(j)) {
						out.append(row.get(j) + "\t");
					} else {
						out.append("-\t");
					}
				}
			} else {
				for (int j = 0; j < maxPosition; j++) {
					out.append("-\t");
				}
			}
			out.append("\n");
		}

		for (Event e : events) {
			out.append("#%% " + e.toString() + "\n");
		}

		return out.toString();
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

		return pc.correlation(sc1, sc2);
	}
}
