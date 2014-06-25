package com.schedulous.group;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.schedulous.onboarding.User;
import com.schedulous.utility.AuthenticationManager;
import com.schedulous.utility.AuthenticationManager.Authentication;
import com.schedulous.utility.server.HttpService;
import com.schedulous.utility.Common;

public class GroupController {
	private static final String URL_CREATE = Common.SCHEDULOUS_URL
			+ "/group/create";

	public static void startChatActivity(Context context, User selected) {
		Intent intent = new Intent(context, GroupActivity.class);
		context.startActivity(intent);
	}

	public static void startCreateGroupActivity(Context context) {
		Intent intent = new Intent(context, CreateGroupActivity.class);
		context.startActivity(intent);
	}

	public static void makeGroup(Context context, ArrayList<User> users,
			String groupname) {
		Authentication auth = AuthenticationManager.digDatabase();
		CreateGroupData data = new CreateGroupData(auth.user.id, groupname);
		for (User u : users) {
			switch (u.userType) {
			case User.SCHEDULOUS_USER:
				data.registered.add(u.id);
				break;
			case User.PHONE_CONTACT:
				data.unregistered.add(u.international_number);
				break;
			}
		}
		Gson gson = new Gson();
		HttpService.startService(context, URL_CREATE, gson.toJson(data),
				HttpService.CREATE_GROUP_REQUEST_CODE);
	}

	static class CreateGroupData {
		String user_id;
		String group_name;
		ArrayList<String> registered;
		ArrayList<String> unregistered;

		public CreateGroupData(String user_id, String group_name) {
			this.user_id = user_id;
			this.group_name = group_name;
			this.registered = new ArrayList<String>();
			this.unregistered = new ArrayList<String>();
		}
	}
}