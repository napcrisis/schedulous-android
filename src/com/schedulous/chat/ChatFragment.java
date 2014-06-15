package com.schedulous.chat;

import android.app.Fragment;
import android.os.Bundle;

public class ChatFragment extends Fragment {

	/*
	 * Ideas: generate random text hint tutorials for text commands / event updates, upcoming event
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	public static int calculateChatBubbleWidth(){
		return -1;
	}
	public void changeChatBubbleToSelf(boolean messageSenderIsSelf){
		// background color, align chatbubble (rl_chat_bubble)
		// hide iv_profile_picture, tv_others_name and show iv_message_status
		// resize chatbubble base on message (tv_message_content)
	}
}
