package com.schedulous.utility;

import android.content.Context;

import com.google.gson.Gson;
import com.schedulous.contacts.User;

public class AuthenticationManager {
	public static final String AUTH_STR = "KEY_AUTH";
	public static Authentication stored;

	public static void logout(Context context) {
		stored = null;
		HashTable.insert_entry(AUTH_STR, null);
	}

	public static Authentication getAuth() {
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
		stored.user.user_id = user_id;
		HashTable.insert_entry(AUTH_STR, gson.toJson(stored));
	}

	public static boolean isAuthenticated() {
		return getAuth() != null;
	}

	public static Authentication getAuthServerToken() {
		Authentication auth = getAuth().clone();
		return auth;
	}

	public static String getEmptyAuthJsonToken() {
		Gson gson = new Gson();
		SingleWrapper wp = new SingleWrapper(getAuthServerToken());
		return gson.toJson(wp);
	}

	public static class SingleWrapper {
		Authentication auth;

		public SingleWrapper(Authentication auth) {
			super();
			this.auth = auth;
		}
	}

	public class Authentication {
		public String session_id;
		public User user;
		public String user_id;

		public Authentication clone() {
			Authentication auth = new Authentication();
			auth.session_id = session_id;
			auth.user_id = user.user_id;
			return auth;
		}
	}
}
