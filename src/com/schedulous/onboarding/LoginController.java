package com.schedulous.onboarding;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.schedulous.event.HomeActivity;
import com.schedulous.server.HttpService;
import com.schedulous.utility.CallbackReceiver;
import com.schedulous.utility.Common;
import com.schedulous.utility.HashTable;
import com.schedulous.utility.ReceiverCallback;
import com.schedulous.utility.database.MainDatabase;

public class LoginController implements ReceiverCallback {
	public static final String REGISTRATION_URL = Common.SCHEDULOUS_URL
			+ "/user/register";
	public static final String VERIFY_URL = Common.SCHEDULOUS_URL
			+ "/user/verify";
	public static final String KEY_AUTHENTICATED_USER = "KEY_AUTHENTICATED_USER";
	private static final String TAG = LoginController.class
			.getSimpleName();
	private Gson gson;
	private Context context;
	private BroadcastReceiver receiver;
	private IntentFilter intentFilter;
	private RegistrationJson currentData;
	private LoginUI callback;

	public LoginController(Context context, LoginUI callback) {
		this.context = context;
		this.callback = callback;
		MainDatabase.initMainDB(context);
		gson = new Gson();
		intentFilter = new IntentFilter(CallbackReceiver.RECEIVER_CODE);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
	}

	public void send_number(String country_code, String mobile_number) {
		try {
			currentData = new RegistrationJson(country_code, mobile_number);
			String json = gson.toJson(currentData);
			HttpService.startService(context, REGISTRATION_URL, json,
					HttpService.REGISTRATION_REQUEST_CODE);

			receiver = new CallbackReceiver(this);
			context.registerReceiver(receiver, intentFilter);
		} catch (Exception ex) {
			if (context != null) {
				Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	public void verify_number(String authentication_code) {
		if (currentData == null) {
			Log.wtf(TAG + "-verify_number", "Lost currentData");
		} else {
			try {
				currentData.setCode(authentication_code);
				String json = gson.toJson(currentData);
				HttpService.startService(context, VERIFY_URL, json,
						HttpService.VERIFICATION_REQUEST_CODE);

				if (receiver == null) {
					receiver = new CallbackReceiver(this);
					context.registerReceiver(receiver, intentFilter);
				}
			} catch (Exception ex) {
				if (context != null) {
					Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG)
							.show();
				}
			}
		}
	}

	public void onPause() {
		if (receiver != null && context != null) {
			context.unregisterReceiver(receiver);
		}
	}

	public void onResume() {
		if (receiver != null && context != null) {
			context.registerReceiver(receiver, intentFilter);
		}
	}
	
	public boolean onCreateAuthCheck() {
		if(HashTable.get_entry(KEY_AUTHENTICATED_USER)==null){
			HomeActivity.startHomeActivity(context);
			return true;
		}
		return false;
	}
	
	@Override
	public void doAction(Bundle data) {
		String response = data.getString(HttpService.KEY_JSON);
		Gson gson = new Gson();
		AuthenticationResponse res_json = gson.fromJson(response,
				AuthenticationResponse.class);
		switch (data.getInt(HttpService.KEY_REQUEST_CODE)) {
		case HttpService.REGISTRATION_REQUEST_CODE:
			if (Common.SUCCESS.equals(res_json.status)) {
				callback.completeSending();
			} else {
				Toast.makeText(
						context,
						"SMS failed to send. Email us at junkuits@gmail.com for assistance.",
						Toast.LENGTH_LONG).show();
			}
			break;
		case HttpService.VERIFICATION_REQUEST_CODE:
			if (Common.SUCCESS.equals(res_json.status)) {
				HashTable.insert_entry(KEY_AUTHENTICATED_USER, res_json.user.toStringJson());
				HomeActivity.startHomeActivity(context);
			} else {
				Toast.makeText(context, res_json.message, Toast.LENGTH_LONG)
						.show();
			}
			break;
		}
	}

	@SuppressWarnings("unused")
	private class RegistrationJson {
		public String country_code;
		public String mobile_number;
		public String country;
		public String device_name;
		public String code;

		public RegistrationJson(String country_code, String mobile_number)
				throws Exception {
			if ("+65".equals(country_code)) {
				if (!(mobile_number.length() == 8 && (mobile_number
						.startsWith("8") || mobile_number.startsWith("9")))) {
					throw new Exception(
							"Singapore mobile number starts with 8 or 9.");
				}
			}
			this.country_code = country_code;
			this.mobile_number = mobile_number;
			country = "Singapore";
			device_name = Common.getDeviceName();
		}

		public void setCode(String authentication_code) throws Exception {
			if (authentication_code.length() != 4) {
				throw new Exception("Authentication code has invalid length");
			}
			code = authentication_code;
		}
	}

	private class AuthenticationResponse {
		public String status;
		public User user;
		public String message;
		
	}
}
