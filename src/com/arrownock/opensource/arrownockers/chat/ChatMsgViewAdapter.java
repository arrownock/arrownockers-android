package com.arrownock.opensource.arrownockers.chat;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.chat.ChatMsgEntity.EntityType;

public class ChatMsgViewAdapter extends BaseAdapter {

	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}

	private List<ChatMsgEntity> coll;

	private LayoutInflater mInflater;

	public ChatMsgViewAdapter(Context context, List<ChatMsgEntity> coll) {
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
		ChatMsgEntity entity = coll.get(position);

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

		ChatMsgEntity entity = coll.get(position);
		boolean isComMsg = entity.isComMsg;
		EntityType entityType = entity.entityType;

		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();

			if (isComMsg) {
				convertView = mInflater.inflate(R.layout.cell_chat_left, null);

			} else {
				convertView = mInflater.inflate(R.layout.cell_chat_right, null);
				viewHolder.tvStatus = (TextView) convertView
						.findViewById(R.id.tv_chat_status);
			}

			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_chat_timestamp);
			viewHolder.tvRealname = (TextView) convertView
					.findViewById(R.id.tv_chat_realname);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_chat_content);
			viewHolder.ivContent = (ImageView) convertView
					.findViewById(R.id.iv_chat_content);
			viewHolder.isComMsg = isComMsg;

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.tvSendTime.setText(entity.date);
		String realname = null;
		if (!isComMsg) {
			String statuString = entity.status;
			if (statuString != null) {
				viewHolder.tvStatus
						.setTextColor(statuString.equals("read") ? Color.GREEN
								: Color.BLUE);
			}
			viewHolder.tvStatus.setText(entity.status);
		} else {
			realname = entity.realname;
			viewHolder.tvRealname.setText(realname);
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
			viewHolder.ivContent.setImageResource(R.drawable.audio_image);
			viewHolder.tvContent.setText(null);
			break;
		case ET_LOCATION:
			viewHolder.ivContent.setImageResource(R.drawable.map);
			viewHolder.tvContent.setText(null);
			break;

		default:
			break;
		}

		viewHolder.tvContent.setTag(entity);
		viewHolder.ivContent.setTag(entity);
		return convertView;
	}

	static class ViewHolder {
		public TextView tvSendTime;
		public TextView tvRealname;
		public TextView tvStatus;
		public ImageView ivUserHead;
		public TextView tvContent;
		public ImageView ivContent;
		public boolean isComMsg = true;
	}

}
