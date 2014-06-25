package com.schedulous.utility.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.schedulous.contacts.ContactFinder;
import com.schedulous.utility.CallbackReceiver;
import com.schedulous.utility.Common;
import com.schedulous.utility.OnCompletionCall;

public class HttpService extends IntentService implements OnCompletionCall {
	public static final String TAG = HttpService.class.getSimpleName();
	public static final int REGISTRATION_REQUEST_CODE = 1001;
	public static final int VERIFICATION_REQUEST_CODE = 1002;
	public static final int SYNC_FRIENDS_REQUEST_CODE = 1003;
	public static final int CREATE_GROUP_REQUEST_CODE = 1004;

	public static final int TYPE_POST = 1;
	public static final int TYPE_GET = 2;

	public static final String KEY_URL_PATH = "KEY_URL_PATH";
	public static final String KEY_JSON = "KEY_JSON";
	public static final String KEY_REQUEST_CODE = "KEY_TYPE";

	public static final String KEY_RESPONSE_CODE = "KEY_RESPONSE_CODE";

	public static void startService(Context context, String url, String json,
			int request_code) {
		Intent intent = new Intent(context, HttpService.class);
		intent.putExtra(HttpService.KEY_URL_PATH, url);
		intent.putExtra(HttpService.KEY_JSON, json);
		intent.putExtra(HttpService.KEY_REQUEST_CODE, request_code);
		context.startService(intent);
		Log.wtf(TAG, "Intent sent");
	}

	public HttpService() {
		super(HttpService.class.getSimpleName());
	}

	private static class StartPostCommunication extends
			AsyncTask<Void, Void, String> {
		private OnCompletionCall listener;
		private String url;
		private String json_data;
		private int requestCode;
		private int type;

		public StartPostCommunication(String url, String json_data,
				OnCompletionCall listener, int requestCode, int typeOfRequest) {
			this.url = url;
			this.json_data = json_data;
			this.listener = listener;
			this.requestCode = requestCode;
			this.type = typeOfRequest;
		}

		@Override
		protected String doInBackground(Void... nothing) {
			String response = "";
			DefaultHttpClient client = new DefaultHttpClient();
			Log.wtf("url", url);
			Log.wtf("json", json_data);

			try {
				HttpResponse execute = null;
				switch (type) {
				case TYPE_POST:
					HttpPost post = new HttpPost(url);
					StringEntity se = new StringEntity(json_data);
					post.setEntity(se);
					// sets a request header so the page receiving the request
					// will know what to do with it
					post.setHeader("Accept", "application/json");
					post.setHeader("Content-type", "application/json");

					execute = client.execute(post);
					break;
				case TYPE_GET:
					HttpGet get = new HttpGet(url);
					execute = client.execute(get);
					break;
				}
				InputStream content = execute.getEntity().getContent();

				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(content));
				String s = "";
				while ((s = buffer.readLine()) != null) {
					response += s;
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				Log.wtf(TAG + "-onPostExecute result:", result);
				if (result != null && !"".equals(result)) {
					// //Log.v("results from server", result);
					listener.onCompleteTask(result, requestCode);
				} else {
					// TODO: no response from server handle
				}
			} catch (Exception e) {
				Log.wtf(TAG + "-Exception", e.getMessage());
			}
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.wtf(TAG, "Received intent");
		String url = intent.getExtras().getString(HttpService.KEY_URL_PATH);
		if (url == null || "".equals(url)) {
			throw new IllegalStateException(
					"Need to init the following extra data");
		}
		String json = intent.getExtras().getString(HttpService.KEY_JSON);
		int type = intent.getExtras().getInt(HttpService.KEY_REQUEST_CODE);
		HttpService.StartPostCommunication asyncTask = new HttpService.StartPostCommunication(
				url, json, this, type, HttpService.TYPE_POST);
		asyncTask.execute();
	}

	@Override
	public void onCompleteTask(String response, int requestCode) {
		JSONObject serverResponseJson = null;
		try {
			serverResponseJson = new JSONObject(response);
		} catch (JSONException error) {

		}

		try {
			if (serverResponseJson.has("status")
					&& Common.SUCCESS.equals(serverResponseJson
							.getString("status"))) {
				switch(requestCode){
				case SYNC_FRIENDS_REQUEST_CODE:
					ContactFinder.completeSync(response, getApplicationContext());
					break;
				default:
					startCallBack(response, requestCode);
					break;
				}
			} else {
				Log.wtf(TAG, response);
			}
		} catch (JSONException e2) {
			// TODO: parse failed, add to crashlytics
			return;
		}
	}

	private void startCallBack(String response, int requestCode) {
		Intent broadcastIntent = new Intent();
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.setAction(CallbackReceiver.RECEIVER_CODE);
		Bundle data = new Bundle();
		data.putString(KEY_JSON, response);
		data.putInt(KEY_REQUEST_CODE, requestCode);
		broadcastIntent.putExtras(data);
		sendBroadcast(broadcastIntent);
	}
}
