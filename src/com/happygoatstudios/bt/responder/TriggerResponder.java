package com.happygoatstudios.bt.responder;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public abstract class TriggerResponder implements Parcelable {

	
	public static final int RESPONDER_TYPE_TOAST = 101;
	public static final int RESPONDER_TYPE_NOTIFICATION = 102;
	public static final int RESPONDER_TYPE_ACK = 103;
	
	public enum RESPONDER_TYPE {
		NOTIFICATION(RESPONDER_TYPE_NOTIFICATION),
		TOAST(RESPONDER_TYPE_TOAST),
		ACK(RESPONDER_TYPE_ACK);
		
		private int value;
		
		private RESPONDER_TYPE(int i) {
			value = i;
		}
		
		public int getIntVal() {
			return value;
		}
	}
	
	private RESPONDER_TYPE type;
	
	public static final String FIRE_WINDOW_OPEN = "windowOpen";
	public static final String FIRE_WINDOW_CLOSED = "windowClosed";
	public static final String FIRE_ALWAYS = "always";
	public static final String FIRE_NEVER = "none";
	
	public enum FIRE_WHEN {
		WINDOW_CLOSED(FIRE_WINDOW_CLOSED),
		WINDOW_OPEN(FIRE_WINDOW_OPEN),
		WINDOW_BOTH(FIRE_ALWAYS),
		WINDOW_NEVER(FIRE_NEVER);
		
		private String value;
		
		private FIRE_WHEN(String i) {
			if(i != null) {
				value = i;
			} else {
				value = "always";
			}
		}
			
		public String getString() {
			return value;
		}
	}
	
	private FIRE_WHEN fireType;
	
	public TriggerResponder(RESPONDER_TYPE pType) {
		setType(pType);
	}

	public void setType(RESPONDER_TYPE type) {
		this.type = type;
	}

	public RESPONDER_TYPE getType() {
		return type;
	}
	
	public void addFireType(FIRE_WHEN in) {
		//will always be WINDOW_OPEN or WINDOW_CLOSED
		switch(in) {
		case WINDOW_OPEN:
			if(fireType == FIRE_WHEN.WINDOW_CLOSED || fireType == FIRE_WHEN.WINDOW_BOTH) {
				fireType = FIRE_WHEN.WINDOW_BOTH;
			} else if(fireType == FIRE_WHEN.WINDOW_OPEN || fireType == FIRE_WHEN.WINDOW_NEVER) {
				fireType = FIRE_WHEN.WINDOW_OPEN;
			}
			break;
		case WINDOW_CLOSED:
			if(fireType == FIRE_WHEN.WINDOW_OPEN || fireType == FIRE_WHEN.WINDOW_BOTH) {
				fireType = FIRE_WHEN.WINDOW_BOTH;
			} else if(fireType == FIRE_WHEN.WINDOW_CLOSED || fireType == FIRE_WHEN.WINDOW_NEVER) {
				fireType = FIRE_WHEN.WINDOW_CLOSED;
			}
			break;
		}
		//Log.e("RESPONDER","ADDED " + in.getString() + " FIRE TYPE NOW " + fireType.getString());
	}
	
	public void removeFireType(FIRE_WHEN in) {
		switch(in) {
		case WINDOW_OPEN:
			if(fireType == FIRE_WHEN.WINDOW_BOTH) {
				fireType = FIRE_WHEN.WINDOW_CLOSED;
			} else if (fireType == FIRE_WHEN.WINDOW_OPEN) {
				fireType = FIRE_WHEN.WINDOW_NEVER;
			}
			break;
		case WINDOW_CLOSED:
			if(fireType == FIRE_WHEN.WINDOW_BOTH) {
				fireType = FIRE_WHEN.WINDOW_OPEN;
			} else if (fireType == FIRE_WHEN.WINDOW_CLOSED) {
				fireType = FIRE_WHEN.WINDOW_NEVER;
			}
			break;
		default:
			break;
		}
		
		//Log.e("RESPONDER","REMOVED " + in.getString() + " FIRE TYPE NOW " + fireType.getString());
	}
	
	public abstract void doResponse(Context c,String displayname,int triggernumber,boolean windowIsOpen,Handler dispatcher);
	public abstract TriggerResponder copy();
	//public abstract void writeToParcel(Parcel in,int args);

	public void setFireType(FIRE_WHEN fireType) {
		this.fireType = fireType;
	}

	public FIRE_WHEN getFireType() {
		return fireType;
	}
	
	//public int describeContents() {
		// TODO Auto-generated method stub
	//	return 0;
	//}
	
	
}
