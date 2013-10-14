package com.arrownock.opensource.arrownockers.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.arrownock.opensource.arrownockers.utils.AnIMWrapper;
import com.arrownock.opensource.arrownockers.utils.DBManager;
import com.arrownock.opensource.arrownockers.utils.DBManager.Session;
import com.arrownock.opensource.arrownockers.utils.DBManager.User;
import com.arrownock.opensource.arrownockers.utils.MRMWrapper;
import com.arrownock.opensource.arrownockers.utils.MainActivity;
import com.arrownock.mrm.MRMJSONResponseHandler;

public class SessionActivity extends Activity {

	private static final String logTag = "SessionActivity";

	private SessionListViewAdapter sessionListViewAdapter;
	private List<SessionEntity> sessionEntities;

	public boolean alive = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_session_list);

		if (AnIMWrapper.getWrapper() == null) {
			AnIMWrapper.init(SessionActivity.this, AnUtils.AppKey);
		}
		AnIMWrapper.getWrapper().connectIfOffline();

		initView();
		initData();

		alive = true;
		AnIMWrapper.sessionActivity = this;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		alive = true;
		initData();
	}

	@Override
	protected void onStop() {
		super.onStop();
		alive = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		alive = false;
		AnIMWrapper.sessionActivity = null;

		if (!MainActivity.alive) {
			AnIMWrapper.getWrapper().disconnect();
		}
	}

	public void initView() {
		TextView tv = (TextView) findViewById(R.id.tv_id);
		if (AnUtils.getCurrentClientId() != null
				&& AnUtils.getCurrentUsername() != null) {
			tv.setText("ID: " + AnUtils.getCurrentUsername());
		} else {
			tv.setText("ID: " + "未连接服务器");
		}

		sessionEntities = new ArrayList<SessionEntity>();

		ListView sessionListView = (ListView) findViewById(R.id.lv);
		sessionListViewAdapter = new SessionListViewAdapter(this,
				sessionEntities);
		sessionListView.setAdapter(sessionListViewAdapter);

		sessionListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				Bundle bundle = new Bundle();
				bundle.putString("clientIds",
						sessionEntities.get(position).clientIds);
				bundle.putString("realnames",
						sessionEntities.get(position).realnames);
				Intent intent = new Intent(SessionActivity.this,
						ChatActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	public void initData() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				sessionEntities.clear();

				List<Session> sessionList = DBManager.getSessions(AnUtils
						.getCurrentClientId());
				if (sessionList == null) {
					return;
				}
				for (Session session : sessionList) {
					SessionEntity entity = new SessionEntity();
					entity.clientIds = session.clientIds;
					entity.realnames = session.realnames;
					entity.status = session.status;
					entity.updateTime = session.updateTime;
					entity.lastMessage = session.lastMessage;
					entity.id = session.id;
					sessionEntities.add(entity);
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						sessionListViewAdapter.notifyDataSetChanged();
					}
				});
			}
		});

		thread.start();
	}

	private void showIDInputView() {
		final EditText et = new EditText(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("请输入ID").setIcon(android.R.drawable.ic_dialog_info)
				.setView(et)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String id = null;
						try {
							id = et.getText().toString();
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}

						if (id.equals(AnUtils.getCurrentUsername())) {
							AnUtils.showToast(SessionActivity.this,
									"不允许使用当前用户ID", Gravity.CENTER, alive);
							return;
						}
						try {
							JSONObject params = new JSONObject();
							params.put("username", id);
							params.put("sort", "-created_at");

							MRMWrapper.getMRM(getBaseContext())
									.sendPostRequest(getBaseContext(),
											"users/search", params,
											new MRMJSONResponseHandler() {

												@Override
												public void onFailure(
														Throwable arg0,
														JSONObject arg1) {
													AnUtils.showToast(
															SessionActivity.this,
															"操作失败",
															Gravity.CENTER,
															alive);
												}

												@Override
												public void onSuccess(int arg0,
														JSONObject arg1) {
													try {
														JSONArray array = arg1
																.getJSONObject(
																		"response")
																.getJSONArray(
																		"users");
														if (array.length() == 0) {
															AnUtils.showToast(
																	SessionActivity.this,
																	"ID不存在",
																	Gravity.CENTER,
																	alive);
														} else {
															try {
																JSONObject userjsObject = (JSONObject) array
																		.get(0);
																String username = userjsObject
																		.getString("username");
																String realname = null;
																if (userjsObject
																		.has("realname")) {
																	realname = userjsObject
																			.getString("realname");
																} else {
																	realname = username;
																}

																String clientId = null;
																if (userjsObject
																		.has("customFields")) {
																	JSONObject customFields = userjsObject
																			.getJSONObject("customFields");
																	if (customFields
																			.has("clientId")) {
																		clientId = customFields
																				.getString("clientId");
																	}
																}

																onGetOtherClientIdDone(
																		username,
																		realname,
																		clientId);
															} catch (Exception e) {
																e.printStackTrace();
															}

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
				}).setNegativeButton("取消", null).show();
	}

	public void onGetOtherClientIdDone(final String username,
			final String realname, final String clientId) {
		if (clientId != null) {
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					DBManager.writeUser(username, realname, clientId);
					List<String> clientIds = new ArrayList<String>();
					clientIds.add(clientId);
					DBManager.addSession(clientIds);

					Bundle bundle = new Bundle();
					bundle.putString("clientIds", clientId);
					User user = DBManager.readUserByClientId(clientId);
					if (user == null) {
						return;
					}
					bundle.putString("realnames", user.realname);
					Intent intent = new Intent(SessionActivity.this,
							ChatActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			thread.start();
		} else {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					AnUtils.showToast(SessionActivity.this, "该用户未开启IM功能",
							Gravity.CENTER, alive);
				}
			});
		}
	}

	public void onAddSessionButtonClick(View view) {
		Log.i(logTag, "onAddSessionButtonClick");

		if (AnUtils.getCurrentClientId() == null) {
			AnUtils.showToast(SessionActivity.this, "尚未初始化IM服务",
					Gravity.CENTER, true);

			return;
		}

		new AlertDialog.Builder(this)
				.setNegativeButton("随机分配",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									int which) {
								try {
									JSONObject params = new JSONObject();
									String usernames = "{\"username\":{\"$ne\":\""
											+ AnUtils.getCurrentUsername()
											+ "\"}}";
									params.put("where", usernames);
									params.put("sort", "-created_at");

									MRMWrapper
											.getMRM(getBaseContext())
											.sendPostRequest(
													getBaseContext(),
													"users/search",
													params,
													new MRMJSONResponseHandler() {

														@Override
														public void onFailure(
																Throwable arg0,
																JSONObject arg1) {

															dialog.dismiss();
														}

														@Override
														public void onSuccess(
																int arg0,
																JSONObject arg1) {
															try {
																int randomIndex = new Random()
																		.nextInt(10);
																int maxIndex = arg1
																		.getJSONObject(
																				"response")
																		.getJSONArray(
																				"users")
																		.length() - 1;
																if (randomIndex > maxIndex) {
																	randomIndex = maxIndex;
																}
																JSONObject userJsonObject = (JSONObject) (arg1
																		.getJSONObject(
																				"response")
																		.getJSONArray(
																				"users")
																		.get(randomIndex));
																String username = userJsonObject
																		.getString("username");
																String realname = null;
																if (userJsonObject
																		.has("realname")) {
																	realname = userJsonObject
																			.getString("realname");
																} else {
																	realname = username;
																}

																String clientId = null;
																if (userJsonObject
																		.has("customFields")) {
																	JSONObject customFields = userJsonObject
																			.getJSONObject("customFields");
																	if (customFields
																			.has("clientId")) {
																		clientId = customFields
																				.getString("clientId");
																	}
																}

																onGetOtherClientIdDone(
																		username,
																		realname,
																		clientId);
															} catch (Exception e) {
																e.printStackTrace();
															}
															dialog.dismiss();
														}

													});
								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						})
				.setPositiveButton("输入ID",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showIDInputView();
								dialog.dismiss();
							}
						}).show();
	}

}
