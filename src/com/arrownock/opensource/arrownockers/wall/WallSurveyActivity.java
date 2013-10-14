package com.arrownock.opensource.arrownockers.wall;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.MRMWrapper;
import com.arrownock.mrm.MRMJSONResponseHandler;
import com.loopj.android.image.SmartImageView;

public class WallSurveyActivity extends Activity {

	private String postId;

	private String title;
	private String content;
	private String imageURLString;
	private ArrayList<String> choices;

	private TextView tvTitle;
	private TextView tvContent;
	private SmartImageView sivTitle;
	private RadioGroup rGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_wall_survey);

		postId = getIntent().getStringExtra("postId");

		initView();
	}

	private void initView() {
		title = getIntent().getStringExtra("title");
		content = getIntent().getStringExtra("content");
		imageURLString = getIntent().getStringExtra("imageURLString");
		choices = getIntent().getStringArrayListExtra("choices");

		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvContent = (TextView) findViewById(R.id.tv_content);
		sivTitle = (SmartImageView) findViewById(R.id.iv_title);
		rGroup = (RadioGroup) findViewById(R.id.rGroup);

		tvTitle.setText(title);
		tvContent.setText(content);
		sivTitle.setImageUrl(imageURLString);
		for (int i = 0; i < choices.size(); i++) {
			RadioButton rbtn = (RadioButton) rGroup.getChildAt(i);
			rbtn.setText(choices.get(i));
		}
	}

	public void onSubmitButtonClick(View view) {
		int checkedId = rGroup.getCheckedRadioButtonId();
		RadioButton rbtn = (RadioButton) rGroup.findViewById(checkedId);
		int index = rGroup.indexOfChild(rbtn);

		createComment(index);
	}

	public void onCheckButtonClick(View view) {
		Intent intent = new Intent(this, WallSurveyResultActivity.class);
		intent.putExtra("postId", postId);
		intent.putExtra("title", title);
		intent.putStringArrayListExtra("choices", choices);

		startActivity(intent);
	}

	private void createComment(final int index) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					JSONObject params = new JSONObject();
					params.put("parentType", "Post");
					params.put("parentId", postId);
					params.put("rating", index);
					MRMWrapper.getMRM(getBaseContext()).sendPostRequest(
							getBaseContext(), "comments/create", params,
							new MRMJSONResponseHandler() {

								@Override
								public void onFailure(Throwable arg0,
										JSONObject arg1) {
									String message = null;
									try {
										message = arg1
												.getJSONObject("response")
												.getString("message");
									} catch (Exception e) {
										e.printStackTrace();
									}
									AnUtils.showToast(WallSurveyActivity.this,
											"操作失败请重试\n" + message,
											Gravity.CENTER, true);
								}

								@Override
								public void onSuccess(int arg0, JSONObject arg1) {
									AnUtils.showToast(WallSurveyActivity.this,
											"操作成功", Gravity.CENTER, true);
								}

							});
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		thread.start();
	}
}
