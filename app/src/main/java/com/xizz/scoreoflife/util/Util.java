package com.xizz.scoreoflife.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Util {

	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd", Locale.ENGLISH);
	public final static long ONEDAY = 86400000;

	public final static String NEWLINE = System.getProperty("line.separator");

	public final static int REQUEST_ADD = 111;
	public final static int REQUEST_EDIT = 222;

	public static long getToday() {
		try {
			return DATE_FORMAT.parse(DATE_FORMAT.format(new Date())).getTime();
		} catch (ParseException e) {
			return 0;
		}
	}
}
