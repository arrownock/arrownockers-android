package com.arrownock.opensource.arrownockers.push;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.push.PushDetailsEntity.EntityType;

public class PushDetailsListAdapter extends BaseAdapter {

	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}

	private List<PushDetailsEntity> coll;

	private LayoutInflater mInflater;

	public PushDetailsListAdapter(Context context, List<PushDetailsEntity> coll) {
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
		PushDetailsEntity entity = coll.get(position);

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

		PushDetailsEntity entity = coll.get(position);
		boolean isComMsg = entity.isComMsg;
		EntityType entityType = entity.entityType;

		ViewHolder viewHolder = null;
		if (convertView == null) {

			if (isComMsg) {
				convertView = mInflater.inflate(R.layout.row_push_left, null);

			} else {
				convertView = mInflater.inflate(R.layout.row_push_right, null);
			}

			viewHolder = new ViewHolder();
			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_sendtime);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_chatcontent);
			viewHolder.ivContent = (ImageView) convertView
					.findViewById(R.id.iv_chatcontent);
			viewHolder.isComMsg = isComMsg;

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.tvSendTime.setText(entity.dateString);
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
		public TextView tvContent;
		public ImageView ivContent;
		public boolean isComMsg = true;
	}

}
