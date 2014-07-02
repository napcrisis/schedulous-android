package com.schedulous.contacts;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.schedulous.utility.AuthenticationManager;
import com.schedulous.utility.AuthenticationManager.Authentication;
import com.schedulous.utility.Common;
import com.schedulous.utility.HashTable;
import com.schedulous.utility.TimeUtility;
import com.schedulous.utility.server.HttpService;

public class User implements Comparable<User> {
	public static final String URL_EXTENSION_GET = Common.SCHEDULOUS_URL
			+ "/user/retrieve-info";
	// pinned listview settings in userListFragment
	public static final int SECTION_TYPE = 1;
	public static final int ROW_TYPE = 2;
	@Expose(serialize = false)
	public int rowType = ROW_TYPE;
	@Expose(serialize = false)
	public boolean isSelected;

	public User(int rowType, String title) {
		this.rowType = rowType;
		name = title;
	}

	public User() {
	}

	public User(Cursor cursor) {
		user_id = cursor.getString(UserTable.ID);
		international_number = cursor.getString(UserTable.INTERNATIONAL_NUMBER);
		name = cursor.getString(UserTable.NAME);
		profile_pic = cursor.getString(UserTable.PROFILE_PIC);
		userType = User.SCHEDULOUS_USER;
	}

	public static final int SCHEDULOUS_USER = 1;
	public static final int FACEBOOK_USER = 2;
	public static final int PHONE_CONTACT = 3;
	public static final int SOMETHING_ELSE = 4;

	public String user_id;
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

	public static ArrayList<User> get(ArrayList<String> ids) {
		ArrayList<User> arr = new ArrayList<User>();
		for (String id : ids) {
			arr.add(get(id));
		}
		return arr;
	}

	public static User get(String id) {
		return UserTable.getUser(id);
	}

	public void save() {
		UserTable.save(this);
	}

	public ContentValues convertToContentValues() {
		ContentValues values = new ContentValues();
		values.put(UserTable.ALL_COLUMNS[UserTable.ID], user_id);
		values.put(UserTable.ALL_COLUMNS[UserTable.INTERNATIONAL_NUMBER],
				international_number);
		values.put(UserTable.ALL_COLUMNS[UserTable.PROFILE_PIC], profile_pic);
		values.put(UserTable.ALL_COLUMNS[UserTable.NAME], name);
		values.put(UserTable.ALL_COLUMNS[UserTable.USERTYPE], userType);
		return values;
	}

	static class QueryData {
		Authentication auth;
		ArrayList<String> user_id;

		public QueryData() {
			auth = AuthenticationManager.getAuthServerToken();
		}
	}

	public static void queryServer(ArrayList<String> id, Context context) {
		if (id.size() != 0) {
			QueryData qd = new QueryData();
			qd.user_id = id;
			Gson gson = new Gson();
			HttpService.startService(context, URL_EXTENSION_GET,
					gson.toJson(qd), HttpService.GET_USER_INFO_REQUEST_CODE);
		}
	}

	public static void saveResponse(String response, Context context) {
		Gson gson = new Gson();
		UserInfo ui = gson.fromJson(response, UserInfo.class);
		if (Common.SUCCESS.equals(ui.status)) {
			for (User u : ui.users) {
				if (u.name == null) {
					u.name = ContactController.findDetails(
							u.international_number, context,
							ContactController.DETAIL_NAME);
				}
				if (u.profile_pic == null) {
					u.profile_pic = ContactController.findDetails(
							u.international_number, context,
							ContactController.DETAIL_PICTURE);
				}
				u.save();
			}
			HashTable.insert_entry(
					ContactController.KEY_LAST_QUERIED_TIMESTAMP_USER,
					TimeUtility.now());
		}
	}

	public static ArrayList<User> getAllFriends() {
		return UserTable.getAll();
	}

	public static void removeFriendsFromContacts(ArrayList<User> contacts) {
		HashSet<String> allNumbers = new HashSet<String>();
		for (User u : getAllFriends()) {
			allNumbers.add(u.international_number);
		}
		for (int index = contacts.size() - 1; index >= 0; index--) {
			for (String number : contacts.get(index).addressBookPhoneNumbers) {
				if (allNumbers.contains(number)) {
					contacts.remove(index);
					break;
				}
			}
		}
	}

	class UserInfo {
		User[] users;
		String status;
	}
}
