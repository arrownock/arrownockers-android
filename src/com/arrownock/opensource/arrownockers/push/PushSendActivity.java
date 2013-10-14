package com.arrownock.opensource.arrownockers.push;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.push.PushDetailsEntity.EntityType;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.CustomReceiver;
import com.arrownock.opensource.arrownockers.utils.CustomReceiver.OnPushArrived;
import com.arrownock.opensource.arrownockers.utils.DBManager;
import com.arrownock.opensource.arrownockers.utils.DBManager.Push;
import com.arrownock.opensource.arrownockers.utils.MapActivity;
import com.arrownock.opensource.arrownockers.utils.MapCallback;
import com.arrownock.opensource.arrownockers.utils.MapCallback.OnLocationUpdated;
import com.baidu.location.BDLocation;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


// 发送上行Push消息，以当前用户的username作为channel，将Push消息发送给自己
public class PushSendActivity extends Activity implements OnClickListener,
		OnLongClickListener, OnTouchListener, OnLocationUpdated, OnPushArrived {

	private Button mBtnSend;
	private Button mBtnImage;
	private Button mBtnAudio;
	private EditText mEditTextContent;
	private ListView mListView;
	private PushDetailsListAdapter mAdapter;
	private List<PushDetailsEntity> mDataArrays;

	private MediaRecorder recorder;
	private boolean recording = false;
	private boolean willSendLocation = false;
	private List<String> channels;
	private boolean alive = false;
	private ProgressBar pBar;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_push_send);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		channels = new ArrayList<String>();
		if (AnUtils.getCurrentUsername() != null) {
			channels.add(AnUtils.getCurrentUsername());
		}

		initView();
		initData();

		recorder = null;

		MapCallback.setPushSendActivity(PushSendActivity.this);
		CustomReceiver.setPushSendActivity(PushSendActivity.this);
	}

	public void addPush(boolean income, String dataType, String message,
			byte[] binary, double latitude, double longitude) {
		Push push = new Push();
		push.type = "type";
		push.dataType = dataType;
		push.messageId = "messageId";
		push.batchNumber = "batchNumber";
		push.income = false;
		push.time = AnUtils.getTimeString(new Date());
		if (dataType.equals("text")) {
			push.binary = null;
			push.message = message;
		} else if (dataType.equals("image") || dataType.equals("audio")) {
			push.binary = binary;
			push.message = null;
		} else if (dataType.equals("location")) {
			push.latitude = latitude;
			push.longitude = longitude;
		}

		push.title = AnUtils.getCurrentUsername();
		push.status = "sent";

		DBManager.addPush(push);
	}

	public void addNewMessage(final boolean income, String message) {
		PushDetailsEntity entity = new PushDetailsEntity();
		entity.dateString = AnUtils.getTimeString(new Date());
		entity.isComMsg = income;
		entity.entityType = EntityType.ET_TEXT;
		entity.text = message;

		mDataArrays.add(entity);

		refresh(income);

		if (!income) {
			addPush(income, "text", message, null, 0, 0);
		}
	}

	public void addNewLocation(final boolean income, double lat, double lon) {
		PushDetailsEntity entity = new PushDetailsEntity();
		entity.dateString = AnUtils.getTimeString(new Date());
		entity.isComMsg = income;
		entity.entityType = EntityType.ET_LOCATION;
		entity.text = "Location...";
		entity.latitude = lat;
		entity.longitude = lon;

		mDataArrays.add(entity);

		refresh(income);

		if (!income) {
			addPush(income, "location", "Location...", null, lat, lon);
		}
	}

	public void addNewImage(boolean income, byte[] bytes) {
		PushDetailsEntity entity = new PushDetailsEntity();
		entity.dateString = AnUtils.getTimeString(new Date());
		entity.isComMsg = income;
		entity.entityType = EntityType.ET_IMAGE;
		entity.imageBytes = bytes;

		mDataArrays.add(entity);

		refresh(income);

		if (!income) {
			addPush(income, "image", null, bytes, 0, 0);
		}
	}

	public void addNewAudio(boolean income, byte[] bytes) {
		PushDetailsEntity entity = new PushDetailsEntity();
		entity.dateString = AnUtils.getTimeString(new Date());
		entity.isComMsg = income;
		entity.entityType = EntityType.ET_AUDIO;
		entity.audioBytes = bytes;

		mDataArrays.add(entity);

		refresh(income);

		if (income) {
			playAudio(bytes);
		}

		if (!income) {
			addPush(income, "audio", null, bytes, 0, 0);
		}
	}

	public void playAudio(byte[] bytes) {
		String pathString = Environment.getExternalStorageDirectory().getPath()
				+ "/receivedPushAudio.m4a";
		try {
			File file = new File(pathString);
			if (!file.exists()) {
				file.createNewFile();
			} else {
				file.delete();
			}
			FileOutputStream os;
			os = new FileOutputStream(file, false);
			os.write(bytes);
			os.close();

			MediaPlayer mp = new MediaPlayer();
			final FileInputStream fis = new FileInputStream(pathString);

			MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					mp.release();
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			mp.setOnCompletionListener(listener);

			mp.setDataSource(fis.getFD());
			mp.prepare();
			mp.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startRecording() {
		if (recording == true)
			return;
		try {
			if (recorder == null) {
				String pathString = Environment.getExternalStorageDirectory()
						.getPath() + "/recordedPushAudio.m4a";
				recorder = new MediaRecorder();
				recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
				recorder.setOutputFile(pathString);
				recorder.prepare();

				recorder.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		recording = true;
	}

	public void stopRecording() {
		if (!recording) {
			return;
		}
		try {
			String pathString = Environment.getExternalStorageDirectory()
					.getPath() + "/recordedPushAudio.m4a";
			File file = new File(pathString);
			FileInputStream iStream;

			recorder.stop();
			recording = false;
			recorder.reset();
			recorder.release();
			recorder = null;
			iStream = new FileInputStream(file);

			sendAudio(IOUtils.toByteArray(iStream));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void refresh(final boolean income) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!income) {
					mEditTextContent.setText("");
				}

				mAdapter.notifyDataSetChanged();
				mListView.setSelection(mListView.getCount() - 1);
			}
		});
	}

	public void initView() {
		pBar = (ProgressBar) findViewById(R.id.progressBar);

		mListView = (ListView) findViewById(R.id.listview);
		mBtnSend = (Button) findViewById(R.id.btn_send);
		mBtnSend.setOnClickListener(PushSendActivity.this);
		mBtnImage = (Button) findViewById(R.id.btn_sendImage);
		mBtnImage.setOnClickListener(PushSendActivity.this);
		mBtnAudio = (Button) findViewById(R.id.btn_sendAudio);
		mBtnAudio.setOnLongClickListener(PushSendActivity.this);
		mBtnAudio.setOnTouchListener(PushSendActivity.this);

		mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);

		mDataArrays = new ArrayList<PushDetailsEntity>();
		mAdapter = new PushDetailsListAdapter(this, mDataArrays);
		mListView.setAdapter(mAdapter);
	}

	public void initData() {
		List<Push> pushList = DBManager.getPushes(AnUtils.getCurrentUsername());
		if (pushList == null) {
			return;
		}

		mDataArrays.clear();
		for (Push push : pushList) {
			PushDetailsEntity pushDetailsEntity = new PushDetailsEntity();
			pushDetailsEntity.dateString = push.time;
			pushDetailsEntity.isComMsg = push.income;
			String dataType = push.dataType;
			if (dataType.equals("image")) {
				pushDetailsEntity.entityType = EntityType.ET_IMAGE;
				pushDetailsEntity.imageBytes = push.binary;
			} else if (dataType.equals("audio")) {
				pushDetailsEntity.entityType = EntityType.ET_AUDIO;
				pushDetailsEntity.audioBytes = push.binary;
			} else if (dataType.equals("text")) {
				pushDetailsEntity.entityType = EntityType.ET_TEXT;
				pushDetailsEntity.text = push.message;
			} else if (dataType.equals("location")) {
				pushDetailsEntity.entityType = EntityType.ET_LOCATION;
				pushDetailsEntity.text = push.message;
				pushDetailsEntity.latitude = push.latitude;
				pushDetailsEntity.longitude = push.longitude;
			}

			mDataArrays.add(pushDetailsEntity);
		}
		mAdapter.notifyDataSetChanged();
		mListView.setSelection(mListView.getCount() - 1);
		DBManager.setPushRead(AnUtils.getCurrentUsername());
	}

	public void handlePushMsg(String payload) {
		try {
			JSONObject jsonObject = new JSONObject(payload);
			jsonObject = jsonObject.getJSONObject("android");
			String alertString = jsonObject.getString("alert");
			String dataType = null;
			if (jsonObject.has("type")) {
				if (jsonObject.getString("type").equals("rich_push")) {
					dataType = "rich";
				}
			} else {
				dataType = jsonObject.getString("dataType");
			}

			TextView titleTextView = (TextView) this
					.findViewById(R.id.tv_title);
			titleTextView.setText("title");

			if (dataType.equals("text")) {
				addNewMessage(true, alertString);
			} else if (dataType.equals("image")) {
				byte[] byteArray = Base64.decode(alertString, Base64.DEFAULT);
				addNewImage(true, byteArray);
			} else if (dataType.equals("audio")) {
				byte[] byteArray = Base64.decode(alertString, Base64.DEFAULT);
				addNewAudio(true, byteArray);
			} else if (dataType.equals("rich")) {
				addNewMessage(true, alertString);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		alive = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		alive = false;
		MapCallback.setPushSendActivity(null);
	}

	@Override
	protected void onStop() {
		super.onStop();
		alive = false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send:
			sendText();
			break;
		case R.id.btn_sendImage:
			ShowPickDialog();
			break;
		}
	}

	public void sendPush(String dataType, String contentString,
			double latitude, double longitude) {
		AsyncHttpClient client = new AsyncHttpClient();
		JSONObject jsonObject = new JSONObject();
		RequestParams params = new RequestParams();
		try {
			if (dataType.equals("location")) {
				jsonObject.put("alert", "new location");
				addNewLocation(false, latitude, longitude);
			} else {
				jsonObject.put("alert", contentString);
			}

			jsonObject.put("badge", 1);
			jsonObject.put("vibrate", true);
			jsonObject.put("title",
					AnUtils.spf.getString("username", "username"));
			jsonObject.put("icon", "default");
			jsonObject.put("sound", "default");

			JSONObject payloadJson = new JSONObject();
			payloadJson.put("android", jsonObject);

			JSONObject typeJson = new JSONObject();
			typeJson.put("dataType", dataType);

			if (dataType.equals("location")) {
				typeJson.put("latitude", latitude);
				typeJson.put("longitude", longitude);
			}
			payloadJson.put("custom_data", typeJson);

			params.put("payload", payloadJson.toString());
			if (channels.size() == 0) {
				AnUtils.showToast(PushSendActivity.this, "发送失败，未注册push服务器",
						Gravity.CENTER, true);
				return;
			}
			String channelsString = StringUtils.join(channels, ",");
			params.put("channel", channelsString);

			client.post(AnUtils.pushEndpoint, params,
					new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
							AnUtils.showToast(PushSendActivity.this, "发送成功",
									Gravity.CENTER, alive);
						}

						@Override
						public void onFailure(Throwable arg0, String arg1) {
							AnUtils.showToast(PushSendActivity.this, "发送失败",
									Gravity.CENTER, alive);
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendText() {
		String contString = mEditTextContent.getText().toString();
		if (contString == null || contString.trim().equals("")) {
			return;
		}
		sendPush("text", contString, 0, 0);
		addNewMessage(false, contString);
	}

	public void sendImage(byte[] bytes) {
		addNewImage(false, bytes);
		String base64String = Base64.encodeToString(bytes, Base64.DEFAULT);
		sendPush("image", base64String, 0, 0);
	}

	public void sendAudio(byte[] bytes) {
		addNewAudio(false, bytes);
		String base64String = Base64.encodeToString(bytes, Base64.DEFAULT);
		sendPush("audio", base64String, 0, 0);
	}

	public void sendLocation(View view) {
		willSendLocation = true;
		if (AnUtils.mapClient != null && AnUtils.mapClient.isStarted()) {
			AnUtils.mapClient.requestLocation();
		} else {
			AnUtils.showToast(PushSendActivity.this, "位置服务模块未启动",
					Gravity.CENTER, alive);
			willSendLocation = false;
		}
	}

	private void ShowPickDialog() {
		new AlertDialog.Builder(PushSendActivity.this)
				.setTitle("选择来源")
				.setNegativeButton("相册", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						try {
							Intent intent = new Intent(Intent.ACTION_PICK, null);
							intent.setDataAndType(
									MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
									"image/*");
							startActivityForResult(intent, 1);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				})
				.setPositiveButton("拍照", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						try {
							Intent intent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
									.fromFile(new File(Environment
											.getExternalStorageDirectory(),
											"cameraImage.png")));
							startActivityForResult(intent, 2);
						} catch (Exception e) {
							e.printStackTrace();
						}
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
					+ "/cameraImage.png");
			startPhotoZoom(Uri.fromFile(temp));
			break;
		case 3:
			if (data == null)
				return;
			Bundle extras = data.getExtras();
			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				photo.compress(Bitmap.CompressFormat.JPEG, 60, stream);
				byte[] b = stream.toByteArray();
				sendImage(b);
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

	public void showMore(View view) {
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl_bottom);
		rl.setVisibility(View.INVISIBLE);
		rl = (RelativeLayout) findViewById(R.id.rl_more);
		rl.setVisibility(View.VISIBLE);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(findViewById(R.id.et_sendmessage)
				.getWindowToken(), 0);
	}

	public void showLess(View view) {
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl_more);
		rl.setVisibility(View.INVISIBLE);
		rl = (RelativeLayout) findViewById(R.id.rl_bottom);
		rl.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onLongClick(View v) {
		pBar.setVisibility(View.VISIBLE);
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				startRecording();
			}
		});
		thread.start();

		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			pBar.setVisibility(View.GONE);
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {

					stopRecording();
				}
			});
			thread.start();

		}
		return false;
	}

	@Override
	public void onLocationUpdated(BDLocation location) {
		if (willSendLocation) {
			sendPush("location", null, location.getLatitude(),
					location.getLongitude());
		}

		willSendLocation = false;
	}

	public void showMap(double latitude, double longitude) {
		Intent intent = new Intent(this, MapActivity.class);
		intent.putExtra("latitude", latitude);
		intent.putExtra("longitude", longitude);

		startActivity(intent);
	}

	public void handleTap(final View view) {
		ImageView tv = (ImageView) view;
		PushDetailsEntity pushDetailsEntity = (PushDetailsEntity) tv.getTag();
		EntityType type = pushDetailsEntity.entityType;
		switch (type) {
		case ET_AUDIO:
			playAudio(pushDetailsEntity.audioBytes);
			break;
		case ET_LOCATION:
			showMap(pushDetailsEntity.latitude, pushDetailsEntity.longitude);
		default:
			break;
		}

	}

	@Override
	public void onPushSaved() {
		initData();
	}

	@Override
	public void onPushSaved(String message) {
	}
}