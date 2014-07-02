package com.schedulous.group;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.schedulous.R;
import com.schedulous.contacts.ContactController;
import com.schedulous.contacts.User;
import com.schedulous.contacts.UserListFragment;
import com.schedulous.contacts.UserListUI;

public class CreateGroupActivity extends Activity implements UserListUI {
	GroupController controller;
	private UserListFragment user_list;
	private EditText group_name;

	private MenuItem toggleBack;
	private MenuItem create;
	private MenuItem group;

	ActionBar actionbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_group);
		actionbar = getActionBar();
		actionbar.setIcon(R.drawable.ic_schedulous_green_icon);
		actionbar.setTitle(R.string.single_chat);
		actionbar.setHomeButtonEnabled(true);

		group_name = (EditText) findViewById(R.id.et_group_name);
		user_list = (UserListFragment) getFragmentManager().findFragmentById(
				R.id.fragment_userlist);
		user_list.setMultipleSelection(false);
		controller = GroupController.get(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_chat_create, menu);
		toggleBack = menu.findItem(R.id.item_if_group_mode);
		create = menu.findItem(R.id.item_group_create);
		group = menu.findItem(R.id.item_group_toggle);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.item_group_create:
			controller.makeGroup(this, user_list.getSelectedUsers(), group_name
					.getText().toString());
			break;
		case R.id.item_group_toggle:
			toggleBack.setVisible(true);
			toggleBack.setChecked(true);
			create.setVisible(true);
			item.setVisible(false);
			user_list.setMultipleSelection(true);
			group_name.setVisibility(View.VISIBLE);
			actionbar.setTitle(R.string.new_group);
			break;
		case R.id.item_if_group_mode:
			group.setVisible(true);
			create.setVisible(false);
			item.setVisible(false);
			user_list.setMultipleSelection(false);
			group_name.setVisibility(View.GONE);
			actionbar.setTitle(R.string.single_chat);
			break;
		}
		return false;
	}

	@Override
	public void onIndividualRowClick(User user) {
		if (user.userType == User.SCHEDULOUS_USER) {
			GroupController.startChatActivity(this, user);
		} else {
			ContactController.inviteUserToSchedulous(this, user);
		}
	}

	@Override
	protected void onPause() {
		controller.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		controller.onResume();
		super.onResume();
	}
}
