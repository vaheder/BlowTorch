package com.offsetnull.bt.responder.ack;

import org.xml.sax.Attributes;

import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.TriggerResponder.FIRE_WHEN;
import com.offsetnull.bt.service.plugin.settings.BasePluginParser;
import com.offsetnull.bt.service.plugin.settings.PluginParser;
import com.offsetnull.bt.service.plugin.settings.PluginSettings;
import com.offsetnull.bt.timer.TimerData;
import com.offsetnull.bt.trigger.TriggerData;

import android.sax.StartElementListener;

public class AckElementListener implements StartElementListener {

	//PluginSettings settings = null;
	//PluginParser.NewItemCallback callback = null;
	TriggerData current_trigger = null;
	TimerData current_timer = null;
	Object selector = null;
	
	public AckElementListener(Object selector,TriggerData current_trigger,TimerData current_timer) {
		//this.settings = settings;
		//this.callback = callback;
		this.selector = selector;
		this.current_timer = current_timer;
		this.current_trigger = current_trigger;
	}
	
	public void start(Attributes a) {
		AckResponder r = new AckResponder();
		r.setAckWith(a.getValue("", BasePluginParser.ATTR_ACKWITH));
		String fireType = a.getValue("",BasePluginParser.ATTR_FIRETYPE);
		if(fireType == null) fireType = "";
		//Log.e("PARSER","ACK TAG READ, FIRETYPE IS:" + fireType);
		if(fireType.equals(TriggerResponder.FIRE_WINDOW_OPEN)) {
			r.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_OPEN);
		} else if (fireType.equals(TriggerResponder.FIRE_WINDOW_CLOSED)) {
			r.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_CLOSED);
		} else if (fireType.equals(TriggerResponder.FIRE_ALWAYS)) {
			r.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
		} else if (fireType.equals(TriggerResponder.FIRE_NEVER)) {
			r.setFireType(FIRE_WHEN.WINDOW_NEVER);
		} else {
			r.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
		}
		
		if(selector instanceof TriggerData) {
			current_trigger.getResponders().add(r.copy());
		} else if(selector instanceof TimerData) {
			current_timer.getResponders().add(r.copy());
		}
	
	}

}
