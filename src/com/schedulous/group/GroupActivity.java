package com.schedulous.group;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.schedulous.R;
import com.schedulous.chat.ChatController;
import com.schedulous.chat.ChatFragment;

public class GroupActivity extends Activity {

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

	public static void startActivity(Context context, String room_id) {
		Intent intent = new Intent(context, GroupActivity.class);
		ChatController.getInstance().changeRoom(room_id);
		context.startActivity(intent);
	}
}
