package com.arrownock.opensource.arrownockers.utils;

import android.content.Context;
import android.util.Log;

import com.arrownock.exception.ArrownockException;
import com.arrownock.push.AnPush;
import com.arrownock.push.AnPushCallbackAdapter;

public class AnPushCallback extends AnPushCallbackAdapter {
	private final String logTag = "AnPushCallback";
	private Context ctx = null;

	public AnPushCallback(Context c) {
		ctx = c;
	}

	@Override
	public void register(boolean err, String anid, ArrownockException exception) {
		if (!err) {
			try {
				AnPush.getInstance(ctx).enable();
				Log.i(logTag, "注册启动Push服务成功");
			} catch (ArrownockException e) {
				e.printStackTrace();
				Log.i(logTag, "注册启动Push服务失败");
			}

			AnUtils.initAnIM(ctx, anid);

		} else {
			Log.i(logTag, "注册Push服务失败");
		}
	}
}
