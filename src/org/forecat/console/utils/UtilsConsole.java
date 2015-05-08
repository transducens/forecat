package org.forecat.console.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.forecat.console.Main;

public class UtilsConsole {
	public static InputStream openFile(String file) {
		InputStream ret = null;

		ret = Main.class.getResourceAsStream(file);

		if (ret == null) {
			try {
				ret = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ret;
	}

	public static Integer multiMax(Integer a, Integer... b) {
		Integer maxValue = a;

		for (Integer i : b) {
			a = Math.max(a, i);
		}

		return a;
	}
}
