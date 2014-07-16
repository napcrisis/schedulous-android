package com.schedulous;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.schedulous.contacts.ContactController;

public class HomeActivity extends ParentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ContactController.query(this);
		setContentView(R.layout.activity_fragment_holder);
		
		getActionBar().setLogo(R.drawable.ic_header);
		getActionBar().setTitle(R.string.empty);
		
		if (getFragmentManager().findFragmentById(R.id.fragment_container) == null) {
			Fragment fragment = new HomeListFragment();
			getFragmentManager().beginTransaction()
					.add(R.id.fragment_container, fragment).commit();
		}
	}

	public static void startHomeActivity(Context context) {
		Intent startIntent = new Intent(context, HomeActivity.class);
		startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(startIntent);
	}
}
