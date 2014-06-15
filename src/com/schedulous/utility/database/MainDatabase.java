package com.schedulous.utility.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class MainDatabase {
	private static SQLiteDatabase database;
	private static DatabaseCreator helper;

	public static void initMainDB(Context context){
		helper = new DatabaseCreator(context);
	}
	public static DatabaseCreator getHelper() {
		if (helper == null)
			throw new IllegalStateException(
					"You have tried to access a helper without first initializing with context");
		return helper;
	}
	
	public static void clearDB(){
		helper.clearDatabase(getDatabase());
	}
	
	public static SQLiteDatabase getDatabase() {
		if (helper == null)
			throw new IllegalStateException(
					"You have tried to access a database without first initializing the helper with context");
		if (database == null) {
			database = helper.getWritableDatabase();
		}
		return database;
	}

	protected static void close() {
		helper.close();
	}
}
