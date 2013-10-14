package com.arrownock.opensource.arrownockers.topic;

import java.util.Date;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.utils.AnUtils;

public class ImageActivity extends Activity {

	private byte[] imageData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image);

		imageData = getIntent().getByteArrayExtra("imageData");
		Bitmap bm = BitmapFactory.decodeByteArray(imageData, 0,
				imageData.length);
		ImageView iv = (ImageView) findViewById(R.id.iv);
		iv.setImageBitmap(bm);
	}

	public void onSaveClick(View view) {
		Bitmap bm = BitmapFactory.decodeByteArray(imageData, 0,
				imageData.length);
		String title = "Arrownockers图片--" + AnUtils.getTimeString(new Date());

		String url = MediaStore.Images.Media.insertImage(getContentResolver(),
				bm, title, "来自Arrownockers");

		if (url != null) {
			AnUtils.showToast(ImageActivity.this, "保存成功", Gravity.CENTER, true);
		} else {
			AnUtils.showToast(ImageActivity.this, "操作失败", Gravity.CENTER, true);
		}
	}
}
