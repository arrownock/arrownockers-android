package com.arrownock.opensource.arrownockers.wall;

import android.net.Uri;

public class PostEntity {
	public String postId;
	public String dateString;
	public String contentText;
	public Boolean hasImage;
	public String imageId;
	public String imageURLString;
	public String ownerId;
	public Uri avatarUri;
	public String username;

	public String commentCount;
	public String likeCount;
	public String dislikeCount;

	public boolean canComment = false;
}
