package com.schedulous.chat;

import android.app.ActionBar;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.schedulous.ParentActivity;
import com.schedulous.R;
import com.schedulous.contacts.ContactController;
import com.schedulous.contacts.User;
import com.schedulous.contacts.UserListFragment;
import com.schedulous.contacts.UserListUI;
import com.schedulous.group.ChatActivity;
import com.schedulous.group.GroupController;

public class CreateChatActivity extends ParentActivity implements UserListUI {
	GroupController controller;
	private UserListFragment user_list;
	private EditText search;
	private EditText group_name;
	private TextView new_group;
//	private ImageView uploadImage;
	private LinearLayout confirmation;
	private static final int STATE_SINGLE = 0;
	private static final int STATE_MULTIPLE = 1;
	private static final int STATE_CONFIRMATION = 2;
	private MenuItem create;
	private int current_state;
	ActionBar actionbar;

	private void toggleVisibility(int state) {
		current_state = state;
		search.setVisibility(View.GONE);
		user_list.setMultipleSelection(false);
		create.setVisible(state != STATE_SINGLE);
		new_group.setVisibility(View.GONE);
		confirmation.setVisibility(View.GONE);
		switch (state) {
		case STATE_SINGLE:
			actionbar.setTitle(R.string.single_chat);
			new_group.setVisibility(View.VISIBLE);
			break;
		case STATE_MULTIPLE:
			actionbar.setTitle(R.string.new_group);
			user_list.setMultipleSelection(true);
			search.setVisibility(View.VISIBLE);
			break;
		case STATE_CONFIRMATION:
			confirmation.setVisibility(View.VISIBLE);
			user_list.showOnly(user_list.getSelectedUsers());
			break;
		}
	}

	@Override
	public void onBackPressed() {
		switch (current_state) {
		case STATE_MULTIPLE:
			toggleVisibility(STATE_SINGLE);
			break;
		case STATE_CONFIRMATION:
			// TODO:
		default:
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		user_list.clear();
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_group);
		actionbar = getActionBar();
		actionbar.setTitle(R.string.single_chat);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setIcon(android.R.color.transparent);

		search = (EditText) findViewById(R.id.et_search_and_selected_people);
		group_name = (EditText) findViewById(R.id.et_group_name);
		confirmation = (LinearLayout) findViewById(R.id.ll_confirmation);
//		uploadImage = (ImageView) findViewById(R.id.iv_profile_picture);
		new_group = (TextView) findViewById(R.id.tv_new_group);

		new_group.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleVisibility(STATE_MULTIPLE);
			}
		});
		search.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				user_list.filter(search.getText().toString());
				return false;
			}
		});
		user_list = (UserListFragment) getFragmentManager().findFragmentById(
				R.id.fragment_userlist);
		user_list.setMultipleSelection(false);

		controller = GroupController.get(this);

		Typeface deliciousroman = Typeface.createFromAsset(
				getApplicationContext().getAssets(),
				"fonts/Delicious-Roman.otf");
		group_name.setTypeface(deliciousroman);
		((TextView) findViewById(R.id.tv_group_photo_text))
				.setTypeface(deliciousroman);
		new_group.setTypeface(deliciousroman);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_chat_create, menu);
		create = menu.findItem(R.id.item_group_create);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.item_group_create:
			if (current_state == STATE_MULTIPLE) {
				toggleVisibility(STATE_CONFIRMATION);
			} else if (current_state == STATE_CONFIRMATION) {
				controller.makeGroup(this, user_list.getSelectedUsers(),
						group_name.getText().toString());
			}
			break;
		}
		return false;
	}

	@Override
	public void onIndividualRowClick(User user) {
		if (user.userType == User.SCHEDULOUS_USER) {
			ChatActivity.startActivity(this, user.user_id, false);
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
