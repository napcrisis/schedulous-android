package com.schedulous.chat;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.schedulous.R;

public class RoomActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_fragment_holder);
		if(getFragmentManager().findFragmentById(R.id.fragment_container)==null){
			Fragment fragment = new ChatFragment();
			getFragmentManager().beginTransaction()
            .add(R.id.fragment_container, fragment).commit();
		}
		super.onCreate(savedInstanceState);
	}

}
