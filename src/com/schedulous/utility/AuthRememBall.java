package com.schedulous.utility;

import com.google.gson.Gson;
import com.schedulous.onboarding.User;

public class AuthRememBall {
	public String user_id;
	public String access_token;
	public static final String AUTH_STR = "KEY_AUTH";
	public static final String USER_STR = "USER_STR";
	private static AuthRememBall auth; // singleton

	public User getUser() {
		Gson gson = new Gson();
		String json = HashTable.get_entry(USER_STR);
		return gson.fromJson(json, User.class);
	}

	// do not let any class create this
	private AuthRememBall(String user_id, String access_token) {
		this.access_token = access_token;
		this.user_id = user_id;
	}

	public static void clearAuth() {
		auth = null;
	}

	public static AuthRememBall getBall() {
		if (auth == null) {
			String json = HashTable.get_entry(AUTH_STR);

			if (json != null && !json.equals("")) {
				Gson gson = new Gson();
				auth = gson.fromJson(json, AuthRememBall.class);
			}
		}
		return auth;
	}

	public static void storeAuthenticationOnMobile(User user,
			String access_token) {
		Gson gson = new Gson();
		auth = new AuthRememBall(user.id, access_token);
		String json = gson.toJson(auth);
		HashTable.insert_entry(AUTH_STR, json);

		String json_user = gson.toJson(user);
		HashTable.insert_entry(USER_STR, json_user);
	}

	public static boolean isAuthenticated() {
		return getBall() != null;
	}

	public static String getOnlyAuthJson() {
		Gson gson = new Gson();
		return "{\"auth\":" + gson.toJson(auth) + "}";
	}
}
