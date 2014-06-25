package com.schedulous.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.schedulous.R;
import com.schedulous.contacts.UserListFragment.UserAdapter.ViewHolder;
import com.schedulous.onboarding.User;
import com.schedulous.utility.Common;

public class UserListFragment extends Fragment {
	private static final String TAG = UserListFragment.class.getSimpleName();
	OnItemClickListener row_click_listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapter, View rowView,
				int position, long arg3) {
			if (mAdapter.getItem(position).rowType == User.ROW_TYPE) {
				if (multipleSelection) {
					ViewHolder vh = (ViewHolder) rowView.getTag();
					vh.check.setChecked(!vh.check.isChecked());
				} else {
					((UserListUI) getActivity()).onIndividualRowClick(mAdapter
							.getItem(position));
				}
			}
		}
	};

	public class UserAdapter extends BaseAdapter implements
			PinnedSectionListAdapter {
		private ArrayList<User> displayData;
		private LayoutInflater mInflater;
		private ImageLoader mImageLoader;
		private ArrayList<User> unregisteredUsers;
		private ArrayList<User> registeredUsers;
		private User headerForRegistered;
		private User headerForUnregistered;

		public UserAdapter(Context context) {
			super();
			mImageLoader = ImageLoader.getInstance();
			mInflater = LayoutInflater.from(context);
			displayData = new ArrayList<User>();
			ArrayList<User> contacts = ContactFinder.getAll(context);
			Collections.sort(contacts);
			registeredUsers = new ArrayList<User>();
			HashSet<String> registeredNumbers = ContactFinder
					.getRegisteredFriendNumbers();
			for (int index = contacts.size() - 1; index >= 0; index--) {
				contacts.get(index).rowType = User.ROW_TYPE;
				for (String number : contacts.get(index).addressBookPhoneNumbers) {
					if (registeredNumbers.contains(number)) {
						registeredUsers.add(contacts.get(index));
						contacts.remove(index);
						break;
					}
				}
			}
			unregisteredUsers = contacts;
			headerForRegistered = new User(User.SECTION_TYPE,
					"Schedulous Users");
			headerForUnregistered = new User(User.SECTION_TYPE,
					"Phonebook Users");

			filter(searchText);
		}

		ArrayList<User> getSelectedUsers() {
			ArrayList<User> selected = new ArrayList<User>();
			for (User user : unregisteredUsers) {
				if (user.isSelected)
					selected.add(user);
			}
			for (User user : registeredUsers) {
				if (user.isSelected)
					selected.add(user);
			}
			return selected;
		}

		void filter(String searchString) {
			ArrayList<User> filteredRegisteredUsers = new ArrayList<User>();
			ArrayList<User> filteredUnregisteredUsers = new ArrayList<User>();
			for (User user : registeredUsers) {
				if (user.name.contains(searchString)) {
					filteredRegisteredUsers.add(user);
				}
			}
			for (User user : unregisteredUsers) {
				if (user.name.contains(searchString)) {
					filteredUnregisteredUsers.add(user);
				}
			}
			if (filteredRegisteredUsers.size() != 0) {
				filteredRegisteredUsers.add(0, headerForRegistered);
			}
			if (filteredUnregisteredUsers.size() != 0) {
				filteredUnregisteredUsers.add(0, headerForUnregistered);
			}
			displayData.clear();
			displayData.addAll(filteredRegisteredUsers);
			displayData.addAll(filteredUnregisteredUsers);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return displayData.size();
		}

		@Override
		public User getItem(int position) {
			return displayData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			return 3;
		}

		@Override
		public boolean isItemViewTypePinned(int viewType) {
			return viewType == User.SECTION_TYPE;
		}

		@Override
		public int getItemViewType(int position) {
			return getItem(position).rowType;
		}

		@Override
		public View getView(int position, View recycled, ViewGroup parent) {
			User currentUser = getItem(position);
			// recycle if possible
			ViewHolder holder;
			if (currentUser.rowType == User.SECTION_TYPE || recycled == null) {
				RelativeLayout row = (RelativeLayout) mInflater.inflate(
						R.layout.row_user, null);
				TextView text = (TextView) row.findViewById(R.id.tv_text);
				CheckBox check = (CheckBox) row
						.findViewById(R.id.cb_check_list);
				ImageView profile_picture = (ImageView) row
						.findViewById(R.id.iv_profile_picture);
				holder = new ViewHolder(text, check, profile_picture, row);
				row.setTag(holder);
			} else {
				holder = (ViewHolder) recycled.getTag();
			}
			holder.profile_picture
					.setVisibility(currentUser.rowType == User.SECTION_TYPE ? View.GONE
							: View.VISIBLE);
			holder.parent
					.setBackgroundResource(currentUser.rowType == User.SECTION_TYPE ? R.color.red_flat_alizarin
							: R.color.white_flat_cloud);
			// set data
			holder.text
					.setText(Common.isNullOrEmpty(currentUser.name) ? currentUser.international_number
							: currentUser.name);

			// Toggle visibility
			holder.check.setOnCheckedChangeListener(null);
			if (!multipleSelection) {
				holder.check.setVisibility(View.GONE);
			} else {
				holder.check
						.setVisibility(currentUser.rowType == User.SECTION_TYPE ? View.GONE
								: View.VISIBLE);
				holder.check.setChecked(currentUser.isSelected);
				holder.check
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								((User) buttonView.getTag()).isSelected = isChecked;
							}
						});
			}

			holder.check.setTag(currentUser);
			if (Common.isNullOrEmpty(currentUser.profile_pic)) {
				holder.profile_picture
						.setImageResource(R.drawable.ic_profile_picture);
			} else {
				mImageLoader.displayImage(currentUser.profile_pic,
						holder.profile_picture);
			}
			return holder.parent;
		}

		class ViewHolder {
			public ViewHolder(TextView text, CheckBox check,
					ImageView profile_picture, RelativeLayout parent) {
				this.text = text;
				this.check = check;
				this.profile_picture = profile_picture;
				this.parent = parent;
			}

			RelativeLayout parent;
			TextView text;
			CheckBox check;
			ImageView profile_picture;
		}
	}

	private UserAdapter mAdapter;
	private boolean multipleSelection;
	private String searchText;

	public UserListFragment() {
		super();
	}

	public void setMultipleSelection(boolean multipleSelection) {
		this.multipleSelection = multipleSelection;
	}

	public ArrayList<User> getSelectedUsers() {
		return mAdapter.getSelectedUsers();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.v(TAG, "onCreateView");
		searchText = "";
		if (!(getActivity() instanceof UserListUI)) {
			throw new IllegalStateException(
					"Please have your activity implement callback UserListUI");
		}
		ListView rootView = (ListView) inflater.inflate(
				R.layout.fragment_pinned_listview, container, false);
		rootView.setOnItemClickListener(row_click_listener);
		mAdapter = new UserAdapter(getActivity());
		rootView.setAdapter(mAdapter);

		return rootView;
	}
}
