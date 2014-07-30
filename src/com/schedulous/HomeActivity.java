package com.schedulous;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;

import com.schedulous.contacts.ContactController;
import com.schedulous.utility.TypeFaceSpan;

public class HomeActivity extends ParentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ContactController.query(this);
		setContentView(R.layout.activity_fragment_holder);
		SpannableString title = new SpannableString(getResources().getString(
				R.string.app_name));
		title.setSpan(new TypeFaceSpan(this, "Existence-Light.otf"), 0, title.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// Update the action bar title with the TypefaceSpan instance
		getActionBar().setIcon(android.R.color.transparent);
		getActionBar().setTitle(title);

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
