package com.schedulous.chat;

import com.schedulous.group.Room;

public class ChatController {

	public static ChatController instance;

	private Room room;

	private ChatController() {
		super();
	}

	public static ChatController getInstance() {
		if (instance == null) {
			instance = new ChatController();
		}
		return instance;
	}

	public void changeRoom(String room_id) {
		if (room != null) {
			// cleanup previous ops
		}
		room = Room.get(room_id);
	}

	public void onPause() {
		ChatTable.readAll(room.group_id);
	}

	public Room getCurrentRoom() {
		return room;
	}
}
