package com.arrownock.opensource.arrownockers.wall;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.MRMWrapper;
import com.arrownock.mrm.MRM;
import com.arrownock.mrm.MRMJSONResponseHandler;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class WallNewsFragment extends ListFragment {
	private PullToRefreshListView pullToRefreshListView = null;
	private static WallNewsListAdapter wallNewsListAdapter = null;
	private static List<WallNewsEntity> wallNewsEntities = null;

	private MRM mrm = null;
	private Context ctx = null;

	private boolean isRefreshing = false;
	private boolean notInit = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_wall_news_list, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initView();
	}

	@Override
	public void onStart() {
		super.onStart();

		if (notInit) {
			initData(null);
		}

	}

	@Override
	public void onStop() {
		super.onStop();

		notInit = false;
	}

	private void initView() {
		pullToRefreshListView = (PullToRefreshListView) getView().findViewById(
				R.id.lv_news);
		pullToRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {

					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						isRefreshing = true;
						initData(null);
					}

				});
		pullToRefreshListView.getRefreshableView().setOnItemClickListener(
				new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						showWallNewsActivity(view, R.id.lv_news);
					}
				});

		if (wallNewsEntities == null) {
			wallNewsEntities = new ArrayList<WallNewsEntity>();
		}

		if (wallNewsListAdapter == null) {
			wallNewsListAdapter = new WallNewsListAdapter(getActivity(),
					wallNewsEntities);
		}

		setListAdapter(wallNewsListAdapter);

		View footerView = ((LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.row_footer,
				null, false);
		footerView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (wallNewsEntities != null && wallNewsEntities.size() > 0) {
					WallNewsEntity wallNewsEntity = wallNewsEntities
							.get(wallNewsEntities.size() - 1);
					String timeString = wallNewsEntity.dateString;
					initData(timeString);
				}
			}
		});
		pullToRefreshListView.getRefreshableView().addFooterView(footerView);
	}

	private void initData(final String timeString) {
		if (timeString == null) {
			wallNewsEntities.clear();
		}

		ctx = getActivity().getBaseContext();
		mrm = MRMWrapper.getMRM(ctx);

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				JSONObject params = new JSONObject();

				try {
					params.put("type", "news");
					params.put("pagesize", 10);
					params.put("sort", "-created_at");
					if (timeString != null) {
						params.put("where", "{\"created_at\":{\"$lt\":\""
								+ timeString + "\"}}");
					}

					// 查找type为news，按照时间倒序排列的前十条记录
					// 返回数据的具体格式请参考文档详细说明
					mrm.sendPostRequest(ctx, "posts/search", params,
							new MRMJSONResponseHandler() {

								@Override
								public void onFailure(Throwable e,
										JSONObject response) {
									handleInitDataComplete(
											"获取失败: " + e.getMessage(), true);
								}

								@Override
								public void onSuccess(int statusCode,
										JSONObject response) {
									try {
										JSONObject responseJsonObject = response
												.getJSONObject("response");
										if (responseJsonObject.getInt("count") < 1) {
											handleInitDataComplete("没有更多的新闻",
													true);
											return;
										}
										JSONArray postsArray = responseJsonObject
												.getJSONArray("posts");
										for (int i = 0; i < postsArray.length(); i++) {
											JSONObject post = (JSONObject) postsArray
													.get(i);
											WallNewsEntity wallNewsEntity = new WallNewsEntity();
											wallNewsEntity.postId = post
													.getString("id");
											wallNewsEntity.title = post
													.getString("title");
											wallNewsEntity.content = post
													.getString("content");
											wallNewsEntity.dateString = post
													.getString("created_at");
											if (post.has("customFields")) {
												if (post.getJSONObject(
														"customFields").has(
														"imageURL")) {
													wallNewsEntity.imageURLString = post
															.getJSONObject(
																	"customFields")
															.getString(
																	"imageURL");
												}
											}

											wallNewsEntities
													.add(wallNewsEntity);
										}

										handleInitDataComplete("", false);

									} catch (Exception e) {
										handleInitDataComplete(
												"发生错误: " + e.getMessage(), true);
									}
								}

							});
				} catch (Exception e) {
					handleInitDataComplete("发生错误: " + e.getMessage(), true);
				}

			}
		});

		thread.start();

	}

	private void handleInitDataComplete(final String message,
			final boolean isError) {

		try {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (getActivity() != null && isError) {
						AnUtils.showToast(getActivity(), message,
								Gravity.CENTER, true);
					}

					if (isRefreshing) {
						pullToRefreshListView.onRefreshComplete();
						isRefreshing = false;
						notInit = false;
					}

					if (!isError) {
						wallNewsListAdapter.notifyDataSetChanged();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showWallNewsActivity(View view, int key) {
		WallNewsEntity entity = (WallNewsEntity) view.getTag(key);

		Intent intent = new Intent(getActivity(), WallNewsActivity.class);
		intent.putExtra("postId", entity.postId);
		intent.putExtra("title", entity.title);
		intent.putExtra("content", entity.content);
		intent.putExtra("imageURLString", entity.imageURLString);

		getActivity().startActivity(intent);
	}
}
