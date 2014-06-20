package com.schedulous;

import org.jivesoftware.smack.SmackAndroid;

import com.crashlytics.android.Crashlytics;

import android.app.Application;

public class SchedulousApplication extends Application {

	@Override
	public void onCreate() {
		SmackAndroid.init(this);
		Crashlytics.start(this);
		super.onCreate();
	}

}
