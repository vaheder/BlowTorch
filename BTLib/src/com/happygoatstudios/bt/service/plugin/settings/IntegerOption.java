package com.happygoatstudios.bt.service.plugin.settings;

import java.math.BigInteger;

import android.os.Parcel;
import android.os.Parcelable;

public class IntegerOption extends BaseOption implements Parcelable {

	public IntegerOption() {
		this.type = TYPE.INTEGER;
		this.setValue(new Integer(0));
	}
	
	public IntegerOption(Parcel p) {
		this.type = TYPE.INTEGER;
		setTitle(p.readString());
		setDescription(p.readString());
		setKey(p.readString());
		setValue(new Integer(p.readInt()));
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeString(title);
		p.writeString(description);
		p.writeString(key);
		p.writeInt((Integer)getValue());
	}

	@Override
	public void setValue(Object o) {
		if(o instanceof Integer) {
			value = (Integer)o;
		} else if(o instanceof String) {
			try {
				String str = (String)o;
				if(str.startsWith("#")) {
					BigInteger bigint = new BigInteger(str.substring(1,str.length()-1),16);
					value = bigint.intValue();
				} else {
					int num = Integer.parseInt((String)o);
					value = (Integer)num;
				}
			} catch(NumberFormatException e) {
				
			}
		}
	}

	@Override
	public Object getValue() {
		// TODO Auto-generated method stub
		return (Object)value;
	}

	@Override
	public Object getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultValue(Object o) {
		// TODO Auto-generated method stub
		
	}
	
	public static final Parcelable.Creator<IntegerOption> CREATOR = new Parcelable.Creator<IntegerOption>() {

		public IntegerOption createFromParcel(Parcel arg0) {
			return new IntegerOption(arg0);
		}

		public IntegerOption[] newArray(int arg0) {
			return new IntegerOption[arg0];
		}
	};

}
