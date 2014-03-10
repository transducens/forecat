package org.forecat.shared.ranker.LM;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FSLM {
	// Wrapper for word-based perplexity language model

	static Process irstlm_scorer = null;
	// static String lm = "/home/eltorre/Escritorio/proyecto/irstlm/lm/lm/es/tl_lm/lm.irstlm.blm";
	static String lm = "/home/eltorre/Escritorio/proyecto/irstlm/irstlm-5.80.01/example/train.lm";
	static String proc = "/home/eltorre/Escritorio/proyecto/irstlm/LexicalInfoGuessing/irstlm-5.70.03/features-lmscore/irstlm-scorer";
	static BufferedReader in;
	static BufferedWriter out;

	static public void init() {
		if (irstlm_scorer == null) {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", proc + " " + lm);
			try {
				irstlm_scorer = pb.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			in = new BufferedReader(new InputStreamReader(irstlm_scorer.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(irstlm_scorer.getOutputStream()));
		}
	}

	static public double getPerplexity(String sentence) {
		init();

		String value = "";
		try {
			out.write(sentence);
			out.newLine();
			out.flush();

			value = in.readLine();
			value = value.split(":")[2].split(" ")[0];

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Double.parseDouble(value);
	}
}
