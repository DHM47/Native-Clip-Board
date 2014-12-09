package com.dhm47.nativeclipboard.comparators;

import java.util.Comparator;

import com.dhm47.nativeclipboard.Clip;

public class PinnedFirst implements Comparator<Clip>{

	@Override
	public int compare(Clip lhs, Clip rhs) {
		if(lhs.isPinned()&&rhs.isPinned()) return (lhs.getTime()>rhs.getTime() ? -1 : (lhs.getTime()==rhs.getTime() ? 0 : 1));
		else if (rhs.isPinned())return 1;
		else if (lhs.isPinned())return-1;
		else return (lhs.getTime()>rhs.getTime() ? -1 : (lhs.getTime()==rhs.getTime() ? 0 : 1));
	}
	
}
