package com.arrownock.opensource.arrownockers.utils;

import com.arrownock.opensource.arrownockers.chat.ChatActivity;
import com.arrownock.opensource.arrownockers.push.PushSendActivity;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

public class MapCallback implements BDLocationListener {

	private static ChatActivity chatActivity;
	private static PushSendActivity pushSendActivity;

	@Override
	public void onReceiveLocation(BDLocation location) {
		if (location != null) {

			if (pushSendActivity != null) {
				pushSendActivity.onLocationUpdated(location);
			}

			if (chatActivity != null) {
				chatActivity.onLocationUpdated(location);
			}
		}
	}

	@Override
	public void onReceivePoi(BDLocation location) {

	}

	public static void setChatActivity(ChatActivity ca) {
		chatActivity = ca;
	}

	public static void setPushSendActivity(PushSendActivity psa) {
		pushSendActivity = psa;
	}

	public interface OnLocationUpdated {
		public void onLocationUpdated(BDLocation location);
	}
}
