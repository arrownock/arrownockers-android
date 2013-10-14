package com.arrownock.opensource.arrownockers.utils;

import java.util.List;

import org.json.JSONObject;

public interface AnIMWrapperCallback {
	public void getTopicsDone(List<JSONObject> topicList);

	public void onCheckIfMyTopic(boolean isMine);
}
