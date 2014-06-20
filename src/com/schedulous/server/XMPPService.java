package com.schedulous.server;

import java.lang.ref.WeakReference;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class XMPPService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		Log.v("XMPPService", "onBind()");
		XMPPConnectionManager.getInstance(this);
		return new LocalBinder<XMPPService>(this);
	}

	public class LocalBinder<S> extends Binder {
		private final WeakReference<S> mService;

		public LocalBinder(final S service) {
			mService = new WeakReference<S>(service);
		}

		public S getService() {
			return mService.get();
		}
	}


	@Override
	public void onDestroy() {
		Log.v("XMPPService", "onDestroy");
		try {
			XMPPConnectionManager.getInstance(getApplicationContext()).logout();
		} catch (IllegalStateException e) {

		}
		super.onDestroy();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {
		return START_NOT_STICKY;
	}

	public static Intent getXMPPIntent(Context context) {
		Intent intent = new Intent(context, XMPPService.class);
		return intent;
	}
}