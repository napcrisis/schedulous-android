package com.schedulous.utility.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.schedulous.chat.ChatTable;
import com.schedulous.contacts.UserTable;
import com.schedulous.group.GroupTable;
import com.schedulous.utility.HashTable;

public class DatabaseCreator extends SQLiteOpenHelper {
	public static final String TAG = DatabaseCreator.class.getSimpleName();
	private static final String DATABASE_NAME = "schedulous.db";
	private static final int DATABASE_VERSION = 21;

	public DatabaseCreator(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(HashTable.TABLE_CREATE);
		database.execSQL(ChatTable.TABLE_CREATE);
		database.execSQL(UserTable.TABLE_CREATE);
		database.execSQL(GroupTable.TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(TAG, "Upgrading Database from " + oldVersion + " to "
				+ newVersion);
		clearDatabase(database);
	}

	public void clearDatabase(SQLiteDatabase database) {
		database.execSQL("DROP TABLE IF EXISTS " + HashTable.TABLE_NAME);
		database.execSQL("DROP TABLE IF EXISTS " + ChatTable.TABLE_NAME);
		database.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE_NAME);
		database.execSQL("DROP TABLE IF EXISTS " + GroupTable.TABLE_NAME);
		onCreate(database);
	}
}
