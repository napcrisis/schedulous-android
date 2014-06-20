package com.schedulous.server;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.xdata.Form;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.schedulous.chat.Chat;
import com.schedulous.chat.ChatTable;
import com.schedulous.utility.AuthRememBall;
import com.schedulous.utility.CallbackReceiver;
import com.schedulous.utility.HashTable;

public class XMPPConnectionManager {
	public static final String TAG = XMPPConnectionManager.class
			.getSimpleName();

	public static final String XMPP_HOST = "test.schedulous.sg";
	public static final String CONFERENCE_HOST_FOR_GROUP_CHAT = "@test.schedulous.sg";

	private static final int LOGIN = 0;
	private static final int LOGOUT = 1;
	private static final int SEND_MESSAGE = 2;
	private static final int LEAVE_ROOM = 3;
	private static final int JOIN_SINGLE_ROOM = 4;

	public static final String TIMESTAMP = "timestamp";

	public static final int RECONNECTING_IN_MILLIS = 1000 * 60 * 2;

	private Object lock = new Object();
	private static XMPPConnection connection;
	private PingManager pm;
	private Context mContext;

	private String xmpp_userid;
	private String xmpp_password;

	private static HashMap<String, XMPPMUCObject> rooms;

	/*
	 * Keeping connection alive while still in active window
	 */
	private Handler handler = new Handler();
	private Runnable timedTask = new Runnable() {
		@Override
		public void run() {
			if (instance != null) {
				try {
					Log.v(TAG, "Trying to ping server");
					pm.pingMyServer();
					handler.postDelayed(timedTask, RECONNECTING_IN_MILLIS);
					Log.v(TAG, "Another ping scheduled in "
							+ RECONNECTING_IN_MILLIS + "milliseconds");
				} catch (NotConnectedException e) {
					Log.v(TAG, "Not connected");
				}
			}
		}
	};

	private static XMPPConnectionManager instance;

	public static XMPPConnectionManager getInstance(Context context) {
		if (instance == null) {
			AuthRememBall auth = AuthRememBall.getBall();
			if (auth == null) {
				throw new IllegalStateException(
						"User has tried to login when they should really been logged out");
			}
			instance = new XMPPConnectionManager(context, auth.getUser().id,
					auth.getUser().xmpp_password);
		}
		return instance;
	}

	public static XMPPConnectionManager testInstance(Context context,
			String id, String pwd) {
		if (instance == null) {
			instance = new XMPPConnectionManager(context, id, pwd);
		}
		return instance;
	}

	private XMPPConnectionManager(Context context, String xmpp_userid,
			String xmpp_password) {
		this.mContext = context;
		this.xmpp_userid = xmpp_userid;
		this.xmpp_password = xmpp_password;

		rooms = new HashMap<String, XMPPMUCObject>();
		new ATask().execute(LOGIN + "");
	}

	private void destroySingleton() {
		Crashlytics.setString("xmppMovementTrace", "logout(empty)");
		if (instance != null) {
			try {
				leaveAllRooms();
				connection.disconnect();
			} catch (NotConnectedException e) {
				Log.e(TAG, "connection is not connected in destroySingleton");
			} finally {
				connection = null;
			}
			instance = null;
		}
	}

	private void establish_connection() throws SmackException, IOException,
			XMPPException {
		if (connection == null || !connection.isConnected()) {
			ConnectionConfiguration cc = new ConnectionConfiguration(XMPP_HOST);
			connection = new XMPPTCPConnection(cc);
			connection.connect();
		}
	}

	private void login() throws XMPPException, SmackException, IOException {
		// asmack configurations
		connection.login(xmpp_userid, xmpp_password);
		pm = PingManager.getInstanceFor(connection);
		handler.postDelayed(timedTask, RECONNECTING_IN_MILLIS);
	}

	public void sendMessage(String room, String message) {
		if (rooms.get(room) == null || !rooms.get(room).muc.isJoined()) {
			new ATask().execute(JOIN_SINGLE_ROOM + "", room);
		}
		new ATask().execute(SEND_MESSAGE + "", room, message);
	}

	private void joinRoomAsync(final String room) throws XMPPException,
			SmackException {
		if (!rooms.containsKey(room)) {
			Log.v(TAG, "Joining room:" + room);
			MultiUserChat muc = createRoomJustIncase(room);
			rooms.put(room, new XMPPMUCObject(muc));

			DiscussionHistory history = new DiscussionHistory();
			String date_json = HashTable.get_entry(TIMESTAMP + "|" + room);
			final Long filter;
			if (date_json != null) {
				Date date = new Date();
				date.setTime((Long.parseLong(date_json) + 1));
				filter = (Long.parseLong(date_json) + 1);
				Log.v("Datejson", filter + "");
				history.setSince(date);
			} else {
				Log.v("date_json", "null");
				filter = 0L;
			}
			rooms.get(room).muc.join(xmpp_userid, null, history,
					SmackConfiguration.getDefaultPacketReplyTimeout());

			rooms.get(room).pl = new PacketListener() {
				private long highestDatetime = filter;

				@Override
				public void processPacket(Packet packet) {
					String pid = packet.getPacketID();
					if (pid == null)
						return;
					Chat chat = new Chat(packet, xmpp_userid);
					long msgDateTime = Long.parseLong(chat.dateTime);
					String dbLastUpdated = HashTable.get_entry(TIMESTAMP + "|"
							+ chat.room);
					if (dbLastUpdated != null) {
						if (highestDatetime == 0) {
							highestDatetime = Long.parseLong(dbLastUpdated);
						}
					}
					if (highestDatetime < msgDateTime) {
						HashTable.insert_entry(TIMESTAMP + "|" + chat.room,
								msgDateTime + "");
						highestDatetime = msgDateTime;
					}
					if (!"0".equals(chat.dateTime)
							&& Long.parseLong(chat.dateTime) < filter) {
						// TODO: snowtrail
						return;
					}
					AuthRememBall auth = AuthRememBall.getBall();
					chat.setStatus(Chat.STATUS_CODE_RECEIVED);
					if (!chat.getId().equals("" + auth.user_id)) {
						ChatTable.insertSingleChat(chat);
						Intent broadcastIntent = new Intent();
						broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
						broadcastIntent
								.setAction(CallbackReceiver.RECEIVER_CODE);
						mContext.sendBroadcast(broadcastIntent);
					}
				}
			};
			rooms.get(room).muc.addMessageListener(rooms.get(room).pl);
		}
	}

	private MultiUserChat createRoomJustIncase(final String room)
			throws XMPPErrorException, SmackException {
		MultiUserChat muc = null;
		muc = new MultiUserChat(connection, room
				+ CONFERENCE_HOST_FOR_GROUP_CHAT);
		muc.create(xmpp_userid);
		muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
		if (connection != null) {
			if (!connection.isConnected()) {
				Crashlytics.log("connection state has failed id:" + xmpp_userid
						+ "\n password:" + xmpp_password + " room" + room);
			}
			if (!connection.isAuthenticated()) {
				Crashlytics.log("xmpp not authenticated id:" + xmpp_userid
						+ "\n password:" + xmpp_password + " room" + room);
			}
		}
		return muc;
	}

	public void joinRoom(String roomId) {
		new ATask().execute(JOIN_SINGLE_ROOM + "", roomId);
	}

	private void leaveAllRooms() throws NotConnectedException {
		for (String key : rooms.keySet()) {
			if (rooms.get(key).muc != null) {
				if (rooms.get(key).pl != null) {
					rooms.get(key).muc.removeMessageListener(rooms.get(key).pl);
				}
				rooms.get(key).muc.leave();
			}
		}
		rooms.clear();
	}

	public void logout() {
		new ATask().execute(LOGOUT + "");
	}

	/*
	 * Asynchronous methods used by all
	 */
	private class ATask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			synchronized (lock) {
				int action = Integer.parseInt(params[0]);
				try {
					establish_connection();
					switch (action) {
					case LOGIN:
						Log.v(TAG, "Logging in");
						login();
						break;
					case JOIN_SINGLE_ROOM:
						joinRoomAsync(params[1]);
						break;
					case LOGOUT:
						Log.v(TAG, "Logout");
						destroySingleton();
						break;
					case SEND_MESSAGE:
						Log.v(TAG, "Sending message");
						String room = params[1];
						String message = params[2];
						MultiUserChat muc = rooms.get(room).muc;
						muc.sendMessage(message);
						break;
					case LEAVE_ROOM:
						Log.v(TAG, "Leaving Room");
						leaveAllRooms();
						break;
					}
				} catch (XMPPException e) {
					Log.wtf(TAG, "XMPPException" + e.getMessage());
				} catch (NotConnectedException e) {
					Log.wtf(TAG, "NotConnectedException" + e.getMessage());
				} catch (NoResponseException e) {
					Log.wtf(TAG, "NoResponseException" + e.getMessage());
				} catch (SmackException e) {
					Log.wtf(TAG, "SmackException" + e.getMessage());
				} catch (IOException e) {
					Log.wtf(TAG, "IOException" + e.getMessage());
				}
			}
			return null;
		}
	}

	class XMPPMUCObject {
		MultiUserChat muc;
		PacketListener pl;

		public XMPPMUCObject(MultiUserChat muc) {
			this.muc = muc;
		}
	}
}
