package com.happygoatstudios.bt.service.plugin.settings;

import android.os.Parcel;
import android.os.Parcelable;

public class EncodingOption extends BaseOption implements Parcelable {

	public EncodingOption() {
		this.type = TYPE.ENCODING;
		setValue("ISO-8859-1");
	}
	
	public EncodingOption(Parcel p) {
		this.type = TYPE.ENCODING;
		setTitle(p.readString());
		setDescription(p.readString());
		setKey(p.readString());
		setValue(p.readString());
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
		p.writeString((String)value);
		
	}

	@Override
	public void setValue(Object o) {
		value = (String)o;
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
	
	public static final Parcelable.Creator<EncodingOption> CREATOR = new Parcelable.Creator<EncodingOption>() {

		public EncodingOption createFromParcel(Parcel arg0) {
			return new EncodingOption(arg0);
		}

		public EncodingOption[] newArray(int arg0) {
			return new EncodingOption[arg0];
		}
	};

}