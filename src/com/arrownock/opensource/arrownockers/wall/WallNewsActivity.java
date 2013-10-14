package com.arrownock.opensource.arrownockers.wall;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.loopj.android.image.SmartImageView;

public class WallNewsActivity extends Activity {

	private String title;
	private String content;
	private String imageURLString;

	private TextView tvTitle;
	private TextView tvContent;
	private SmartImageView sivTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_wall_news);

		initView();
	}

	private void initView() {
		title = getIntent().getStringExtra("title");
		content = getIntent().getStringExtra("content");
		imageURLString = getIntent().getStringExtra("imageURLString");

		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvContent = (TextView) findViewById(R.id.tv_content);
		sivTitle = (SmartImageView) findViewById(R.id.iv_title);

		DisplayMetrics metrics = WallNewsActivity.this.getResources()
				.getDisplayMetrics();
		int width = metrics.widthPixels;
		int result = width;
		LayoutParams lParams = sivTitle.getLayoutParams();
		lParams.width = result;
		lParams.height = result;
		sivTitle.setLayoutParams(lParams);

		content = content.replace("\\n", "\n");
		title = title.replace("\\n", "\n");

		tvTitle.setText(title);
		tvContent.setText(content);
		sivTitle.setImageUrl(imageURLString);
	}
}
