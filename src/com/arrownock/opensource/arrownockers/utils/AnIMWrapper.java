package com.arrownock.opensource.arrownockers.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.arrownock.opensource.arrownockers.chat.ChatActivity;
import com.arrownock.opensource.arrownockers.chat.SessionActivity;
import com.arrownock.opensource.arrownockers.topic.Message;
import com.arrownock.opensource.arrownockers.topic.MessageEntity.EntityType;
import com.arrownock.opensource.arrownockers.topic.MessageListActivity;
import com.arrownock.opensource.arrownockers.topic.TopicListActivity;
import com.arrownock.opensource.arrownockers.utils.DBManager.Chat;
import com.arrownock.exception.ArrownockException;
import com.arrownock.im.AnIM;
import com.arrownock.im.AnIMMessage;
import com.arrownock.im.AnIMMessageType;
import com.arrownock.im.AnIMStatus;
import com.arrownock.im.AnPushType;
import com.arrownock.im.callback.AnIMAddClientsCallbackData;
import com.arrownock.im.callback.AnIMBinaryCallbackData;
import com.arrownock.im.callback.AnIMBindAnPushServiceCallbackData;
import com.arrownock.im.callback.AnIMCreateTopicCallbackData;
import com.arrownock.im.callback.AnIMGetClientIdCallbackData;
import com.arrownock.im.callback.AnIMGetClientsStatusCallbackData;
import com.arrownock.im.callback.AnIMGetSessionInfoCallbackData;
import com.arrownock.im.callback.AnIMGetTopicInfoCallbackData;
import com.arrownock.im.callback.AnIMGetTopicListCallbackData;
import com.arrownock.im.callback.AnIMMessageCallbackData;
import com.arrownock.im.callback.AnIMMessageSentCallbackData;
import com.arrownock.im.callback.AnIMNoticeCallbackData;
import com.arrownock.im.callback.AnIMReadACKCallbackData;
import com.arrownock.im.callback.AnIMReceiveACKCallbackData;
import com.arrownock.im.callback.AnIMRemoveClientsCallbackData;
import com.arrownock.im.callback.AnIMStatusUpdateCallbackData;
import com.arrownock.im.callback.AnIMTopicBinaryCallbackData;
import com.arrownock.im.callback.AnIMTopicMessageCallbackData;
import com.arrownock.im.callback.AnIMUnbindAnPushServiceCallbackData;
import com.arrownock.im.callback.AnIMUpdateTopicCallbackData;
import com.arrownock.im.callback.IAnIMCallback;
import com.arrownock.im.callback.IAnIMHistoryCallback;
import com.arrownock.mrm.MRMJSONResponseHandler;

public class AnIMWrapper implements IAnIMCallback {

	private static final String logTag = "AnIMWrapper";

	private static AnIMWrapper wrapper;
	private AnIM anIM;
	private Map<String, Message> tmpMessages = new HashMap<String, Message>();

	private static AnIMWrapperCallback delegate;
	public static MessageListActivity messageListActivity;
	public static TopicListActivity topicListActivity;
	public static ChatActivity chatActivity;
	public static SessionActivity sessionActivity;

	public static boolean status = false;

	private boolean willDisconnect = false;
	static int retryCount = 1;
	private boolean willCheckIfMyTopic = false;

	public static String thisAnId;
	private static Context thisContext;

	// FIXME 以下注释代码用于监听网络连接状态的改变，从而执行重新连接服务器的操作
	// private static NetworkChangeMonitor monitor = null;

	// private class NetworkChangeMonitor extends BroadcastReceiver {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// String action = intent.getAction();
	// if (TextUtils
	// .equals(action, "android.net.conn.CONNECTIVITY_CHANGE")) {
	// if (ATUtils.getCurrentUsername() != null) {
	// getWrapper().getClientId(ATUtils.getCurrentUsername());
	// }
	// }
	// }
	// }

	// public void unregisterMonitor() {
	// try {
	// thisContext.unregisterReceiver(monitor);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	// private void registerDataTransReceiver() {
	// try {
	// IntentFilter filter = new IntentFilter();
	// filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
	// filter.setPriority(1000);
	//
	// unregisterMonitor();
	// monitor = new NetworkChangeMonitor();
	// thisContext.registerReceiver(monitor, filter);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	

	// 获取当前网络状态
	private NetworkInfo getActiveNetwork(Context context) {
		if (context == null)
			return null;
		ConnectivityManager mConnMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (mConnMgr == null)
			return null;
		NetworkInfo aActiveInfo = mConnMgr.getActiveNetworkInfo();
		return aActiveInfo;
	}

	public static void init(Context context, String appKey) {
		try {
			if (wrapper == null) {
				wrapper = new AnIMWrapper();
				wrapper.anIM = new AnIM(context, appKey);
				wrapper.anIM.setSecureConnection(true);
				wrapper.anIM.setCallback(wrapper);

				thisContext = context;
			}
			// FIXME 注册网络状态监听
			// wrapper.registerDataTransReceiver();
		} catch (ArrownockException e) {
			e.printStackTrace();
		}
	}

	public static AnIMWrapper getWrapper() {
		if (wrapper == null) {
			init(thisContext, AnUtils.AppKey);
		}

		return wrapper;
	}

	public void getClientId(String userId) {
		Log.i(logTag, "getClientId: " + userId);

		try {
			wrapper.anIM.getClientId(userId);
		} catch (ArrownockException e) {
			e.printStackTrace();
		}
	}

	public void getTopics(AnIMWrapperCallback callback) {
		Log.i(logTag, "getTopics");

		if (callback != null) {
			delegate = callback;
		}

		try {
			wrapper.anIM.getTopicList();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void willJoinTopic(String topicId) {
		Log.i(logTag, "will join topic: " + topicId);

		try {
			Set<String> clientIds = new HashSet<String>();
			clientIds.add(AnUtils.getCurrentClientId());
			wrapper.anIM.addClientsToTopic(topicId, clientIds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void willQuitTopic(String topicId) {
		Log.i(logTag, "will quit topic: " + topicId);

		try {
			Set<String> clientIds = new HashSet<String>();
			clientIds.add(AnUtils.getCurrentClientId());
			wrapper.anIM.removeClientsFromTopic(topicId, clientIds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 查询群组信息，获取群组内用户的clientId列表
	public void willCheckIfMyTopic(String topicId) {
		Log.i(logTag, "will get topicInfo: " + topicId);

		willCheckIfMyTopic = true;
		try {
			wrapper.anIM.getTopicInfo(topicId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 验证是否处于该群组
	private void verifyIfMyTopic(Set<String> clientIds) {
		Log.i(logTag, "will verify if my topic");

		if (willCheckIfMyTopic) {
			if (messageListActivity != null) {
				if (clientIds == null) {
					messageListActivity.onCheckIfMyTopic(false, true);
				} else {
					if (clientIds.contains(AnUtils.getCurrentClientId())) {
						messageListActivity.onCheckIfMyTopic(true, false);
					} else {
						messageListActivity.onCheckIfMyTopic(false, false);
					}
				}
			}

			willCheckIfMyTopic = false;
		}
	}

	public void connect(String clientId) {
		Log.i(logTag, "connect clientId: " + clientId);

		willDisconnect = false;

		try {
			wrapper.anIM.connect(clientId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		Log.i(logTag, "\t" + "disconnect");

		willDisconnect = true;

		try {
			wrapper.anIM.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void connectIfOffline() {
		if (status == false && AnUtils.getCurrentUsername() != null) {
			try {
				wrapper.anIM.getClientId(AnUtils.getCurrentUsername());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 绑定Push服务
	public void bindPush(String anid) {
		thisAnId = anid;
		try {
			wrapper.anIM.bindAnPushService(anid, AnUtils.AppKey,
					AnPushType.AnPushTypeAndroid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getOfflineMessages() {
		wrapper.anIM.getOfflineHistory(AnUtils.getCurrentClientId(), 10, new IAnIMHistoryCallback() {
			@Override
			public void onError(ArrownockException e) {
				Log.e(logTag, "Failed to fetch offline messages. " + e.getMessage());
			}

			@Override
			public void onSuccess(List messages, int count) {
				for(int i = 0; i< messages.size(); i++) {
					AnIMMessage message = (AnIMMessage)messages.get(i);
					if(message != null) {
						if(AnIMMessageType.AnIMTextMessage == message.getType()) {
							Map<String, String> customData = message.getCustomData();
							String username = customData.get("username");
							double timeDouble = Double.parseDouble(customData.get("time"));
							long longTime = (long) timeDouble * 1000;
							Date time = new Date(longTime);

							Chat chat = new Chat();
							chat.type = "text";
							chat.fromClientId = message.getFrom();
							List<String> p = new ArrayList<String>();
							p.add(AnUtils.getCurrentClientId());
							p.add(message.getFrom());
							Collections.sort(p);
							chat.parties = StringUtils.join(p, ",");
							chat.message = message.getMessage();
							chat.messageId = message.getMsgId();
							chat.status = "unread";
							chat.income = true;
							chat.time = AnUtils.getTimeString(time);
							chat.realname = username;
							
							saveReceivedMessage(chat, p);
						} else if(AnIMMessageType.AnIMBinaryMessage == message.getType()) {
							Map<String, String> customData = message.getCustomData();
							String username = customData.get("username");
							double timeDouble = Double.parseDouble(customData.get("time"));
							long longTime = (long) timeDouble * 1000;
							Date time = new Date(longTime);

							Chat chat = new Chat();
							chat.type = message.getFileType();
							chat.fromClientId = message.getFrom();
							List<String> p = new ArrayList<String>();
							p.add(AnUtils.getCurrentClientId());
							p.add(message.getFrom());
							Collections.sort(p);
							chat.parties = StringUtils.join(p, ",");
							chat.messageId = message.getMsgId();
							chat.status = "unread";
							chat.income = true;
							chat.time = AnUtils.getTimeString(time);
							chat.realname = username;
							if (chat.type.equals("location")) {
								chat.latitude = Double.valueOf(customData.get("latitude")
										.toString());
								chat.longitude = Double.valueOf(customData.get("longitude")
										.toString());
							} else {
								chat.binary = message.getContent();
							}
							saveRecievedBinaryMessage(chat, p);
						}
					}
				}
				// if there is still message left, fetch them
				if(count > 0) {
					getOfflineMessages();
				}
			}
		});
	}
	
	public void getTopicOfflineMessages() {
		wrapper.anIM.getOfflineTopicHistory(AnUtils.getCurrentClientId(), 10, new IAnIMHistoryCallback() {
			public void onError(ArrownockException e) {
				Log.e(logTag, "Failed to fetch topic offline messages. " + e.getMessage());
			}

			@Override
			public void onSuccess(List messages, int count) {
				for(int i = 0; i< messages.size(); i++) {
					AnIMMessage message = (AnIMMessage)messages.get(i);
					if(message != null) {
						if(AnIMMessageType.AnIMTextMessage == message.getType()) {
							String messageId = message.getMsgId();
							String content = message.getMessage();
							String topicId = message.getTopicId();
							Map<String, String> customData = message.getCustomData();
							String realname = customData.get("realname");
							String username = customData.get("username");
							if (realname == null) {
								realname = username;
							}
							double timeDouble = Double.parseDouble(customData.get("time"));
							long longTime = (long) timeDouble * 1000;
							Date time = new Date(longTime);

							Message msg = new Message();
							msg.messageId = messageId;
							msg.content = content;
							msg.topicId = topicId;
							msg.realname = realname;
							msg.username = username;
							msg.timestamp = time;
							msg.isUnread = true;
							msg.isIncoming = true;
							msg.isUnsent = false;
							msg.isError = false;
							msg.type = EntityType.ET_TEXT;

							DBManager.writeMessage(msg);

							if (messageListActivity != null) {
								messageListActivity.onMessage(msg);
							}

							if (topicListActivity != null) {
								topicListActivity.onMessage(msg);
							}
						} else if(AnIMMessageType.AnIMBinaryMessage == message.getType()) {
							String messageId = message.getMsgId();
							byte[] contentData = message.getContent();
							String fileType = message.getFileType();
							String topicId = message.getTopicId();
							Map<String, String> customData = message.getCustomData();
							String realname = customData.get("realname");
							String username = customData.get("username");
							if (realname == null) {
								realname = username;
							}
							double timeDouble = Double.parseDouble(customData.get("time"));
							long longTime = (long) timeDouble * 1000;
							Date time = new Date(longTime);

							Message msg = new Message();
							msg.messageId = messageId;
							msg.topicId = topicId;
							msg.realname = realname;
							msg.username = username;
							msg.timestamp = time;
							msg.isUnread = true;
							msg.isIncoming = true;
							msg.isUnsent = false;
							msg.isError = false;
							if (fileType.equals("image")) {
								msg.imageData = contentData;
								msg.type = EntityType.ET_IMAGE;
							} else if (fileType.equals("audio")) {
								msg.audioData = contentData;
								msg.type = EntityType.ET_AUDIO;
							}

							DBManager.writeMessage(msg);

							if (messageListActivity != null) {
								messageListActivity.onMessage(msg);
							}

							if (topicListActivity != null) {
								topicListActivity.onMessage(msg);
							}
						}
					}
				}
				// if there is still message left, fetch them
				if(count > 0) {
					getTopicOfflineMessages();
				}
			}
		});
	}
	
	public String sendMessageToClients(String message, List<String> clientIds) {
		String messageId = null;
		try {
			Set<String> clientIdSet = new HashSet<String>(clientIds);
			List<String> clientIdList = new ArrayList<String>(clientIds);
			Map<String, String> customData = new HashMap<String, String>();
			customData.put("username", AnUtils.getCurrentUsername());
			customData.put("time",
					String.format("%f", new Date().getTime() / 1000.0));
			messageId = wrapper.anIM.sendMessage(clientIdSet, message,
					customData, true);

			Chat chat = new Chat();
			chat.type = "text";
			chat.fromClientId = AnUtils.getCurrentClientId();
			clientIdList.add(AnUtils.getCurrentClientId());
			Collections.sort(clientIdList);
			chat.parties = StringUtils.join(clientIdList, ",");
			chat.message = message;
			chat.messageId = messageId;
			chat.status = "sending";
			chat.income = false;
			chat.time = AnUtils.getTimeString(new Date());
			DBManager.addChat(chat);
		} catch (ArrownockException e) {
			Log.e("sendMessageToClients: ", e.getMessage());
		}
		return messageId;
	}

	public String sendBinaryToClients(byte[] binary, String fileType,
			List<String> clientIds) {
		String messageId = null;
		try {
			Set<String> clientIdSet = new HashSet<String>(clientIds);
			List<String> clientIdList = new ArrayList<String>(clientIds);
			Map<String, String> customData = new HashMap<String, String>();
			customData.put("username", AnUtils.getCurrentUsername());
			customData.put("time",
					String.format("%f", new Date().getTime() / 1000.0));
			messageId = wrapper.anIM.sendBinary(clientIdSet, binary, fileType,
					customData, true);

			Chat chat = new Chat();
			chat.type = fileType;
			chat.fromClientId = AnUtils.getCurrentClientId();
			clientIdList.add(AnUtils.getCurrentClientId());
			Collections.sort(clientIdList);
			chat.parties = StringUtils.join(clientIdList, ",");
			chat.binary = binary;
			chat.messageId = messageId;
			chat.status = "sending";
			chat.income = false;
			chat.time = AnUtils.getTimeString(new Date());
			DBManager.addChat(chat);
		} catch (ArrownockException e) {
			Log.e("sendBinaryToClient: ", e.getMessage());
		}
		return messageId;
	}

	public String sendLocationToClients(double latitude, double longitude,
			List<String> clientIds) {
		Set<String> clientIdSet = new HashSet<String>(clientIds);
		List<String> clientIdList = new ArrayList<String>(clientIds);
		String messageId = null;
		Map<String, String> locMap = new HashMap<String, String>();
		locMap.put("latitude", String.valueOf(latitude));
		locMap.put("longitude", String.valueOf(longitude));
		locMap.put("username", AnUtils.getCurrentUsername());
		locMap.put("time", String.format("%f", new Date().getTime() / 1000.0));

		try {
			messageId = wrapper.anIM.sendBinary(clientIdSet,
					"Location".getBytes(), "location", locMap, true);

			Chat chat = new Chat();
			chat.type = "location";
			chat.fromClientId = AnUtils.getCurrentClientId();
			clientIdList.add(AnUtils.getCurrentClientId());
			Collections.sort(clientIdList);
			chat.parties = StringUtils.join(clientIdList, ",");
			chat.binary = null;
			chat.message = "Location...";
			chat.latitude = latitude;
			chat.longitude = longitude;
			chat.messageId = messageId;
			chat.status = "sending";
			chat.income = false;
			chat.time = AnUtils.getTimeString(new Date());
			DBManager.addChat(chat);
		} catch (ArrownockException e) {
			Log.e("sendBinaryToClient: ", e.getMessage());
		}
		return messageId;
	}

	// 发送已读回执
	public String sendReadACKToClients(List<String> clientIds, String messageId) {
		String returnedMessageId = null;
		try {
			Set<String> clientIdSet = new HashSet<String>(clientIds);
			returnedMessageId = wrapper.anIM
					.sendReadACK(clientIdSet, messageId);
		} catch (ArrownockException e) {
			Log.e("sendReadACK: ", e.getMessage());
		}
		return returnedMessageId;
	}

	// 用户群组聊天发送消息
	public String sendText(Message message) {
		String messageId = null;

		String topicId = message.topicId;
		String content = message.content;
		Map<String, String> customData = new HashMap<String, String>();
		customData.put("realname", message.realname);
		customData.put("username", message.username);
		customData.put("time",
				String.format("%f", message.timestamp.getTime() / 1000.0));

		try {
			messageId = wrapper.anIM.sendMessageToTopic(topicId, content,
					customData, false);
		} catch (ArrownockException e) {
			e.printStackTrace();
		}

		if (messageId != null) {
			message.messageId = messageId;
			message.isIncoming = false;
			message.isUnread = true;
			message.isUnsent = true;

			tmpMessages.put(messageId, message);
		}

		return messageId;
	}

	// 用户群组聊天发送图片
	public String sendImage(Message message) {
		String messageId = null;

		String topicId = message.topicId;
		byte[] imageData = message.imageData;
		Map<String, String> customData = new HashMap<String, String>();
		customData.put("realname", message.realname);
		customData.put("username", message.username);
		customData.put("time",
				String.format("%f", message.timestamp.getTime() / 1000.0));

		try {
			messageId = wrapper.anIM.sendBinaryToTopic(topicId, imageData,
					"image", customData, false);
		} catch (ArrownockException e) {
			e.printStackTrace();
		}

		if (messageId != null) {
			message.messageId = messageId;
			message.isIncoming = false;
			message.isUnread = true;
			message.isUnsent = true;

			tmpMessages.put(messageId, message);
		}

		return messageId;
	}

	// 用户群组聊天发送声音
	public String sendAudio(Message message) {
		String messageId = null;

		String topicId = message.topicId;
		byte[] audioData = message.audioData;
		Map<String, String> customData = new HashMap<String, String>();
		customData.put("realname", message.realname);
		customData.put("username", message.username);
		customData.put("time",
				String.format("%f", message.timestamp.getTime() / 1000.0));

		try {
			messageId = wrapper.anIM.sendBinaryToTopic(topicId, audioData,
					"audio", customData, false);
		} catch (ArrownockException e) {
			e.printStackTrace();
		}

		if (messageId != null) {
			message.messageId = messageId;
			message.isIncoming = false;
			message.isUnread = true;
			message.isUnsent = true;

			tmpMessages.put(messageId, message);
		}

		return messageId;
	}

	// 回调函数。添加clientId到某一个群组的回调函数
	@Override
	public void addClientsToTopic(AnIMAddClientsCallbackData arg0) {
		if (messageListActivity != null) {
			if (arg0.getException() != null) {
				messageListActivity.onJoinTopic(false);
			} else {
				messageListActivity.onJoinTopic(true);
			}
		}
	}

	// 回调函数。绑定Push服务的回调函数
	@Override
	public void bindAnPushService(AnIMBindAnPushServiceCallbackData arg0) {
		Log.i(logTag, "anPush bound"
				+ (arg0.isError() ? arg0.getException().getErrorCode() : ""));

		if (!arg0.isError()) {
			AnUtils.setAnId(thisAnId);
		}
	}

	@Override
	public void createTopic(AnIMCreateTopicCallbackData arg0) {
	}

	// 回调函数。获取某一个userId所对应的clientId的回调函数
	@Override
	public void getClientId(AnIMGetClientIdCallbackData arg0) {
		if (!arg0.isError()) {
			Log.i(logTag, "clientId retrived: " + arg0.getClientId());

			final String clientId = arg0.getClientId();

			if (AnUtils.getCurrentClientId() == null) {
				JSONObject params = new JSONObject();
				JSONObject customFields = new JSONObject();
				try {
					customFields.put("clientId", clientId);
					params.put("customFields", customFields);
					if (AnUtils.getCurrentUserId() != null) {
						params.put("id", AnUtils.getCurrentUserId());
					}

					MRMWrapper.getMRM(thisContext).sendPostRequest(thisContext,
							"users/update", params,
							new MRMJSONResponseHandler() {

								@Override
								public void onFailure(Throwable e,
										JSONObject response) {

								}

								@Override
								public void onSuccess(int statusCode,
										JSONObject response) {
									AnUtils.setCurrentClientId(clientId);
								}

							});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			try {
				wrapper.anIM.connect(clientId);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			Log.i(logTag, "clientId retrived error: "
					+ arg0.getException().getErrorCode());

			arg0.getException().getInnerException().printStackTrace();
		}
	}

	@Override
	public void getClientsStatus(AnIMGetClientsStatusCallbackData arg0) {
	}

	@Override
	public void getSessionInfo(AnIMGetSessionInfoCallbackData arg0) {
	}

	// 回调函数。获取群组信息的回调函数
	@SuppressWarnings("unchecked")
	@Override
	public void getTopicInfo(AnIMGetTopicInfoCallbackData arg0) {
		Log.i(logTag, "topicInfo retrived");

		if (arg0.getException() != null) {
			verifyIfMyTopic(null);
		} else {
			verifyIfMyTopic(arg0.getParties());
		}
	}

	// 回调函数。获取所有群组列表的回调函数
	@SuppressWarnings("unchecked")
	@Override
	public void getTopicList(AnIMGetTopicListCallbackData arg0) {
		if (delegate != null) {
			if (!arg0.isError()) {
				delegate.getTopicsDone(arg0.getTopicList());
			} else {
				delegate.getTopicsDone(null);
				arg0.getException().printStackTrace();
			}
		}
	}

	// 回调函数。某条消息发送成功/失败的回调函数
	@Override
	public void messageSent(AnIMMessageSentCallbackData arg0) {
		if (arg0.getException() != null) {
			String messageId = arg0.getMsgId();
			tmpMessages.remove(messageId);

			if (messageListActivity != null) {
				messageListActivity.onMessageSent(null);
			}

			if (chatActivity != null) {
				chatActivity.onChatSent(null);
			}
		} else {
			final String messageId = arg0.getMsgId();

			if (messageId != null) {
				Message message = tmpMessages.get(messageId);
				if (message != null) {
					message.isUnsent = false;
					DBManager.writeMessage(message);
					tmpMessages.remove(messageId);

					if (messageListActivity != null) {
						messageListActivity.onMessageSent(messageId);
					}
				}

				DBManager.updateChatStatus(messageId, false, "sent");
				if (chatActivity != null) {
					chatActivity.onChatSent(messageId);
				}
			}
		}
	}

	// 回调函数。收到来自某个用户的binary数据时的回调函数
	@SuppressWarnings("unchecked")
	@Override
	public void receivedBinary(AnIMBinaryCallbackData arg0) {
		Log.i(logTag, "received binary: " + arg0.getMsgId());

		try {
			Map<String, String> customData = arg0.getCustomData();
			String username = customData.get("username");
			double timeDouble = Double.parseDouble(customData.get("time"));
			long longTime = (long) timeDouble * 1000;
			Date time = new Date(longTime);

			Chat chat = new Chat();
			chat.type = arg0.getFileType();
			chat.fromClientId = arg0.getFrom();
			List<String> p = new ArrayList<String>();
			p.add(AnUtils.getCurrentClientId());
			p.add(arg0.getFrom());
			Collections.sort(p);
			chat.parties = StringUtils.join(p, ",");
			chat.messageId = arg0.getMsgId();
			chat.status = "unread";
			chat.income = true;
			chat.time = AnUtils.getTimeString(time);
			chat.realname = username;
			if (chat.type.equals("location")) {
				chat.latitude = Double.valueOf(customData.get("latitude")
						.toString());
				chat.longitude = Double.valueOf(customData.get("longitude")
						.toString());
			} else {
				chat.binary = arg0.getContent();
			}
			saveRecievedBinaryMessage(chat, p);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void saveRecievedBinaryMessage(Chat chat, List<String> parties) {
		DBManager.addChat(chat);

		parties.remove(AnUtils.getCurrentClientId());
		boolean existing = DBManager.setSessionUnread(parties, chat.time, "收到了新" + chat.type);
		if (existing) {
			if ((chatActivity == null && sessionActivity == null)
					|| (chatActivity == null && !sessionActivity.alive)
					|| (chatActivity != null && !chatActivity.alive)) {
				AnUtils.showNotification(thisContext, "session");
				return;
			}
			if (chatActivity != null && chatActivity.alive) {
				chatActivity.onReceivedChat();
				Log.i(logTag, "sendReadACK messageReceived");
				wrapper.sendReadACKToClients(parties, chat.messageId);
			}
			if (sessionActivity != null && sessionActivity.alive) {
				sessionActivity.initData();
			}

		} else {
			DBManager.addSession(parties, chat.realname, chat.time, "收到了新" + chat.type, "unread");
		}
	}
	
	// 回调函数。收到来自某个用户的文本消息时的回调函数
	@SuppressWarnings("unchecked")
	@Override
	public void receivedMessage(AnIMMessageCallbackData arg0) {
		Log.i(logTag, "received message: " + arg0.getMessage());

		try {
			Map<String, String> customData = arg0.getCustomData();
			String username = customData.get("username");
			double timeDouble = Double.parseDouble(customData.get("time"));
			long longTime = (long) timeDouble * 1000;
			Date time = new Date(longTime);

			Chat chat = new Chat();
			chat.type = "text";
			chat.fromClientId = arg0.getFrom();
			List<String> p = new ArrayList<String>();
			p.add(AnUtils.getCurrentClientId());
			p.add(arg0.getFrom());
			Collections.sort(p);
			chat.parties = StringUtils.join(p, ",");
			chat.message = arg0.getMessage();
			chat.messageId = arg0.getMsgId();
			chat.status = "unread";
			chat.income = true;
			chat.time = AnUtils.getTimeString(time);
			chat.realname = username;
			
			saveReceivedMessage(chat, p);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void saveReceivedMessage(Chat chat, List<String> parties) {
		DBManager.addChat(chat);

		parties.remove(AnUtils.getCurrentClientId());
		boolean existing = DBManager.setSessionUnread(parties, chat.time, chat.message);

		if (existing) {
			if ((chatActivity == null && sessionActivity == null)
					|| (chatActivity == null && !sessionActivity.alive)
					|| (chatActivity != null && !chatActivity.alive)) {
				AnUtils.showNotification(thisContext, "session");
				return;
			}
			if (chatActivity != null && chatActivity.alive) {
				chatActivity.onReceivedChat();
				Log.i(logTag, "sendReadACK messageReceived");
				wrapper.sendReadACKToClients(parties, chat.messageId);
			}
			if (sessionActivity != null && sessionActivity.alive) {
				sessionActivity.initData();
			}
		} else {
			DBManager.addSession(parties, chat.realname, chat.time, chat.message, "unread");
		}
	}
	
	@Override
	public void receivedNotice(AnIMNoticeCallbackData arg0) {
	}

	// 回调函数。当某条发送出的消息已经被对方阅读的回调函数
	@Override
	public void receivedReadACK(AnIMReadACKCallbackData arg0) {
		Log.i(logTag,
				"message: " + arg0.getMsgId() + " read by: " + arg0.getFrom());
		DBManager.updateChatStatus(arg0.getMsgId(), false, "read");
		if (chatActivity != null && chatActivity.alive) {
			chatActivity.onChatRead();
		}
	}

	// 回调函数。当某条发送出的消息已经被对方接收到的回调函数
	@Override
	public void receivedReceiveACK(AnIMReceiveACKCallbackData arg0) {
		Log.i(logTag,
				"message: " + arg0.getMsgId() + " recevied by: "
						+ arg0.getFrom());
		DBManager.updateChatStatus(arg0.getMsgId(), false, "received");
		if (chatActivity != null && chatActivity.alive) {
			chatActivity.onChatReceived();
		}
	}

	// 回调函数。收到来自某个群组的binary数据时的回调函数
	@SuppressWarnings("unchecked")
	@Override
	public void receivedTopicBinary(AnIMTopicBinaryCallbackData arg0) {
		String messageId = arg0.getMsgId();
		byte[] contentData = arg0.getContent();
		String fileType = arg0.getFileType();
		String topicId = arg0.getTopic();
		Map<String, String> customData = arg0.getCustomData();
		String realname = customData.get("realname");
		String username = customData.get("username");
		if (realname == null) {
			realname = username;
		}
		double timeDouble = Double.parseDouble(customData.get("time"));
		long longTime = (long) timeDouble * 1000;
		Date time = new Date(longTime);

		Message message = new Message();
		message.messageId = messageId;
		message.topicId = topicId;
		message.realname = realname;
		message.username = username;
		message.timestamp = time;
		message.isUnread = true;
		message.isIncoming = true;
		message.isUnsent = false;
		message.isError = false;
		if (fileType.equals("image")) {
			message.imageData = contentData;
			message.type = EntityType.ET_IMAGE;
		} else if (fileType.equals("audio")) {
			message.audioData = contentData;
			message.type = EntityType.ET_AUDIO;
		}

		DBManager.writeMessage(message);

		if (messageListActivity != null) {
			messageListActivity.onMessage(message);
		}

		if (topicListActivity != null) {
			topicListActivity.onMessage(message);
		}
	}

	// 回调函数。收到来自某个群组的文本消息时的回调函数
	@SuppressWarnings("unchecked")
	@Override
	public void receivedTopicMessage(AnIMTopicMessageCallbackData arg0) {
		String messageId = arg0.getMsgId();
		String content = arg0.getMessage();
		String topicId = arg0.getTopic();
		Map<String, String> customData = arg0.getCustomData();
		String realname = customData.get("realname");
		String username = customData.get("username");
		if (realname == null) {
			realname = username;
		}
		double timeDouble = Double.parseDouble(customData.get("time"));
		long longTime = (long) timeDouble * 1000;
		Date time = new Date(longTime);

		Message message = new Message();
		message.messageId = messageId;
		message.content = content;
		message.topicId = topicId;
		message.realname = realname;
		message.username = username;
		message.timestamp = time;
		message.isUnread = true;
		message.isIncoming = true;
		message.isUnsent = false;
		message.isError = false;
		message.type = EntityType.ET_TEXT;

		DBManager.writeMessage(message);

		if (messageListActivity != null) {
			messageListActivity.onMessage(message);
		}

		if (topicListActivity != null) {
			topicListActivity.onMessage(message);
		}
	}

	// 回调函数。从某个群组将某个（某些）用户移除的回调函数
	@Override
	public void removeClientsFromTopic(AnIMRemoveClientsCallbackData arg0) {
		if (messageListActivity != null) {
			if (arg0.getException() != null) {
				messageListActivity.onQuitTopic(false);
			} else {
				messageListActivity.onQuitTopic(true);
			}
		}
	}

	// 回调函数。当IM与服务器连接状态有更新时的回调函数
	@Override
	public void statusUpdate(AnIMStatusUpdateCallbackData arg0) {
		Log.i(logTag, " statusUpdate");

		if (arg0.getException() != null) {
			Log.i(logTag, "statusUpdate error: "
					+ arg0.getException().getErrorCode());

			status = false;

			int ec = arg0.getException().getErrorCode();
			if (ec == 3102 || ec == 3105) {
				return;
			}
		}

		if (arg0.getStatus() == AnIMStatus.OFFLINE) {
			Log.i(logTag, "statusUpdate OFFLINE");

			status = false;

			if (!willDisconnect && retryCount > 0) {

				try {
					retryCount--;
					if (wrapper.getActiveNetwork(thisContext) == null) {
						return;
					}
					wrapper.anIM.connect(AnUtils.getCurrentClientId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				retryCount = 1;
			}
		} else {
			Log.i(logTag, "statusUpdate ONLINE");

			status = true;

			if (AnUtils.getAnId() == null) {
				if (thisAnId != null) {
					Log.i(logTag, "bind anPush");

					try {
						wrapper.anIM.bindAnPushService(thisAnId,
								AnUtils.AppKey, AnPushType.AnPushTypeAndroid);
					} catch (ArrownockException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	// 解除Push服务的回调函数
	@Override
	public void unbindAnPushService(AnIMUnbindAnPushServiceCallbackData arg0) {
		Log.i(logTag, "didUnbindPush");

		try {
			willDisconnect = true;

			wrapper.anIM.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateTopic(AnIMUpdateTopicCallbackData arg0) {
		// the topic has been updated
	}
}
