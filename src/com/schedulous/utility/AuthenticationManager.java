package com.schedulous.utility;

import android.content.Context;

import com.google.gson.Gson;
import com.schedulous.onboarding.User;

public class AuthenticationManager {
	public static final String AUTH_STR = "KEY_AUTH";
	public static Authentication stored;

	public static void logout(Context context) {
		stored = null;
		HashTable.insert_entry(AUTH_STR, null);

	}

	public static Authentication digDatabase() {
		if (stored == null) {
			String json = HashTable.get_entry(AUTH_STR);
			if (!Common.isNullOrEmpty(json)) {
				Gson gson = new Gson();
				stored = gson.fromJson(json, Authentication.class);
			}
		}
		return stored;
	}

	public static void storeAuthenticationOnMobile(String server_response,
			String user_id) {
		Gson gson = new Gson();
		stored = gson.fromJson(server_response, Authentication.class);
		stored.user.id = user_id;
		HashTable.insert_entry(AUTH_STR, gson.toJson(stored));
	}

	public static boolean isAuthenticated() {
		return digDatabase() != null;
	}

	public static String getOnlyAuthJson() {
		Gson gson = new Gson();
		return "{\"auth\":" + gson.toJson(stored) + "}";
	}

	public class Authentication {
		public String session_id;
		public User user;
	}
}
