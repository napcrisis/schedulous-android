package com.schedulous.contacts;

import java.util.ArrayList;

import android.database.Cursor;

import com.schedulous.utility.database.MainDatabase;

public class UserTable extends MainDatabase {
	public static final String TABLE_NAME = "user";
	public static final int ID = 0;
	public static final int INTERNATIONAL_NUMBER = 1;
	public static final int NAME = 2;
	public static final int PROFILE_PIC = 3;
	public static final int USERTYPE = 4;
	public static final String[] ALL_COLUMNS = { "id", "international_number",
			"name", "profile_pic", "userType" };

	public static final String TABLE_CREATE = "create table " + TABLE_NAME
			+ " (" + ALL_COLUMNS[ID] + " text, "
			+ ALL_COLUMNS[INTERNATIONAL_NUMBER] + " text, " + ALL_COLUMNS[NAME]
			+ " text, " + ALL_COLUMNS[PROFILE_PIC] + " text, "
			+ ALL_COLUMNS[USERTYPE]
			+ " integer, _id INTEGER PRIMARY KEY AUTOINCREMENT);";

	public static void clearTable() {
		getDatabase().delete(TABLE_NAME, null, null);
	}

	static User getUser(String id) {
		Cursor cursor = getDatabase().query(TABLE_NAME, ALL_COLUMNS,
				ALL_COLUMNS[ID] + "=" + id, null, null, null, null);
		cursor.moveToFirst();

		User user = null;
		if (cursor.getCount() == 1) {
			user = new User(cursor);
		}
		cursor.close();
		return user;
	}

	static void save(User user) {
		if (exist(user.user_id)) {
			String where = ALL_COLUMNS[ID] + "=" + user.user_id;
			getDatabase().update(TABLE_NAME, user.convertToContentValues(),
					where, null);
		} else {
			getDatabase().insert(TABLE_NAME, null,
					user.convertToContentValues());
		}
	}

	static boolean exist(String id) {
		Cursor cursor = getDatabase().query(TABLE_NAME, ALL_COLUMNS,
				ALL_COLUMNS[ID] + "=" + id, null, null, null, null);
		cursor.moveToFirst();
		try {
			return cursor.getCount() != 0;
		} finally {
			cursor.close();
		}
	}

	static ArrayList<User> getAll() {
		Cursor cursor = getDatabase().query(TABLE_NAME, ALL_COLUMNS, null,
				null, null, null, null);
		cursor.moveToFirst();
		ArrayList<User> temp = new ArrayList<User>();
		if (cursor.getCount() != 0) {
			do {
				temp.add(new User(cursor));
				cursor.moveToNext();
			} while (cursor.isLast());
		}
		try {
			return temp;
		} finally {
			cursor.close();
		}
	}
}
