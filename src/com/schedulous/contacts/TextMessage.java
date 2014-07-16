package com.schedulous.contacts;

import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class TextMessage {
	private static final String TAG = TextMessage.class.getSimpleName();
	public static final String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private static final String INCOMING_ADDRESS = null;

	public static String getVerificationCode(Bundle data, String action) {
		if (action.equals(SMS_ACTION)) {
			SmsMessage[] msgs = null;
			String msg_from = null;
			try {
				Object[] pdus = (Object[]) data.get("pdus");
				msgs = new SmsMessage[pdus.length];
				String msg = "";
				for (int i = 0; i < msgs.length; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					msg_from = msgs[i].getOriginatingAddress();
					msg += msgs[i].getMessageBody();
				}
				if (INCOMING_ADDRESS != null) {
					if (INCOMING_ADDRESS.equals(msg_from)) {
						return msg;
					}
				} else {
					return msg;
				}
			} catch (Exception e) {
				Log.i(TAG, e.getMessage());
			}
		}
		return null;
	}
}
