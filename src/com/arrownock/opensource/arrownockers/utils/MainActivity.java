package com.arrownock.opensource.arrownockers.utils;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.arrownock.opensource.arrownockers.R;
import com.arrownock.opensource.arrownockers.chat.SessionActivity;
import com.arrownock.opensource.arrownockers.push.PushWelcomeActivity;
import com.arrownock.opensource.arrownockers.topic.TopicListActivity;
import com.arrownock.opensource.arrownockers.wall.WallNavigationActivity;
import com.capricorn.ArcMenu;

public class MainActivity extends Activity implements OnTouchListener,
		OnGestureListener {

	private static final String logTag = "MainActivity";

	private static final int[] ITEM_DRAWABLES = { R.drawable.icon_push,
			R.drawable.icon_im, R.drawable.icon_circle, R.drawable.icon_wall };

	private ArcMenu featureArcMenu;
	private ImageView ivID;
	private TextView tvID;
	private TextView tvClock;
	private RelativeLayout rlYellowBackground;
	private RelativeLayout rlBlueBackground;
	private RelativeLayout rlMain;
	private RelativeLayout rlSettings;
	private boolean pulled = false;
	private static boolean hasEverPulled = false;
	private float thisDelta = 0.05f;
	private static int height;
	private static int statusHeight = 0;

	private GestureDetector gestureDetector;

	private int PULL_UPWARD_DURATION = 950;

	public static boolean alive = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();

		AnUtils.mainActivity = MainActivity.this;

		AnUtils.initArrownockComponents(MainActivity.this);

		alive = true;
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		if (AnUtils.getCurrentClientId() == null || AnUtils.getAnId() == null) {
			AnUtils.initArrownockComponents(MainActivity.this);
		}

		alive = true;

		paintBackground();

		// 跳动提示可以上拉
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				jump(thisDelta);
			}
		}, 3000);

		// 展开菜单
		if (!featureArcMenu.mArcLayout.isExpanded()) {
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					featureArcMenu.controlButton.performClick();
				}
			}, 800);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		alive = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (AnIMWrapper.getWrapper() != null) {
			AnIMWrapper.getWrapper().disconnect();
		}

		alive = false;
	}

	private void initView() {
		featureArcMenu = (ArcMenu) findViewById(R.id.feature_menu);
		featureArcMenu.bringToFront();
		ivID = (ImageView) findViewById(R.id.iv_main_id);
		tvID = (TextView) findViewById(R.id.tv_main_id);
		if (AnUtils.getCurrentUsername() != null) {
			tvID.setText("ID: " + AnUtils.getCurrentUsername());
		}
		tvClock = (TextView) findViewById(R.id.tv_clock);
		tvClock.setText(AnUtils.getMainClockString(new Date()));
		rlYellowBackground = (RelativeLayout) findViewById(R.id.iv_yellow);
		rlBlueBackground = (RelativeLayout) findViewById(R.id.iv_blue);
		rlMain = (RelativeLayout) findViewById(R.id.rl_main);
		rlMain.setLongClickable(true);
		rlMain.setOnTouchListener(this);
		gestureDetector = new GestureDetector(this, this);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		height = metrics.heightPixels;
		statusHeight = getStatusHeight();

		rlSettings = (RelativeLayout) findViewById(R.id.rl_settings);
		final LayoutParams settingsParams = (LayoutParams) rlSettings
				.getLayoutParams();
		settingsParams.topMargin = (int) (0.2 * height - statusHeight);

		paintBackground();

		setPullAnimation();

		initArcMenu(featureArcMenu, ITEM_DRAWABLES);

		setHeartbeatAnimation();

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				jump(thisDelta);
			}
		}, 3000);
	}

	// 主页面跳动
	private void jump(float delta) {
		if (thisDelta - 0.03f < 0.001f) {
			thisDelta = 0.05f;
			return;
		}
		thisDelta = delta;

		if (hasEverPulled) {
			return;
		}
		playJumpAnimation(thisDelta);
	}

	// 跳起动画
	private void playJumpAnimation(final float delta) {
		float originalY = 0;
		float finalY = 0 - height * delta;
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(new TranslateAnimation(0, 0, originalY,
				finalY));

		animationSet.setDuration(300);
		animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
		animationSet.setFillAfter(true);

		animationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				playLandAnimation(delta);
			}
		});

		rlMain.startAnimation(animationSet);
	}

	// 落下动画
	private void playLandAnimation(final float delta) {
		float originalY = 0 - height * delta;
		float finalY = 0;
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(new TranslateAnimation(0, 0, originalY,
				finalY));

		animationSet.setDuration(200);
		animationSet.setInterpolator(new AccelerateInterpolator());
		animationSet.setFillAfter(true);

		animationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				jump(0.03f);
			}
		});

		rlMain.startAnimation(animationSet);
	}

	// 按钮模拟心脏跳动
	private void playHeartbeatAnimation() {
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(new ScaleAnimation(1.0f, 1.8f, 1.0f, 1.8f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f));
		animationSet.addAnimation(new AlphaAnimation(1.0f, 0.4f));

		animationSet.setDuration(200);
		animationSet.setInterpolator(new AccelerateInterpolator());
		animationSet.setFillAfter(true);

		animationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				AnimationSet animationSet = new AnimationSet(true);
				animationSet.addAnimation(new ScaleAnimation(1.8f, 1.0f, 1.8f,
						1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f));
				animationSet.addAnimation(new AlphaAnimation(0.4f, 1.0f));

				animationSet.setDuration(600);
				animationSet.setInterpolator(new DecelerateInterpolator());
				animationSet.setFillAfter(false);

				featureArcMenu.controlButton.startAnimation(animationSet);
			}
		});

		featureArcMenu.controlButton.startAnimation(animationSet);
	}

	// 根据当前时间填充背景颜色
	private void paintBackground() {
		Date date = new Date();
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		LayoutParams yellowParams = (LayoutParams) rlYellowBackground
				.getLayoutParams();
		LayoutParams blueParams = (LayoutParams) rlBlueBackground
				.getLayoutParams();

		if (hour == 0) {
			// paint all blue
			yellowParams.width = 0;
			blueParams.width = LayoutParams.MATCH_PARENT;
		} else if (hour == 12) {
			// paint all yellow
			yellowParams.width = LayoutParams.MATCH_PARENT;
			blueParams.width = 0;
		} else if (hour > 0 && hour < 12) {
			// paint left yellow & right blue
			int yellowWidth = hour * width / 12;
			yellowParams.width = yellowWidth;
			yellowParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

			int blueWidth = width - yellowWidth;
			blueParams.width = blueWidth;
			blueParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		} else if (hour > 12 && hour <= 23) {
			// paint left blue & right yellow
			int blueWidth = (hour - 12) * width / 12;
			blueParams.width = blueWidth;
			blueParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

			int yellowWidth = width - blueWidth;
			yellowParams.width = yellowWidth;
			yellowParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}

		rlYellowBackground.setLayoutParams(yellowParams);
		rlBlueBackground.setLayoutParams(blueParams);
	}

	// 上拉/下拉主页面
	private void pull(boolean upward) {
		if (upward && pulled) {
			return;
		}
		if (!upward && !pulled) {
			return;
		}

		float originalY;
		float finalY;
		if (!pulled) {
			originalY = 0;
			finalY = (float) (0 - height + 0.2 * height);
		} else {
			originalY = (float) (0 - height + 0.2 * height);
			finalY = 0;
		}
		pulled = !pulled;

		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(new TranslateAnimation(0, 0, originalY,
				finalY));

		animationSet.setDuration(300);
		animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
		animationSet.setFillAfter(true);

		animationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				if (!pulled) {
					rlMain.bringToFront();
					for (int i = 0; i < featureArcMenu.mArcLayout
							.getChildCount(); i++) {
						featureArcMenu.mArcLayout.getChildAt(i).setClickable(
								true);
					}
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (pulled) {
					ivID.setRotation(180.0f);
					rlSettings.bringToFront();
					for (int i = 0; i < featureArcMenu.mArcLayout
							.getChildCount(); i++) {
						featureArcMenu.mArcLayout.getChildAt(i).setClickable(
								false);
					}
				} else {
					ivID.setRotation(360.0f);
				}
			}
		});

		rlMain.startAnimation(animationSet);

		hasEverPulled = true;
	}

	// 设置按钮动画
	private void setHeartbeatAnimation() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				String lastTimeString = AnUtils.getMainClockString(new Date());
				while (true) {
					try {
						Thread.sleep(1000);

						if (!featureArcMenu.mArcLayout.isExpanded()) {
							runOnUiThread(new Runnable() {
								public void run() {
									playHeartbeatAnimation();
								}
							});
						}

						final String currentTimeString = AnUtils
								.getMainClockString(new Date());
						if (!currentTimeString.equals(lastTimeString)) {
							lastTimeString = currentTimeString;
							runOnUiThread(new Runnable() {
								public void run() {
									tvClock.setText(currentTimeString);
									paintBackground();
								}
							});
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		});
		thread.start();
	}

	// 设置主界面上拉动画
	private void setPullAnimation() {
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f));
		animationSet.addAnimation(new AlphaAnimation(0.8f, 0.1f));

		animationSet.setDuration(PULL_UPWARD_DURATION);
		animationSet.setInterpolator(new DecelerateInterpolator());
		animationSet.setFillAfter(false);

		animationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}
		});
	}

	private int getStatusHeight() {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0;
		int height = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			height = getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return height;
	}

	// 初始化功能按钮
	private void initArcMenu(ArcMenu menu, int[] itemDrawables) {
		final int itemCount = itemDrawables.length;
		for (int i = 0; i < itemCount; i++) {
			ImageView item = new ImageView(this);
			item.setImageResource(itemDrawables[i]);

			final int position = i;
			menu.addItem(item, new OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (position) {
					case 0:
						showPush();
						break;
					case 1:
						showIM();
						break;
					case 2:
						showCircle();
						break;
					case 3:
						showWall();
						break;

					default:
						break;
					}
				}
			});
		}
	}

	private void showPush() {
		showFeatureActivity(PushWelcomeActivity.class);
	}

	private void showIM() {
		showFeatureActivity(SessionActivity.class);
	}

	private void showCircle() {
		showFeatureActivity(TopicListActivity.class);
	}

	private void showWall() {
		showFeatureActivity(WallNavigationActivity.class);
	}

	private void showFeatureActivity(final Class<?> activityClass) {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent(MainActivity.this, activityClass);
				startActivity(intent);

				overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
			}
		}, 300);
	}

	public void onClearClick(View view) {
		DBManager.clear();

		AnUtils.showToast(this, "操作成功", Gravity.CENTER, alive);
	}

	public void onSetCurrentUsername(final String username,
			final boolean isError) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				tvID.setText("ID: " + username);
				if (isError) {
					AnUtils.showToast(MainActivity.this, username,
							Gravity.CENTER, alive);
				}
			}
		});

	}

	@Override
	public boolean onDown(MotionEvent e) {
		pull(false);

		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.i(logTag, "onFling");

		if (e1.getY() - e2.getY() > 100) {
			pull(true);
		} else if (e2.getY() >= e1.getY()) {
			pull(false);
		}

		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (pulled) {
			if (event.getY() > height * 0.2 - statusHeight) {
				return false;
			}
		}

		return gestureDetector.onTouchEvent(event);
	}
}
