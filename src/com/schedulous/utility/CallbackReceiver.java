package com.schedulous.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.schedulous.utility.server.HttpService;

public class CallbackReceiver extends BroadcastReceiver {
	public static final String RECEIVER_CODE = CallbackReceiver.class
			.getSimpleName();
	private static Callback callback;
	public IntentFilter intentFilter;
	private Context context;

	public CallbackReceiver() {
		super();
	}

	public CallbackReceiver(Context context, Callback callback) {
		this();
		CallbackReceiver.callback = callback;
		intentFilter = new IntentFilter(CallbackReceiver.RECEIVER_CODE);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		this.context = context;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle data = intent.getExtras();
		if (callback != null) {
			callback.doAction(data, intent.getAction());
		}
	}

	public void register() {
		context.registerReceiver(this, intentFilter);
	}

	public void unregister() {
		context.unregisterReceiver(this);
	}

	public static void notify(Context context) {
		Intent broadcastIntent = new Intent();
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.setAction(CallbackReceiver.RECEIVER_CODE);
		context.sendBroadcast(broadcastIntent);
	}

	public static void notify(Context context, String response, int requestCode) {
		Intent broadcastIntent = new Intent();
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.setAction(CallbackReceiver.RECEIVER_CODE);
		Bundle data = new Bundle();
		data.putString(HttpService.KEY_JSON, response);
		data.putInt(HttpService.KEY_REQUEST_CODE, requestCode);
		broadcastIntent.putExtras(data);
		context.sendBroadcast(broadcastIntent);
	}
}
