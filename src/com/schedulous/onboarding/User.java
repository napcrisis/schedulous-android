package com.schedulous.onboarding;

import java.util.ArrayList;

import com.google.gson.Gson;

public class User implements Comparable<User>{
	// pinned listview settings in userListFragment
	public static final int SECTION_TYPE = 1;
	public static final int ROW_TYPE = 2;
	public int rowType;
	public boolean isSelected;
	public User(int rowType, String title) {
		this.rowType = rowType;
		name = title;
	}

	public User() {
	}

	public static final int SCHEDULOUS_USER = 1;
	public static final int FACEBOOK_USER = 2;
	public static final int PHONE_CONTACT = 3;
	public static final int SOMETHING_ELSE = 4;

	public String id;
	public String xmpp;
	public String international_number;
	public String name;
	public String profile_pic;
	public String country;
	public String referral_code;

	public ArrayList<String> addressBookEmails;
	public ArrayList<String> addressBookPhoneNumbers;
	public int userType;

	public String toStringJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	@Override
	public int compareTo(User another) {
		return name.compareToIgnoreCase(another.name);
	}
}
