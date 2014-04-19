package com.arrownock.opensource.arrownockers.push;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.CustomReceiver;
import com.arrownock.opensource.arrownockers.utils.CustomReceiver.OnPushArrived;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;


// 模拟实际使用场景，由用户申请随机验证码，一分钟有效
public class PushCaseActivity extends Activity implements OnPushArrived,
		OnFocusChangeListener {

	private ScrollView sv;
	private Button requireButton;
	private TextView timeCounterTextView;
	private EditText codeTextView;
	private TextView receivedTextView;
	private boolean reset = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_push_case);

		initView();

		CustomReceiver.setPushCaseActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		CustomReceiver.setPushCaseActivity(null);
	}

	private void initView() {
		sv = (ScrollView) findViewById(R.id.sv);
		requireButton = (Button) findViewById(R.id.btn_push_case_require);
		timeCounterTextView = (TextView) findViewById(R.id.tv_push_case_time_counter);
		codeTextView = (EditText) findViewById(R.id.tv_push_case_code);
		receivedTextView = (TextView) findViewById(R.id.tv_received_code);

		codeTextView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				sv.scrollTo(0, sv.getHeight());
			}
		});
	}

	public void onRequireButtonClick(View view) {
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams("id",
				AnUtils.getCurrentUsername());
		client.post(AnUtils.requireVerifyCodeEndpoint, params,
				new JsonHttpResponseHandler() {

					@Override
					public void onSuccess(JSONObject response) {
						requireButton.setEnabled(false);
						requireButton.setTextColor(Color.GRAY);
						timeCounterTextView.setVisibility(View.VISIBLE);

						Thread thread = new Thread(new Runnable() {

							@Override
							public void run() {
								int max = 60;

								while (max > 0) {
									if (reset) {
										reset = false;
										break;
									}

									final String message = max
											+ " 秒后需要重新获取新验证码";
									runOnUiThread(new Runnable() {
										public void run() {
											timeCounterTextView
													.setText(message);
										}
									});

									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}

									max--;
								}

								runOnUiThread(new Runnable() {
									public void run() {
										timeCounterTextView.setText("");
										timeCounterTextView
												.setVisibility(View.INVISIBLE);
										requireButton.setEnabled(true);
										requireButton.setTextColor(Color.BLACK);
									}
								});
							}
						});

						thread.start();
					}

					@Override
					public void onFailure(Throwable e, JSONObject errorResponse) {
						AnUtils.showToast(PushCaseActivity.this, "操作失败",
								Gravity.CENTER, true);
					}

				});

	}

	public void onVerifyButtonClick(View view) {
		String code = codeTextView.getText().toString();
		if (code.trim().length() > 0) {
			AsyncHttpClient client = new AsyncHttpClient();
			RequestParams params = new RequestParams("id",
					AnUtils.getCurrentUsername());
			params.put("auth_code", code);
			client.post(AnUtils.verifyEndpoint, params,
					new JsonHttpResponseHandler() {

						@Override
						public void onSuccess(JSONObject response) {
							AnUtils.showToast(PushCaseActivity.this, "验证成功",
									Gravity.CENTER, true);
							reset = true;

							runOnUiThread(new Runnable() {

								public void run() {
									timeCounterTextView.setText("");
									timeCounterTextView
											.setVisibility(View.INVISIBLE);
									requireButton.setEnabled(true);
									requireButton.setTextColor(Color.BLACK);
								}
							});
						}

						@Override
						public void onFailure(Throwable e,
								JSONObject errorResponse) {
							AnUtils.showToast(PushCaseActivity.this, "验证失败",
									Gravity.CENTER, true);
						}

					});
		}
	}

	@Override
	public void onPushSaved(String code) {
		receivedTextView.setText(code);
	}

	@Override
	public void onPushSaved() {
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
	}

}
