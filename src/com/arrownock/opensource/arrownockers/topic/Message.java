package com.arrownock.opensource.arrownockers.topic;

import java.util.Date;

import com.arrownock.opensource.arrownockers.topic.MessageEntity.EntityType;

public class Message {

	public String messageId;
	public String topicId;
	public Date timestamp;
	public String realname;
	public String username;
	public String content;
	public byte[] imageData;
	public byte[] audioData;
	public EntityType type;
	public Boolean isIncoming;
	public Boolean isUnread;
	public Boolean isUnsent;
	public Boolean isError;
}
