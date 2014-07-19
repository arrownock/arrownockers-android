package com.arrownock.opensource.arrownockers.topic;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.AnIMWrapper;
import com.arrownock.opensource.arrownockers.utils.AnIMWrapperCallback;
import com.arrownock.opensource.arrownockers.utils.DBManager;
import com.arrownock.opensource.arrownockers.utils.MainActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class TopicListActivity extends Activity {

	private static final String logTag = "TopicListActivity";

	private AnIMWrapperCallback anIMWrapperCallback;

	private PullToRefreshListView pullToRefreshListView;
	private TopicListAdapter topicListAdapter;
	private List<TopicEntity> topicEntities = new ArrayList<TopicEntity>();

	private boolean hasInitTopicList = false;
	private boolean isRefreshing = false;
	private boolean alive = true;
	private boolean willShowMessageListActivity = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_topic_list);

		if (getIntent().getBooleanExtra("fromPush", false)) {
			AnIMWrapper.init(TopicListActivity.this, AnUtils.AppKey);
		}
		AnIMWrapper.getWrapper().connectIfOffline();

		initView();
		initCallback();
		initData();

		if (getIntent().getStringExtra("where") != null) {
			AnUtils.clearNotifyIcon(getBaseContext());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.i(logTag, "onDestroy");

		alive = false;

		if (!MainActivity.alive) {
			AnIMWrapper.getWrapper().disconnect();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		Log.i(logTag, "onStop willShowMessageListActivity: "
				+ willShowMessageListActivity);

		alive = false;
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		Log.i(logTag, "onRestart willShowMessageListActivity: "
				+ willShowMessageListActivity);

		alive = true;

		refreshTopics();
	}

	private void initView() {
		pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_topic);
		pullToRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {

					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						if (isRefreshing) {
							return;
						}
						if (hasInitTopicList) {
							isRefreshing = true;
							retriveTopics();
						} else {
							pullToRefreshListView.onRefreshComplete();
						}
					}

				});
		pullToRefreshListView.getRefreshableView().setOnItemClickListener(
				new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						showMessageListActivity(position - 1);
					}
				});

		topicEntities = new ArrayList<TopicEntity>();

		topicListAdapter = new TopicListAdapter(this, topicEntities);

		pullToRefreshListView.setAdapter(topicListAdapter);

		refreshTopics();
	}

	private void initCallback() {
		AnIMWrapper.topicListActivity = this;

		anIMWrapperCallback = new AnIMWrapperCallback() {

			@Override
			public void getTopicsDone(List<JSONObject> topicList) {
				if (topicList != null) {
					Log.i(logTag, "getTopicsDone");

					handleTopicsRetrived(topicList);

				} else {
					Log.i(logTag, "getTopicsDone: null");
				}

				hasInitTopicList = true;
			}

			@Override
			public void onCheckIfMyTopic(boolean isMine) {
			}
		};
	}

	private void initData() {
		retriveTopics();
	}

	private void retriveTopics() {
		AnIMWrapper.getWrapper().getTopics(anIMWrapperCallback);
	}

	private void refreshTopics() {
		List<Topic> topics = DBManager.readAllTopics();
		if (topics == null) {
			return;
		}

		topicEntities.clear();

		for (Topic topic : topics) {
			TopicEntity topicEntity = new TopicEntity();
			topicEntity.topicId = topic.topicId;
			topicEntity.topicName = topic.topicName;
			topicEntity.count = topic.count;
			topicEntity.lastMessage = topic.lastMessage;
			topicEntity.lastTimeString = AnUtils.getTimeString(topic.lastTime);
			topicEntity.unread = topic.unread;

			topicEntities.add(topicEntity);
		}

		runOnUiThread(new Runnable() {
			public void run() {
				pullToRefreshListView.onRefreshComplete();
				isRefreshing = false;
				topicListAdapter.notifyDataSetChanged();
			}
		});
	}

	private void handleTopicsRetrived(List<JSONObject> topicsJsonObjects) {

		DBManager.overwriteAllTopics(topicsJsonObjects);

		refreshTopics();
	}

	private void showMessageListActivity(int index) {
		String topicId = topicEntities.get(index).topicId;

		Log.i(logTag, "showMessageListActivity topicId: " + topicId);

		Intent intent = new Intent(this, MessageListActivity.class);
		intent.putExtra("topicId", topicId);

		willShowMessageListActivity = true;

		startActivity(intent);
	}

	public void onMessage(Message message) {
		if (!alive && !willShowMessageListActivity) {
			AnUtils.showNotification(getBaseContext(), "topic");
			return;
		}

		if (alive) {
			refreshTopics();
		}
	}
}
