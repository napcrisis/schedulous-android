package com.schedulous.chat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.delay.packet.DelayInformation;

import android.content.ContentValues;
import android.database.Cursor;

public class Chat {
	public static final boolean SELF = true;
	public static final boolean OTHERS = false;

	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

	public static final int STATUS_CODE_NOT_SENT = 0;
	public static final int STATUS_CODE_SENT = 1;
	public static final int STATUS_CODE_RECEIVED = 2;

	public String message;
	public String unique_id;
	public boolean isCurrentUser;
	public String username;
	public String dateTime;
	public String room;
	public int sqlite_id;

	public int status = -1;
	public int show_dp = 0;

	public static final int SHOW_DP = 1;
	public static final int HIDE_DP = 2;

	public Chat(String message, String username, String room,
			boolean isCurrentUser) {
		this.message = message;
		this.username = username;
		this.dateTime = "" + new Date().getTime();
		this.room = room;
		this.isCurrentUser = isCurrentUser;
	}

	public Chat(Cursor cursor) {
		sqlite_id = cursor.getInt(ChatTable.UNUSED_ID);
		message = cursor.getString(ChatTable.MESSAGE);
		username = cursor.getString(ChatTable.FROM);
		dateTime = cursor.getString(ChatTable.DATETIME_RECEIVED);
		room = cursor.getString(ChatTable.ROOM_ID);
		status = cursor.getInt(ChatTable.SENT_TO_SERVER);
		show_dp = cursor.getInt(ChatTable.DISPLAY_PHOTO);
	}
	public Chat(Packet packet, String currentUser) {
		DelayInformation delay = (DelayInformation) packet.getExtension("x",
				"jabber:x:delay");
		if (packet instanceof Message) {
			Message msg = ((Message) packet);
			long messageTime = 0;
			if (delay != null) {
				messageTime = delay.getStamp().getTime();
			} else {
				Calendar c = Calendar.getInstance();
				messageTime = c.getTimeInMillis();
			}
			message = msg.getBody();
			dateTime = "" + messageTime;
			String from = msg.getFrom();
			String[] splittedText = from.split("/");
			room = splittedText[0].split("@")[0];
			String currentUserId = splittedText[splittedText.length - 1];
			username = currentUserId;
			isCurrentUser = currentUserId.equals(currentUser);
			unique_id = packet.getPacketID();
		}
	}

	public Chat(Message message, String currentUser) {
		DelayInformation delay = (DelayInformation) message.getExtension("x",
				"jabber:x:delay");
		long messageTime = 0;
		if (delay != null) {
			messageTime = delay.getStamp().getTime();
		} else {
			messageTime = new Date().getTime();
		}
		this.message = message.getBody();
		dateTime = "" + messageTime;
		String from = message.getFrom();
		String[] splittedText = from.split("/");
		room = splittedText[0].split("@")[0];
		String currentUserId = splittedText[splittedText.length - 1];
		username = currentUserId;
		isCurrentUser = currentUserId.equals(currentUser);
		unique_id = message.getPacketID();
	}

	public String getId() {
		return username.split("@")[0];
	}

	@Override
	public String toString() {
		return "Chat [message=" + message + ", isCurrentUser=" + isCurrentUser
				+ ", username=" + username + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Chat) {
			Chat chat = (Chat) o;
			return chat.isCurrentUser == isCurrentUser
					&& chat.message.equals(message);
		}
		return false;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public ContentValues convertToContentValues() {
		ContentValues values = new ContentValues();
		values.put(ChatTable.ALL_COLUMNS[ChatTable.ROOM_ID], room);
		values.put(ChatTable.ALL_COLUMNS[ChatTable.DATETIME_RECEIVED], dateTime);
		values.put(ChatTable.ALL_COLUMNS[ChatTable.MESSAGE], message);
		values.put(ChatTable.ALL_COLUMNS[ChatTable.FROM], username);
		values.put(ChatTable.ALL_COLUMNS[ChatTable.SENT_TO_SERVER], status);
		values.put(ChatTable.ALL_COLUMNS[ChatTable.UNIQUE_CHAT_ID], unique_id);
		values.put(ChatTable.ALL_COLUMNS[ChatTable.DISPLAY_PHOTO], show_dp);
		return values;
	}
}
