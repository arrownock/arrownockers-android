package com.arrownock.opensource.arrownockers.chat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

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
import android.util.Log;
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
import android.widget.Toast;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.chat.ChatMsgEntity.EntityType;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.AnIMWrapper;
import com.arrownock.opensource.arrownockers.utils.DBManager;
import com.arrownock.opensource.arrownockers.utils.DBManager.Chat;
import com.arrownock.opensource.arrownockers.utils.MapActivity;
import com.arrownock.opensource.arrownockers.utils.MapCallback;
import com.arrownock.opensource.arrownockers.utils.MapCallback.OnLocationUpdated;
import com.baidu.location.BDLocation;

public class ChatActivity extends Activity implements OnClickListener,
		OnLongClickListener, OnTouchListener, OnLocationUpdated {

	private Button mBtnSend;
	private Button mBtnImage;
	private Button mBtnAudio;
	private EditText mEditTextContent;
	private TextView tvTitle;
	private RelativeLayout rlBottom;
	private RelativeLayout rlMore;
	private ListView mListView;
	private ChatMsgViewAdapter mAdapter;
	private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();
	private List<String> clientIdsList;
	private List<String> allClientIds;

	MediaRecorder recorder;
	boolean recording = false;
	ProgressBar pBar;
	public boolean alive = false;
	private boolean willSendLocation = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_list);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		recorder = null;
		Bundle bundle = new Bundle();
		bundle = this.getIntent().getExtras();
		setClientIds(bundle.getString("clientIds"));
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText(bundle.getString("realnames"));

		initView();
		initData();

		AnIMWrapper.chatActivity = this;

		MapCallback.setChatActivity(this);

		alive = true;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		alive = true;
		initData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		alive = false;

		AnIMWrapper.chatActivity = null;
		MapCallback.setChatActivity(null);
	}

	@Override
	protected void onStop() {
		super.onStop();
		alive = false;
	}

	public void addNewLocation(boolean income, double lat, double lon) {
		ChatMsgEntity entity = new ChatMsgEntity();
		entity.date = AnUtils.getTimeString(new Date());
		if (income) {
			entity.realname = "";
		} else {
			entity.realname = AnUtils.getCurrentRealname();
		}
		entity.isComMsg = income;
		entity.entityType = EntityType.ET_LOCATION;
		entity.text = "Location...";
		entity.latitude = lat;
		entity.longitude = lon;

		mDataArrays.add(entity);
		mAdapter.notifyDataSetChanged();

		if (!income) {
			mEditTextContent.setText("");
		}
		mListView.setSelection(mListView.getCount() - 1);
	}

	public void addNewMessage(boolean income, String message) {
		ChatMsgEntity entity = new ChatMsgEntity();
		entity.date = AnUtils.getTimeString(new Date());
		if (income) {
			entity.realname = "";
		} else {
			entity.realname = AnUtils.getCurrentRealname();
		}
		entity.isComMsg = income;
		entity.entityType = EntityType.ET_TEXT;
		entity.text = message;

		mDataArrays.add(entity);
		mAdapter.notifyDataSetChanged();

		if (!income) {
			mEditTextContent.setText("");
		}
		mListView.setSelection(mListView.getCount() - 1);
	}

	public void addNewImage(boolean income, byte[] bytes) {
		ChatMsgEntity entity = new ChatMsgEntity();
		entity.date = AnUtils.getTimeString(new Date());
		if (income) {
			entity.realname = "";
		} else {
			entity.realname = AnUtils.getCurrentRealname();
		}
		entity.isComMsg = income;
		entity.entityType = EntityType.ET_IMAGE;
		entity.imageBytes = bytes;

		mDataArrays.add(entity);
		mAdapter.notifyDataSetChanged();

		mListView.setSelection(mListView.getCount() - 1);
	}

	public void addNewAudio(boolean income, byte[] bytes) {
		ChatMsgEntity entity = new ChatMsgEntity();
		entity.date = AnUtils.getTimeString(new Date());
		if (income) {
			entity.realname = "";
		} else {
			entity.realname = AnUtils.getCurrentRealname();
		}
		entity.isComMsg = income;
		entity.entityType = EntityType.ET_AUDIO;
		entity.audioBytes = bytes;

		mDataArrays.add(entity);
		mAdapter.notifyDataSetChanged();

		mListView.setSelection(mListView.getCount() - 1);

		if (income) {
			playAudio(bytes);
		}
	}

	@SuppressWarnings("resource")
	public void playAudio(byte[] bytes) {
		String pathString = Environment.getExternalStorageDirectory().getPath()
				+ "/receivedAudio.m4a";
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

			MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					mp.release();
				}
			};
			mp.setOnCompletionListener(listener);

			FileInputStream fis = new FileInputStream(pathString);
			mp.setDataSource(fis.getFD());
			mp.prepare();
			Log.i("MediaPlayer", "Start Player");
			mp.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startRecording() {
		if (recording == true)
			return;
		if (recorder == null) {
			String pathString = Environment.getExternalStorageDirectory()
					.getPath() + "/recordedAudio.m4a";
			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			recorder.setOutputFile(pathString);
			try {
				recorder.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			recorder.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		recording = true;
	}

	public void stopRecording() {
		if (!recording) {
			return;
		}
		String pathString = Environment.getExternalStorageDirectory().getPath()
				+ "/recordedAudio.m4a";
		File file = new File(pathString);
		FileInputStream iStream;
		try {
			recorder.stop();
			recording = false;
			recorder.reset();
			recorder.release();
			recorder = null;
			iStream = new FileInputStream(file);
			sendAudio(IOUtils.toByteArray(iStream));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void initView() {
		mListView = (ListView) findViewById(R.id.listview);
		mBtnSend = (Button) findViewById(R.id.btn_send);
		mBtnSend.setOnClickListener(this);
		mBtnImage = (Button) findViewById(R.id.btn_sendImage);
		mBtnImage.setOnClickListener(this);
		mBtnAudio = (Button) findViewById(R.id.btn_sendAudio);
		mBtnAudio.setOnLongClickListener(this);
		mBtnAudio.setOnTouchListener(this);
		pBar = (ProgressBar) findViewById(R.id.progressBar);
		rlBottom = (RelativeLayout) findViewById(R.id.rl_bottom);
		rlMore = (RelativeLayout) findViewById(R.id.rl_more);

		mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);

		mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
		mListView.setAdapter(mAdapter);
	}

	public void initData() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				allClientIds = new ArrayList<String>(clientIdsList);
				allClientIds.add(AnUtils.getCurrentClientId());
				Collections.sort(allClientIds);
				List<Chat> chatList = DBManager.getAllChats(
						StringUtils.join(allClientIds, ","), "parties");
				if (chatList == null) {
					return;
				}
				mDataArrays.clear();
				for (Chat chat : chatList) {
					ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
					chatMsgEntity.realname = chat.realname;
					chatMsgEntity.messageId = chat.messageId;
					chatMsgEntity.date = chat.time;
					chatMsgEntity.isComMsg = chat.income;
					chatMsgEntity.status = chat.status;
					String dataType = chat.type;
					if (dataType.equals("image")) {
						chatMsgEntity.entityType = EntityType.ET_IMAGE;
						chatMsgEntity.imageBytes = chat.binary;
					} else if (dataType.equals("audio")) {
						chatMsgEntity.entityType = EntityType.ET_AUDIO;
						chatMsgEntity.audioBytes = chat.binary;
					} else if (dataType.equals("text")) {
						chatMsgEntity.entityType = EntityType.ET_TEXT;
						chatMsgEntity.text = chat.message;
					} else if (dataType.equals("location")) {
						chatMsgEntity.entityType = EntityType.ET_LOCATION;
						chatMsgEntity.text = chat.message;
						chatMsgEntity.latitude = chat.latitude;
						chatMsgEntity.longitude = chat.longitude;
					}

					mDataArrays.add(chatMsgEntity);
				}

				runOnUiThread(new Runnable() {
					public void run() {
						mAdapter.notifyDataSetChanged();
						mListView.setSelection(mListView.getCount() - 1);
					}
				});

				for (Chat chat : chatList) {
					if (chat.status.equals("unread") && chat.income) {
						AnIMWrapper.getWrapper().sendReadACKToClients(
								clientIdsList, chat.messageId);
						DBManager
								.updateChatStatus(chat.messageId, true, "read");
					}
				}
				DBManager.setSessionRead(clientIdsList);
			}
		});

		thread.start();

	}

	public void refresh() {
		List<Chat> chatList = DBManager.getAllChats(
				StringUtils.join(allClientIds, ","), "parties");
		if (chatList == null) {
			return;
		}
		mDataArrays.clear();
		for (Chat chat : chatList) {
			ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
			chatMsgEntity.realname = chat.realname;
			chatMsgEntity.messageId = chat.messageId;
			chatMsgEntity.date = chat.time;
			chatMsgEntity.isComMsg = chat.income;
			chatMsgEntity.status = chat.status;
			String dataType = chat.type;
			if (dataType.equals("image")) {
				chatMsgEntity.entityType = EntityType.ET_IMAGE;
				chatMsgEntity.imageBytes = chat.binary;
			} else if (dataType.equals("audio")) {
				chatMsgEntity.entityType = EntityType.ET_AUDIO;
				chatMsgEntity.audioBytes = chat.binary;
			} else if (dataType.equals("text")) {
				chatMsgEntity.entityType = EntityType.ET_TEXT;
				chatMsgEntity.text = chat.message;
			} else if (dataType.equals("location")) {
				chatMsgEntity.entityType = EntityType.ET_LOCATION;
				chatMsgEntity.text = chat.message;
				chatMsgEntity.latitude = chat.latitude;
				chatMsgEntity.longitude = chat.longitude;
			}

			mDataArrays.add(chatMsgEntity);
		}

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mAdapter.notifyDataSetChanged();
				mListView.setSelection(mListView.getCount() - 1);
			}
		});

		DBManager.setSessionRead(clientIdsList);
	}

	public void onChatSent(final String messageId) {
		if (messageId != null) {
			refresh();
		} else {
			AnUtils.showToast(ChatActivity.this, "发送失败请重试", Gravity.CENTER,
					alive);
		}
	}

	public void onChatReceived() {
		refresh();
	}

	public void onChatRead() {
		refresh();
	}

	public void onReceivedChat() {
		refresh();
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

	public void handleTap(final View view) {
		ImageView iv = (ImageView) view;
		ChatMsgEntity entity = (ChatMsgEntity) iv.getTag();
		EntityType type = entity.entityType;
		switch (type) {
		case ET_AUDIO:
			playAudio(entity.audioBytes);
			break;
		case ET_LOCATION:
			showMap(entity.latitude, entity.longitude);
		default:
			break;
		}

	}

	public void stopProgressBar() {
		if (pBar != null) {
			pBar.setVisibility(View.GONE);
		}
	}

	public void showMore(View view) {
		rlBottom.setVisibility(View.INVISIBLE);
		rlBottom = (RelativeLayout) findViewById(R.id.rl_more);
		rlBottom.setVisibility(View.VISIBLE);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(findViewById(R.id.et_sendmessage)
				.getWindowToken(), 0);
	}

	public void showLess(View view) {
		rlMore = (RelativeLayout) findViewById(R.id.rl_more);
		rlMore.setVisibility(View.INVISIBLE);
		rlMore = (RelativeLayout) findViewById(R.id.rl_bottom);
		rlMore.setVisibility(View.VISIBLE);
	}

	public void showMap(double latitude, double longitude) {
		Intent intent = new Intent(ChatActivity.this, MapActivity.class);
		Bundle bundle = new Bundle();
		bundle.putDouble("latitude", latitude);
		bundle.putDouble("longitude", longitude);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	public void sendLocation(View view) {
		willSendLocation = true;
		if (AnUtils.mapClient != null && AnUtils.mapClient.isStarted()) {
			AnUtils.mapClient.requestLocation();
		} else {
			Toast.makeText(getBaseContext(), "位置服务模块未启动", Toast.LENGTH_LONG)
					.show();
			willSendLocation = false;
		}
	}

	public void sendText() {
		String contString = mEditTextContent.getText().toString();
		if (contString.trim().length() > 0) {
			AnIMWrapper.getWrapper().sendMessageToClients(contString,
					clientIdsList);

			addNewMessage(false, contString);
		}
	}

	public void sendImage(byte[] bytes) {
		addNewImage(false, bytes);
		AnIMWrapper.getWrapper().sendBinaryToClients(bytes, "image",
				clientIdsList);
	}

	public void sendAudio(byte[] bytes) {
		addNewAudio(false, bytes);
		AnIMWrapper.getWrapper().sendBinaryToClients(bytes, "audio",
				clientIdsList);
	}

	public void sendLocation(double lat, double lon) {
		addNewLocation(false, lat, lon);
		AnIMWrapper.getWrapper().sendLocationToClients(lat, lon, clientIdsList);
	}

	private void ShowPickDialog() {
		new AlertDialog.Builder(this)
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
											"pickedImage.png")));
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

	private void setClientIds(String clientIdsString) {
		clientIdsList = new ArrayList<String>(Arrays.asList(clientIdsString
				.split("\\,")));
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
		thread.run();

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
			thread.run();

		}
		return false;
	}

	@Override
	public void onLocationUpdated(BDLocation location) {
		if (willSendLocation) {
			sendLocation(location.getLatitude(), location.getLongitude());
		}

		willSendLocation = false;

	}
}