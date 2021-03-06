package com.schedulous.onboarding;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.schedulous.HomeActivity;
import com.schedulous.contacts.ContactController;
import com.schedulous.contacts.TextMessage;
import com.schedulous.contacts.User;
import com.schedulous.utility.AuthenticationManager;
import com.schedulous.utility.CallbackReceiver;
import com.schedulous.utility.Common;
import com.schedulous.utility.HashTable;
import com.schedulous.utility.ReceiverCallback;
import com.schedulous.utility.database.MainDatabase;
import com.schedulous.utility.server.HttpService;

public class LoginController implements ReceiverCallback {
	public static final String REGISTRATION_URL = Common.SCHEDULOUS_URL
			+ "/user/register";
	public static final String VERIFY_URL = Common.SCHEDULOUS_URL
			+ "/user/verify";
	private static final String TAG = LoginController.class.getSimpleName();
	private static final int VERIFICATION_CODE_LENGTH = 5;
	private Gson gson;
	private Context context;
	private RegistrationJson currentData;
	private LoginUI callback;
	private CallbackReceiver receiver;

	public LoginController(Context context, LoginUI callback) {
		this.context = context;
		this.callback = callback;
		MainDatabase.initMainDB(context);
		gson = new Gson();
		receiver = new CallbackReceiver(context, this);
	}

	public void send_number(String mobile_number) throws Exception {
		String internationalNumberString = ContactController
				.make_international_number_from_singapore_number(mobile_number);
		currentData = new RegistrationJson(internationalNumberString);
		String json = gson.toJson(currentData);
		HttpService.startService(context, REGISTRATION_URL, json,
				HttpService.REGISTRATION_REQUEST_CODE);
	}

	public void verify_number(String authentication_code) {
		if (currentData == null) {
			Log.wtf(TAG + "-verify_number", "Lost currentData");
			return;
		}
		try {
			currentData.setCode(authentication_code);
			String json = gson.toJson(currentData);
			HttpService.startService(context, VERIFY_URL, json,
					HttpService.VERIFICATION_REQUEST_CODE);
		} catch (Exception ex) {
			if (context != null) {
				Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG)
						.show();
			}
		}

	}

	public void onPause() {
		receiver.unregister();
	}

	public void onResume() {
		receiver.register();
	}

	public boolean onCreateAuthCheck() {
		if (HashTable.get_entry(AuthenticationManager.AUTH_STR) != null) {
			HomeActivity.startHomeActivity(context);
			return true;
		}
		return false;
	}

	@Override
	public void doAction(Bundle data, String action) {
		if (action.equals(TextMessage.SMS_ACTION)) {
			String verification_code = TextMessage.getVerificationCode(data,
					action);
			if (verification_code != null) {
				callback.receivedVerificationCode(verification_code);
			}
		} else {
			String response = data.getString(HttpService.KEY_JSON);
			Gson gson = new Gson();
			AuthenticationResponse res_json = gson.fromJson(response,
					AuthenticationResponse.class);
			switch (data.getInt(HttpService.KEY_REQUEST_CODE)) {
			case HttpService.REGISTRATION_REQUEST_CODE:
				if (Common.SUCCESS.equals(res_json.status)) {
					currentData.setUserId(res_json.user_id);
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
					AuthenticationManager.storeAuthenticationOnMobile(response,
							currentData.user_id);
					HomeActivity.startHomeActivity(context);
				} else {
					Toast.makeText(context, res_json.message, Toast.LENGTH_LONG)
							.show();
				}
				break;
			}
		}
	}

	@SuppressWarnings("unused")
	private class RegistrationJson {
		public String international_number;
		public String device_model;
		public String code;
		public String user_id;

		public RegistrationJson(String international_number) {
			this.international_number = international_number;
			device_model = Common.getDeviceName();
		}

		public void setCode(String authentication_code) throws Exception {
			if (authentication_code.length() != VERIFICATION_CODE_LENGTH) {
				throw new Exception("Authentication code has invalid length");
			}
			code = authentication_code;
		}

		public void setUserId(String user_id) {
			this.user_id = user_id;
		}
	}

	private class AuthenticationResponse {
		public String status;
		public String user_id;
		@SuppressWarnings("unused")
		public User user;
		public String message;

	}
}
