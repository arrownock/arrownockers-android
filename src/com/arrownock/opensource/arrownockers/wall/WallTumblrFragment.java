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

public class WallTumblrFragment extends ListFragment {

	private PullToRefreshListView pullToRefreshListView = null;
	private static WallTumblrListAdapter wallTumblrListAdapter = null;
	private static List<WallTumblrEntity> wallTumblrEntities = null;

	private MRM mrm = null;
	private Context ctx = null;

	private boolean isRefreshing = false;
	private boolean notInit = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_wall_list, container, false);
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
			initData();
		}

	}

	@Override
	public void onStop() {
		super.onStop();

		notInit = false;
	}

	private void initView() {
		pullToRefreshListView = (PullToRefreshListView) getView().findViewById(
				R.id.lv_wall);
		pullToRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {

					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						isRefreshing = true;
						initData();
					}

				});
		pullToRefreshListView.getRefreshableView().setOnItemClickListener(
				new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						showPostListActivity(view, R.id.lv_wall);
					}
				});

		if (wallTumblrEntities == null) {
			wallTumblrEntities = new ArrayList<WallTumblrEntity>();
		}

		if (wallTumblrListAdapter == null) {
			wallTumblrListAdapter = new WallTumblrListAdapter(getActivity(),
					wallTumblrEntities);
		}

		setListAdapter(wallTumblrListAdapter);
	}

	private void initData() {
		wallTumblrEntities.clear();

		ctx = getActivity();
		mrm = MRMWrapper.getMRM(ctx);

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				JSONObject params = new JSONObject();
				try {
					params.put("type", "tumblr");
					params.put("pagesize", 10);
					params.put("sort", "-created_at");

					// 查找type为tumblr，按照时间倒序排列的前十条记录
					// 返回数据的具体格式请参考文档详细说明
					mrm.sendPostRequest(ctx, "walls/search", params,
							new MRMJSONResponseHandler() {

								@Override
								public void onFailure(Throwable e,
										JSONObject response) {
									handleInitDataComplete(
											"获取消息墙信息失败: " + e.getMessage(),
											true);
								}

								@Override
								public void onSuccess(int statusCode,
										JSONObject response) {
									try {
										JSONObject responseJsonObject = response
												.getJSONObject("response");
										if (responseJsonObject.getInt("count") < 1) {
											handleInitDataComplete("没有更多的消息墙",
													true);
											return;
										}
										JSONArray wallsArray = responseJsonObject
												.getJSONArray("walls");
										for (int i = 0; i < wallsArray.length(); i++) {
											JSONObject wall = (JSONObject) wallsArray
													.get(i);
											WallTumblrEntity wallEntity = new WallTumblrEntity();
											wallEntity.wallId = wall
													.getString("id");
											wallEntity.title = wall
													.getString("title");
											if (wall.has("customFields")) {
												if (wall.getJSONObject(
														"customFields").has(
														"imageURL")) {
													wallEntity.imageURLString = wall
															.getJSONObject(
																	"customFields")
															.getString(
																	"imageURL");
												}
											}

											wallTumblrEntities.add(wallEntity);
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
						wallTumblrListAdapter.notifyDataSetChanged();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showPostListActivity(View view, int key) {
		String wallId = view.getTag(key).toString();

		Intent intent = new Intent(getActivity(), PostListActivity.class);
		intent.putExtra("wallId", wallId);
		intent.putExtra("fromWhere", "wall");

		startActivity(intent);
	}

}
