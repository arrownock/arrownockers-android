package com.arrownock.opensource.arrownockers.push;


public class PushDetailsEntity {
	
	public String dateString;
	public String text;
	public byte[] imageBytes;
	public byte[] audioBytes;
	public double latitude;
	public double longitude;

	public boolean isComMsg = true;

	public enum EntityType {
		ET_TEXT, ET_AUDIO, ET_IMAGE, ET_LOCATION
	}

	public EntityType entityType;
}
