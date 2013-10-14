package com.arrownock.opensource.arrownockers.utils;

import android.app.Application;

import com.activeandroid.ActiveAndroid;

public class Arrownockers extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		ActiveAndroid.initialize(this);

		AnUtils.initDataStoreComponents(getApplicationContext());

		AnUtils.initLocationComponents(getApplicationContext());
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		ActiveAndroid.dispose();
	}
}
