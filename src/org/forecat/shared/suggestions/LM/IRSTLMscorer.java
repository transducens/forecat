package org.forecat.shared.suggestions.LM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class IRSTLMscorer {

	static Process proc;
	public static String irstlm_scorer = "resources/lm_score/irstlm_prob_future";
	public static String lm_location = "resources/lm_score/lm.txt";
	public static String vocab_location = "resources/lm_score/vocab";
	static PrintWriter out;
	static BufferedReader in;
	static StreamGobbler err;

	public static void init() {
		if (proc == null) {
			ProcessBuilder pb = new ProcessBuilder(irstlm_scorer, lm_location, vocab_location);
			try {
				proc = pb.start();
				out = new PrintWriter(new OutputStreamWriter(proc.getOutputStream()));
				in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				System.out.println("#LM# " + irstlm_scorer);
				System.out.println("#LM# " + lm_location);
				System.out.println("#LM# " + vocab_location);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static double getPerplexity(String words) {
		out.write(words);
		out.write("\n");
		out.flush();
		try {
			return (Double.parseDouble(in.readLine()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Double.MAX_VALUE;
	}
}
