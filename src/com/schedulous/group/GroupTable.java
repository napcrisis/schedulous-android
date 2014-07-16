package com.schedulous.group;

import java.util.ArrayList;

import android.database.Cursor;

import com.schedulous.utility.database.MainDatabase;

public class GroupTable extends MainDatabase {

	public static final String TABLE_NAME = "groups";
	public static final int GROUP_ID = 0;
	public static final int GROUP_NAME = 1;
	public static final int GROUP_PIC_URL = 2;
	public static final int MEMBERS = 3;
	public static final int LAST_UPDATED_TIME = 4;
	public static final String[] ALL_COLUMNS = { "group_id", "group_name",
			"group_pic_url", "members", "last_updated_time" };

	public static final String TABLE_CREATE = "create table " + TABLE_NAME
			+ " (" + ALL_COLUMNS[GROUP_ID] + " text not null, "
			+ ALL_COLUMNS[GROUP_NAME] + " text not null, "
			+ ALL_COLUMNS[GROUP_PIC_URL] + " text, " + ALL_COLUMNS[MEMBERS]
			+ " text not null, " + ALL_COLUMNS[LAST_UPDATED_TIME]
			+ " datetime, _id INTEGER PRIMARY KEY AUTOINCREMENT);";

	public static void clearTable() {
		getDatabase().delete(TABLE_NAME, null, null);
	}

	static Room getGroup(String group_id) {
		Cursor cursor = getDatabase().query(TABLE_NAME, ALL_COLUMNS,
				ALL_COLUMNS[GROUP_ID] + "=" + group_id, null, null, null, null);
		cursor.moveToFirst();

		Room group = null;
		if (cursor.getCount() != 0) {
			group = new Room(cursor);
		}
		cursor.close();
		return group;
	}

	static void save(Room group) {
		if (exist(group.group_id)) {
			String where = ALL_COLUMNS[GROUP_ID] + "=" + group.group_id;
			getDatabase().update(TABLE_NAME, group.convertToContentValues(),
					where, null);
		} else {
			getDatabase().insert(TABLE_NAME, null,
					group.convertToContentValues());
		}
	}

	static boolean exist(String group_id) {
		Cursor cursor = getDatabase().query(TABLE_NAME, ALL_COLUMNS,
				ALL_COLUMNS[GROUP_ID] + "=" + group_id, null, null, null, null);
		cursor.moveToFirst();
		try {
			return cursor.getCount() != 0;
		} finally {
			cursor.close();
		}
	}

	static ArrayList<Room> getAll() {
		Cursor cursor = getDatabase().query(TABLE_NAME, ALL_COLUMNS, null,
				null, null, null, null);
		cursor.moveToFirst();
		ArrayList<Room> temp = new ArrayList<Room>();
		if (cursor.getCount() != 0) {
			while (!cursor.isAfterLast()){
				temp.add(new Room(cursor));
				cursor.moveToNext();
			}
		}
		try {
			return temp;
		} finally {
			cursor.close();
		}
	}
}
