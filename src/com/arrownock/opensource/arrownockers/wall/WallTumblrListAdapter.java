package com.arrownock.opensource.arrownockers.wall;

import java.util.List;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.loopj.android.image.SmartImageView;

public class WallTumblrListAdapter extends BaseAdapter {

	private List<WallTumblrEntity> coll;

	private Context ctx;

	private LayoutInflater mInflater;

	public WallTumblrListAdapter(Context context, List<WallTumblrEntity> coll) {
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

	public View getView(int position, View convertView, ViewGroup parent) {

		WallTumblrEntity entity = coll.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.row_wall, null);

			viewHolder = new ViewHolder();
			viewHolder.ivTitle = (SmartImageView) convertView
					.findViewById(R.id.iv_title);
			viewHolder.tvTitle = (TextView) convertView
					.findViewById(R.id.tv_title);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		int result = width - 50 * 2;
		LayoutParams lParams = viewHolder.ivTitle.getLayoutParams();
		lParams.width = result;
		lParams.height = result;
		viewHolder.ivTitle.setLayoutParams(lParams);
		if (entity.imageURLString != null) {
			viewHolder.ivTitle.setImageUrl(entity.imageURLString);
		} else {
			viewHolder.ivTitle.setImageBitmap(null);
		}

		if (entity.title != null)
			viewHolder.tvTitle.setText(entity.title);

		convertView.setTag(R.id.lv_wall, entity.wallId);

		return convertView;
	}

	static class ViewHolder {
		public SmartImageView ivTitle;
		public TextView tvTitle;
	}
}
