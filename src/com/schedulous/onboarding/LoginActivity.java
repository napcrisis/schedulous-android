package com.schedulous.onboarding;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.schedulous.R;

public class LoginActivity extends Activity implements LoginUI {
	private static final String SINGAPORE_CC_PREFIX = "+65";
	private OnClickListener button_listener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.tv_action_button:
				if (isVerifyMode) {
					controller.verify_number(user_input.getText().toString());
				} else {
					String mobile_number = user_input.getText().toString();
					try {
						controller.send_number(SINGAPORE_CC_PREFIX
								+ mobile_number);
						Toast.makeText(
								LoginActivity.this,
								"Sending message to " + SINGAPORE_CC_PREFIX
										+ " " + mobile_number,
								Toast.LENGTH_LONG).show();
					} catch (Exception e) {
						Toast.makeText(LoginActivity.this, e.getMessage(),
								Toast.LENGTH_LONG).show();
					}
				}
				view.setEnabled(false);
				break;
			}
		}
	};
	private LoginController controller;
	private boolean isVerifyMode;

	private EditText user_input;
	private TextView next;
	private TextView title;
	private ImageView logo;
	private LinearLayout interfaceContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.onboard_login_activity);
		controller = new LoginController(getApplicationContext(),
				LoginActivity.this);
		isVerifyMode = false;

		if (controller.onCreateAuthCheck()) {
			return;
		}

		interfaceContainer = (LinearLayout) findViewById(R.id.ll_interface_container);
		user_input = (EditText) findViewById(R.id.et_user_input);
		next = (TextView) findViewById(R.id.tv_action_button);
		title = (TextView) findViewById(R.id.tv_title);
		logo = (ImageView) findViewById(R.id.tv_logo);

		next.setOnClickListener(button_listener);

		Typeface deliciousheavy = Typeface.createFromAsset(
				getApplicationContext().getAssets(),
				"fonts/Delicious-Heavy.otf");
		Typeface deliciousroman = Typeface.createFromAsset(
				getApplicationContext().getAssets(),
				"fonts/Delicious-Roman.otf");

		title.setTypeface(deliciousheavy);
		user_input.setTypeface(deliciousroman);
		next.setTypeface(deliciousroman);
	}

	@Override
	public void completeSending() {
		user_input.setText("");
		user_input.setHint(R.string.verification_code);

		Toast.makeText(this, "Awaiting message", Toast.LENGTH_LONG).show();
		isVerifyMode = true;
		next.setEnabled(true);
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

	@Override
	public void receivedVerificationCode(String code) {
		user_input.setText(code);
		next.performClick();
	}

	public static void ImageViewAnimatedChange(Context c, final ImageView v,
			final int new_image) {
		final Animation anim_out = AnimationUtils.loadAnimation(c,
				android.R.anim.fade_out);
		final Animation anim_in = AnimationUtils.loadAnimation(c,
				android.R.anim.fade_in);
		anim_out.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				v.setImageResource(new_image);
				v.setLayoutParams(new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, 300));
				anim_in.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
					}
				});
				v.startAnimation(anim_in);
			}
		});
		v.startAnimation(anim_out);
	}
}
