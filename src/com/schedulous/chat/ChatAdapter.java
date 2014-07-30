package com.schedulous.chat;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.schedulous.R;
import com.schedulous.contacts.User;
import com.schedulous.utility.AuthenticationManager;
import com.schedulous.utility.CallbackReceiver;
import com.schedulous.utility.Common;
import com.schedulous.utility.TimeUtility;

public class ChatAdapter extends CursorAdapter {
	public final String TAG = ChatAdapter.class.getSimpleName();
	private ListView mListview;
	private ImageLoader imageloader;
	private HashMap<String, User> users;
	private Drawable default_dp;
	private String currentUserId;
	private LayoutInflater mInflator;
	private int screenWidth;
	private int screenHeight;
	private String room;
	private boolean isScrolling = false;
	private boolean allowScrollDown = true;
	private static final int DELAY_BEFORE_NEXTSCROLLDOWN = 2000;

	private CallbackReceiver receiver;

	public ChatAdapter(Context context) {
		super(context, null, 0);
		currentUserId = AuthenticationManager.getAuth().user.user_id + "";
		mInflator = LayoutInflater.from(context);
		default_dp = context.getResources().getDrawable(
				R.drawable.ic_profile_picture);
		imageloader = ImageLoader.getInstance();
		users = new HashMap<String, User>();
		screenWidth = (int) (Common.getScreenSize(context)[0] / 3.0 * 2) - 50;
	}

	public void switchRoom(String room) {
		this.room = room;
		users.clear();
		// TODO: reload data
	}

	private class ViewHolder {
		RelativeLayout mMessageHolder;
		TextViewEx mMessageTextView;
		TextView mOtherUserName;
		ImageView mOtherUsersDP;
		TextView mTime;
	}

	public void scrollDown() {
		if (mListview == null) {
			Log.wtf(TAG, "mListView Not attached");
			return;
		}
		if (allowScrollDown) {
			mListview.post(new Runnable() {
				@Override
				public void run() {
					mListview.setSelection(getCount() - 1);
				}
			});
		}
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.mOtherUsersDP.setImageDrawable(default_dp);
		Msg currentChat = new Msg(cursor);
		String message = currentChat.message;

		holder.mMessageTextView.setText(message);
		String time = TimeUtility.now();

		String chat_user_id = currentChat.getId();
		currentChat.isCurrentUser = currentUserId.equals(chat_user_id);
		User chatUser = users.get(chat_user_id);
		if (chatUser == null) {
			holder.mOtherUsersDP.setVisibility(View.GONE);
			holder.mMessageTextView.setGravity(Gravity.CENTER);
			holder.mMessageHolder
					.setBackgroundResource(R.drawable.ic_chat_bubble_white);
			return;
		}
		if (currentChat.isCurrentUser) {
			holder.mOtherUserName.setVisibility(View.GONE);
			holder.mOtherUsersDP.setVisibility(View.GONE);
			holder.mMessageTextView.setGravity(Gravity.RIGHT);
			holder.mMessageHolder
					.setBackgroundResource(R.drawable.ic_self_chat);
		} else {
			holder.mMessageHolder
					.setBackgroundResource(R.drawable.ic_friend_chat);
			holder.mMessageTextView.setGravity(Gravity.LEFT);
			String name = chatUser.name;
			if (name != null && !"".equals(name)) {
				if (name.length() > 9) {
					name = name.substring(0, 8);
				}
				holder.mOtherUserName.setText(name);
				holder.mOtherUserName.setVisibility(View.VISIBLE);
				holder.mOtherUsersDP.setVisibility(View.VISIBLE);
			} else {
				holder.mOtherUserName.setVisibility(View.GONE);
				holder.mOtherUsersDP.setVisibility(View.GONE);
			}
		}
		holder.mTime.setText(time);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View rowView = mInflator.inflate(R.layout.row_chat, null);
		ViewHolder temp = new ViewHolder();
		temp.mMessageHolder = (RelativeLayout) rowView
				.findViewById(R.id.rl_chat_bubble);
		temp.mMessageTextView = (TextViewEx) rowView
				.findViewById(R.id.tv_message_content);
		temp.mMessageTextView.setMaxWidth(screenWidth);
		temp.mOtherUsersDP = (ImageView) rowView
				.findViewById(R.id.iv_profile_picture);
		temp.mOtherUserName = (TextView) rowView
				.findViewById(R.id.tv_others_name);
		temp.mTime = (TextView) rowView.findViewById(R.id.tv_timestamp);
		rowView.setTag(temp);
		return rowView;
	}

	private Handler handler = new Handler();
	private Runnable timedTask = new Runnable() {
		@Override
		public void run() {
			if (isScrolling) {
				handler.postDelayed(timedTask, DELAY_BEFORE_NEXTSCROLLDOWN);
			} else {
				allowScrollDown = true;
			}
		}
	};

	public void setListView(ListView mListView) {
		this.mListview = mListView;
		this.mListview.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		this.mListview.setStackFromBottom(true);

		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				isScrolling = scrollState != 0;
				allowScrollDown = false;
				handler.postDelayed(timedTask, DELAY_BEFORE_NEXTSCROLLDOWN);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
	}
}