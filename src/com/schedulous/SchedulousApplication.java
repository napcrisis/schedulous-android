package com.schedulous;

import org.jivesoftware.smack.SmackAndroid;

import com.crashlytics.android.Crashlytics;
import com.schedulous.utility.database.MainDatabase;

import android.app.Application;

public class SchedulousApplication extends Application {

	@Override
	public void onCreate() {
		MainDatabase.initMainDB(this);
		SmackAndroid.init(this);
		Crashlytics.start(this);
		super.onCreate();
	}

}
