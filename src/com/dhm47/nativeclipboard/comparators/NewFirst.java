package com.dhm47.nativeclipboard.comparators;

import java.util.Comparator;

import com.dhm47.nativeclipboard.Clip;

public class NewFirst implements Comparator<Clip>{

	@Override
	public int compare(Clip lhs, Clip rhs) {
		return (lhs.getTime()>rhs.getTime() ? -1 : (lhs.getTime()==rhs.getTime() ? 0 : 1));
	}
	
}
