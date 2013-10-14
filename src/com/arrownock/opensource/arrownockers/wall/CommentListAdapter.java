package com.arrownock.opensource.arrownockers.wall;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.utils.AnUtils;
import com.loopj.android.image.SmartImageView;

public class CommentListAdapter extends BaseAdapter {

	private List<CommentEntity> coll;

	private LayoutInflater mInflater;

	public CommentListAdapter(Context context, List<CommentEntity> coll) {
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

		CommentEntity entity = coll.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.row_comment, null);
			viewHolder = new ViewHolder();
			viewHolder.ivAvatar = (SmartImageView) convertView
					.findViewById(R.id.iv_avatar);
			viewHolder.tvUsername = (TextView) convertView
					.findViewById(R.id.tv_username);
			viewHolder.tvDate = (TextView) convertView
					.findViewById(R.id.tv_date);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_content);

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

		return convertView;
	}

	static class ViewHolder {
		public SmartImageView ivAvatar;
		public TextView tvUsername;
		public TextView tvDate;
		public TextView tvContent;
	}
}
