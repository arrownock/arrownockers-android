package com.arrownock.opensource.arrownockers.wall;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.MRMWrapper;
import com.arrownock.mrm.MRMJSONResponseHandler;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class CommentListActivity extends SherlockActivity {

	private PullToRefreshListView pullToRefreshListView;
	private CommentListAdapter commentListAdapter;
	private ArrayList<CommentEntity> commentEntities = new ArrayList<CommentEntity>();
	private String postId = null;
	private String ownerId = null;
	private boolean canComment = false;
	private Bundle bundle = null;

	private boolean isRefreshing = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_comment_list);

		bundle = this.getIntent().getExtras();
		postId = bundle.getString("parentId");
		ownerId = bundle.getString("ownerId");
		canComment = bundle.getBoolean("canComment");

		initView();

		initData(postId, null);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {

		if (ownerId.equals(AnUtils.getCurrentUserId()) || !canComment) {
			return false;
		}

		getSupportMenuInflater().inflate(R.menu.comment_list, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {

		case R.id.action_add_comment:
			showCommentAddDialog(postId);

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initView() {
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.color.blue_color));

		pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_comment);
		pullToRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {

					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						isRefreshing = true;
						initData(postId, null);
					}

				});
		commentListAdapter = new CommentListAdapter(this, commentEntities);
		pullToRefreshListView.setAdapter(commentListAdapter);

		View footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.row_footer, null, false);
		footerView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (commentEntities != null && commentEntities.size() > 0) {
					CommentEntity commentEntity = commentEntities
							.get(commentEntities.size() - 1);
					String timeString = commentEntity.dateString;
					initData(postId, timeString);
				}
			}
		});
		pullToRefreshListView.getRefreshableView().addFooterView(footerView);
	}

	public void initData(String postId, String timeString) {
		if (timeString == null) {
			commentEntities.clear();
		}

		try {
			JSONObject params = new JSONObject();
			params.put("parentId", postId);
			params.put("pagesize", 10);
			params.put("sort", "-created_at");
			if (timeString != null) {
				params.put("where", "{\"created_at\":{\"$lt\":\"" + timeString
						+ "\"}}");
			}

			// 查找Comment，按照时间倒序排列的前十条记录
			// 返回数据的具体格式请参考文档详细说明
			MRMWrapper.getMRM(getBaseContext()).sendPostRequest(
					getBaseContext(), "comments/search", params,
					new MRMJSONResponseHandler() {

						@Override
						public void onFailure(Throwable e, JSONObject response) {
							handleInitDataComplete(
									"获取Comment失败: " + e.getMessage(), true);
						}

						@Override
						public void onSuccess(int statusCode,
								JSONObject response) {
							try {
								JSONObject responseJsonObject = response
										.getJSONObject("response");
								if (responseJsonObject.getInt("count") < 1) {
									handleInitDataComplete("没有更多的Comment", true);
									return;
								}
								JSONArray commentsArray = responseJsonObject
										.getJSONArray("comments");
								for (int i = 0; i < commentsArray.length(); i++) {
									JSONObject comment = (JSONObject) commentsArray
											.get(i);
									CommentEntity commentEntity = new CommentEntity();
									commentEntity.commentId = comment
											.getString("id");
									if (comment.has("content")) {
										commentEntity.contentText = comment
												.getString("content");
									} else {
										commentEntity.contentText = "无内容";
									}

									commentEntity.dateString = comment
											.getString("created_at");

									commentEntities.add(commentEntity);
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

	public void showCommentAddDialog(final String postId) {
		final CommentAddDialog dialog = new CommentAddDialog(this);
		dialog.setPositiveButton(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String content = dialog.getContent();
				if (content != null && content.trim().length() > 0) {
					Thread thread = new Thread(new Runnable() {

						@Override
						public void run() {
							createComment(dialog, postId);
						}
					});

					thread.start();
				} else {
					AnUtils.showToast(CommentListActivity.this, "输入不能为空",
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

	private void handleInitDataComplete(final String message,
			final boolean isError) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (isError) {
					AnUtils.showToast(CommentListActivity.this, message,
							Gravity.CENTER, true);
				}
				if (isRefreshing) {
					pullToRefreshListView.onRefreshComplete();
					isRefreshing = false;
				}
				if (!isError) {
					commentListAdapter.notifyDataSetChanged();
				}
			}
		});
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
								Toast.makeText(getBaseContext(),
										"添加Comment失败: " + e.getMessage(),
										Toast.LENGTH_SHORT).show();
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
															Toast.makeText(
																	getBaseContext(),
																	"添加Like失败: "
																			+ e.getMessage(),
																	Toast.LENGTH_SHORT)
																	.show();
														}

														@Override
														public void onSuccess(
																int statusCode,
																JSONObject response) {
															dialog.dismiss();
															initData(postId,
																	null);
														}

													});
								} catch (Exception e) {
									Toast.makeText(getBaseContext(),
											"添加Like失败: " + e.getMessage(),
											Toast.LENGTH_SHORT).show();
									e.printStackTrace();
								}
							}
						});
			} catch (Exception e) {
				Toast.makeText(getBaseContext(),
						"添加Comment失败: " + e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				e.printStackTrace();
			}
		}
	}

}
