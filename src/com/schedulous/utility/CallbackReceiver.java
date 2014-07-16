package com.schedulous.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class CallbackReceiver extends BroadcastReceiver {
	public static final String RECEIVER_CODE = CallbackReceiver.class.getSimpleName();
	private static Callback callback;
	public IntentFilter intentFilter;
	public CallbackReceiver(){
		super();
	}
	public CallbackReceiver(Callback callback) {
		this();
		CallbackReceiver.callback = callback;
		intentFilter = new IntentFilter(CallbackReceiver.RECEIVER_CODE);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle data  = intent.getExtras();
		if (callback != null) {
			callback.doAction(data, intent.getAction());
		}
	}

}
