package com.dhm47.nativeclipboard;

import java.io.Serializable;
import java.util.List;

public class Clip implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 595229838452849440L;
	//private variables
	int position;
	long time;
	String text;
	String title;
	boolean pinned;
	
	// Empty constructor
	public Clip(){
		
	}
	// constructor
	public Clip(int position, long time, String text, String title,boolean pinned){
		this.position = position;
		this.time = time;
		this.text = text;
		this.title = title;
		this.pinned = pinned;
	}
	
	// constructor
	public Clip(long time, String text, String title,boolean pinned){
		this.time = time;
		this.text = text;
		this.title = title;
		this.pinned = pinned;
	}
	// getting Position
	public int getPosition(){
		return this.position;
	}
		
	// setting Position
	public void setPosition(int position){
		this.position = position;
	}
	// getting time
	public long getTime(){
		return this.time;
	}
	
	// setting time
	public void setTime(long time){
		this.time = time;
	}
	
	// getting text
	public String getText(){
		return this.text;
	}
	
	// setting text
	public void setText(String text){
		this.text = text;
	}
	
	// getting title
	public String getTitle(){
		return this.title;
	}
	
	// setting title
	public void setTitle(String title){
		this.title = title;
	}
	// getting if pinned
	public boolean isPinned(){
		return this.pinned;
	}
		
	// setting if pinned
	public void setPinned(boolean pinned){
		this.pinned = pinned;
	}
	public static int contains(List<Clip> mClip,Clip clip){
		int x=-1;
		for(Clip tClip:mClip){
			x++;
			if(clip.getText().equals(tClip.getText()))return x;
		}
		return -1;
	}
}
