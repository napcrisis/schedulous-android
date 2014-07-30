package com.schedulous.contacts;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.schedulous.R;
import com.schedulous.contacts.UserListFragment.UserAdapter.ViewHolder;
import com.schedulous.utility.Common;

public class UserListFragment extends Fragment implements OnQueryTextListener {
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
		Typeface deliciousroman;
		Typeface deliciousbold;

		public UserAdapter(Context context) {
			super();
			mImageLoader = ImageLoader.getInstance();
			mInflater = LayoutInflater.from(context);
			displayData = new ArrayList<User>();
			ArrayList<User> contacts = ContactController.getAll(context);
			Collections.sort(contacts);
			registeredUsers = User.getAllFriends();
			User.removeFriendsFromContacts(contacts);
			unregisteredUsers = contacts;
			filter(searchText);
			deliciousroman = Typeface.createFromAsset(
					getActivity().getAssets(), "fonts/Delicious-Roman.otf");

			deliciousbold = Typeface.createFromAsset(getActivity().getAssets(),
					"fonts/Delicious-Bold.otf");
		}

		void clearSelection() {
			for (User user : unregisteredUsers) {
				user.isSelected = false;
			}
			for (User user : registeredUsers) {
				user.isSelected = false;
			}
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
			ArrayList<User> filtered = new ArrayList<User>();
			for (User user : registeredUsers) {
				if (!Common.isNullOrEmpty(user.name)
						&& user.name.contains(searchString)) {
					filtered.add(user);
				}
			}
			for (User user : unregisteredUsers) {
				if (!Common.isNullOrEmpty(user.name)
						&& user.name.contains(searchString)) {
					filtered.add(user);
				}
			}
			Collections.sort(filtered);
			displayData.clear();
			char current = 'Z';
			for (User u : filtered) {
				if (Common.isNullOrEmpty(searchString)) {
					if (u.name != null) {
						if (current != Character.toUpperCase(u.name.charAt(0))) {
							current = Character.toUpperCase(u.name.charAt(0));
							if (current == '+') {
								displayData.add(new User(User.SECTION_TYPE,
										"Unnamed"));
							} else {
								displayData.add(new User(User.SECTION_TYPE,
										current + ""));
							}
						}
					} else {
						displayData.add(new User(User.SECTION_TYPE, "Unnamed"));
					}
				}
				displayData.add(u);
			}
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
						R.layout.row_user, parent, false);
				TextView text = (TextView) row.findViewById(R.id.tv_text);
				CheckBox check = (CheckBox) row
						.findViewById(R.id.iv_check_circle);
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
					.setBackgroundResource(currentUser.rowType == User.SECTION_TYPE ? R.drawable.bg_white_with_bottom_line
							: R.color.white);
			holder.text
					.setTypeface(currentUser.rowType == User.SECTION_TYPE ? deliciousbold
							: deliciousroman);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.text
					.getLayoutParams();
			params.topMargin = currentUser.rowType == User.SECTION_TYPE ? 20
					: 0;
			holder.text.setLayoutParams(params);
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
				if (currentUser.profile_pic.startsWith("content://")) {
					holder.profile_picture.setImageBitmap(ContactController
							.loadContactPhotoThumbnail(currentUser.profile_pic,
									getActivity().getContentResolver()));
				} else {
					mImageLoader.displayImage(currentUser.profile_pic,
							holder.profile_picture);
				}
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
		rootView.setDividerHeight(0);
		rootView.setDivider(null);
		rootView.setAdapter(mAdapter);

		setHasOptionsMenu(true);
		return rootView;
	}

	public void showOnly(ArrayList<User> displayable) {
		mAdapter.displayData.clear();
		for (User u : displayable) {
			mAdapter.displayData.add(u);
			mAdapter.notifyDataSetChanged();
		}
	}

	public void clear() {
		mAdapter.clearSelection();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.userlist_search, menu);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
				.getActionView();
		searchView.setOnQueryTextListener(this);
		super.onCreateOptionsMenu(menu, inflater);
	}

	public void filter(String searchedText) {
		mAdapter.filter(searchedText);
	}

	@Override
	public boolean onQueryTextChange(String searchText) {
		mAdapter.filter(searchText);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
