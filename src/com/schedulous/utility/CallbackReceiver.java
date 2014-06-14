package com.schedulous.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CallbackReceiver extends BroadcastReceiver {
	public static final String RECEIVER_CODE = CallbackReceiver.class.getSimpleName();
	private Callback callback;
	
	public CallbackReceiver(Callback callback) {
		this.callback = callback;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle data  = intent.getExtras();
		if (callback != null) {
			callback.doAction(data);
		}
	}

}
