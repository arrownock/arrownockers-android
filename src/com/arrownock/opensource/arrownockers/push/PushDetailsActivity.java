package com.arrownock.opensource.arrownockers.push;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.push.PushDetailsEntity.EntityType;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.CustomReceiver;
import com.arrownock.opensource.arrownockers.utils.CustomReceiver.OnPushArrived;
import com.arrownock.opensource.arrownockers.utils.DBManager;
import com.arrownock.opensource.arrownockers.utils.DBManager.Push;
import com.arrownock.opensource.arrownockers.utils.MapActivity;


// 来自系统发送的Push消息
public class PushDetailsActivity extends Activity implements OnPushArrived {

	private ListView mListView;
	private PushDetailsListAdapter mAdapter;
	private List<PushDetailsEntity> mDataArrays = new ArrayList<PushDetailsEntity>();

	boolean recording = false;
	String title = AnUtils.SystemPushTitle;
	public boolean alive = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_push_details);

		initView();

		initData();

		CustomReceiver.setPushDetailsActivity(this);
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

	private void initView() {
		mAdapter = new PushDetailsListAdapter(this, mDataArrays);
		mListView = (ListView) findViewById(R.id.lv_push_details);
		mListView.setAdapter(mAdapter);
	}

	public void initData() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				List<Push> pushList = DBManager.getPushes(title);
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

				DBManager.setPushRead(title);

				runOnUiThread(new Runnable() {
					public void run() {
						mAdapter.notifyDataSetChanged();
						mListView.setSelection(mListView.getCount() - 1);
					}
				});
			}
		});

		thread.start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		alive = true;

		initData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		alive = false;

		CustomReceiver.setPushDetailsActivity(null);
	}

	@Override
	protected void onStop() {
		super.onStop();
		alive = false;
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