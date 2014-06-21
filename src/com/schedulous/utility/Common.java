package com.schedulous.utility;

import android.os.Build;

public class Common {
	public static final String SCHEDULOUS_URL = "http://test.schedulous.sg";

	public static final String SUCCESS = "success";

	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	public static boolean isNullOrEmpty(String str) {
		return null == str || "".equals(str);
	}
}
