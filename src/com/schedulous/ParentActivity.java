package com.schedulous;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.schedulous.utility.server.XMPPConnectionManager;
import com.schedulous.utility.server.XMPPService;
import com.schedulous.utility.server.XMPPService.LocalBinder;

public abstract class ParentActivity extends Activity{

	boolean mBound;
	@SuppressWarnings("unused")
	private XMPPService xmppService;
	public XMPPConnectionManager connectionManager;
	private final ServiceConnection mConnection = new ServiceConnection() {
		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(final ComponentName name,
				final IBinder service) {
			xmppService = ((LocalBinder<XMPPService>) service).getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			xmppService = null;
			mBound = false;
		}
	};


	@Override
	protected void onStart() {
		connectionManager = XMPPConnectionManager.get(this);
		doBindService();
		super.onStart();
	}

	@Override
	protected void onStop() {
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		super.onStop();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	void doBindService() {
		bindService(XMPPService.getXMPPIntent(this), mConnection,
				Context.BIND_AUTO_CREATE);
	}

	void doUnbindService() {
		unbindService(mConnection);
	}
}
