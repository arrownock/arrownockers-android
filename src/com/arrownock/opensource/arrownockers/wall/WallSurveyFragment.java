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

public class WallSurveyFragment extends ListFragment {
	private PullToRefreshListView pullToRefreshListView = null;
	private static WallSurveyListAdapter wallSurveyListAdapter = null;
	private static List<WallSurveyEntity> wallSurveyEntities = null;

	private MRM mrm = null;
	private Context ctx = null;

	private boolean isRefreshing = false;
	private boolean notInit = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_wall_survey_list, container,
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
				R.id.lv_surveys);
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
						showWallNewsActivity(view, R.id.lv_surveys);
					}
				});

		if (wallSurveyEntities == null) {
			wallSurveyEntities = new ArrayList<WallSurveyEntity>();
		}

		if (wallSurveyListAdapter == null) {
			wallSurveyListAdapter = new WallSurveyListAdapter(getActivity(),
					wallSurveyEntities);
		}

		setListAdapter(wallSurveyListAdapter);

		View footerView = ((LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.row_footer,
				null, false);
		footerView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (wallSurveyEntities != null && wallSurveyEntities.size() > 0) {
					WallSurveyEntity wallSurveyEntity = wallSurveyEntities
							.get(wallSurveyEntities.size() - 1);
					String timeString = wallSurveyEntity.dateString;
					initData(timeString);
				}
			}
		});
		pullToRefreshListView.getRefreshableView().addFooterView(footerView);
	}

	private void initData(final String timeString) {
		if (timeString == null) {
			wallSurveyEntities.clear();
		}

		ctx = getActivity().getBaseContext();
		mrm = MRMWrapper.getMRM(ctx);

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				JSONObject params = new JSONObject();

				try {
					params.put("type", "survey");
					params.put("pagesize", 10);
					params.put("sort", "-created_at");
					if (timeString != null) {
						params.put("where", "{\"created_at\":{\"$lt\":\""
								+ timeString + "\"}}");
					}

					// 查找type为survey，按照时间倒序排列的前十条记录
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
											handleInitDataComplete("没有更多的调查问卷",
													true);
											return;
										}
										JSONArray postsArray = responseJsonObject
												.getJSONArray("posts");
										for (int i = 0; i < postsArray.length(); i++) {
											JSONObject post = (JSONObject) postsArray
													.get(i);
											WallSurveyEntity wallSurveyEntity = new WallSurveyEntity();
											wallSurveyEntity.postId = post
													.getString("id");
											wallSurveyEntity.title = post
													.getString("title");
											wallSurveyEntity.content = post
													.getString("content");
											wallSurveyEntity.dateString = post
													.getString("created_at");
											if (post.has("customFields")) {
												if (post.getJSONObject(
														"customFields").has(
														"imageURL")) {
													wallSurveyEntity.imageURLString = post
															.getJSONObject(
																	"customFields")
															.getString(
																	"imageURL");
												}
												if (post.getJSONObject(
														"customFields").has(
														"choices")) {
													JSONArray choicesJsonArray = post
															.getJSONObject(
																	"customFields")
															.getJSONArray(
																	"choices");
													wallSurveyEntity.choices = new ArrayList<String>();
													for (int j = 0; j < choicesJsonArray
															.length(); j++) {
														wallSurveyEntity.choices
																.add(choicesJsonArray
																		.getString(j));
													}
												}
											}

											wallSurveyEntities
													.add(wallSurveyEntity);
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
						wallSurveyListAdapter.notifyDataSetChanged();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showWallNewsActivity(View view, int key) {
		WallSurveyEntity entity = (WallSurveyEntity) view.getTag(key);

		Intent intent = new Intent(getActivity(), WallSurveyActivity.class);
		intent.putExtra("postId", entity.postId);
		intent.putExtra("title", entity.title);
		intent.putExtra("content", entity.content);
		if (entity.imageURLString != null) {
			intent.putExtra("imageURLString", entity.imageURLString);
		}

		intent.putStringArrayListExtra("choices", entity.choices);

		getActivity().startActivity(intent);
	}
}
