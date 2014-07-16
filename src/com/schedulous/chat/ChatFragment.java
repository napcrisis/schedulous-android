package com.schedulous.chat;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.schedulous.R;

public class ChatFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private EditText mInputBox;
	private TextView mSend;
	private ListView mListViewHistory;

	private ChatAdapter mAdapter;

	private static final int LOADER_ID = 0;

	private OnClickListener send_message_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Toast.makeText(getActivity(), mInputBox.getText().toString(),
					Toast.LENGTH_LONG).show();
		}
	};

	public ChatFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		RelativeLayout rootView = (RelativeLayout) inflater.inflate(
				R.layout.fragment_chat, container, false);

		mInputBox = (EditText) rootView.findViewById(R.id.et_chat_input);
		mSend = (TextView) rootView.findViewById(R.id.tv_send);
		mListViewHistory = (ListView) rootView
				.findViewById(R.id.lv_chat_history);

		mSend.setOnClickListener(send_message_listener);
		mAdapter = new ChatAdapter(getActivity());
		mListViewHistory.setAdapter(mAdapter);
		mListViewHistory.setDivider(null);
		mListViewHistory.setDividerHeight(0);

		mAdapter.setListView(mListViewHistory);

		getLoaderManager().initLoader(LOADER_ID, null, this);

		return rootView;
	}

	@Override
	public void onPause() {
		ChatController.getInstance().onPause();
		super.onPause();
	}

	public void changeChatBubbleToSelf(boolean messageSenderIsSelf) {
		// background color, align chatbubble (rl_chat_bubble)
		// hide iv_profile_picture, tv_others_name and show iv_message_status
		// resize chatbubble base on message (tv_message_content)
	}

	/*
	 * Ideas: generate random text hint tutorials for text commands / event
	 * updates, upcoming event
	 */

	public static int calculateChatBubbleWidth() {
		return -1;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Create a new CursorLoader with the following query parameters.
		return new CursorLoader(
				getActivity(),
				ChatProvider.CONTENT_URI,
				ChatTable.ALL_COLUMNS,
				ChatTable.ALL_COLUMNS[ChatTable.ROOM_ID]
						+ "="
						+ ChatController.getInstance().getCurrentRoom().group_id,
				null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case LOADER_ID:
			// The asynchronous load is complete and the data
			// is now available for use. Only now can we associate
			// the queried Cursor with the SimpleCursorAdapter.
			mAdapter.swapCursor(cursor);
			mAdapter.scrollDown();
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}

}
