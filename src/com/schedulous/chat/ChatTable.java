package com.schedulous.chat;

import android.content.ContentValues;
import android.database.Cursor;

import com.schedulous.utility.database.MainDatabase;

public class ChatTable extends MainDatabase {
	public static final String TABLE_NAME = "chat";
	public static final int ROOM_ID = 0;
	public static final int DATETIME_RECEIVED = 1;
	public static final int MESSAGE = 2;
	public static final int FROM = 3;
	public static final int SENT_TO_SERVER = 4;
	public static final int UNUSED_ID = 5;
	public static final int UNIQUE_CHAT_ID = 6;
	public static final int DISPLAY_PHOTO = 7;
	public static final int READ = 3;
	private static final Object MESSAGE_COMING_FROM_CURRENT_USER = null;
	public static final String[] ALL_COLUMNS = { "room_id", "datetime",
			"message", "user_id", "status", "_id", "unique_chat_id",
			"display_photo" };

	public static final String CREATE_TABLE_SQL = "create table " + TABLE_NAME
			+ " (" + ALL_COLUMNS[ROOM_ID] + " text not null, "
			+ ALL_COLUMNS[DATETIME_RECEIVED] + " datetime, "
			+ ALL_COLUMNS[MESSAGE] + " text not null, " + ALL_COLUMNS[FROM]
			+ " text not null, " + ALL_COLUMNS[SENT_TO_SERVER]
			+ " integer, _id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ ALL_COLUMNS[UNIQUE_CHAT_ID] + " text, "
			+ ALL_COLUMNS[DISPLAY_PHOTO] + " INTEGER);";

	public static void clearTable() {
		getDatabase().delete(TABLE_NAME, null, null);
	}

	public static void insertSingleChat(Chat chat) {
		if (chat.unique_id == MESSAGE_COMING_FROM_CURRENT_USER) {
			chat.unique_id = "";
		}
		if ("This room is not anonymous.".equals(chat.message) || exist(chat))
			return;
		Chat previousChat = getLatestChat(chat.room);
		if (previousChat != null && previousChat.username.equals(chat.username)) {
			update(previousChat);
		}

		getDatabase().insert(TABLE_NAME, null, chat.convertToContentValues());
	}

	public static void update(Chat chat) {
		String where = "_id=" + chat.sqlite_id;
		chat.show_dp = 1;
		getDatabase().update(TABLE_NAME, chat.convertToContentValues(), where,
				null);
	}

	public static boolean exist(Chat chat) {
		Cursor cursor = null;
		if ("".equals(chat.unique_id)) {
			// because if the message comes from this user, then it should be
			// saved
			// TODO: future problem of multiple device sending messages,
			// this device will not receive message send by himself
			return false;
		} else {
			cursor = getDatabase().query(
					TABLE_NAME,
					ALL_COLUMNS,
					ALL_COLUMNS[ROOM_ID] + "='" + chat.room + "' and "
							+ ALL_COLUMNS[MESSAGE] + "='" + chat.message
							+ "' and " + ALL_COLUMNS[FROM] + "='"
							+ chat.username + "'", null, null, null, null);
			boolean result = cursor.getCount() > 0;
			cursor.close();
			return result;
		}
	}

	public static boolean existInRoom(Chat chat, String room) {
		Cursor cursor = getDatabase().query(
				TABLE_NAME,
				ALL_COLUMNS,
				ALL_COLUMNS[ROOM_ID] + "='" + room + "' and "
						+ ALL_COLUMNS[MESSAGE] + "='" + chat.message + "'",
				null, null, null, null);
		boolean result = cursor.getCount() > 0;
		cursor.close();
		return result;
	}

	public static Chat getLatestChat(String room) {
		Cursor cursor = getDatabase().query(TABLE_NAME, ALL_COLUMNS,
				ALL_COLUMNS[ROOM_ID] + "=" + room, null, null, null,
				ALL_COLUMNS[UNUSED_ID] + " DESC");
		cursor.moveToFirst();

		Chat chat = null;
		if (cursor.getCount() != 0) {
			chat = new Chat(cursor);
		}
		cursor.close();
		return chat;
	}

	public static void readAll(String mRoomId) {

		String where = ALL_COLUMNS[ROOM_ID] + "=" + mRoomId;
		ContentValues args = new ContentValues();
		args.put(ALL_COLUMNS[SENT_TO_SERVER], READ);
		getDatabase().update(TABLE_NAME, args, where, null);
	}
}