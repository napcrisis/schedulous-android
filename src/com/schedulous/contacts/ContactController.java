package com.schedulous.contacts;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.schedulous.R;
import com.schedulous.utility.AuthenticationManager;
import com.schedulous.utility.AuthenticationManager.Authentication;
import com.schedulous.utility.Common;
import com.schedulous.utility.HashTable;
import com.schedulous.utility.TimeUtility;
import com.schedulous.utility.server.HttpService;

public class ContactController {
	private static final String TAG = ContactController.class.getSimpleName();
	private static final String URL_SYNC_PHONEBOOK = Common.SCHEDULOUS_URL
			+ "/user/sync-phonebook";

	private static final String REFERRAL = Common.SCHEDULOUS_URL + "/referral/";
	public static final String SG = "SG";
	public static final String KEY_LAST_QUERIED_TIMESTAMP_USER = "KEY_LAST_QUERIED_TIMESTAMP_USER";
	private static ArrayList<User> phoneBook;

	public static String make_international_number_from_singapore_number(
			String mobile_number) throws Exception {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber internationalNumber = null;
		try {
			internationalNumber = phoneUtil.parse(mobile_number, SG);
		} catch (NumberParseException e) {
			throw new Exception("Not a valid number");
		}
		boolean isValid = phoneUtil.isValidNumber(internationalNumber);
		if (!isValid) {
			throw new Exception("Not a valid number");
		}
		String internationalNumberString = phoneUtil.format(
				internationalNumber, PhoneNumberFormat.INTERNATIONAL);
		return internationalNumberString;
	}

	/*
	 * Goes to server with a list of all user's contacts matches people who are
	 * also registered on schedulous
	 */
	public static void query(Context context) {
		String lastCheckDateTime = HashTable
				.get_entry(KEY_LAST_QUERIED_TIMESTAMP_USER);
		if (!TimeUtility.isTenMinutesLater(lastCheckDateTime)) {
			return;
		}
		Authentication auth = AuthenticationManager.getAuth();
		SendingData jsonObj = new SendingData(auth.user.user_id);
		ArrayList<User> contacts = getAll(context);
		for (User user : contacts) {
			for (String number : user.addressBookPhoneNumbers) {
				jsonObj.contacts.add(number);
			}
		}
		Gson gson = new Gson();
		String json = gson.toJson(jsonObj);
		HttpService.startService(context, URL_SYNC_PHONEBOOK, json,
				HttpService.SYNC_FRIENDS_REQUEST_CODE);
	}

	public static void completeSync(String response, Context context) {
		Gson gson = new Gson();
		ReceivingData rd = gson.fromJson(response, ReceivingData.class);
		if (Common.SUCCESS.equals(rd.status)) {
			ArrayList<String> ids = new ArrayList<String>();
			for (String id : rd.registered.keySet()) {
				ids.add(rd.registered.get(id));
			}
			User.queryServer(ids, context);
		}
	}

	static class ReceivingData {
		String status;
		HashMap<String, String> registered;
		String last_updated;
	}

	static class SendingData {
		Authentication auth;
		String user_id;
		ArrayList<String> contacts;

		public SendingData(String user_id) {
			auth = AuthenticationManager.getAuthServerToken();
			this.user_id = user_id;
			this.contacts = new ArrayList<String>();
		}

	}

	public static ArrayList<User> getAll(Context context) {
		if (phoneBook == null && context != null) {
			phoneBook = getAll(context, true);
		}
		return phoneBook;
	}

	@SuppressLint("UseSparseArrays")
	private static ArrayList<User> getAll(Context context, boolean withPhoto) {
		Map<Integer, User> myContacts = new HashMap<Integer, User>();

		// first get phone numbers
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String[] projection;
		if (withPhoto) {
			projection = new String[] {
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
					ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
					ContactsContract.CommonDataKinds.Phone.NUMBER,
					ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI };
		} else {
			projection = new String[] {
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
					ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
					ContactsContract.CommonDataKinds.Phone.NUMBER };
		}
		ContentResolver cr = context.getContentResolver();
		Cursor people = null;
		try {
			try {
				people = cr.query(uri, projection, null, null, null);
			} catch (RuntimeException e) {
				if (withPhoto) {
					return getAll(context, false);
				} else {
					Crashlytics.logException(e);
				}
			}
			if (people != null) {
				int indexId = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
				int indexName = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
				int indexNumber = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				int indexPhoto = withPhoto ? people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
						: -1;
				while (people.moveToNext()) {
					int contactId = people.getInt(indexId);
					String name = people.getString(indexName);
					// this skips operator's special numbers:
					if (Common.isNullOrEmpty(name) || name.startsWith("#"))
						continue;
					//
					String phonenumber = people.getString(indexNumber);
					String photouri = withPhoto ? people.getString(indexPhoto)
							: null;

					// even if phonenumber is empty, we still want to add the
					// contact
					// in case the contact has email address. if neither
					// present, we'll filter
					// them out later
					if (!Common.isNullOrEmpty(name)) {
						User contact = myContacts.get(contactId);
						if (contact == null) {
							contact = new User();
							contact.userType = User.PHONE_CONTACT;
							contact.name = name;
							contact.profile_pic = photouri;
							myContacts.put(contactId, contact);
						}
						if (!Common.isNullOrEmpty(phonenumber)) {
							if (contact.addressBookPhoneNumbers == null) {
								contact.addressBookPhoneNumbers = new ArrayList<String>();
							}
							try {
								String number = make_international_number_from_singapore_number(phonenumber);
								contact.addressBookPhoneNumbers.add(number);
							} catch (Exception e) {
								Log.i(TAG, "Toss away" + phonenumber);
							}
						}
					}
				}
			}
		} finally {
			if (people != null)
				people.close();
		}

		Cursor emailCur = null;
		String[] emailProjection = new String[] {
				ContactsContract.CommonDataKinds.Email.CONTACT_ID,
				ContactsContract.CommonDataKinds.Email.ADDRESS };
		try {
			emailCur = cr.query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI,
					emailProjection, null, null, null);
			if (emailCur != null) {
				int indexId = emailCur
						.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID);
				int indexEmail = emailCur
						.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);

				while (emailCur.moveToNext()) {
					int contactId = emailCur.getInt(indexId);
					String email = emailCur.getString(indexEmail);
					if (!(Common.isNullOrEmpty(email))) {
						User contact = myContacts.get(contactId);
						// not populating contacts not in phone list, because
						// pure email contacts
						// has a lot of garbage (anyone you've emailed with)
						if (contact != null) {
							if (contact.addressBookEmails == null) {
								contact.addressBookEmails = new ArrayList<String>();
							}
							contact.addressBookEmails.add(email);
						}
					}
				}
			}
		} finally {
			if (emailCur != null)
				emailCur.close();
		}

		ArrayList<User> contactsWithPhoneOrEmail = new ArrayList<User>();
		for (User user : myContacts.values()) {
			if ((user.addressBookEmails != null && user.addressBookEmails
					.size() > 0)
					|| (user.addressBookPhoneNumbers != null && user.addressBookPhoneNumbers
							.size() > 0)) {
				contactsWithPhoneOrEmail.add(user);
			}
		}
		return contactsWithPhoneOrEmail;
	}

	/**
	 * Load a contact photo thumbnail and return it as a Bitmap, resizing the
	 * image to the provided image dimensions as needed.
	 * 
	 * @param photoData
	 *            photo ID Prior to Honeycomb, the contact's _ID value. For
	 *            Honeycomb and later, the value of PHOTO_THUMBNAIL_URI.
	 * @return A thumbnail Bitmap, sized to the provided width and height.
	 *         Returns null if the thumbnail is not found.
	 */
	public static Bitmap loadContactPhotoThumbnail(String pictureUri,
			ContentResolver contentResolver) {
		if (pictureUri == null)
			return null;
		// Creates an asset file descriptor for the thumbnail file.
		AssetFileDescriptor afd = null;
		// try-catch block for file not found
		try {
			// Creates a holder for the URI.
			// Sets the URI from the incoming PHOTO_THUMBNAIL_URI
			Uri thumbUri = Uri.parse(pictureUri);
			if (thumbUri == null)
				return null;

			/*
			 * Retrieves an AssetFileDescriptor object for the thumbnail URI
			 * using ContentResolver.openAssetFileDescriptor
			 */
			afd = contentResolver.openAssetFileDescriptor(thumbUri, "r");

			if (afd == null)
				return null;

			/*
			 * Gets a file descriptor from the asset file descriptor. This
			 * object can be used across processes.
			 */
			FileDescriptor fileDescriptor = afd.getFileDescriptor();
			// Decode the photo file and return the result as a Bitmap
			// If the file descriptor is valid
			if (fileDescriptor != null) {
				// Decodes the bitmap
				BitmapFactory.Options options = new BitmapFactory.Options();
				// options.inSampleSize = 2;
				options.inPreferredConfig = Bitmap.Config.RGB_565;
				options.inPurgeable = true;
				options.inInputShareable = true;
				return BitmapFactory.decodeFileDescriptor(fileDescriptor, null,
						options);
			}
			// If the file isn't found
		} catch (FileNotFoundException e) {
			/*
			 * Handle file not found errors
			 */
			// In all cases, close the asset file descriptor
		} catch (Exception e) {
			Crashlytics.logException(e);
		} finally {
			if (afd != null) {
				try {
					afd.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	public static void sendSMS(String phoneNumber, String sms_body) {
		SmsManager smsManager = SmsManager.getDefault();
		// according to this article
		// http://stackoverflow.com/questions/4580952/why-do-i-get-nullpointerexception-when-sending-an-sms-on-an-htc-desire-or-what
		// if message is too long could get null pointer exception
		// https://www.crashlytics.com/social-studio-inc/android/apps/com.jumpcam.ijump/issues/522efc3faa5760e29b459d4f/sessions/522efacc01ae000174c6c28a1cd79ecf
		// smsManager.sendTextMessage(phoneNumber, null, sms_body, null, null);
		ArrayList<String> parts = smsManager.divideMessage(sms_body);
		try {
			smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null,
					null);
		} catch (NullPointerException e) {
			// for https://code.google.com/p/android/issues/detail?id=3718

			// for some reason we are still getting crashes from this
			// https://crashlytics.com/social-studio-inc/android/apps/com.jumpcam.ijump/issues/528088e1e2c70d5d81826a4a/sessions/5280f117026f000216fafee8612cd1ac
			// to find out why, im trying to find out which variable is null.

			// OK, FROM THE DEBUG CODE BELOW, FOUND OUT THAT THESE CRASHES
			// ARE BECAUSE THE PHONE NUMBERS ARE BAD, LIKE REALLY BAD.
			// RANDOM TEXT, EMAIL ADDRESS, BLANK, ETC. SO WE CAN JUST
			// IGNORE THESE EXCEPTIONS...

			/*
			 * String debug = ""; if (phoneNumber == null) { debug +=
			 * "Phonenumber is null"; } else { debug += "Phone number is:" +
			 * phoneNumber; } if (sms_body != null) { debug +=
			 * "| sms_body:"+sms_body; boolean partsAllOk = true; for (String s
			 * : parts) { if (s == null) { partsAllOk = false; } } if
			 * (!partsAllOk) { debug += "; partCount:" + parts.size() +
			 * ";Mesage:" + sms_body; } } else { debug += "sms_body is null"; }
			 */
		}
	}

	public static void startSMSView(Context context, String number,
			String message) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"
				+ number));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("sms_body", message);
		context.startActivity(intent);
	}

	public static void inviteUserToSchedulous(Context context, User selectedUser) {
		Authentication auth = AuthenticationManager.getAuth();
		String numbers = "";
		for (String number : selectedUser.addressBookPhoneNumbers) {
			numbers += number + ",";
		}
		if (numbers.length() != 0) {
			numbers = numbers.substring(0, numbers.length() - 1);
		}
		String message = context.getResources().getString(R.string.invite_text)
				+ REFERRAL + auth.user.referral_code + ".";
		startSMSView(context, numbers, message);
	}

	public static final int DETAIL_PICTURE = 1;
	public static final int DETAIL_NAME = 2;

	public static String findDetails(String international_number,
			Context context, int detail_reference) {
		if (phoneBook == null) {
			getAll(context);
		}
		for (User u : phoneBook) {
			boolean match = false;
			for (String num : u.addressBookPhoneNumbers) {
				if (international_number.equals(num)) {
					match = true;
					break;
				}
			}
			if (match) {
				switch (detail_reference) {
				case DETAIL_NAME:
					return u.name;
				case DETAIL_PICTURE:
					return u.profile_pic;
				}
			}
		}
		switch (detail_reference) {
		case DETAIL_NAME:
			return international_number;
		default:
			return null;
		}
	}
}