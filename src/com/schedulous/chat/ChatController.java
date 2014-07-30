package com.schedulous.chat;

import android.content.Context;

import com.schedulous.contacts.User;
import com.schedulous.group.Group;
import com.schedulous.utility.AuthenticationManager;
import com.schedulous.utility.CallbackReceiver;
import com.schedulous.utility.server.XMPPConnectionManager;

public class ChatController {

	public static ChatController instance;

	private Group group;
	private User user;
	private Context mContext;

	public static final String INDIVIDUAL_PREFIX = "I";

	private ChatController(Context context) {
		super();
		mContext = context;
	}

	public static ChatController get(Context context) {
		if (instance == null) {
			instance = new ChatController(context);
		}
		return instance;
	}

	public String getChatID() {
		return user != null ? INDIVIDUAL_PREFIX + user.user_id : group.group_id;
	}

	public void changeRoom(Context context, String id, boolean multiUserRoom) {
		user = multiUserRoom ? null : User.get(id);
		group = multiUserRoom ? Group.get(id) : null;
		if (user == null) {
			XMPPConnectionManager.get(context).joinRoom(id);
		} else {
			XMPPConnectionManager.get(context).startPrivateChat(id);
		}
	}

	public ChatDisplayObjects getDisplayObject() {
		ChatDisplayObjects cdo = new ChatDisplayObjects();
		cdo.display_picture = user != null ? user.profile_pic
				: group.group_pic_url;
		cdo.title = user != null ? user.name : group.group_name;
		return cdo;
	}

	public class ChatDisplayObjects {
		String title;
		String display_picture;
	}

	public void sendMessage(String msg) {
		if (user == null) {
			XMPPConnectionManager.get(mContext).sendMessage(getChatID(), msg);

		} else {
			XMPPConnectionManager.get(mContext).message(user.user_id, msg);
		}
		Msg chat = new Msg(msg, AuthenticationManager.getAuth().user.user_id
				+ "", getChatID(), Msg.SELF);

		ChatTable.insertSingleChat(chat);
		CallbackReceiver.notify(mContext);
	}
}
