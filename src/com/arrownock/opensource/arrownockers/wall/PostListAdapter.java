package com.arrownock.opensource.arrownockers.wall;

import java.util.List;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.loopj.android.image.SmartImageView;

public class PostListAdapter extends BaseAdapter {

	private List<PostEntity> coll;

	private Context ctx;

	private LayoutInflater mInflater;

	public PostListAdapter(Context context, List<PostEntity> coll) {
		ctx = context;
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return coll.size();
	}

	public Object getItem(int position) {
		return coll.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		PostEntity entity = coll.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.row_post, null);
			viewHolder = new ViewHolder();
			viewHolder.ivAvatar = (SmartImageView) convertView
					.findViewById(R.id.iv_avatar);
			viewHolder.tvUsername = (TextView) convertView
					.findViewById(R.id.tv_username);
			viewHolder.tvDate = (TextView) convertView
					.findViewById(R.id.tv_date);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_content);
			viewHolder.ivContent = (SmartImageView) convertView
					.findViewById(R.id.iv_content);

			viewHolder.tv_commentCount = (TextView) convertView
					.findViewById(R.id.tv_commentCount);
			viewHolder.tv_likeCount = (TextView) convertView
					.findViewById(R.id.tv_likeCount);
			viewHolder.tv_dislikeCount = (TextView) convertView
					.findViewById(R.id.tv_dislikeCount);

			viewHolder.btn_action = (Button) convertView
					.findViewById(R.id.btn_action);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (entity.dateString != null) {
			String dateString = AnUtils.getTimeString(entity.dateString);
			if (dateString != null) {
				viewHolder.tvDate.setText(dateString);
			}
		}

		if (entity.contentText != null) {
			viewHolder.tvContent.setText(entity.contentText);
		}

		if (entity.username != null) {
			viewHolder.tvUsername.setText(entity.username);
		}
		if (entity.avatarUri != null) {
			viewHolder.ivAvatar.setImageURI(entity.avatarUri);
		}

		DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		int result = width - 50 * 2;
		LayoutParams lParams = viewHolder.ivContent.getLayoutParams();

		if (entity.imageURLString != null) {
			lParams.width = result;
			lParams.height = result;
			viewHolder.ivContent.setLayoutParams(lParams);
			viewHolder.ivContent.setImageUrl(entity.imageURLString);
		} else {
			lParams.width = result;
			lParams.height = 0;
			viewHolder.ivContent.setLayoutParams(lParams);
			viewHolder.ivContent.setImageBitmap(null);
		}

		if (entity.commentCount != null) {
			viewHolder.tv_commentCount.setText(entity.commentCount);
		} else {
			viewHolder.tv_commentCount.setText("0");
		}
		if (entity.likeCount != null) {
			viewHolder.tv_likeCount.setText(entity.likeCount);
		} else {
			viewHolder.tv_likeCount.setText("0");
		}
		if (entity.dislikeCount != null) {
			viewHolder.tv_dislikeCount.setText(entity.dislikeCount);
		} else {
			viewHolder.tv_dislikeCount.setText("0");
		}

		viewHolder.btn_action.setTag(entity.postId);

		if (!entity.canComment) {
			viewHolder.btn_action.setVisibility(View.GONE);
		}

		RelativeLayout rl_container = (RelativeLayout) convertView
				.findViewById(R.id.rl_container);
		rl_container.setTag(entity);

		return convertView;
	}

	static class ViewHolder {
		public SmartImageView ivAvatar;
		public TextView tvUsername;
		public TextView tvDate;
		public TextView tvContent;
		public SmartImageView ivContent;

		public TextView tv_commentCount;
		public TextView tv_likeCount;
		public TextView tv_dislikeCount;

		public Button btn_action;
	}

}
