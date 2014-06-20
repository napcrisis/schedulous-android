package com.schedulous.onboarding;

import com.google.gson.Gson;

public class User {
	public String id;
	public String xmpp_password;
	public String country_code;
	public String mobile_number;
	public String name;
	public String profile_pic;
	public String country;

	public String toStringJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
