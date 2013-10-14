package com.arrownock.opensource.arrownockers.wall;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.arrownock.opensource.arrownockers.R;

public class WallNavigationActivity extends SherlockFragmentActivity {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mWallTitles;

	private Fragment currentFragment;
	private WallNewsFragment wallNewsFragment;
	private WallTumblrFragment wallTumblrFragment;
	private WallSurveyFragment wallSurveyFragment;

	private static boolean hasEverOpened = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wall_summary);

		initView();

		if (savedInstanceState == null) {
			selectItem(0);
		}

		setDrawerOpenDelay(1500);
	}

	private void initView() {
		mTitle = mDrawerTitle = getTitle();
		mWallTitles = getResources().getStringArray(R.array.wall_titles);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.cell_wall_drawer, mWallTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		getSupportActionBar().setBackgroundDrawable(
				this.getBaseContext().getResources()
						.getDrawable(R.color.blue_color));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, 0, 0) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
			}

			public void onDrawerOpened(View drawerView) {
				hasEverOpened = true;
				getSupportActionBar().setTitle(mDrawerTitle);
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void setDrawerOpenDelay(final int millis) {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (!hasEverOpened) {
					mDrawerLayout.openDrawer(Gravity.LEFT);
				}
			}
		}, millis);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {

			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		boolean isNew = false;
		FragmentManager fragmentManager = getSupportFragmentManager();
		if (currentFragment != null) {
			fragmentManager.beginTransaction().hide(currentFragment).commit();
		}

		switch (position) {
		case 0:
			if (wallNewsFragment == null) {
				wallNewsFragment = new WallNewsFragment();
				isNew = true;
			}
			currentFragment = wallNewsFragment;
			break;
		case 1:
			if (wallTumblrFragment == null) {
				wallTumblrFragment = new WallTumblrFragment();
				isNew = true;
			}
			currentFragment = wallTumblrFragment;
			break;
		case 2:
			if (wallSurveyFragment == null) {
				wallSurveyFragment = new WallSurveyFragment();
				isNew = true;
			}
			currentFragment = wallSurveyFragment;
			break;
		default:
			if (wallNewsFragment == null) {
				wallNewsFragment = new WallNewsFragment();
				isNew = true;
			}
			currentFragment = wallNewsFragment;
			break;
		}

		if (isNew) {
			fragmentManager.beginTransaction()
					.add(R.id.content_frame, currentFragment).commit();
		} else {
			fragmentManager.beginTransaction().show(currentFragment).commit();
		}

		mDrawerList.setItemChecked(position, true);
		setTitle(mWallTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
}
