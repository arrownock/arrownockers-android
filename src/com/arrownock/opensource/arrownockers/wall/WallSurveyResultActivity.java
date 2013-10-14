package com.arrownock.opensource.arrownockers.wall;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.MRMWrapper;
import com.arrownock.mrm.MRMJSONResponseHandler;

@SuppressLint("SetJavaScriptEnabled")
public class WallSurveyResultActivity extends Activity {

	private String postId;
	private String title;
	private ArrayList<String> choices;
	private WebView webView;

	private int[] counts = { 0, 0, 0, 0 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_wall_survey_result);

		postId = getIntent().getStringExtra("postId");
		title = getIntent().getStringExtra("title");
		choices = getIntent().getStringArrayListExtra("choices");

		initView();

		retriveComments(null);
	}

	private void initView() {
		webView = (WebView) findViewById(R.id.webview);
		WebSettings wSet = webView.getSettings();
		wSet.setBuiltInZoomControls(true);
		wSet.setJavaScriptEnabled(true);
		wSet.setDefaultTextEncodingName("utf-8");

		webView.loadUrl("file:///android_asset/chart/chart.html");
	}

	private void retriveComments(final String timeString) {
		System.out.println("retriveComments" + timeString);

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					JSONObject params = new JSONObject();
					params.put("parentId", postId);
					params.put("sort", "-created_at");
					params.put("pagesize", 100);
					if (timeString != null) {
						params.put("where", "{\"created_at\":{\"$lt\":\""
								+ timeString + "\"}}");
					}

					MRMWrapper.getMRM(getBaseContext()).sendPostRequest(
							getBaseContext(), "comments/search", params,
							new MRMJSONResponseHandler() {

								@Override
								public void onFailure(Throwable arg0,
										JSONObject arg1) {
									AnUtils.showToast(
											WallSurveyResultActivity.this,
											"操作失败", Gravity.CENTER, true);
								}

								@Override
								public void onSuccess(int arg0, JSONObject arg1) {
									try {
										JSONArray commentJsonArray = arg1
												.getJSONObject("response")
												.getJSONArray("comments");
										int count = commentJsonArray.length();
										if (count > 0) {
											for (int i = 0; i < count; i++) {
												JSONObject comment = commentJsonArray
														.getJSONObject(i);
												int rating = comment
														.getInt("rating");
												counts[rating] = counts[rating] + 1;
											}
											String newTimeString = (commentJsonArray
													.getJSONObject(count - 1))
													.getString("created_at");
											retriveComments(newTimeString);
										} else {
											System.out.println("showResult");
											showResult();
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

							});
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		thread.start();

	}

	private void showResult() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				try {
					JSONObject data = new JSONObject();
					JSONArray choicesJsonArray = new JSONArray(choices);
					JSONArray percentagesJsonArray = new JSONArray();

					for (int i = 0; i < counts.length; i++) {
						percentagesJsonArray.put(counts[i]);
					}
					data.put("type", "pie");
					data.put("title", title);
					data.put("choices", choicesJsonArray);
					data.put("percentages", percentagesJsonArray);

					String argString = data.toString();
					String newString = argString.replace("'", "’");
					webView.loadUrl("javascript:initANData('" + newString
							+ "')");
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

	}

}
