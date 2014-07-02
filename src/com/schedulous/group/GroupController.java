package com.schedulous.group;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.schedulous.HomeActivity;
import com.schedulous.HomeListFragment;
import com.schedulous.contacts.User;
import com.schedulous.utility.AuthenticationManager;
import com.schedulous.utility.AuthenticationManager.Authentication;
import com.schedulous.utility.CallbackReceiver;
import com.schedulous.utility.Common;
import com.schedulous.utility.HashTable;
import com.schedulous.utility.ReceiverCallback;
import com.schedulous.utility.TimeUtility;
import com.schedulous.utility.server.HttpService;

public class GroupController implements ReceiverCallback {
	private static final String URL_CREATE = Common.SCHEDULOUS_URL
			+ "/group/create";
	
	private CallbackReceiver receiver;
	public Context context;

	private HomeListFragment homeListFragment;
	private static GroupController single;

	public static GroupController get(Context context) {
		if (single == null) {
			single = new GroupController(context);
		} else {
			single.context = context;
		}
		return single;
	}

	private GroupController(Context context) {
		super();
		this.context = context;
	}

	public void query(Context context) {
		String lastCheckDateTime = HashTable
				.get_entry(Group.KEY_LAST_QUERIED_TIMESTAMP_GROUP);
		if (!TimeUtility.isTenMinutesLater(lastCheckDateTime)) {
			return;
		}
		Group.queryServer(context);
		startReceiver(context);
	}

	public static void startChatActivity(Context context, User selected) {
		Intent intent = new Intent(context, GroupActivity.class);
		context.startActivity(intent);
	}

	public static void startCreateGroupActivity(Context context) {
		Intent intent = new Intent(context, CreateGroupActivity.class);
		context.startActivity(intent);
	}

	public void makeGroup(Context context, ArrayList<User> users,
			String groupname) {
		Authentication auth = AuthenticationManager.getAuth();
		CreateGroupData data = new CreateGroupData(auth.user.user_id, groupname);
		for (User u : users) {
			switch (u.userType) {
			case User.SCHEDULOUS_USER:
				data.registered.add(u.user_id);
				break;
			case User.PHONE_CONTACT:
				for (String number : u.addressBookPhoneNumbers) {
					data.unregistered.add(number);
				}
				break;
			}
		}
		Gson gson = new Gson();
		HttpService.startService(context, URL_CREATE, gson.toJson(data),
				HttpService.CREATE_GROUP_REQUEST_CODE);
		startReceiver(context);
	}

	private void startReceiver(Context context) {
		if (receiver == null) {
			receiver = new CallbackReceiver(this);
			context.registerReceiver(receiver, receiver.intentFilter);
		}
	}

	static class CreateGroupData {
		String group_name;
		ArrayList<String> registered;
		ArrayList<String> unregistered;
		Authentication auth;
		public CreateGroupData(String user_id, String group_name) {
			auth = AuthenticationManager.getAuthServerToken();
			this.group_name = group_name;
			this.registered = new ArrayList<String>();
			this.unregistered = new ArrayList<String>();
		}
	}

	@Override
	public void doAction(Bundle data,String action) {
		// String response = data.getString(HttpService.KEY_JSON);
		// Gson gson = new Gson();
		switch (data.getInt(HttpService.KEY_REQUEST_CODE)) {
		case HttpService.CREATE_GROUP_REQUEST_CODE:
			Group.clearLastUpdatedTiming();
			HomeActivity.startHomeActivity(context);
			break;
		case HttpService.GROUP_LIST_REQUEST_CODE:
			if(homeListFragment!=null && homeListFragment.isVisible()){
				homeListFragment.refresh();
			}
		}

	}

	public void onResume() {
		if (receiver != null && context != null) {
			context.registerReceiver(receiver, receiver.intentFilter);
		}
	}

	public void onPause() {
		if (receiver != null && context != null) {
			context.unregisterReceiver(receiver);
		}
	}

	public void set(HomeListFragment homeListFragment) {
		this.homeListFragment = homeListFragment;
	}
}