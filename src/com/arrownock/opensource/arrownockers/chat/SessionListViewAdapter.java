package com.arrownock.opensource.arrownockers.chat;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;

public class SessionListViewAdapter extends BaseAdapter {

	private List<SessionEntity> coll;

	private LayoutInflater mInflater;

	public SessionListViewAdapter(Context context, List<SessionEntity> coll) {
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

	public View getView(int position, View convertView, ViewGroup parent) {

		SessionEntity entity = coll.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.cell_session_list, null);

			viewHolder = new ViewHolder();
			viewHolder.tvTitle = (TextView) convertView
					.findViewById(R.id.tv_session_title);
			viewHolder.tvText = (TextView) convertView
					.findViewById(R.id.tv_text);
			viewHolder.tvTime = (TextView) convertView
					.findViewById(R.id.tv_time);
			viewHolder.ivNew = (ImageView) convertView
					.findViewById(R.id.iv_new);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.tvTime.setText(entity.updateTime);
		viewHolder.tvTitle.setText(entity.realnames);
		String textString = entity.lastMessage;
		if (textString != null) {
			if (textString.length() > 30) {
				textString = textString.substring(0, 30) + "...";
			}
			viewHolder.tvText.setText(textString);
			viewHolder.tvText.getLayoutParams().height = 100;
		}

		viewHolder.ivNew
				.setVisibility(entity.status.equals("unread") ? View.VISIBLE
						: View.INVISIBLE);
		viewHolder.tvTitle.setTypeface(null,
				entity.status.equals("unread") ? Typeface.BOLD
						: Typeface.NORMAL);

		return convertView;
	}

	static class ViewHolder {
		public ImageView ivHead;
		public TextView tvTime;
		public TextView tvTitle;
		public TextView tvText;
		public ImageView ivNew;
	}

}
