package com.arrownock.opensource.arrownockers.utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.arrownock.opensource.arrownockers.chat.SessionActivity;
import com.arrownock.opensource.arrownockers.topic.MessageListActivity;
import com.arrownock.opensource.arrownockers.topic.TopicListActivity;
import com.arrownock.opensource.arrownockers.utils.DBManager.Session;
import com.arrownock.exception.ArrownockException;
import com.arrownock.mrm.MRMJSONResponseHandler;
import com.arrownock.push.AnPush;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class AnUtils {

	private static final String logTag = "AnUtils";
	// TODO 填写您创建的Arrownock App Key
	public static final String AppKey = <your app's key>;

	public static final String SystemPushTitle = "系统推送消息";
	public static final String AppName = "Arrownockers";

	public static final String requireVerifyCodeEndpoint = "http://api.arrownock.com/v1/demo/create_auth_code.json?key="
			+ AppKey;
	public static final String verifyEndpoint = "http://api.arrownock.com/v1/demo/verify_auth_code.json?key="
			+ AppKey;
	public static final String pushEndpoint = "http://api.arrownock.com/v1/push_notification/send.json?key="
			+ AppKey;

	public static SharedPreferences spf;
	public static Editor editor;
	public static LocationClient mapClient;

	public static MainActivity mainActivity;
	public static Context applicationContext;

	public static void initArrownockComponents(final Activity ctx) {

		Log.i(logTag, "initArrownockComponents");

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doInitArrownockComponents(ctx);
			}
		});
		thread.start();
	}

	// 初始化anPush
	private static void initAnPush(Activity ctx) {
		Log.i(logTag, "initAnPush");

		Context appContext = ctx.getApplicationContext();
		try {
			AnPush.getInstance(appContext).setAppKey(AnUtils.AppKey);
			AnPush.getInstance(appContext).setSecureConnection(true);
			AnPush.getInstance(appContext).setCallback(
					new AnPushCallback(appContext));
			if (AnUtils.getAnId() == null) {
				Log.i(logTag, "anPush register");

				List<String> channels = new ArrayList<String>();
				channels.add(AnUtils.getCurrentUsername());
				AnPush.getInstance(appContext).register(channels, true);
			} else {
				Log.i(logTag, "anPush already registered");

				AnPush.getInstance(appContext).enable();

				initAnIM(ctx, AnPush.getInstance(ctx).getAnID());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 初始化anIM
	public static void initAnIM(Context ctx, String anid) {
		Log.i(logTag, "init anIM");

		AnIMWrapper.init(ctx, AnUtils.AppKey);

		AnIMWrapper.thisAnId = anid;

		AnIMWrapper.getWrapper().getClientId(AnUtils.getCurrentUsername());
	}

	// 初始化百度地图
	public static void initLocationComponents(Context ctx) {
		// baidu locating api
		mapClient = new LocationClient(ctx); //
		// 声明LocationClient类
		mapClient.registerLocationListener(new MapCallback()); // 注册监听函数
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		// option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
		// option.disableCache(true);// 禁止启用缓存定位
		// option.setPoiNumber(5); // 最多返回POI个数
		// option.setPoiDistance(1000); // poi查询距离
		// option.setPoiExtraInfo(true); // 是否需要POI的电话和地址等详细信息

		mapClient.setLocOption(option);
		mapClient.start();
	}

	// 设置SharedPreferences
	public static void initDataStoreComponents(Context ctx) {
		spf = ctx.getSharedPreferences("com.arrownock.opensource.arrownockers",
				Activity.MODE_PRIVATE);
		if (spf != null) {
			editor = spf.edit();
		}
	}

	private static void doInitArrownockComponents(final Activity ctx) {
		try {
			AnIMWrapper.init(ctx, AnUtils.AppKey);

			if (AnUtils.getCurrentUsername() == null) {
				Log.i(logTag, "will create new user");

				final String username = generateUsername();
				final String password = generatePassword();

				JSONObject params = new JSONObject();
				try {
					params.put("username", username);
					params.put("password", password);
					params.put("realname", username);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// 新建用户
				MRMWrapper.getMRM(ctx).sendPostRequest(ctx, "users/create",
						params, new MRMJSONResponseHandler() {

							@Override
							public void onFailure(Throwable arg0,
									JSONObject arg1) {

								int errorCode = 0;
								try {
									String message = arg1.getJSONObject("meta")
											.getString("message");
									errorCode = arg1.getJSONObject("meta")
											.getInt("errorCode");
									Log.i(logTag, "init user failure: "
											+ message);
								} catch (Exception e) {
									e.printStackTrace();
								}

								if (errorCode == 10202) {
									doInitArrownockComponents(ctx);
									return;
								}

								if (mainActivity != null) {
									mainActivity.onSetCurrentUsername(
											"网络错误.初始化失败", true);
								}
							}

							@Override
							public void onSuccess(int arg0, JSONObject arg1) {
								Log.i(logTag, "init user success: " + username);

								try {
									String userId = arg1
											.getJSONObject("response")
											.getJSONObject("user")
											.getString("id");
									AnUtils.setCurrentUserId(userId);
								} catch (Exception e) {
									e.printStackTrace();
								}

								showToast(ctx, "初始化User成功", Gravity.CENTER,
										true);

								if (mainActivity != null) {
									mainActivity.onSetCurrentUsername(username,
											false);
								}

								AnUtils.setCurrentUsername(username);
								AnUtils.setCurrentPassword(password);
								AnUtils.setCurrentRealname(username);

								initAnPush(ctx);
							}
						});

			} else {
				Log.i(logTag, "existing user: " + AnUtils.getCurrentUsername());

				final String username = AnUtils.getCurrentUsername();
				final String password = AnUtils.getCurrentPassword();
				JSONObject params = new JSONObject();
				try {
					params.put("username", username);
					params.put("password", password);
				} catch (Exception e) {
					e.printStackTrace();
				}
				MRMWrapper.getMRM(ctx).sendPostRequest(ctx, "users/login",
						params, new MRMJSONResponseHandler() {

							@Override
							public void onFailure(Throwable arg0,
									JSONObject arg1) {
								try {
									String message = arg1.getJSONObject("meta")
											.getString("message");
									Log.i(logTag, "login user failure: "
											+ message);
								} catch (Exception e) {
									e.printStackTrace();
								}

								if (mainActivity != null) {
									mainActivity.onSetCurrentUsername(
											"登录失败.请返回重试", true);
								}
							}

							@Override
							public void onSuccess(int arg0, JSONObject arg1) {
								Log.i(logTag, "login user success: " + username);

								if (mainActivity != null) {
									mainActivity.onSetCurrentUsername(
											AnUtils.getCurrentUsername(), false);
								}

								initAnPush(ctx);

							}
						});

			}

		} catch (ArrownockException e) {
			e.printStackTrace();
		}
	}

	// 生成随机数用户名
	private static String generateUsername() {
		int randomInt = new Random().nextInt(1000000);
		String username = String.valueOf(randomInt);
		int len = username.length();
		while (len < 6) {
			username = "0" + String.valueOf(randomInt);
			len = username.length();
		}

		return username;
	}

	// 生成随机数密码
	private static String generatePassword() {
		int randomInt = new Random().nextInt(1000000);

		return String.valueOf(randomInt);
	}

	// 获取UTC时间转化的本地时间字符串
	public static String getTimeString(String utcTimeString) {

		try {
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date date = sdf.parse(utcTimeString);
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
			return sdf.format(date.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}

	}

	// 获取Date转化的本地时间字符串
	public static String getTimeString(Date utcDate) {
		if (utcDate.getTime() == 0) {
			return "";
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",
				Locale.CHINA);
		return sdf.format(utcDate.getTime());
	}

	// 获取用于在欢迎页面显示的时间字符串
	public static String getMainClockString(Date utcDate) {
		if (utcDate.getTime() == 0) {
			return "";
		}

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);
		return sdf.format(utcDate.getTime());
	}

	public static String getCurrentClientId() {
		return spf.getString("clientId", null);
	}

	public static String getCurrentUsername() {
		return spf.getString("username", null);
	}

	public static String getCurrentPassword() {
		return spf.getString("password", null);
	}

	public static String getCurrentRealname() {
		return spf.getString("realname", null);
	}

	public static String getAnId() {
		return spf.getString("anId", null);
	}

	public static String getCurrentUserId() {
		return spf.getString("userId", null);
	}

	public static void setAnId(String id) {
		editor.putString("anId", id);
		editor.commit();
	}

	public static void setCurrentClientId(String id) {
		editor.putString("clientId", id);
		editor.commit();

		addAngelSession();
	}

	public static void setCurrentUsername(String id) {
		editor.putString("username", id);
		editor.commit();
	}

	public static void setCurrentPassword(String id) {
		editor.putString("password", id);
		editor.commit();
	}

	public static void setCurrentRealname(String id) {
		editor.putString("realname", id);
		editor.commit();
	}

	public static void setCurrentUserId(String id) {
		editor.putString("userId", id);
		editor.commit();
	}

	public static void showToast(final Activity ctx, final String text,
			final int position, final boolean alive) {
		ctx.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!alive) {
					return;
				}
				Toast toast = Toast.makeText(ctx, text, Toast.LENGTH_SHORT);
				toast.setGravity(position, 0, 0);

				toast.show();
			}
		});
	}

	public static void removeAll() {
		editor.clear();
		editor.commit();
	}

	public static Bitmap getByteArrayFromImageUri(Activity ctx, Uri uri) {
		Bitmap data = null;
		try {
			ContentResolver cr = ctx.getContentResolver();
			InputStream inputStream = cr.openInputStream(uri);
			data = BitmapFactory.decodeStream(inputStream);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return data;
	}

	// 当前用户处于IM在线，并程序处在未激活状态时，放置新消息提醒
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static void showNotification(Context context, String which) {
		long[] vibrateTag = new long[] { 0, 500 };
		int iconID = 0;

		String alert = "新消息";
		boolean vibrate = true;
		String title = "Arrownockers";
		int badge = 1;

		iconID = context.getApplicationInfo().icon;

		Intent intent = null;
		if (which.equals("topic")) {
			intent = new Intent(context, TopicListActivity.class);
		} else if (which.equals("message")) {
			intent = new Intent(context, MessageListActivity.class);
		} else if (which.equals("session")) {
			intent = new Intent(context, SessionActivity.class);
		}
		intent.putExtra("where", "notify");

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationManager notifManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = null;

		if (Build.VERSION.SDK_INT < 11) {
			n = new Notification();

			n.flags |= Notification.FLAG_SHOW_LIGHTS;
			n.flags |= Notification.FLAG_AUTO_CANCEL;
			n.defaults |= Notification.DEFAULT_SOUND;
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
			builder.setDefaults(Notification.DEFAULT_SOUND);
			if (vibrate)
				builder.setVibrate(vibrateTag);

			n = builder.getNotification();
		}

		int notifyId;
		try {
			notifyId = Integer.valueOf(AnUtils.getCurrentUsername());
		} catch (Exception e) {
			notifyId = 123456789;
		}
		notifManager.notify(notifyId, n);
	}

	// 清除通知中心提醒
	public static void clearNotifyIcon(Context context) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager;
		mNotificationManager = (NotificationManager) context
				.getSystemService(ns);
		try {
			mNotificationManager.cancel(Integer.valueOf(AnUtils
					.getCurrentUsername()));
		} catch (Exception e) {
			mNotificationManager.cancel(123456789);
		}
	}

	// 添加预置的客服信息
	private static void addAngelSession() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					List<Session> sessions = DBManager.getAngelSession(AnUtils
							.getCurrentClientId());
					if (sessions != null && sessions.size() > 0) {
						return;
					}

					JSONObject params = new JSONObject();
					params.put("username", "Angel");
					MRMWrapper.getMRM(mainActivity).sendPostRequest(
							mainActivity, "users/search", params,
							new MRMJSONResponseHandler() {

								@Override
								public void onFailure(Throwable e,
										JSONObject response) {
									super.onFailure(e, response);
								}

								@Override
								public void onSuccess(int statusCode,
										JSONObject response) {
									try {
										JSONArray usersJsonArray = response
												.getJSONObject("response")
												.getJSONArray("users");
										if (usersJsonArray.length() > 0) {
											JSONObject user = usersJsonArray
													.getJSONObject(0);
											String clientId = user
													.getJSONObject(
															"customFields")
													.getString("clientId");
											DBManager.writeUser("Angel",
													"Angel", clientId);
											List<String> clientIds = new ArrayList<String>();
											clientIds.add(clientId);
											DBManager.addSession(clientIds,
													"Angel",
													"2013-10-10 00:00",
													"你好，箭扣者", "unread");

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
}
