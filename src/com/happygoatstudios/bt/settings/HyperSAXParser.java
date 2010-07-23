package com.happygoatstudios.bt.settings;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.xml.sax.Attributes;


import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.responder.ack.AckResponder;
import com.happygoatstudios.bt.responder.notification.NotificationResponder;
import com.happygoatstudios.bt.responder.toast.ToastResponder;
import com.happygoatstudios.bt.trigger.TriggerData;

import android.content.Context;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;

public class HyperSAXParser extends BaseParser {

	public HyperSAXParser(String location, Context context) {
		super(location, context);
		// TODO Auto-generated constructor stub
	}
	
	public HyperSettings load() {
		final HyperSettings tmp = new HyperSettings();
		RootElement root = new RootElement("root");
		Element window = root.getChild(TAG_WINDOW);
		Element data = root.getChild(TAG_SERVICE);
		Element aliases = root.getChild(TAG_ALIASES);
		Element alias = aliases.getChild(TAG_ALIAS);
		Element buttonsets = root.getChild(TAG_BUTTONSETS);
		Element buttonset = buttonsets.getChild(TAG_BUTTONSET);
		Element selected = buttonsets.getChild(TAG_SELECTEDSET);
		Element button = buttonset.getChild(TAG_BUTTON);
		Element processperiod = root.getChild(TAG_PROCESSPERIOD);
		Element triggers = root.getChild(TAG_TRIGGERS);
		Element trigger = triggers.getChild(TAG_TRIGGER);
		Element notificationResponder = trigger.getChild(TAG_NOTIFICATIONRESPONDER);
		Element toastResponder = trigger.getChild(TAG_TOASTRESPONDER);
		Element ackResponder = trigger.getChild(TAG_ACKRESPONDER);
		
		final HashMap<String,String> aliases_read = new HashMap<String,String>();
		final HashMap<String,Vector<SlickButtonData>> buttons = new HashMap<String,Vector<SlickButtonData>>();
		final Vector<SlickButtonData> current_button_set = new Vector<SlickButtonData>();
		final StringBuffer button_set_name = new StringBuffer("default");
		final ColorSetSettings setinfo =  new ColorSetSettings();
		final HashMap<String,ColorSetSettings> colorsets = new HashMap<String,ColorSetSettings>();
		final HashMap<String,TriggerData> triggerSet = new HashMap<String,TriggerData>();
		final TriggerData current_trigger = new TriggerData();
		
		window.setStartElementListener(new StartElementListener() {

			public void start(Attributes attributes) {
				//read in attribute values.
				tmp.setLineSize(new Integer(attributes.getValue("",ATTR_LINESIZE)).intValue());
				tmp.setLineSpaceExtra(new Integer(attributes.getValue("",ATTR_SPACEEXTRA)).intValue());
				tmp.setMaxLines(new Integer(attributes.getValue("",ATTR_MAXLINES)).intValue());
				tmp.setFontName(attributes.getValue("",ATTR_FONTNAME));
				tmp.setFontPath(attributes.getValue("",ATTR_FONTPATH));
				
				int wmode = new Integer(attributes.getValue("",ATTR_WRAPMODE));
				switch(wmode) {
				case 0:
					tmp.setWrapMode(HyperSettings.WRAP_MODE.NONE);
					break;
				case 1:
					tmp.setWrapMode(HyperSettings.WRAP_MODE.BREAK);
					break;
				case 2:
					tmp.setWrapMode(HyperSettings.WRAP_MODE.WORD);
					break;
				default:
				}
			}
			
		});
		
		data.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				//read in the attributes.
				//ouch. just look at that nested ternary operator.
				tmp.setUseExtractUI( (a.getValue("",ATTR_USEEXTRACTUI) == null) ? false : (a.getValue("",ATTR_USEEXTRACTUI).equals("true")) ? true : false);
				tmp.setSemiIsNewLine( (a.getValue("",ATTR_SEMINEWLINE) == null) ? true : (a.getValue("",ATTR_SEMINEWLINE).equals("true")) ? true : false);
				tmp.setThrottleBackground( (a.getValue("",ATTR_THROTTLEBACKGROUND) == null) ? false : (a.getValue("",ATTR_THROTTLEBACKGROUND).equals("true")) ? true : false);
				
			}
			
		});
		
		/*(data.setEndTextElementListener(new EndTextElementListener() {

			public void end(String body) {
				int newline = new Integer(body).intValue();
				if(newline == 0) {
					tmp.setSemiIsNewLine(false);
				} else {
					tmp.setSemiIsNewLine(true);
				}
			}
			
		});*/
		
		alias.setStartElementListener(new StartElementListener() {

			public void start(Attributes attributes) {
				aliases_read.put(attributes.getValue("",ATTR_PRE), attributes.getValue("",ATTR_POST));
			}
			
		});
		
		aliases.setEndElementListener(new EndElementListener() {

			public void end() {
				tmp.setAliases((HashMap<String,String>)aliases_read.clone());
			}
			
		});
		
		buttonset.setStartElementListener(new StartElementListener() {

			public void start(Attributes attributes) {
				button_set_name.setLength(0);
				button_set_name.append(attributes.getValue("",ATTR_SETNAME));
				
				//we have the button set name, construct a new ColorSetSettings for it
				//BigInteger fline = new BigInteger("FFFFFFFF",16);
				//Log.e("PARSER","HOLY CRAP I PARSED " +fline.intValue() + " FROM FFFFFFFF");
				setinfo.setPrimaryColor( (attributes.getValue("",ATTR_PRIMARYCOLOR) == null) ? SlickButtonData.DEFAULT_COLOR : new BigInteger((attributes.getValue("",ATTR_PRIMARYCOLOR)).toUpperCase(),16).intValue());
				setinfo.setSelectedColor( (attributes.getValue("",ATTR_SELECTEDCOLOR) == null) ? SlickButtonData.DEFAULT_SELECTED_COLOR : new BigInteger((attributes.getValue("",ATTR_SELECTEDCOLOR)),16).intValue());
				setinfo.setFlipColor( (attributes.getValue("",ATTR_FLIPCOLOR) == null) ? SlickButtonData.DEFAULT_FLIP_COLOR : new BigInteger(attributes.getValue("",ATTR_FLIPCOLOR),16).intValue());
				setinfo.setLabelColor( (attributes.getValue("",ATTR_LABELCOLOR) == null) ? SlickButtonData.DEFAULT_LABEL_COLOR : new BigInteger(attributes.getValue("",ATTR_LABELCOLOR),16).intValue());
				setinfo.setButtonWidth( (attributes.getValue("",ATTR_BUTTONWIDTH) == null) ? SlickButtonData.DEFAULT_BUTTON_WDITH : new Integer(attributes.getValue("",ATTR_BUTTONWIDTH)));
				setinfo.setButtonHeight( (attributes.getValue("",ATTR_BUTTONHEIGHT) == null) ? SlickButtonData.DEFAULT_BUTTON_HEIGHT : new Integer(attributes.getValue("",ATTR_BUTTONHEIGHT)));
				setinfo.setLabelSize( (attributes.getValue("",ATTR_LABELSIZE)==null) ? SlickButtonData.DEFAULT_LABEL_SIZE : new Integer(attributes.getValue("",ATTR_LABELSIZE)));
				setinfo.setFlipLabelColor( (attributes.getValue("",ATTR_FLIPLABELCOLOR) == null ? SlickButtonData.DEFAULT_FLIPLABEL_COLOR : new BigInteger((attributes.getValue("",ATTR_FLIPLABELCOLOR)),16).intValue()));
				colorsets.put(button_set_name.toString(), setinfo.copy());
			}
			
		});
		
		buttonset.setEndElementListener(new EndElementListener() {

			@SuppressWarnings("unchecked")
			public void end() {
				//add the current set
				buttons.put(button_set_name.toString(), (Vector<SlickButtonData>)current_button_set.clone());
				
				//reset for next found
				button_set_name.setLength(0);
				button_set_name.append("default");
				current_button_set.removeAllElements();
				current_button_set.clear();
				setinfo.toDefautls();
			}
			
		});
		
		button.setStartElementListener(new StartElementListener() {

			public void start(Attributes attributes) {
				SlickButtonData tmp  = new SlickButtonData();
				tmp.setX( (attributes.getValue("",ATTR_XPOS) == null) ? 40 : new Integer(attributes.getValue("",ATTR_XPOS)));
				tmp.setY( (attributes.getValue("",ATTR_YPOS) == null) ? 40 : new Integer(attributes.getValue("",ATTR_YPOS)));
				tmp.setText(attributes.getValue("",ATTR_CMD));
				tmp.setFlipCommand(attributes.getValue("", ATTR_FLIPCMD));
				tmp.setLabel(attributes.getValue("",ATTR_LABEL));
				tmp.MOVE_STATE = new Integer(attributes.getValue("",ATTR_MOVEMETHOD));
				tmp.setTargetSet(attributes.getValue("",ATTR_TARGETSET));
				tmp.setWidth( (attributes.getValue("",ATTR_WIDTH) == null) ? setinfo.getButtonWidth() : new Integer(attributes.getValue("",ATTR_WIDTH)));
				tmp.setHeight( (attributes.getValue("",ATTR_HEIGHT)==null) ? setinfo.getButtonHeight() : new Integer(attributes.getValue("",ATTR_HEIGHT)));
				
				tmp.setPrimaryColor( (attributes.getValue("",ATTR_PRIMARYCOLOR) == null) ? setinfo.getPrimaryColor() : new BigInteger(attributes.getValue("",ATTR_PRIMARYCOLOR),16).intValue());
				tmp.setSelectedColor( (attributes.getValue("",ATTR_SELECTEDCOLOR) == null) ? setinfo.getSelectedColor() : new BigInteger(attributes.getValue("",ATTR_SELECTEDCOLOR),16).intValue());
				tmp.setFlipColor( (attributes.getValue("",ATTR_FLIPCOLOR) == null) ? setinfo.getFlipColor() : new BigInteger(attributes.getValue("",ATTR_FLIPCOLOR),16).intValue());
				tmp.setLabelColor( (attributes.getValue("",ATTR_LABELCOLOR) == null) ? setinfo.getLabelColor() : new BigInteger(attributes.getValue("",ATTR_LABELCOLOR),16).intValue());
				tmp.setLabelSize((attributes.getValue("",ATTR_LABELSIZE) == null) ? setinfo.getLabelSize() : new Integer(attributes.getValue("",ATTR_LABELSIZE)));
				
				tmp.setFlipLabel(attributes.getValue("",ATTR_FLIPLABEL));
				tmp.setFlipLabelColor( (attributes.getValue("",ATTR_FLIPLABELCOLOR) == null) ? setinfo.getFlipLabelColor() : new BigInteger(attributes.getValue("",ATTR_FLIPLABELCOLOR),16).intValue());
				//add the new button to the current list.
				current_button_set.add(tmp);
			}
			
		});
		
		buttonsets.setEndElementListener(new EndElementListener() {

			public void end() {
				tmp.setButtonSets(buttons);
				tmp.setSetSettings(colorsets);
			}
			
		});
		
		selected.setEndTextElementListener(new EndTextElementListener() {

			public void end(String body) {
				tmp.setLastSelected(body);
			}
			
		});
		
		processperiod.setEndTextElementListener(new EndTextElementListener() {

			public void end(String arg0) {
				if(arg0.equals("true")) {
					tmp.setProcessPeriod(true);
				} else {
					tmp.setProcessPeriod(false);
				}
				
			}
			
		});
		
		trigger.setStartElementListener(new StartElementListener() {

			public void start(Attributes attr) {
				//current_trigger = new TriggerData();
				//Log.e("PARSER","PARSING NOTIFICATION ELEMENT");
				current_trigger.setName(attr.getValue("",ATTR_TRIGGERTITLE));
				current_trigger.setPattern(attr.getValue("",ATTR_TRIGGERPATTERN));
				current_trigger.setInterpretAsRegex( attr.getValue("",ATTR_TRIGGERLITERAL).equals("true") ? true : false);
				current_trigger.setFireOnce(attr.getValue("",ATTR_TRIGGERONCE).equals("true") ? true : false);
				current_trigger.setHidden( (attr.getValue("",ATTR_TRIGGERHIDDEN) == null) ? false : (attr.getValue("",ATTR_TRIGGERHIDDEN)).equals("true") ? true : false);
				current_trigger.setResponders(new ArrayList<TriggerResponder>());
			}
			
		});
		
		notificationResponder.setStartElementListener(new StartElementListener() {

			public void start(Attributes attr) {
				//Log.e("PARSER","PARSING NOTIFICATION ELEMENT");
				NotificationResponder responder = new NotificationResponder();
				responder.setMessage(attr.getValue("",ATTR_NOTIFICATIONMESSAGE));
				responder.setTitle(attr.getValue("",ATTR_NOTIFICATIONTITLE));
				String fireType = attr.getValue("",ATTR_FIRETYPE);
				if(fireType == null) fireType = "";
				if(fireType.equals(TriggerResponder.FIRE_WINDOW_OPEN)) {
					responder.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_OPEN);
				} else if (fireType.equals(TriggerResponder.FIRE_WINDOW_CLOSED)) {
					responder.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_CLOSED);
				} else if (fireType.equals(TriggerResponder.FIRE_ALWAYS)) {
					responder.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
				} else if (fireType.equals(TriggerResponder.FIRE_NEVER)) {
					responder.setFireType(FIRE_WHEN.WINDOW_NEVER);
				} else {
					responder.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
				}
				String spawnnew = attr.getValue("",ATTR_NEWNOTIFICATION);
				if(spawnnew == null) spawnnew = "";
				if(spawnnew.equals("true")) {
					responder.setSpawnNewNotification(true);
				} else {
					responder.setSpawnNewNotification(false);
				}
				
				String useongoing = attr.getValue("",ATTR_USEONGOING);
				if(useongoing == null) useongoing = "";
				if(useongoing.equals("true")) {
					responder.setUseOnGoingNotification(true);
				} else {
					responder.setUseOnGoingNotification(false);
				}
				
				String usedefaultlight = attr.getValue("",ATTR_USEDEFAULTLIGHT);
				if(usedefaultlight == null) usedefaultlight = "false";
				if(usedefaultlight.equals("true")) {
					responder.setUseDefaultLight(true);
					responder.setColorToUse( (attr.getValue("",ATTR_LIGHTCOLOR) == null) ? 0xFFFF0000 : new BigInteger(attr.getValue("",ATTR_LIGHTCOLOR),16).intValue());
				} else {
					responder.setUseDefaultLight(false);
				}
				
				String usedefaultvibrate = attr.getValue("",ATTR_USEDEFAULTVIBRATE);
				if(usedefaultvibrate == null) usedefaultvibrate = "false";
				if(usedefaultvibrate.equals("true")) {
					responder.setUseDefaultVibrate(true);
					responder.setVibrateLength( (attr.getValue("",ATTR_VIBRATELENGTH) == null) ? 0 : Integer.parseInt(attr.getValue("",ATTR_VIBRATELENGTH)));
				} else {
					responder.setUseDefaultVibrate(false);
				}
				
				String usedefaultsound = attr.getValue("",ATTR_USEDEFAULTSOUND);
				if(usedefaultsound == null) usedefaultsound = "false";
				if(usedefaultsound.equals("true")) {
					responder.setUseDefaultSound(true);
					responder.setSoundPath(attr.getValue("",ATTR_SOUNDPATH));
				} else {
					responder.setUseDefaultSound(false);
				}
				
				current_trigger.getResponders().add(responder);
			}
			
		});
		
		toastResponder.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				ToastResponder toast = new ToastResponder();
				toast.setDelay( (a.getValue("",ATTR_TOASTDELAY) == null) ? 1500 : Integer.parseInt(a.getValue("",ATTR_TOASTDELAY)));
				toast.setMessage(a.getValue("",ATTR_TOASTMESSAGE));
				
				String fireType = a.getValue("",ATTR_FIRETYPE);
				if(fireType == null) fireType = "";
				if(fireType.equals(TriggerResponder.FIRE_WINDOW_OPEN)) {
					toast.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_OPEN);
				} else if (fireType.equals(TriggerResponder.FIRE_WINDOW_CLOSED)) {
					toast.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_CLOSED);
				} else if (fireType.equals(TriggerResponder.FIRE_ALWAYS)) {
					toast.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
				} else if (fireType.equals(TriggerResponder.FIRE_NEVER)) {
					toast.setFireType(FIRE_WHEN.WINDOW_NEVER);
				} else {
					toast.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
				}
				
				current_trigger.getResponders().add(toast);
			}
			
		});
		
		ackResponder.setStartElementListener(new StartElementListener() {

			public void start(Attributes attributes) {
				
				AckResponder ack = new AckResponder();
				ack.setAckWith(attributes.getValue("",ATTR_ACKWITH));
				
				String fireType = attributes.getValue("",ATTR_FIRETYPE);
				if(fireType == null) fireType = "";
				//Log.e("PARSER","ACK TAG READ, FIRETYPE IS:" + fireType);
				if(fireType.equals(TriggerResponder.FIRE_WINDOW_OPEN)) {
					ack.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_OPEN);
				} else if (fireType.equals(TriggerResponder.FIRE_WINDOW_CLOSED)) {
					ack.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_CLOSED);
				} else if (fireType.equals(TriggerResponder.FIRE_ALWAYS)) {
					ack.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
				} else if (fireType.equals(TriggerResponder.FIRE_NEVER)) {
					ack.setFireType(FIRE_WHEN.WINDOW_NEVER);
				} else {
					ack.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
				}
				
				
				current_trigger.getResponders().add(ack);
			}
			
		});
		
		trigger.setEndElementListener(new EndElementListener() {

			public void end() {
				tmp.getTriggers().put(current_trigger.getPattern(), current_trigger.copy());
			}
			
		});
		
		
		try {
			Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return tmp; 
	}

}
