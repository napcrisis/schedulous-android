package com.schedulous.onboarding;

import java.util.ArrayList;

import com.google.gson.Gson;

public class User {
	public static final int SCHEDULOUS_USER = 1;
	public static final int FACEBOOK_USER = 2;
	public static final int PHONE_CONTACT = 3;
	public static final int SOMETHING_ELSE = 4;
	
	public String id;
	public String xmpp;
	public String country_code;
	public String mobile_number;
	public String international_number;
	public String name;
	public String profile_pic;
	public String country;
	public String referral_code;
	
	public ArrayList<String> addressBookEmails;
	public ArrayList<String> addressBookPhoneNumbers;
	public Object userType;

	public String toStringJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
