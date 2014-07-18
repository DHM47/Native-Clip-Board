package com.dhm47.nativeclipboard;

import android.content.Context;

public class Util{
	public static int px(int dp, Context c) {
		return (int) (dp * c.getResources().getDisplayMetrics().density);
	}
	
}