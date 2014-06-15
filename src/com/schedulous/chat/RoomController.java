package com.schedulous.chat;

import android.content.Context;
import android.content.Intent;

public class RoomController {
	public static void startRoomActivity(Context context, String room){
		Intent intent = new Intent(context, RoomActivity.class);
		context.startActivity(intent);
	}
}