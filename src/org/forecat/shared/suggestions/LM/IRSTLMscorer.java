package org.forecat.shared.suggestions.LM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class IRSTLMscorer {

	public static String irstlm_scorer = "resources/lm_score/irstlm_prob_future";
	public static String lm_location = "resources/lm_score/lm.txt";
	public static String vocab_location = "resources/lm_score/vocab";

	static Process proc;

	static int lmTimeOut = 1000;
	static PrintWriter out;
	static BufferedReader in;
	static StreamGobbler err;

	static Duration timeout = Duration.ofSeconds(lmTimeOut);
	static ExecutorService executor = null;

	public static void setTimeOut(int to) {
		lmTimeOut = to;
		timeout = Duration.ofSeconds(lmTimeOut);
	}

	public final static class ReadFuture implements Callable<String> {
		@Override
		public String call() throws Exception {
			return in.readLine();
		}
	}

	public static void init() {
		if (proc == null) {
			executor = Executors.newSingleThreadExecutor();
			ProcessBuilder pb = new ProcessBuilder(irstlm_scorer, lm_location, vocab_location);
			try {
				proc = pb.start();
				out = new PrintWriter(new OutputStreamWriter(proc.getOutputStream()));
				in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				System.out.println("#LM# " + irstlm_scorer);
				System.out.println("#LM# " + lm_location);
				System.out.println("#LM# " + vocab_location);

				int i;

				for (i = 0; i < 10; i++)
					out.write("Initial batch \n");
				out.flush();

				final Future<String> handler = executor.submit(new ReadFuture());

				for (i = 0; i < 10; i++) {
					try {
						handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
					} catch (TimeoutException e) {
						handler.cancel(true);
						System.err.println(
								"The LM engine is unresponsive after " + lmTimeOut + " seconds.");
						System.exit(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}

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
			final Future<String> handler = executor.submit(new ReadFuture());
			try {
				return Double.parseDouble(handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS));
			} catch (TimeoutException e) {
				handler.cancel(true);
				System.err
						.println("The LM engine is unresponsive after " + lmTimeOut + " seconds.");
				System.exit(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Double.MAX_VALUE;
	}

	public static void kill() {
		if (executor != null)
			executor.shutdown();
		if (proc != null)
			proc.destroy();
	}
}
