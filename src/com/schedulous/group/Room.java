package com.schedulous.group;

import java.util.ArrayList;
import java.util.Collections;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.gson.Gson;
import com.schedulous.contacts.User;
import com.schedulous.utility.AuthenticationManager;
import com.schedulous.utility.Common;
import com.schedulous.utility.HashTable;
import com.schedulous.utility.TimeUtility;
import com.schedulous.utility.server.HttpService;

public class Room implements Comparable<Room> {
	public static final String URL_EXTENSION_GET = Common.SCHEDULOUS_URL
			+ "/group/list";
	public static final String KEY_LAST_QUERIED_TIMESTAMP_GROUP = "KEY_LAST_QUERIED_TIMESTAMP_GROUP";
	public String group_id;
	public String group_name;
	public String group_pic_url;
	public String creator_id;
	public ArrayList<String> members;

	public Room(Cursor cursor) {
		group_id = cursor.getString(GroupTable.GROUP_ID);
		group_name = cursor.getString(GroupTable.GROUP_NAME);
		group_pic_url = cursor.getString(GroupTable.GROUP_PIC_URL);
		String temp = cursor.getString(GroupTable.MEMBERS);
		members = new ArrayList<String>();
		for (String member : temp.split(",")) {
			members.add(member);
		}
	}

	public ContentValues convertToContentValues() {
		ContentValues values = new ContentValues();
		values.put(GroupTable.ALL_COLUMNS[GroupTable.GROUP_ID], group_id);
		values.put(GroupTable.ALL_COLUMNS[GroupTable.GROUP_NAME], group_name);
		values.put(GroupTable.ALL_COLUMNS[GroupTable.GROUP_PIC_URL],
				group_pic_url);
		String temp = "";
		for (String s : members) {
			temp += s + ",";
		}
		if (temp.length() > 1) {
			temp = temp.substring(0, temp.length() - 1);
		}
		values.put(GroupTable.ALL_COLUMNS[GroupTable.MEMBERS], temp);
		values.put(GroupTable.ALL_COLUMNS[GroupTable.LAST_UPDATED_TIME],
				TimeUtility.getCurrentTime());
		return values;
	}

	public void save() {
		GroupTable.save(this);
	}

	public static Room get(String group_id) {
		return GroupTable.getGroup(group_id);
	}

	public static ArrayList<Room> getAll() {
		ArrayList<Room> groups = GroupTable.getAll();
		Collections.sort(groups);
		return groups;
	}

	public static void queryServer(Context context) {
		HttpService.startService(context, URL_EXTENSION_GET,
				AuthenticationManager.getEmptyAuthJsonToken(),
				HttpService.GROUP_LIST_REQUEST_CODE);
	}

	public static void saveResponse(String response, Context context) {
		Gson gson = new Gson();
		GroupInfo gi = gson.fromJson(response, GroupInfo.class);
		if (Common.SUCCESS.equals(gi.status)) {
			ArrayList<String> new_people = new ArrayList<String>();
			for (Room g : gi.groups) {
				for (String group_member_id : g.members) {
					if (User.get(group_member_id) == null) {
						new_people.add(group_member_id);
					}
				}
				g.save();
			}
			User.queryServer(new_people, context);
			HashTable.insert_entry(KEY_LAST_QUERIED_TIMESTAMP_GROUP,
					TimeUtility.now());
		}
	}

	static class GroupInfo {
		String status;
		ArrayList<Room> groups;
	}
	
	@Override
	public int compareTo(Room another) {
		return group_name.compareToIgnoreCase(another.group_name);
	}

	public static void clearLastUpdatedTiming() {
		HashTable.insert_entry(KEY_LAST_QUERIED_TIMESTAMP_GROUP, "");		
	}
}
