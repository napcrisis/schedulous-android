package com.schedulous.group;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.schedulous.R;
import com.schedulous.chat.ChatFragment;

public class GroupActivity extends Activity {

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
