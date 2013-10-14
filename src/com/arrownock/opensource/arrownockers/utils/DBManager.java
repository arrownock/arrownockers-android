package com.arrownock.opensource.arrownockers.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.arrownock.opensource.arrownockers.topic.Message;
import com.arrownock.opensource.arrownockers.topic.MessageEntity.EntityType;
import com.arrownock.opensource.arrownockers.topic.Topic;
import com.arrownock.mrm.MRMJSONResponseHandler;

public class DBManager {

	private static final String logTag = "DBManager";

	@Table(name = "User")
	public static class User extends Model {
		@Column(name = "username")
		public String username;
		@Column(name = "realname")
		public String realname;
		@Column(name = "clientId")
		public String clientId;
	}

	@Table(name = "Session")
	public static class Session extends Model {
		@Column(name = "currentClientId")
		public String currentClientId;
		@Column(name = "clientIds")
		public String clientIds;
		@Column(name = "realnames")
		public String realnames;
		@Column(name = "updateTime")
		public String updateTime;
		@Column(name = "lastMessage")
		public String lastMessage;
		@Column(name = "status")
		public String status;
		@Column(name = "id")
		public int id;
	}

	// 一对一聊天的消息
	@Table(name = "Chat")
	public static class Chat extends Model {
		@Column(name = "message")
		public String message;
		@Column(name = "binary")
		public byte[] binary;
		@Column(name = "type")
		public String type; // text, image, audio, location
		@Column(name = "time")
		public String time;
		@Column(name = "fromClientId")
		public String fromClientId;
		@Column(name = "parties")
		public String parties;
		@Column(name = "topicId")
		public String topicId;
		@Column(name = "latitude")
		public double latitude;
		@Column(name = "longitude")
		public double longitude;
		@Column(name = "income")
		public boolean income;
		@Column(name = "status")
		public String status; // sending, sent, received, read
		@Column(name = "messageId")
		public String messageId;
		@Column(name = "currentClientId")
		public String currentClientId;
		@Column(name = "realname")
		public String realname;
	}

	@Table(name = "Push")
	public static class Push extends Model {
		@Column(name = "clientId")
		public String clientId;
		@Column(name = "channel")
		public String channel;
		@Column(name = "title")
		public String title;
		@Column(name = "message")
		public String message;
		@Column(name = "binary")
		public byte[] binary;
		@Column(name = "dataType")
		public String dataType;
		@Column(name = "type")
		public String type;
		@Column(name = "time")
		public String time;
		@Column(name = "latitude")
		public double latitude;
		@Column(name = "longitude")
		public double longitude;
		@Column(name = "income")
		public boolean income;
		@Column(name = "status")
		public String status;
		@Column(name = "messageId")
		public String messageId;
		@Column(name = "batchNumber")
		public String batchNumber;
		@Column(name = "path")
		public String path;
		@Column(name = "currentUsername")
		public String currentUsername;
	}

	@Table(name = "Topic")
	public static class DBTopic extends Model {
		@Column(name = "topicId")
		public String topicId;
		@Column(name = "topicName")
		public String topicName;
		@Column(name = "count")
		public int count;
		@Column(name = "lastMessage")
		public String lastMessage;
		@Column(name = "lastTime")
		public long lastTime;
		@Column(name = "unread")
		public boolean unread;
		@Column(name = "ownerUsername")
		public String ownerUsername;
	}

	// 群组聊天
	@Table(name = "Message")
	public static class DBMessage extends Model {
		@Column(name = "messageId")
		public String messageId;
		@Column(name = "username")
		public String username;
		@Column(name = "topicId")
		public String topicId;
		@Column(name = "contentString")
		public String contentString;
		@Column(name = "contentImage")
		public byte[] contentImage;
		@Column(name = "contentAudio")
		public byte[] contentAudio;
		@Column(name = "contentType")
		public int contentType; // 0:text, 1:image, 2:audio
		@Column(name = "time")
		public long time;
		@Column(name = "incoming")
		public boolean incoming;
		@Column(name = "unread")
		public boolean unread;
		@Column(name = "unsent")
		public boolean unsent;
		@Column(name = "ownerUsername")
		public String ownerUsername;
	}

	public static void writeUser(String username, String realname,
			String clientId) {
		Log.i(logTag, "writeUser: " + username);

		try {
			User user = new User();
			user.username = username;
			user.realname = realname;
			user.clientId = clientId;

			user.save();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static User readUserByUsername(String username) {
		Log.i(logTag, "readUserByUsername: " + username);

		User user = null;
		try {
			user = new Select().from(User.class)
					.where("username = ?", username).executeSingle();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return user;
	}

	public static User readUserByClientId(String clientId) {
		Log.i(logTag, "readUserByClientId: " + clientId);

		User user = null;
		try {
			user = new Select().from(User.class)
					.where("clientId = ?", clientId).executeSingle();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return user;
	}

	public static Topic readOneTopic(String topicId) {
		Log.i(logTag, "readOneTopic");

		try {
			List<DBTopic> dbTopics = new Select()
					.from(DBTopic.class)
					.where("ownerUsername = ? AND topicId = ?",
							AnUtils.getCurrentUsername(), topicId).execute();

			DBTopic dbTopic = dbTopics.get(0);
			Topic topic = new Topic();
			topic.topicId = dbTopic.topicId;
			topic.topicName = dbTopic.topicName;
			topic.count = dbTopic.count;
			topic.lastTime = new Date(dbTopic.lastTime);
			topic.lastMessage = dbTopic.lastMessage;
			topic.unread = dbTopic.unread;

			return topic;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@SuppressLint("DefaultLocale")
	public static List<Topic> readAllTopics() {
		Log.i(logTag, "readAllTopics");

		try {
			List<Topic> topics = new ArrayList<Topic>();

			List<DBTopic> dbTopics = new Select()
					.from(DBTopic.class)
					.where("ownerUsername = ? Order By topicId",
							AnUtils.getCurrentUsername()).execute();
			for (DBTopic dbTopic : dbTopics) {
				String query = String
						.format("ownerUsername = \"%s\" AND topicId = \"%s\" AND incoming = 1 Order By time Desc Limit 1",
								AnUtils.getCurrentUsername(), dbTopic.topicId);
				List<DBMessage> dbmessages = new Select().from(DBMessage.class)
						.where(query).execute();
				if (dbmessages.size() > 0) {
					DBMessage dbmessage = dbmessages.get(0);
					dbTopic.lastTime = dbmessage.time;
					dbTopic.unread = dbmessage.unread;
					String lastMessage = dbmessage.username + ": ";
					if (dbmessage.contentType == 0) {
						lastMessage += dbmessage.contentString;
					} else if (dbmessage.contentType == 1) {
						lastMessage += "发来了一张图片";
					} else if (dbmessage.contentType == 2) {
						lastMessage += "发来了一段语音";
					}
					dbTopic.lastMessage = lastMessage;
				}

				Topic topic = new Topic();
				topic.topicId = dbTopic.topicId;
				topic.topicName = dbTopic.topicName;
				topic.count = dbTopic.count;
				topic.lastTime = new Date(dbTopic.lastTime);
				topic.lastMessage = dbTopic.lastMessage;
				topic.unread = dbTopic.unread;

				topics.add(topic);
			}

			return topics;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void overwriteAllTopics(List<JSONObject> topicjJsonObjects) {
		Log.i(logTag, "overwriteAllTopics");

		try {
			List<Topic> existingTopics = DBManager.readAllTopics();
			List<Topic> serverTopics = new ArrayList<Topic>();
			List<Topic> allTopics = new ArrayList<Topic>();

			// 得到Server上的所有Topic
			for (JSONObject topicJsonObject : topicjJsonObjects) {
				Topic topic = new Topic();
				try {
					topic.topicId = topicJsonObject.getString("id");
					topic.topicName = topicJsonObject.getString("name");
					topic.count = topicJsonObject.getInt("parties_count");
					topic.lastMessage = "没有任何消息";
					topic.lastTime = new Date(0);
					topic.unread = false;
				} catch (Exception e) {
					Log.e(logTag, "overwriteAllTopics");
					e.printStackTrace();
				}

				serverTopics.add(topic);
			}

			// 找出本地不存在的那些新Topic
			for (Topic serverTopic : serverTopics) {
				boolean isNew = true;
				for (Topic existingTopic : existingTopics) {
					if (existingTopic.topicId.equals(serverTopic.topicId)) {
						isNew = false;
						break;
					}
				}

				if (isNew) {
					allTopics.add(serverTopic);
				}
			}

			// 更新每一个Topic下的人数
			for (Topic existingTopic : existingTopics) {
				for (Topic serverTopic : serverTopics) {
					if (existingTopic.topicId.equals(serverTopic.topicId)) {
						existingTopic.count = serverTopic.count;
						existingTopic.topicName = serverTopic.topicName;
						break;
					}
				}

				allTopics.add(existingTopic);
			}

			Map<String, Topic> topicMap = new HashMap<String, Topic>();
			for (Topic topic : allTopics) {
				try {
					topicMap.put(topic.topicId, topic);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			allTopics = new ArrayList<Topic>(topicMap.values());

			new Delete().from(DBTopic.class)
					.where("ownerUsername = ?", AnUtils.getCurrentUsername())
					.execute();

			for (Topic topic : allTopics) {
				DBTopic dbTopic = new DBTopic();
				dbTopic.topicId = topic.topicId;
				dbTopic.topicName = topic.topicName;
				dbTopic.count = topic.count;
				dbTopic.lastMessage = topic.lastMessage;
				dbTopic.lastTime = topic.lastTime.getTime();
				dbTopic.unread = topic.unread;
				dbTopic.ownerUsername = AnUtils.getCurrentUsername();

				dbTopic.save();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeMessage(Message message) {

		try {
			DBMessage dbmessage = new DBMessage();
			dbmessage.messageId = message.messageId;
			dbmessage.topicId = message.topicId;
			dbmessage.incoming = message.isIncoming;
			dbmessage.unread = message.isUnread;
			dbmessage.unsent = message.isUnsent;
			dbmessage.username = message.username;
			dbmessage.time = message.timestamp.getTime();
			dbmessage.contentString = message.content;
			dbmessage.contentImage = message.imageData;
			dbmessage.contentAudio = message.audioData;
			if (message.type == EntityType.ET_TEXT) {
				dbmessage.contentType = 0;
			} else if (message.type == EntityType.ET_IMAGE) {
				dbmessage.contentType = 1;
			} else if (message.type == EntityType.ET_AUDIO) {
				dbmessage.contentType = 2;
			}
			dbmessage.ownerUsername = AnUtils.getCurrentUsername();

			dbmessage.save();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void deleteMessage(String messageId) {

		try {
			new Delete()
					.from(DBMessage.class)
					.where("ownerUsername = ? AND messageId = ?",
							AnUtils.getCurrentUsername(), messageId).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void updateOutgoingMessageAsSent(final String messageId) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String query = String.format(
							"ownerUsername = \"%s\" AND messageId = \"%s\"",
							AnUtils.getCurrentUsername(), messageId);
					new Update(DBMessage.class).set("unsent = ?", false)
							.where(query).execute();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.start();
	}

	public static void updateIncomingMessageAsRead(final String messageId) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String query = String.format(
							"ownerUsername = \"%s\" AND messageId = \"%s\"",
							AnUtils.getCurrentUsername(), messageId);
					new Update(DBMessage.class).set("unread = ?", false)
							.where(query).execute();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		thread.start();
	}

	@SuppressLint("DefaultLocale")
	public static List<Message> readMessages(String topicId, long beforeWhen,
			int count) {
		Log.i(logTag, "readMessages topicId: " + topicId);

		try {
			List<Message> messages = new ArrayList<Message>();

			String query = null;
			try {
				if (beforeWhen > 0) {
					query = String
							.format("ownerUsername = \"%s\" AND topicId = \"%s\" AND time < %d Order By time Desc Limit %d",
									AnUtils.getCurrentUsername(), topicId,
									beforeWhen - 10, count);
				} else {
					query = String
							.format("ownerUsername = \"%s\" AND topicId = \"%s\" Order By time Desc Limit %d",
									AnUtils.getCurrentUsername(), topicId,
									count);
				}

			} catch (Exception e) {
				e.printStackTrace();
				return messages;
			}

			List<DBMessage> dbMessages = new Select().from(DBMessage.class)
					.where(query).execute();
			Collections.reverse(dbMessages);
			for (DBMessage dbMessage : dbMessages) {
				Message message = new Message();
				message.messageId = dbMessage.messageId;
				message.topicId = dbMessage.topicId;
				message.isIncoming = dbMessage.incoming;
				message.isUnread = dbMessage.unread;
				message.isUnsent = dbMessage.unsent;
				message.username = dbMessage.username;
				message.timestamp = new Date(dbMessage.time);
				message.content = dbMessage.contentString;
				message.imageData = dbMessage.contentImage;
				message.audioData = dbMessage.contentAudio;
				switch (dbMessage.contentType) {
				case 0:
					message.type = EntityType.ET_TEXT;
					break;
				case 1:
					message.type = EntityType.ET_IMAGE;
					break;
				case 2:
					message.type = EntityType.ET_AUDIO;
					break;
				default:
					break;
				}

				messages.add(message);
			}

			return messages;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static List<Session> getAngelSession(String clientId) {
		try {
			return new Select()
					.from(Session.class)
					.where("currentClientId = ? AND realnames = ?", clientId,
							"Angel").execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static List<Session> getSessions(String clientId) {
		try {
			return new Select().from(Session.class)
					.where("currentClientId = ?", clientId).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static List<Session> getSession(List<String> clientIds) {
		try {
			Collections.sort(clientIds);
			return new Select()
					.from(Session.class)
					.where("currentClientId = ? AND clientIds = ?",
							AnUtils.getCurrentClientId(),
							StringUtils.join(clientIds, ",")).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static boolean addSession(List<String> clientIds) {

		try {
			if (DBManager.getSession(clientIds).size() > 0) {
				return false;
			}
			Collections.sort(clientIds);
			List<String> queryClientIds = new ArrayList<String>();
			for (String clientId : clientIds) {
				queryClientIds.add("\"" + clientId + "\"");
			}

			Session session = new Session();
			session.currentClientId = AnUtils.getCurrentClientId();
			session.clientIds = StringUtils.join(clientIds, ",");
			session.realnames = DBManager.readUserByClientId(session.clientIds).realname;
			session.lastMessage = null;
			session.updateTime = AnUtils.getTimeString(new Date());
			session.save();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean addSession(final List<String> clientIds,
			final String username, final String time, final String message,
			final String status) {

		try {
			if (DBManager.getSession(clientIds).size() > 0) {
				return false;
			}
			Collections.sort(clientIds);
			List<String> queryClientIds = new ArrayList<String>();
			for (String clientId : clientIds) {
				queryClientIds.add("\"" + clientId + "\"");
			}

			// 查找该用户的clientId
			User user = DBManager.readUserByUsername(username);
			if (user == null) {
				try {
					JSONObject params = new JSONObject();
					params.put("username", username);
					MRMWrapper.getMRM(AnUtils.applicationContext)
							.sendPostRequest(AnUtils.applicationContext,
									"users/search", params,
									new MRMJSONResponseHandler() {

										@Override
										public void onFailure(Throwable arg0,
												JSONObject arg1) {
											Log.i(logTag,
													"retrive user info of incoming new session failed");
										}

										@Override
										public void onSuccess(int arg0,
												JSONObject arg1) {
											try {
												JSONObject userJsonObject = arg1
														.getJSONObject(
																"response")
														.getJSONArray("users")
														.getJSONObject(0);
												String realname = null;
												if (userJsonObject
														.has("realname")) {
													realname = userJsonObject
															.getString("realname");
												} else {
													realname = username;
												}

												Session session = new Session();
												session.currentClientId = AnUtils
														.getCurrentClientId();
												session.clientIds = StringUtils
														.join(clientIds, ",");
												session.realnames = realname;
												session.lastMessage = message;
												session.updateTime = time;
												session.status = status;
												session.save();

												if (AnIMWrapper.sessionActivity != null
														&& AnIMWrapper.sessionActivity.alive) {
													AnIMWrapper.sessionActivity
															.initData();
												}
											} catch (Exception e) {
												e.printStackTrace();
											}

										}

									});
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				Session session = new Session();
				session.currentClientId = AnUtils.getCurrentClientId();
				session.clientIds = StringUtils.join(clientIds, ",");
				session.realnames = DBManager
						.readUserByClientId(session.clientIds).realname;
				session.lastMessage = message;
				session.updateTime = time;
				session.status = status;
				session.save();
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean setSessionRead(List<String> clientIds) {
		try {
			List<Session> sessions = DBManager.getSession(clientIds);
			if (sessions.size() > 0) {
				Session s = sessions.get(0);
				s.status = "read";
				s.save();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean setSessionUnread(List<String> clientIds,
			String updateTime, String lastMessage) {
		try {
			List<Session> sessions = DBManager.getSession(clientIds);
			if (sessions.size() > 0) {
				Session s = sessions.get(0);
				s.status = "unread";
				s.updateTime = updateTime;
				s.lastMessage = lastMessage;
				s.save();
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static List<Chat> getAllChats(String queryString, String key) {
		try {
			return new Select()
					.from(Chat.class)
					.where(key + " = " + "\"" + queryString + "\""
							+ " AND currentClientId = " + "\""
							+ AnUtils.getCurrentClientId() + "\"")
					.orderBy("time").execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void addChat(Chat chat) {
		try {
			chat.currentClientId = AnUtils.getCurrentClientId();
			chat.save();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void updateChatStatus(String messageId, boolean income,
			String status) {
		try {
			Chat chat = new Select()
					.from(Chat.class)
					.where("messageId = ? AND income = ? AND currentClientId = ?",
							messageId, income ? 1 : 0,
							AnUtils.getCurrentClientId()).executeSingle();
			if (chat == null) {
				return;
			}
			if (chat.status.equals("read")) {
				return;
			}
			chat.status = status;
			chat.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<Push> getPushes(String channel) {
		try {
			return new Select()
					.from(Push.class)
					.where("title = ? AND currentUsername = ?", channel,
							AnUtils.getCurrentUsername()).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static List<Push> getUnreadPushes(String channel) {
		try {
			return new Select()
					.from(Push.class)
					.where("channel = ? AND status = ? AND currentUsername = ? order by time desc",
							channel, "unread", AnUtils.getCurrentUsername())
					.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void addPush(Push push) {
		try {
			push.currentUsername = AnUtils.getCurrentUsername();
			push.save();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void setPushRead(String title) {
		try {
			List<Push> unreadPushesList = new Select()
					.from(Push.class)
					.where("title = ? AND status = ? AND currentUsername = ?",
							title, "unread", AnUtils.getCurrentUsername())
					.execute();
			for (int i = 0; i < unreadPushesList.size(); i++) {
				Push push = unreadPushesList.get(i);
				push.status = "read";
				push.save();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void clear() {
		try {
			new Delete().from(Chat.class).execute();
			new Delete().from(Push.class).execute();
			new Delete().from(DBTopic.class).execute();
			new Delete().from(DBMessage.class).execute();
			new Delete().from(Session.class).where("realnames <> ?", "Angel")
					.execute();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
