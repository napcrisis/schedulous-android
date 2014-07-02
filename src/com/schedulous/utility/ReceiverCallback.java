package com.schedulous.utility;

import android.os.Bundle;


public interface ReceiverCallback extends Callback {
	/*
	 * (non-Javadoc)
	 * @see com.schedulous.utility.Callback#doAction(android.os.Bundle)
	 * UI Action only!
	 */
	public void doAction(Bundle data, String action);
}
