package com.happygoatstudios.bt.service.plugin.settings;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

public class SettingsGroup extends Option implements Parcelable {
	
	private static final int OPTION_BOOLEAN = 1;
	private static final int OPTION_OPTIONS = 2;
	private static final int OPTION_LIST = 3;
	private static final int OPTION_ENCODING = 4;
	private static final int OPTION_INTEGER = 5;
	private HashMap<String,Option> optionsMap = new HashMap<String,Option>();
	
	ArrayList<Option> options;
	
	public SettingsGroup() {
		options = new ArrayList<Option>();
		type = TYPE.GROUP;
	}
	
	public ArrayList<Option> getOptions() {
		return options;
	}
	
	public SettingsGroup(Parcel p) {
		type = TYPE.GROUP;
		setTitle(p.readString());
		setDescription(p.readString());
		setKey(p.readString());
		int size = p.readInt();
		options = new ArrayList<Option>(size);
		for(int i=0;i<size;i++) {
			int type = p.readInt();
			Option o = null;
			switch(type) {
			case OPTION_BOOLEAN:
				o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.BooleanOption.class.getClassLoader());
				break;
			case OPTION_LIST:
				o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.ListOption.class.getClassLoader());
				
				break;
			case OPTION_OPTIONS:
				o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.SettingsGroup.class.getClassLoader());
				
				break;
			case OPTION_ENCODING:
				o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.EncodingOption.class.getClassLoader());
				break;
			case OPTION_INTEGER:
				o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.IntegerOption.class.getClassLoader());
				break;
			}

			//Option o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.Option.class.getClassLoader());
			options.add(o);
		}
	}

	public void addOption(Option option) {
		updateOptionsMap(option);
		options.add(option);
	}
	
	private void updateOptionsMap(Option option) {
		switch(option.type) {
		case GROUP:
			SettingsGroup sg = (SettingsGroup)option;
			for(int i=0;i<sg.getOptions().size();i++) {
				updateOptionsMap(sg.getOptions().get(i));
			}
			break;
		default:
			optionsMap.put(option.getKey(),option);
			break;
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeString(getTitle());
		p.writeString(getDescription());
		p.writeString(getKey());
		p.writeInt(options.size());
		for(int i=0;i<options.size();i++) {
			Option o = options.get(i);
			switch(o.type) {
			case BOOLEAN:
				p.writeInt(OPTION_BOOLEAN);
				break;
			case GROUP:
				p.writeInt(OPTION_OPTIONS);
				break;
			case LIST:
				p.writeInt(OPTION_LIST);
				break;
			case ENCODING:
				p.writeInt(OPTION_ENCODING);
				break;
			case INTEGER:
				p.writeInt(OPTION_INTEGER);
				break;
			}
			p.writeParcelable(o, flags);
		}
	}
	
	public static final Parcelable.Creator<SettingsGroup> CREATOR = new Parcelable.Creator<SettingsGroup>() {

		public SettingsGroup createFromParcel(Parcel arg0) {
			return new SettingsGroup(arg0);
		}

		public SettingsGroup[] newArray(int arg0) {
			return new SettingsGroup[arg0];
		}
	};
	
	public Option findOptionByKey(String key) {
		return optionsMap.get(key);
	}
	
}