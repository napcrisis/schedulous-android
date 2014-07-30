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

import com.schedulous.R;
import com.schedulous.chat.ChatController.ChatDisplayObjects;
import com.schedulous.utility.Callback;
import com.schedulous.utility.CallbackReceiver;
import com.schedulous.utility.Common;

public class ChatFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>, Callback {
	private EditText mInputBox;
	private TextView mSend;
	private ListView mListViewHistory;
	private CallbackReceiver receiver;
	private ChatAdapter mAdapter;

	private static final int LOADER_ID = 0;
	private ChatDisplayObjects cdo;

	private OnClickListener send_message_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!Common.isNullOrEmpty(mInputBox.getText().toString())) {
				ChatController.get(getActivity()).sendMessage(
						mInputBox.getText().toString());
				mInputBox.setText(R.string.empty);
			}
		}
	};

	public ChatFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		receiver = new CallbackReceiver(getActivity(), this);
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
		receiver.unregister();
		super.onPause();
	}

	@Override
	public void onResume() {
		receiver.register();
		cdo = ChatController.get(getActivity()).getDisplayObject();
		getActivity().getActionBar().setTitle(cdo.title);
		super.onResume();
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
		return new CursorLoader(getActivity(), ChatProvider.CONTENT_URI,
				ChatTable.ALL_COLUMNS, ChatTable.ALL_COLUMNS[ChatTable.ROOM_ID]
						+ "='" + ChatController.get(getActivity()).getChatID()
						+ "'", null, null);
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

	@Override
	public void doAction(Bundle data, String action) {
		refresh();
	}

	public void refresh() {
		if (getActivity() != null) {
			getLoaderManager().restartLoader(LOADER_ID, null, this);
		}
	}
}
