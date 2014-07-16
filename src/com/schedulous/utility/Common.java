package com.schedulous.utility;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

public class Common {
	public static final String SCHEDULOUS_URL = "http://go.schedulous.sg";
	public static final String ILLEGAL_ACTIVITY_STARTER = "Please call this activity using the static method helper";
	public static final String SUCCESS = "success";

	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		return model.replace(manufacturer, "").toUpperCase();
	}

	public static boolean isNullOrEmpty(String str) {
		return null == str || "".equals(str);
	}

	public static int[] getScreenSize(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int[] ret = new int[2];
		ret[0] = size.x;
		ret[1] = size.y;
		return ret;
	}
}
