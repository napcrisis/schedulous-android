package com.schedulous.utility;

import android.os.Build;

public class Common {
	public static final String SCHEDULOUS_URL = "http://test.schedulous.sg";

	public static final String SUCCESS = "success";

	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		return model.replace(manufacturer, "").toUpperCase();
	}

	public static boolean isNullOrEmpty(String str) {
		return null == str || "".equals(str);
	}
}
