package com.schedulous.utility;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TimeUtility {
	private static final String SQLITE_DATETIME_FORMAT = "Y-M-d H:m:s";
	private static DateTimeFormatter dtf = DateTimeFormat.forPattern(
			SQLITE_DATETIME_FORMAT).withLocale(Locale.US);

	public static String getCurrentTime() {
		DateTime dt = new DateTime();
		return dtf.print(dt);
	}

	public static String now() {
		return DateTime.now().toString(dtf);
	}

	public static boolean isTenMinutesLater(String timestamp) {
		if (!Common.isNullOrEmpty(timestamp)) {
			DateTimeFormatter formatter = DateTimeFormat.forPattern(
					"Y-M-d H:m:s").withLocale(Locale.US);
			DateTime lastUpdated = formatter.parseDateTime(timestamp);
			if (!lastUpdated.plusMinutes(10).isAfterNow()) {
				return false;
			}
		}
		return true;
	}
}
