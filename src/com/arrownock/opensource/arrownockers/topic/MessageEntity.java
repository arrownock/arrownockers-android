package com.arrownock.opensource.arrownockers.topic;

import java.util.Date;

public class MessageEntity {

	public String messageId;
	public Date date;
	public String dateString;
	public String text;
	public byte[] imageBytes;
	public byte[] audioBytes;
	public String realname;
	public String username;

	public boolean isComMsg = true;
	public boolean unsent = true;
	public boolean unread = true;

	public enum EntityType {
		ET_TEXT, ET_AUDIO, ET_IMAGE
	}

	public EntityType entityType;
}
