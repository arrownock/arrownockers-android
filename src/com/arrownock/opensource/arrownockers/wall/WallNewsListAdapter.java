package com.arrownock.opensource.arrownockers.wall;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.loopj.android.image.SmartImageView;

public class WallNewsListAdapter extends BaseAdapter {

	private List<WallNewsEntity> coll;

	private LayoutInflater mInflater;

	public WallNewsListAdapter(Context context, List<WallNewsEntity> coll) {
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

		WallNewsEntity entity = coll.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.cell_wall_news, null);

			viewHolder = new ViewHolder();
			viewHolder.ivTitle = (SmartImageView) convertView
					.findViewById(R.id.iv_title);
			viewHolder.tvTitle = (TextView) convertView
					.findViewById(R.id.tv_title);
			viewHolder.tvSummary = (TextView) convertView
					.findViewById(R.id.tv_summary);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (entity.imageURLString != null) {
			viewHolder.ivTitle.setImageUrl(entity.imageURLString);
		} else {
			viewHolder.ivTitle.setImageBitmap(null);
		}

		if (entity.title != null) {
			entity.title = entity.title.replace("\\n", "\n");
			viewHolder.tvTitle.setText(entity.title);
		}

		if (entity.content != null) {
			entity.content = entity.content.replace("\\n", "\n");
			viewHolder.tvSummary.setText(entity.content);
		}

		convertView.setTag(R.id.lv_news, entity);

		return convertView;
	}

	static class ViewHolder {
		public SmartImageView ivTitle;
		public TextView tvTitle;
		public TextView tvSummary;
	}
}
