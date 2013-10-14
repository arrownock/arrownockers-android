package com.arrownock.opensource.arrownockers.push;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.arrownock.opensource.arrownockers.R;

public class PushWelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_push_welcome);
	}

	public void onSendButtonClick(View view) {
		showFeatureActivity(PushSendActivity.class);
	}

	public void onSystemButtonClick(View view) {
		showFeatureActivity(PushDetailsActivity.class);
	}

	public void onCaseButtonClick(View view) {
		showFeatureActivity(PushCaseActivity.class);
	}

	private void showFeatureActivity(final Class<?> activityClass) {
		Intent intent = new Intent(PushWelcomeActivity.this, activityClass);
		startActivity(intent);
	}
}
