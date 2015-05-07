package org.miniforecat.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class UtilsConsole {
	public static InputStream openFile(String file) {
		InputStream ret = null;

		ret = UtilsConsole.class.getResourceAsStream(file);

		if (ret == null) {
			try {
				ret = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}

		return ret;
	}

	public static Integer multiMax(Integer a, Integer... b) {
		for (Integer i : b) {
			a = Math.max(a, i);
		}

		return a;
	}
}
