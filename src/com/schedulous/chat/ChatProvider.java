package com.schedulous.chat;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.schedulous.utility.database.MainDatabase;

public class ChatProvider extends ContentProvider {
	private static final UriMatcher mURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final String AUTHORITY = ChatProvider.class.getPackage()
			.getName() + "." + ChatProvider.class.getSimpleName();
	private static final String CHAT_BASE_PATH = "chats";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + CHAT_BASE_PATH);

//	private static final int CHAT_ID = 10;
	private static final int CHAT = 20;

	static {
		mURIMatcher.addURI(AUTHORITY, CHAT_BASE_PATH, CHAT);
	}

	@Override
	public int delete(Uri uri, String arg1, String[] arg2) {
		throw new IllegalStateException("Unknown URI: " + uri);
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues arg1) {
		throw new IllegalStateException("Unknown URI: " + uri);
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Set the table
		queryBuilder.setTables(ChatTable.TABLE_NAME);

		int uriType = mURIMatcher.match(uri);
		switch (uriType) {
		case CHAT:
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = MainDatabase.getDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		return 0;
	}

}
