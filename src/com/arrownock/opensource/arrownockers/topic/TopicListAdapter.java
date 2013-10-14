package com.arrownock.opensource.arrownockers.topic;

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

public class TopicListAdapter extends BaseAdapter {

	private List<TopicEntity> coll;

	private LayoutInflater mInflater;

	public TopicListAdapter(Context context, List<TopicEntity> coll) {
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

		TopicEntity entity = coll.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.cell_topic, null);

			viewHolder = new ViewHolder();
			viewHolder.tvTime = (TextView) convertView
					.findViewById(R.id.tv_topic_timestamp);
			viewHolder.tvTitle = (TextView) convertView
					.findViewById(R.id.tv_topic_name);
			viewHolder.tvText = (TextView) convertView
					.findViewById(R.id.tv_topic_last_message);
			viewHolder.ivNew = (ImageView) convertView
					.findViewById(R.id.iv_topic_new);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.tvTime.setText(entity.lastTimeString);
		viewHolder.tvTitle.setText(entity.topicName + " (" + entity.count
				+ "人)");
		String textString = entity.lastMessage;
		viewHolder.tvText.setText(textString);
		viewHolder.ivNew.setVisibility(entity.unread ? View.VISIBLE
				: View.INVISIBLE);
		viewHolder.tvTitle.setTypeface(null, entity.unread ? Typeface.BOLD
				: Typeface.NORMAL);

		convertView.setTag(R.id.lv_topic, entity.topicId);

		return convertView;
	}

	static class ViewHolder {
		public TextView tvTime;
		public TextView tvTitle;
		public TextView tvText;
		public ImageView ivNew;
	}

}
