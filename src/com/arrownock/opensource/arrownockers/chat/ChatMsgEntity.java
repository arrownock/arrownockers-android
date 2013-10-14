package com.arrownock.opensource.arrownockers.chat;

public class ChatMsgEntity {
	public String realname;
	public String date;
	public String text;
	public byte[] imageBytes;
	public byte[] audioBytes;
	public String status;
	public String messageId;
	public double latitude;
	public double longitude;

	public boolean isComMsg = true;

	public enum EntityType {
		ET_TEXT, ET_AUDIO, ET_IMAGE, ET_LOCATION
	}

	public EntityType entityType;
}
