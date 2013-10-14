package com.arrownock.opensource.arrownockers.topic;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.topic.MessageEntity.EntityType;

public class MessageListAdapter extends BaseAdapter {

	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}

	private List<MessageEntity> coll;

	private LayoutInflater mInflater;

	public MessageListAdapter(Context context, List<MessageEntity> coll) {
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

	public int getItemViewType(int position) {
		MessageEntity entity = coll.get(position);

		if (entity.isComMsg) {
			return IMsgViewType.IMVT_COM_MSG;
		} else {
			return IMsgViewType.IMVT_TO_MSG;
		}

	}

	public int getViewTypeCount() {
		return 2;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		MessageEntity entity = coll.get(position);
		boolean isComMsg = entity.isComMsg;
		EntityType entityType = entity.entityType;

		ViewHolder viewHolder = null;
		if (convertView == null) {

			viewHolder = new ViewHolder();
			if (isComMsg) {
				convertView = mInflater.inflate(R.layout.cell_message_left,
						null);
				viewHolder.tvRealname = (TextView) convertView
						.findViewById(R.id.tv_message_realname);
			} else {
				convertView = mInflater.inflate(R.layout.cell_message_right,
						null);
				viewHolder.pbar = (ProgressBar) convertView
						.findViewById(R.id.pbar);
			}

			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_message_timestamp);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_message_content);
			viewHolder.ivContent = (ImageView) convertView
					.findViewById(R.id.iv_message_content);
			viewHolder.isComMsg = isComMsg;

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.tvSendTime.setText(entity.dateString);
		if (entity.isComMsg) {
			if (entity.realname == null) {
				viewHolder.tvRealname.setText(entity.username);
			} else {
				viewHolder.tvRealname.setText(entity.realname);
			}
		} else {
			if (!entity.unsent) {
				viewHolder.pbar.setVisibility(View.INVISIBLE);
			} else {
				viewHolder.pbar.setVisibility(View.VISIBLE);
			}
		}

		switch (entityType) {
		case ET_TEXT:
			viewHolder.tvContent.setText(entity.text);
			viewHolder.ivContent.setImageBitmap(null);
			break;
		case ET_IMAGE:
			byte[] imageBytes = entity.imageBytes;
			Bitmap bm = BitmapFactory.decodeByteArray(imageBytes, 0,
					imageBytes.length);
			viewHolder.ivContent.setImageBitmap(bm);
			viewHolder.tvContent.setText(null);
			break;
		case ET_AUDIO:
			viewHolder.ivContent.setImageResource(R.drawable.speaker);
			viewHolder.tvContent.setText(null);
			break;

		default:
			break;
		}

		viewHolder.ivContent.setBackgroundResource(0);
		AnimationDrawable animationDrawable = (AnimationDrawable) viewHolder.ivContent
				.getBackground();
		if (animationDrawable != null) {
			animationDrawable.stop();
		}

		viewHolder.tvContent.setTag(entity);
		viewHolder.ivContent.setTag(entity);
		return convertView;
	}

	static class ViewHolder {
		public TextView tvSendTime;
		public TextView tvContent;
		public ImageView ivContent;
		public boolean isComMsg = true;
		public TextView tvRealname;
		public ProgressBar pbar;
	}

}
