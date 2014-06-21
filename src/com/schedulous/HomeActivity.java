package com.schedulous;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.schedulous.R;
import com.schedulous.event.EventListFragment;
import com.schedulous.onboarding.ContactFinder;

public class HomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ContactFinder.requestMatchFriendList(this);
		setContentView(R.layout.activity_fragment_holder);
		
		getActionBar().setLogo(R.drawable.ic_header);
		getActionBar().setTitle(R.string.empty);
		
		if (getFragmentManager().findFragmentById(R.id.fragment_container) == null) {
			Fragment fragment = new EventListFragment();
			getFragmentManager().beginTransaction()
					.add(R.id.fragment_container, fragment).commit();
		}
	}

	public static void startHomeActivity(Context context) {
		Intent startIntent = new Intent(context, HomeActivity.class);
		startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(startIntent);
	}
}
