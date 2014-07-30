package com.schedulous;

import org.jivesoftware.smack.SmackAndroid;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.schedulous.utility.database.MainDatabase;

public class SchedulousApplication extends Application {

	@Override
	public void onCreate() {
		MainDatabase.initMainDB(this);
		SmackAndroid.init(this);
		Crashlytics.start(this);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).build();
		ImageLoader.getInstance().init(config);
		super.onCreate();
	}

}
