package com.schedulous.group;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.schedulous.ParentActivity;
import com.schedulous.R;
import com.schedulous.chat.ChatController;
import com.schedulous.chat.ChatFragment;

public class ChatActivity extends ParentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment_holder);
		if (getFragmentManager().findFragmentById(R.id.fragment_container) == null) {
			Fragment fragment = new ChatFragment();
			getFragmentManager().beginTransaction()
					.add(R.id.fragment_container, fragment).commit();
		}
	}

	public static void startActivity(Context context, String id,
			boolean multiUserRoom) {
		Intent intent = new Intent(context, ChatActivity.class);
		ChatController.get(context).changeRoom(context, id, multiUserRoom);
		context.startActivity(intent);
	}
}
