package com.arrownock.opensource.arrownockers.wall;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.MRMWrapper;
import com.arrownock.mrm.MRMJSONResponseHandler;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class PostListActivity extends SherlockActivity {

	private PullToRefreshListView pullToRefreshListView;
	private PostListAdapter postListAdapter;
	private ArrayList<PostEntity> postEntities = new ArrayList<PostEntity>();
	private String wallId = null;
	private String fromWhere = null;

	private boolean isRefreshing = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_post_list);

		initView();

		initData(wallId, null);
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		initData(wallId, null);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {

		getSupportMenuInflater().inflate(R.menu.post_list_from_wall, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_add_post:
			showPostAddActivity(null);
			break;
		default:
			break;
		}
		return true;
	}

	private void initView() {
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.color.blue_color));

		pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_post);

		pullToRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {

					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						isRefreshing = true;
						initData(wallId, null);
					}

				});

		postListAdapter = new PostListAdapter(this, postEntities);
		pullToRefreshListView.setAdapter(postListAdapter);

		wallId = getIntent().getStringExtra("wallId");
		fromWhere = getIntent().getStringExtra("fromWhere");

		View footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.row_footer, null, false);
		footerView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (postEntities != null && postEntities.size() > 0) {
					PostEntity postEntity = postEntities.get(postEntities
							.size() - 1);
					String timeString = postEntity.dateString;
					initData(wallId, timeString);
				}
			}
		});
		pullToRefreshListView.getRefreshableView().addFooterView(footerView);
	}

	private void initData(final String wallId, final String timeString) {
		if (timeString == null) {
			postEntities.clear();
		}

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					JSONObject params = new JSONObject();
					params.put("parentId", wallId);
					params.put("sort", "-created_at");
					params.put("pagesize", 10);
					if (timeString != null) {
						params.put("where", "{\"created_at\":{\"$lt\":\""
								+ timeString + "\"}}");
					}

					// 查找指定消息墙的Post，按照时间倒序排列的前十条记录
					// 返回数据的具体格式请参考文档详细说明
					MRMWrapper.getMRM(getBaseContext()).sendPostRequest(
							getBaseContext(), "posts/search", params,
							new MRMJSONResponseHandler() {

								@Override
								public void onFailure(Throwable e,
										JSONObject response) {
									handleInitDataComplete(
											"获取Post失败: " + e.getMessage(), true);
								}

								@Override
								public void onSuccess(int statusCode,
										JSONObject response) {
									try {
										JSONObject responseJsonObject = response
												.getJSONObject("response");
										if (responseJsonObject.getInt("count") < 1) {
											handleInitDataComplete("没有更多的Post",
													true);
											return;
										}
										JSONArray postsArray = responseJsonObject
												.getJSONArray("posts");
										for (int i = 0; i < postsArray.length(); i++) {
											JSONObject post = (JSONObject) postsArray
													.get(i);
											PostEntity postEntity = new PostEntity();
											postEntity.ownerId = post
													.getString("ownerId");
											postEntity.postId = post
													.getString("id");
											postEntity.contentText = post
													.getString("content");
											postEntity.dateString = post
													.getString("created_at");
											if (post.has("commentCount")) {
												postEntity.commentCount = post
														.getString("commentCount");
											}
											if (post.has("likeCount")) {
												postEntity.likeCount = post
														.getString("likeCount");
											}
											if (post.has("dislikeCount")) {
												postEntity.dislikeCount = post
														.getString("dislikeCount");
											}
											if (post.has("customFields")) {
												if (post.getJSONObject(
														"customFields").has(
														"imageURL")) {
													postEntity.imageURLString = post
															.getJSONObject(
																	"customFields")
															.getString(
																	"imageURL");
												}
												if (post.getJSONObject(
														"customFields").has(
														"username")) {
													postEntity.username = post
															.getJSONObject(
																	"customFields")
															.getString(
																	"username");
												}

											}
											if (fromWhere.equals("wall")
													&& !postEntity.ownerId.equals(AnUtils
															.getCurrentUserId())) {
												postEntity.canComment = true;
											}

											postEntities.add(postEntity);
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
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (isError) {
					AnUtils.showToast(PostListActivity.this, message,
							Gravity.CENTER, true);
				}
				if (isRefreshing) {
					pullToRefreshListView.onRefreshComplete();
					isRefreshing = false;
				}
				if (!isError) {
					postListAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	public void showPostAddActivity(View view) {
		Bundle bundle = new Bundle();
		bundle.putString("parentId", wallId);
		bundle.putString("parentType", "Wall");
		Intent intent = new Intent(this, PostAddActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	public void showCommentListActivity(View view) {
		PostEntity postEntity = (PostEntity) view.getTag();
		Bundle bundle = new Bundle();
		bundle.putBoolean("canComment", postEntity.canComment);
		bundle.putString("ownerId", postEntity.ownerId);
		bundle.putString("content", postEntity.contentText);
		bundle.putString("date", postEntity.dateString);
		bundle.putString("commentCount", postEntity.commentCount);
		bundle.putString("likeCount", postEntity.likeCount);
		bundle.putString("dislikeCount", postEntity.dislikeCount);
		bundle.putString("imageURLString", postEntity.imageURLString);
		bundle.putString("parentId", postEntity.postId);
		bundle.putString("parentType", "Post");
		Intent intent = new Intent(this, CommentListActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	public void showCommentAddDialog(View view) {
		final CommentAddDialog dialog = new CommentAddDialog(this);
		final String parentId = view.getTag().toString();
		dialog.setPositiveButton(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String content = dialog.getContent();
				if (content != null && content.trim().length() > 0) {
					Thread thread = new Thread(new Runnable() {

						@Override
						public void run() {
							createComment(dialog, parentId);
						}
					});

					thread.start();

				} else {
					AnUtils.showToast(PostListActivity.this, "输入不能为空",
							Gravity.CENTER, true);
				}
			}
		});
		dialog.setNegativeButton(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void createComment(final CommentAddDialog dialog,
			final String parentId) {
		if (dialog.getContent() != null) {
			try {
				JSONObject params = new JSONObject();
				params.put("parentType", "Post");
				params.put("parentId", parentId);
				params.put("content", dialog.getContent());
				
				// 创建Comment（评论）
				// 参数的具体格式请参考文档详细说明
				MRMWrapper.getMRM(getBaseContext()).sendPostRequest(
						getBaseContext(), "comments/create", params,
						new MRMJSONResponseHandler() {

							@Override
							public void onFailure(Throwable e,
									JSONObject response) {
								AnUtils.showToast(PostListActivity.this,
										"添加Comment失败: " + e.getMessage(),
										Gravity.CENTER, true);
							}

							@Override
							public void onSuccess(int statusCode,
									JSONObject response) {
								try {
									JSONObject params = new JSONObject();
									params.put("positive", dialog.getLike());
									params.put("parentType", "Post");
									params.put("parentId", parentId);
									
									// 创建Like（赞）
									// 参数的具体格式请参考文档详细说明
									MRMWrapper
											.getMRM(getBaseContext())
											.sendPostRequest(
													getBaseContext(),
													"likes/create",
													params,
													new MRMJSONResponseHandler() {

														@Override
														public void onFailure(
																Throwable e,
																JSONObject response) {
															AnUtils.showToast(
																	PostListActivity.this,
																	"添加Comment失败: "
																			+ e.getMessage(),
																	Gravity.CENTER,
																	true);
														}

														@Override
														public void onSuccess(
																int statusCode,
																JSONObject response) {
															dialog.dismiss();
															initData(wallId,
																	null);
														}

													});
								} catch (Exception e) {
									AnUtils.showToast(PostListActivity.this,
											"添加Comment失败: " + e.getMessage(),
											Gravity.CENTER, true);
									e.printStackTrace();
								}
							}
						});
			} catch (Exception e) {
				AnUtils.showToast(PostListActivity.this,
						"添加Comment失败: " + e.getMessage(), Gravity.CENTER, true);
				e.printStackTrace();
			}
		}
	}
}
