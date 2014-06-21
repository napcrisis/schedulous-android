package com.schedulous.onboarding;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.schedulous.R;

public class LoginActivity extends Activity implements LoginUI {
	private static final String TAG = LoginActivity.class.getSimpleName();

	private OnClickListener button_listener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			boolean showMobileField = true;
			switch (view.getId()) {
			case R.id.ib_back:
				showMobileField = false;
			case R.id.tv_register:
				register.startAnimation(showMobileField ? mFadeOut : mFadeIn);
				register.setVisibility(showMobileField ? View.GONE
						: View.VISIBLE);
				mobile_field_container.startAnimation(showMobileField ? mFadeIn
						: mFadeOut);
				mobile_field_container
						.setVisibility(showMobileField ? View.VISIBLE
								: View.GONE);
				bottom_text.setVisibility(showMobileField ? View.GONE
						: View.VISIBLE);
				// TODO: set or lose focus of keyboard
				break;
			case R.id.tv_send:
				if (isVerifyMode) {
					controller.verify_number(mobile_field.getText().toString());
				} else {
					String country_code = mobile_country_code.getText()
							.toString();
					String mobile_number = mobile_field.getText().toString();

					controller.send_number(country_code, mobile_number);

					Toast.makeText(
							LoginActivity.this,
							"Sending message to " + country_code + " "
									+ mobile_number, Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
	};
	private OnKeyListener mobile_field_listener = new OnKeyListener() {
		@Override
		public boolean onKey(View view, int arg1, KeyEvent arg2) {
			boolean showSendButton = ((EditText) view).getText().length() > 7;
			if (isVerifyMode) {
				showSendButton = ((EditText) view).getText().length() > 3;
			}
			if (!send.isShown() && showSendButton)
				send.startAnimation(mFadeIn);
			send.setVisibility(showSendButton ? View.VISIBLE : View.INVISIBLE);
			return false;
		}
	};
	private LoginController controller;

	private Animation mFadeIn;
	private Animation mFadeOut;
	private boolean isVerifyMode;

	private TextView register;
	private TextView mobile_country_code;
	private EditText mobile_field;
	private TextView send;
	private TextView bottom_text;
	private ImageButton back;
	private LinearLayout mobile_field_container;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		Log.v(TAG, "oncreate");
		setContentView(R.layout.onboard_login_activity);

		controller = new LoginController(getApplicationContext(),
				LoginActivity.this);
		isVerifyMode = false;

		if (controller.onCreateAuthCheck()) {
			return;
		}

		mobile_field_container = (LinearLayout) findViewById(R.id.ll_mobile_field_container);
		register = (TextView) findViewById(R.id.tv_register);
		mobile_country_code = (TextView) findViewById(R.id.tv_mobile_country_code);
		mobile_field = (EditText) findViewById(R.id.et_mobile_number);
		send = (TextView) findViewById(R.id.tv_send);
		bottom_text = (TextView) findViewById(R.id.tv_bottom_slider_text);
		back = (ImageButton) findViewById(R.id.ib_back);

		register.setOnClickListener(button_listener);
		back.setOnClickListener(button_listener);
		send.setOnClickListener(button_listener);
		mobile_field.setOnKeyListener(mobile_field_listener);

		mFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		mFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
	}

	@Override
	public void completeSending() {
		mobile_country_code.setVisibility(View.GONE);
		mobile_field.setText("");
		mobile_field.setHint(R.string.verification_code);
		send.setText(R.string.verify);

		Toast.makeText(this, "Awaiting message", Toast.LENGTH_LONG).show();
		isVerifyMode = true;
		// TODO: loading icon & start broadcast receiver
	}

	@Override
	protected void onPause() {
		controller.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		controller.onResume();
	}

}
