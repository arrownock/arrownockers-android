package com.arrownock.opensource.arrownockers.topic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
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
import com.arrownock.opensource.arrownockers.topic.MessageEntity.EntityType;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.AnIMWrapper;
import com.arrownock.opensource.arrownockers.utils.DBManager;
import com.arrownock.opensource.arrownockers.utils.SwipeDismissListViewTouchListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.svenkapudija.imageresizer.ImageResizer;

public class MessageListActivity extends Activity implements
		OnLongClickListener, OnTouchListener {
	private static final String logTag = "MessageListActivity";

	private PullToRefreshListView pullToRefreshListView;
	private MessageListAdapter messageListAdapter;
	private List<MessageEntity> messageEntities = new ArrayList<MessageEntity>();

	private Handler handler;

	private EditText etInput;
	private ProgressBar pbar;
	private Button btnJoinTopic;
	private Button btnQuitTopic;

	private String topicId;

	MediaRecorder recorder;
	boolean isRecording = false;
	boolean isRefreshing = false;
	public boolean alive = true;
	private boolean willShowSubView = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_list);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		topicId = getIntent().getStringExtra("topicId");

		handler = new Handler();

		initView();
		initCallback();
		initData();

		AnIMWrapper.getWrapper().willCheckIfMyTopic(topicId);

		recorder = null;

		if (getIntent().getStringExtra("where") != null) {
			AnUtils.clearNotifyIcon(getBaseContext());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.i(logTag, "onDestroy");

		AnIMWrapper.messageListActivity = null;
	}

	@Override
	protected void onStop() {
		super.onStop();

		alive = false;
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		alive = true;

		setShowingMessagesAsRead();

		messageListAdapter.notifyDataSetChanged();
		pullToRefreshListView.getRefreshableView().setSelection(
				pullToRefreshListView.getRefreshableView().getCount() - 1);

		if (!willShowSubView) {
			AnIMWrapper.getWrapper().connect(AnUtils.getCurrentClientId());
		}

		willShowSubView = false;
	}

	public Handler getHandler() {
		return handler;
	}

	private void setTitle() {
		Topic topic = DBManager.readOneTopic(topicId);
		if (topic == null) {
			return;
		}

		TextView tvTitle = (TextView) findViewById(R.id.tv_messsage_title);
		tvTitle.setText(topic.topicName + " (" + topic.count + "人)");
	}

	private void setJoinButtonEnabled(boolean enabled) {
		if (enabled) {
			btnJoinTopic.setVisibility(View.VISIBLE);
		} else {
			btnJoinTopic.setVisibility(View.INVISIBLE);
		}
	}

	private void setQuitButtonEnabled(boolean enabled) {
		if (enabled) {
			btnQuitTopic.setVisibility(View.VISIBLE);
		} else {
			btnQuitTopic.setVisibility(View.INVISIBLE);
		}
	}

	private void initView() {
		setTitle();

		etInput = (EditText) findViewById(R.id.et_input);
		pbar = (ProgressBar) findViewById(R.id.pbar);
		btnJoinTopic = (Button) findViewById(R.id.btn_message_join_topic);
		btnQuitTopic = (Button) findViewById(R.id.btn_message_quit_topic);

		Button btnSendAudio = (Button) findViewById(R.id.btn_sendAudio);
		btnSendAudio.setOnLongClickListener(this);
		btnSendAudio.setOnTouchListener(this);

		pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_messsage);
		pullToRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {

					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						if (isRefreshing) {
							return;
						}
						isRefreshing = true;
						retriveOldData();
					}

				});

		messageEntities = new ArrayList<MessageEntity>();

		messageListAdapter = new MessageListAdapter(this, messageEntities);

		pullToRefreshListView.setAdapter(messageListAdapter);

		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				pullToRefreshListView.getRefreshableView(),
				new SwipeDismissListViewTouchListener.DismissCallbacks() {
					@Override
					public boolean canDismiss(int position) {
						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
							return false;
						}
						return true;
					}

					@Override
					public void onDismiss(ListView listView,
							int[] reverseSortedPositions, final View view) {
						for (int p : reverseSortedPositions) {

							final int position = p;
							Log.i(logTag,
									"onDismiss will remove item on position: "
											+ (position - 1));

							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
									MessageListActivity.this).setTitle("删除消息")
									.setMessage("是否确认删除?");
							alertDialogBuilder.setPositiveButton("确定",
									new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											MessageEntity messageEntity = messageEntities
													.get(position - 1);
											DBManager
													.deleteMessage(messageEntity.messageId);
											messageEntities
													.remove(position - 1);
											runOnUiThread(new Runnable() {
												public void run() {
													view.setAlpha(1f);
													view.setTranslationX(0);
													messageListAdapter
															.notifyDataSetChanged();
												}
											});

										}
									});
							alertDialogBuilder.setNegativeButton("取消",
									new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											runOnUiThread(new Runnable() {
												public void run() {
													view.setAlpha(1f);
													view.setTranslationX(0);
													messageListAdapter
															.notifyDataSetChanged();
												}
											});
										}
									});
							alertDialogBuilder.show();
						}
					}
				});
		pullToRefreshListView.getRefreshableView().setOnTouchListener(
				touchListener);
		// Setting this scroll listener is required to ensure that during
		// ListView scrolling,
		// we don't look for swipes.
		pullToRefreshListView.getRefreshableView().setOnScrollListener(
				touchListener.makeScrollListener());
	}

	private void initCallback() {
		AnIMWrapper.messageListActivity = this;
	}

	private void setShowingMessagesAsRead() {
		int count = messageEntities.size();
		for (int i = count - 1; i > -1; i--) {
			if (messageEntities.get(i).isComMsg
					&& messageEntities.get(i).unread) {
				DBManager
						.updateIncomingMessageAsRead(messageEntities.get(i).messageId);
				messageEntities.get(i).unread = false;
			}
		}

		messageListAdapter.notifyDataSetChanged();
	}

	public void initData() {

		List<Message> messageList = DBManager.readMessages(topicId, 0, 10);
		if (messageList == null) {
			return;
		}
		messageEntities.clear();

		for (Message message : messageList) {
			MessageEntity messageEntity = new MessageEntity();
			messageEntity.messageId = message.messageId;
			messageEntity.date = message.timestamp;
			messageEntity.dateString = AnUtils.getTimeString(message.timestamp);
			messageEntity.isComMsg = message.isIncoming;
			messageEntity.entityType = message.type;
			messageEntity.realname = message.realname;
			messageEntity.username = message.username;
			messageEntity.unsent = message.isUnsent;
			messageEntity.unread = message.isUnread;
			if (messageEntity.entityType == EntityType.ET_IMAGE) {
				messageEntity.imageBytes = message.imageData;
			} else if (messageEntity.entityType == EntityType.ET_AUDIO) {
				messageEntity.audioBytes = message.audioData;
			} else if (messageEntity.entityType == EntityType.ET_TEXT) {
				messageEntity.text = message.content;
			}

			messageEntities.add(messageEntity);

			if (message.isUnread) {
				DBManager.updateIncomingMessageAsRead(message.messageId);
			}
		}

		messageListAdapter.notifyDataSetChanged();
		pullToRefreshListView.getRefreshableView().setSelection(
				pullToRefreshListView.getRefreshableView().getCount() - 1);
	}

	public void retriveOldData() {
		Date date = null;
		if (messageEntities.size() > 0) {
			date = messageEntities.get(0).date;
		} else {
			date = new Date();
		}
		List<Message> oldMessages = DBManager.readMessages(topicId,
				date.getTime(), 10);
		if (oldMessages == null) {
			return;
		}
		if (oldMessages.size() == 0) {
			AnUtils.showToast(MessageListActivity.this, "没有更多的消息",
					Gravity.CENTER, alive);
		} else {
			List<MessageEntity> oldMessageEntities = new ArrayList<MessageEntity>();

			for (Message message : oldMessages) {
				MessageEntity messageEntity = new MessageEntity();
				messageEntity.messageId = message.messageId;
				messageEntity.date = message.timestamp;
				messageEntity.dateString = AnUtils
						.getTimeString(message.timestamp);
				messageEntity.isComMsg = message.isIncoming;
				messageEntity.entityType = message.type;
				messageEntity.realname = message.realname;
				messageEntity.username = message.username;
				messageEntity.unsent = message.isUnsent;
				messageEntity.unread = message.isUnread;
				if (messageEntity.entityType == EntityType.ET_IMAGE) {
					messageEntity.imageBytes = message.imageData;
				} else if (messageEntity.entityType == EntityType.ET_AUDIO) {
					messageEntity.audioBytes = message.audioData;
				} else if (messageEntity.entityType == EntityType.ET_TEXT) {
					messageEntity.text = message.content;
				}

				oldMessageEntities.add(messageEntity);

				if (message.isUnread) {
					DBManager.updateIncomingMessageAsRead(message.messageId);
				}
			}

			oldMessageEntities.addAll(messageEntities);
			messageEntities = oldMessageEntities;

			messageListAdapter = new MessageListAdapter(this, messageEntities);
			pullToRefreshListView.setAdapter(messageListAdapter);
			messageListAdapter.notifyDataSetChanged();
		}

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				pullToRefreshListView.onRefreshComplete();
				isRefreshing = false;
			}
		}, 100);
	}

	public void startRecording() {
		if (isRecording == true)
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

		isRecording = true;
	}

	public void stopRecording() {
		if (!isRecording) {
			return;
		}
		String pathString = Environment.getExternalStorageDirectory().getPath()
				+ "/recordedAudio.m4a";
		File file = new File(pathString);
		FileInputStream iStream;
		try {
			recorder.stop();
			isRecording = false;
			recorder.reset();
			recorder.release();
			recorder = null;
			iStream = new FileInputStream(file);

			final Message message = new Message();
			message.topicId = topicId;
			message.isIncoming = false;
			message.isUnread = false;
			message.isUnsent = true;
			message.realname = AnUtils.getCurrentRealname();
			message.username = AnUtils.getCurrentUsername();
			message.timestamp = new Date();
			message.audioData = IOUtils.toByteArray(iStream);
			message.type = EntityType.ET_AUDIO;

			String messageId = AnIMWrapper.getWrapper().sendAudio(message);
			if (messageId != null) {
				message.messageId = messageId;
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						addMessageBubble(message);
					}
				});

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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

					runOnUiThread(new Runnable() {
						public void run() {
							messageListAdapter.notifyDataSetChanged();
						}
					});
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

	private void playAudioAnimation(View view) {
		ImageView imageView = (ImageView) view;
		imageView.setBackgroundResource(R.anim.audio_animation_list);
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView
				.getBackground();
		animationDrawable.setOneShot(false);
		if (animationDrawable.isRunning()) {
			animationDrawable.stop();
		}

		imageView.setImageBitmap(null);
		animationDrawable.start();
	}

	private void showImage(byte[] imageData) {
		Intent intent = new Intent(this, ImageActivity.class);
		intent.putExtra("imageData", imageData);

		startActivity(intent);

		willShowSubView = true;
	}

	private void addMessageBubble(Message message) {
		MessageEntity messageEntity = new MessageEntity();
		messageEntity.messageId = message.messageId;
		messageEntity.date = message.timestamp;
		messageEntity.dateString = AnUtils.getTimeString(message.timestamp);
		messageEntity.text = message.content;
		messageEntity.imageBytes = message.imageData;
		messageEntity.audioBytes = message.audioData;
		messageEntity.isComMsg = message.isIncoming;
		messageEntity.entityType = message.type;
		messageEntity.realname = message.realname;
		messageEntity.username = message.username;
		messageEntity.unsent = true;
		messageEntity.unread = true;

		messageEntities.add(messageEntity);

		messageListAdapter.notifyDataSetChanged();
		pullToRefreshListView.getRefreshableView().setSelection(
				pullToRefreshListView.getRefreshableView().getCount() - 1);
	}

	private void processAndSendImage(final Uri uri) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Bitmap bitmap = AnUtils.getByteArrayFromImageUri(
						MessageListActivity.this, uri);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				byte[] imageData = baos.toByteArray();

				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				float scale = (float) (640.0 / width);

				if (scale < 1) {
					width = 640;
					height = (int) (height * scale);
					bitmap = Bitmap.createScaledBitmap(bitmap, width, height,
							false);
				}

				while (imageData.length > 1024 * 70) {
					width = (int) (width * 0.9);
					height = (int) (height * 0.9);

					bitmap = ImageResizer.resize(imageData, width, height);
					ByteArrayOutputStream tmpBAOS = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, tmpBAOS);
					imageData = tmpBAOS.toByteArray();
				}

				final Message message = new Message();
				message.topicId = topicId;
				message.isIncoming = false;
				message.isUnread = false;
				message.isUnsent = true;
				message.realname = AnUtils.getCurrentRealname();
				message.username = AnUtils.getCurrentUsername();
				message.timestamp = new Date();
				message.imageData = imageData;
				message.type = EntityType.ET_IMAGE;

				String messageId = AnIMWrapper.getWrapper().sendImage(message);
				if (messageId != null) {
					message.messageId = messageId;
					runOnUiThread(new Runnable() {
						public void run() {
							pbar.setVisibility(View.INVISIBLE);
							addMessageBubble(message);
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						public void run() {
							pbar.setVisibility(View.INVISIBLE);
						}
					});
				}
			}
		});

		thread.start();

		pbar.setVisibility(View.VISIBLE);
	}

	public void onSendImageClick(View view) {
		ShowPickDialog();
	}

	public void onSendTextClick(View view) {
		String text = etInput.getText().toString().trim();
		if (text == null || text.trim().equals("")) {
			AnUtils.showToast(MessageListActivity.this, "输入不能为空",
					Gravity.CENTER, alive);
			return;
		}

		Message message = new Message();
		message.topicId = topicId;
		message.isIncoming = false;
		message.isUnread = false;
		message.isUnsent = true;
		message.realname = AnUtils.getCurrentRealname();
		message.username = AnUtils.getCurrentUsername();
		message.timestamp = new Date();
		message.content = text;
		message.type = EntityType.ET_TEXT;

		String messageId = AnIMWrapper.getWrapper().sendText(message);
		if (messageId != null) {
			etInput.setText(null);

			message.messageId = messageId;
			addMessageBubble(message);
		}
	}

	public void onMessage(final Message message) {
		if (topicId.equals(message.topicId)) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					addMessageBubble(message);
				}
			});

			if (alive) {
				DBManager.updateIncomingMessageAsRead(message.messageId);
			} else {
				AnUtils.showNotification(getBaseContext(), "topic");
			}
		} else {
			if (alive) {
				return;
			} else {
				AnUtils.showNotification(getBaseContext(), "topic");
			}
		}

	}

	public void onMessageSent(final String messageId) {
		Log.i(logTag, "onMessageSent messageId: " + messageId);

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				handleMessageSent(messageId);
			}
		});

	}

	private void handleMessageSent(String messageId) {
		Log.i(logTag, "handleMessageSent messageId: " + messageId);

		if (messageId != null) {
			int count = messageEntities.size();
			for (int i = count - 1; i > -1; i--) {
				String id = messageEntities.get(i).messageId;
				if (id.equals(messageId)) {
					Log.i(logTag, "setMessageSent messageId: " + messageId);

					messageEntities.get(i).unsent = false;
					break;
				}
			}
		}

		messageListAdapter.notifyDataSetChanged();
	}

	public void onCheckIfMyTopic(boolean isMine, boolean isError) {
		if (!isError) {
			if (!isMine) {
				final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						MessageListActivity.this).setTitle("加入群组").setMessage(
						"必须加入群组才能收发消息\n是否确认加入?");
				alertDialogBuilder.setPositiveButton("确定",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								AnIMWrapper.getWrapper().willJoinTopic(topicId);
							}
						});
				alertDialogBuilder.setNegativeButton("取消",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setJoinButtonEnabled(true);
							}
						});

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						alertDialogBuilder.show();
					}
				});
			} else {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						AnUtils.showToast(MessageListActivity.this, "已经加入该群组",
								Gravity.CENTER, alive);
						setJoinButtonEnabled(false);
						setQuitButtonEnabled(true);
					}
				});
			}
		} else {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					AnUtils.showToast(MessageListActivity.this,
							"验证群组权限失败\n请返回后重新尝试", Gravity.CENTER, alive);
					setJoinButtonEnabled(false);
					setQuitButtonEnabled(false);
				}
			});
		}
	}

	public void onJoinTopic(final boolean success) {
		String message = null;
		if (success) {
			message = "加入群组成功";
		} else {
			message = "加入群组失败\n请重新尝试";
		}

		final String msg = message;

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setJoinButtonEnabled(!success);
				setQuitButtonEnabled(success);
				AnUtils.showToast(MessageListActivity.this, msg,
						Gravity.CENTER, alive);
			}
		});

	}

	public void onQuitTopic(final boolean success) {
		String message = null;
		if (success) {
			message = "退出群组成功";
			finish();

			return;
		} else {
			message = "退出群组失败\n请重新尝试";
		}

		final String msg = message;

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setQuitButtonEnabled(!success);
				setJoinButtonEnabled(success);
				AnUtils.showToast(MessageListActivity.this, msg,
						Gravity.CENTER, alive);
			}
		});

	}

	public void onJoinButtonClick(View view) {
		AnIMWrapper.getWrapper().willCheckIfMyTopic(topicId);
	}

	public void onQuitButtonClick(View view) {
		AnIMWrapper.getWrapper().willQuitTopic(topicId);
	}

	public void switchToText(boolean yes) {
		RelativeLayout rlText = (RelativeLayout) findViewById(R.id.rl_text_container);
		RelativeLayout rlMore = (RelativeLayout) findViewById(R.id.rl_more_container);

		InputMethodManager imm = (InputMethodManager) getSystemService(MessageListActivity.INPUT_METHOD_SERVICE);

		if (yes) {
			rlText.setVisibility(View.VISIBLE);
			rlMore.setVisibility(View.INVISIBLE);
			etInput.requestFocus();
			imm.showSoftInput(etInput, 0);
		} else {
			rlText.setVisibility(View.INVISIBLE);
			rlMore.setVisibility(View.VISIBLE);
			imm.hideSoftInputFromWindow(etInput.getWindowToken(), 0);
		}
	}

	public void onSwitchToMoreClick(View view) {
		switchToText(false);
	}

	public void onSwitchToTextClick(View view) {
		switchToText(true);
	}

	public void onBubbleClick(View view) {
		MessageEntity messageEntity = (MessageEntity) view.getTag();
		Log.i(logTag, "onImageClick messageId: " + messageEntity.messageId);

		if (messageEntity.entityType == EntityType.ET_IMAGE) {
			showImage(messageEntity.imageBytes);
		} else if (messageEntity.entityType == EntityType.ET_AUDIO) {
			playAudioAnimation(view);
			playAudio(messageEntity.audioBytes);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		pbar.setVisibility(View.VISIBLE);
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
			pbar.setVisibility(View.GONE);
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

							willShowSubView = true;
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

							willShowSubView = true;
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
			processAndSendImage(data.getData());
			break;
		case 2:
			if (resultCode != -1) {
				return;
			}
			File temp = new File(Environment.getExternalStorageDirectory()
					+ "/pickedImage.png");
			processAndSendImage(Uri.fromFile(temp));
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
