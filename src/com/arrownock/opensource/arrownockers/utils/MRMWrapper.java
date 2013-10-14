package com.arrownock.opensource.arrownockers.utils;

import android.content.Context;

import com.arrownock.exception.ArrownockException;
import com.arrownock.mrm.MRM;

public class MRMWrapper {
	private static MRM mrm = null;

	public static MRM getMRM(Context ctx) {
		if (mrm == null) {
			try {
				mrm = new MRM(ctx, AnUtils.AppKey);
				mrm.setSecure(true);
				mrm.setTimeout(10000);
			} catch (ArrownockException e) {
				e.printStackTrace();
				return null;
			}
		}
		return mrm;
	}
}
