package com.schedulous;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.schedulous.group.Group;
import com.schedulous.group.GroupController;

public class HomeListFragment extends Fragment {
	GroupController controller;
	private EventListAdapter mAdapter;

	class EventListAdapter extends BaseAdapter implements
			PinnedSectionListAdapter {

		public ArrayList<EventDisplayObjects> list;
		LayoutInflater mInflator;
		boolean firstLoad = true;
		public EventListAdapter(Context context) {
			super();
			mInflator = LayoutInflater.from(context);
			list = new ArrayList<HomeListFragment.EventDisplayObjects>();
		}

		public void refresh() {
			list.clear();
			ArrayList<Group> groups = Group.getAll();
			if (groups.size() > 0) {
				list.add(new EventDisplayObjects("Chat",
						EventDisplayObjects.HEADER_TYPE));
				for (Group g : groups) {
					list.add(new EventDisplayObjects(g));
				}
			}
			if (firstLoad) {
				firstLoad = false;
			} else {
				notifyDataSetChanged();
			}
		}

		// return 'true' for all view types to pin
		@Override
		public boolean isItemViewTypePinned(int viewType) {
			return viewType == EventDisplayObjects.HEADER_TYPE;
		}

		@Override
		public int getItemViewType(int position) {
			return getItem(position).type;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public EventDisplayObjects getItem(int position) {
			return list.get(position);
		}

		@Override
		public int getViewTypeCount() {
			return 3;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			View rootView = (View) mInflator.inflate(R.layout.row_event, null);
			TextView tv = (TextView) rootView.findViewById(R.id.tv_text);
			EventDisplayObjects item = getItem(position);
			tv.setText(item.mainText);
			switch (item.type) {
			case EventDisplayObjects.DATA_TYPE:
				tv.setTextColor(Color.DKGRAY);
				rootView.setBackgroundColor(parent.getResources().getColor(
						R.color.white_flat_cloud));
				break;
			case EventDisplayObjects.HEADER_TYPE:
				tv.setTextColor(Color.WHITE);
				rootView.setBackgroundColor(parent.getResources().getColor(
						R.color.red_flat_pomegranate));
				break;
			}
			return rootView;
		}
	}

	class EventDisplayObjects {
		private static final int HEADER_TYPE = 1;
		private static final int DATA_TYPE = 2;
		Group group;
		String mainText;
		int type;

		public EventDisplayObjects(String mainText, int typeOfObject) {
			this.mainText = mainText;
			this.type = typeOfObject;
		}

		public EventDisplayObjects(Group group) {
			this.group = group;
			type = DATA_TYPE;
			mainText = group.group_name;
		}

		@Override
		public String toString() {
			return mainText;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ListView rootView = (ListView) inflater.inflate(
				R.layout.fragment_pinned_listview, container, false);
		controller = GroupController.get(getActivity());
		controller.set(this);
		mAdapter = new EventListAdapter(getActivity());
		rootView.setAdapter(mAdapter);
		setHasOptionsMenu(true);
		return rootView;
	}

	@Override
	public void onPause() {
		controller.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		controller.onResume();
		controller.query(getActivity());
		mAdapter.refresh();
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_event_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_create_group:
			GroupController.startCreateGroupActivity(getActivity());
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void refresh() {
		mAdapter.refresh();
	}

}
