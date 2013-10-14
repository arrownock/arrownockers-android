package com.arrownock.opensource.arrownockers.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.arrownock.opensource.arrownockers.chat.SessionActivity;
import com.arrownock.opensource.arrownockers.push.PushCaseActivity;
import com.arrownock.opensource.arrownockers.push.PushDetailsActivity;
import com.arrownock.opensource.arrownockers.push.PushSendActivity;
import com.arrownock.opensource.arrownockers.topic.TopicListActivity;
import com.arrownock.opensource.arrownockers.utils.DBManager.Push;
import com.arrownock.exception.ArrownockException;
import com.arrownock.push.AnPush;
import com.arrownock.push.PushService;


// 用于解析收到的Push消息格式并进行数据库存储，同时显示提醒在通知中心里，或者刷新UI界面
@SuppressLint({ "NewApi", "SdCardPath" })
public class CustomReceiver extends BroadcastReceiver {
	public final static String LOG_TAG = CustomReceiver.class.getName();

	protected final static String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
	protected final static String USER_PRESENT = "android.intent.action.USER_PRESENT";
	protected final static String MSG_ARRIVAL = PushService.ACTION_MSG_ARRIVAL;

	private static PushSendActivity pushSendActivity = null;
	private static PushDetailsActivity pushDetailsActivity = null;
	private static PushCaseActivity pushCaseActivity = null;

	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.i("onReceive", "push msg received");

		if (intent == null || context == null)
			return;

		try {
			if (!AnPush.getInstance(context).isEnabled())
				return;
		} catch (ArrownockException e) {
			return;
		}

		if (intent.getAction().equals(BOOT_ACTION)) {
			Log.i("onReceive", "BOOT_ACTION");
			PushService.actionStart(context);
		}

		if (intent.getAction().equals(USER_PRESENT)) {
			Log.i("onReceive", "USER_PRESENT");
		}

		if (intent.getAction().equals(MSG_ARRIVAL)) {
			String payloadStr = intent.getStringExtra("payload");

			try {
				final JSONObject payload = new JSONObject(payloadStr);

				showNotification(context, payload);

				String dataType = null;
				if (payload.has("dataType")) {
					dataType = payload.getString("dataType");
				} else {
					dataType = "text";
				}
				savePush(dataType, payload, context);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void showNotification(Context context, JSONObject payload) {
		showNotification(context, payload, -1);
	}

	@SuppressWarnings("deprecation")
	protected void showNotification(Context context, JSONObject payload,
			int notificationId) {
		if (payload == null) {
			Log.e(LOG_TAG, "Payload is null!");
		}

		String alert = null;
		boolean vibrate = false;
		long[] vibrateTag = new long[] { 0, 500 };
		String sound = null;
		Uri soundUri = null;
		String title = null;
		String icon = null;
		int iconID = 0;
		int badge = 0;

		try {
			JSONObject androidPartJson = payload.getJSONObject("android");
			alert = androidPartJson.optString("alert", null);
			vibrate = androidPartJson.optBoolean("vibrate", false);
			sound = androidPartJson.optString("sound", null);
			title = androidPartJson.optString("title", null);
			icon = androidPartJson.optString("icon", null);
			badge = androidPartJson.optInt("badge", 0);
		} catch (JSONException ex) {
			if (alert == null)
				alert = payload.toString();
		}

		if (title == null) {
			title = AnUtils.AppName;
		}

		if (icon != null) {
			try {
				iconID = context.getResources().getIdentifier(icon, "drawable",
						context.getPackageName());
				if (iconID < 1)
					iconID = context.getApplicationInfo().icon;
			} catch (Exception ex) {
				iconID = context.getApplicationInfo().icon;
			}
		} else {
			iconID = context.getApplicationInfo().icon;
		}

		if (sound == null) {
		} else if (sound.startsWith("media:")) {
			String number = null;
			try {
				number = sound.substring(6);
			} catch (Exception ex) {
			}
			if (number == null) {
			} else {
				soundUri = Uri.parse("content://media/internal/audio/media/"
						+ number);
			}
		} else if (sound.startsWith("sd:")) {
			String name = null;
			try {
				name = sound.substring(3);
			} catch (Exception ex) {
			}
			if (name == null) {
			} else {
				soundUri = Uri.parse("file:///sdcard/" + name);
			}
		} else {
			String uriAddr = getAndTransferFile(context, sound);
			soundUri = Uri.parse("file://" + uriAddr);
		}

		// 设置当点击通知中心图标时，打开哪个界面
		Intent intent = null;
		if (title.equals(AnUtils.getCurrentUsername())) {
			// 通过上行通道发给自己的Push消息
			intent = new Intent(context, PushSendActivity.class);
		} else if (title.equals(AnUtils.SystemPushTitle)) {
			// 系统发送的Push消息
			intent = new Intent(context, PushDetailsActivity.class);
		} else if (title.equals(AnUtils.AppName)) {
			// IM的离线Push提醒
			if (payload.has("topic")) {
				intent = new Intent(context, TopicListActivity.class);
			} else {
				intent = new Intent(context, SessionActivity.class);
			}
		} else {
			return;
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			intent.putExtra("title", payload.getJSONObject("android")
					.getString("title"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationManager notifManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = null;

		if (Build.VERSION.SDK_INT < 11) {
			n = new Notification();

			n.flags |= Notification.FLAG_SHOW_LIGHTS;
			n.flags |= Notification.FLAG_AUTO_CANCEL;
			if (sound != null && sound.equals("default"))
				n.defaults |= Notification.DEFAULT_SOUND;
			else
				n.sound = soundUri;
			n.when = System.currentTimeMillis();
			n.icon = iconID;
			if (badge > 0)
				n.number = badge;
			if (vibrate)
				n.vibrate = vibrateTag;
			n.setLatestEventInfo(context, title, alert, pi);
		} else {
			Notification.Builder builder = new Notification.Builder(context);
			builder.setContentIntent(pi).setSmallIcon(iconID)
					.setWhen(System.currentTimeMillis()).setAutoCancel(true)
					.setContentTitle(title);
			if (badge > 0)
				builder.setNumber(badge);
			if (alert != null && !"".equals(alert) && alert.length() < 512)
				builder.setContentText(alert);
			if (sound != null && sound.equals("default"))
				builder.setDefaults(Notification.DEFAULT_SOUND);
			else
				builder.setSound(soundUri);
			if (vibrate)
				builder.setVibrate(vibrateTag);

			n = builder.getNotification();
		}

		int notifyId = (notificationId == -1 ? (int) System.currentTimeMillis()
				: notificationId);
		notifManager.notify(notifyId, n);
	}

	private String getAndTransferFile(Context context, String filename) {
		AssetManager assetManager = context.getAssets();
		String destFolder = "/sdcard/arrownock/sound/"
				+ context.getPackageName() + "/";

		try {
			long fromFileSize = assetManager.openFd("sound/" + filename)
					.getLength();
			long destFileSize = 0;
			File destFile = new File(destFolder + filename);
			if (!destFile.exists()) {
				new File(destFolder).mkdirs();
			} else {
				destFileSize = destFile.length();
			}

			if (fromFileSize != destFileSize) {
				InputStream in = null;
				OutputStream out = null;
				in = assetManager.open("sound/" + filename);

				String newFileName = destFolder + filename;
				out = new FileOutputStream(newFileName);

				byte[] buffer = new byte[1024];
				int read;
				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
		}
		return destFolder + filename;
	}

	// 数据库存储Push消息，不包括IM的离线提醒
	public void savePush(String dataType, JSONObject payload,
			final Context context) {
		Push push = new Push();
		try {
			String message = payload.getJSONObject("android")
					.getString("alert");
			String messageId = payload.has("msg_id") ? payload
					.getString("msg_id") : null;
			String batchNumber = payload.has("batch_num") ? payload
					.getString("batch_num") : null;
			boolean income = true;
			String time = AnUtils.getTimeString(new Date());
			JSONObject androidJson = payload.getJSONObject("android");
			String alert = androidJson.getString("alert");
			byte[] binary = null;
			double latitude = 0;
			double longitude = 0;
			if (dataType.equals("image") || dataType.equals("audio")) {
				binary = Base64.decode(alert, Base64.DEFAULT);
			} else if (dataType.equals("location")) {
				latitude = Double.valueOf(payload.getString("latitude"));
				longitude = Double.valueOf(payload.getString("longitude"));
			} else {
				push.message = alert;
			}
			final String title = androidJson.has("title") ? androidJson
					.getString("title") : "title";
			push.type = "type";
			push.dataType = dataType;
			push.messageId = messageId;
			push.batchNumber = batchNumber;
			push.income = income;
			push.time = time;
			push.binary = binary;
			push.latitude = latitude;
			push.longitude = longitude;
			push.message = message;
			push.title = title;
			push.status = "unread";

			if (!push.title.equals(AnUtils.getCurrentUsername())
					&& !push.title.equals(AnUtils.SystemPushTitle)) {
				return;
			}

			DBManager.addPush(push);

			handlePushSaved(push);

		} catch (Exception e) {

		}

	}

	private void handlePushSaved(Push push) {
		if (pushSendActivity != null) {
			if (push.title.equals(AnUtils.getCurrentUsername())) {
				pushSendActivity.onPushSaved();
			}
		}
		if (pushDetailsActivity != null) {
			if (push.title.equals(AnUtils.SystemPushTitle)) {
				pushDetailsActivity.onPushSaved();
			}
		}
		if (pushCaseActivity != null) {
			if (push.title.equals(AnUtils.SystemPushTitle)
					|| push.message.contains("您申请的验证码为:")) {	// 这里应该使用更准确的判断方式，比如在Push消息中加入customData
				pushCaseActivity.onPushSaved(push.message);
			}
		}
	}

	public static void setPushSendActivity(PushSendActivity psa) {
		pushSendActivity = psa;
	}

	public static void setPushDetailsActivity(PushDetailsActivity pda) {
		pushDetailsActivity = pda;
	}

	public static void setPushCaseActivity(PushCaseActivity pca) {
		pushCaseActivity = pca;
	}

	public interface OnPushArrived {
		public void onPushSaved();

		public void onPushSaved(String message);
	}
}
