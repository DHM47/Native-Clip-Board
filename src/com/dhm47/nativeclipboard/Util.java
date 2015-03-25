package com.dhm47.nativeclipboard;

import android.content.Context;

public class Util{
	public static int px(int dp, Context ctx) {
		return (int) (dp * ctx.getResources().getDisplayMetrics().density);
	}
	
}