package com.arrownock.opensource.arrownockers.wall;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.MRMWrapper;
import com.arrownock.exception.ArrownockException;
import com.arrownock.mrm.MRMJSONResponseHandler;
import com.loopj.android.image.SmartImageView;

public class PostAddActivity extends SherlockActivity {

	private String parentId = null;
	private byte[] imageData = null;
	private SmartImageView ivContent = null;
	private EditText tvContent = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_add);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		Bundle bundle = new Bundle();
		bundle = this.getIntent().getExtras();
		parentId = bundle.getString("parentId");

		ivContent = (SmartImageView) findViewById(R.id.iv_content);
		tvContent = (EditText) findViewById(R.id.tv_content);
	}

	private void ShowPickDialog() {
		new AlertDialog.Builder(this)
				.setTitle("选择来源")
				.setNegativeButton("相册", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent(Intent.ACTION_PICK, null);
						intent.setDataAndType(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								"image/*");
						startActivityForResult(intent, 1);

					}
				})
				.setPositiveButton("拍照", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
								.fromFile(new File(Environment
										.getExternalStorageDirectory(),
										"pickedImage.png")));
						startActivityForResult(intent, 2);
					}
				}).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case 1:
			if (data == null)
				return;
			startPhotoZoom(data.getData());
			break;
		case 2:
			if (resultCode != -1) {
				return;
			}
			File temp = new File(Environment.getExternalStorageDirectory()
					+ "/pickedImage.png");
			startPhotoZoom(Uri.fromFile(temp));
			break;
		case 3:
			if (data == null)
				return;
			Bundle extras = data.getExtras();
			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] b = stream.toByteArray();

				imageData = b;
				Bitmap bm = BitmapFactory.decodeByteArray(b, 0, b.length);
				ivContent.setImageBitmap(bm);
				DisplayMetrics metrics = getApplicationContext().getResources()
						.getDisplayMetrics();
				int width = metrics.widthPixels;
				int result = width - 10 * 2;
				LayoutParams lParams = ivContent.getLayoutParams();
				lParams.width = result;
				lParams.height = result;
				ivContent.setLayoutParams(lParams);
			}
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 3);
	}

	public void willPickImage(View view) {
		ShowPickDialog();
	}

	public void createPost(View view) {
		if (tvContent.getText() == null
				|| tvContent.getText().toString().trim().equals("")) {
			AnUtils.showToast(PostAddActivity.this, "内容不能为空", Gravity.CENTER,
					true);
			return;
		}

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (imageData != null) {
					try {

						// 如果有图片，首先上传图片
						// 参数的具体格式请参考文档详细说明
						MRMWrapper.getMRM(getBaseContext()).uploadData(
								getBaseContext(), "images/create", null,
								imageData, "png", new MRMJSONResponseHandler() {

									@Override
									public void onFailure(Throwable e,
											JSONObject response) {
										AnUtils.showToast(PostAddActivity.this,
												"发送图片失败", Gravity.CENTER, true);
									}

									@Override
									public void onSuccess(int statusCode,
											JSONObject response) {
										createNoImagePost(response);
									}

								});
					} catch (ArrownockException e) {
						AnUtils.showToast(PostAddActivity.this,
								"发生错误: " + e.getMessage(), Gravity.CENTER, true);
					}

				} else {
					// 如果没有图片，直接创建Post
					// 参数的具体格式请参考文档详细说明
					createNoImagePost(null);
				}

			}
		});

		thread.start();
	}

	private void createNoImagePost(JSONObject response) {
		try {
			JSONObject params = new JSONObject();
			params.put("parentType", "Wall");
			params.put("parentId", parentId);
			params.put("content", tvContent.getText().toString());

			JSONObject customFields = new JSONObject();
			if (response != null) {
				JSONObject responseJsonObject = response
						.getJSONObject("response");
				JSONObject image = responseJsonObject.getJSONObject("image");
				String imageId = image.getString("id");
				String imageURLString = image.getString("url");
				params.put("imageId", imageId);
				customFields.put("imageURL", imageURLString);
			}
			customFields.put("username", AnUtils.getCurrentUsername());
			params.put("customFields", customFields);

			// 创建Post
			// 参数的具体格式请参考文档详细说明
			MRMWrapper.getMRM(getBaseContext()).sendPostRequest(
					getBaseContext(), "posts/create", params,
					new MRMJSONResponseHandler() {

						@Override
						public void onFailure(Throwable e, JSONObject response) {
							AnUtils.showToast(PostAddActivity.this, "新建Post失败",
									Gravity.CENTER, true);
						}

						@Override
						public void onSuccess(int statusCode,
								JSONObject response) {
							finish();
						}

					});
		} catch (Exception e) {
			AnUtils.showToast(PostAddActivity.this, "发生错误: " + e.getMessage(),
					Gravity.CENTER, true);
		}
	}

	public void cancel(View view) {
		finish();
	}
}