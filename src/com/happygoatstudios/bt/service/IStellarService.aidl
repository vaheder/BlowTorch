package com.happygoatstudios.bt.service;

import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.service.IStellarServiceCallback;
import com.happygoatstudios.bt.settings.ColorSetSettings;
import com.happygoatstudios.bt.trigger.TriggerData;
import com.happygoatstudios.bt.responder.notification.NotificationResponder;
import com.happygoatstudios.bt.responder.ack.AckResponder;
import com.happygoatstudios.bt.responder.toast.ToastResponder;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.timer.TimerProgress;
import com.happygoatstudios.bt.alias.AliasData;

interface IStellarService {
	void registerCallback(IStellarServiceCallback c);
	void unregisterCallback(IStellarServiceCallback c);
	int getPid();
	void initXfer();
	void endXfer();
	boolean hasBuffer();
	boolean isConnected();
	void sendData(in byte[] seq);
	void saveSettings();
	void setNotificationText(CharSequence seq);
	void setConnectionData(String host,int port,String display);
	void beginCompression();
	void stopCompression();
	void requestBuffer();
	void saveBuffer(inout byte[] buffer);
	void addAlias(in AliasData a);
	List getSystemCommands();
	Map getAliases();
	void setAliases(in Map map);
	void setFontSize(int size);
	//void setFontSize(String size);
	int getFontSize();
	//String getFontSize();
	void setFontSpaceExtra(int size);
	int getFontSpaceExtra();
	void setFontName(String name);
	String getFontName();
	void setFontPath(String path);
	void setMaxLines(int keepcount);
	int getMaxLines();
	void setSemiOption(boolean bools_are_newline);
	void addButton(String targetset, in SlickButtonData new_button);
	void removeButton(String targetset,in SlickButtonData button_to_nuke);
	List<SlickButtonData> getButtonSet(String setname);
	List<String> getButtonSetNames();
	void modifyButton(String targetset,in SlickButtonData orig, in SlickButtonData mod);
	void addNewButtonSet(String name);
	List<String> getButtonSets();
	int deleteButtonSet(String name);
	int clearButtonSet(String name);
	Map getButtonSetListInfo();
	String getLastSelectedSet();
	void LoadSettingsFromPath(String path);
	void ExportSettingsToPath(String path);
	void resetSettings();
	ColorSetSettings getCurrentColorSetDefaults();
	ColorSetSettings getColorSetDefaultsForSet(String the_set);
	void setColorSetDefaultsForSet(String the_set,in ColorSetSettings input);
	void setProcessPeriod(boolean value);
	Map getTriggerData();
	void newTrigger(in TriggerData data);
	void updateTrigger(in TriggerData from,in TriggerData to);
	void deleteTrigger(String which);
	TriggerData getTrigger(String pattern);
	void setUseExtractUI(boolean use);
	boolean getUseExtractUI();
	void setThrottleBackground(boolean use);
	boolean isThrottleBackground();
	boolean isProcessPeriod();
	boolean isSemiNewline();
	void setKeepWifiActive(boolean use);
	boolean isKeepWifiActive();
	void setAttemptSuggestions(boolean use);
	boolean isAttemptSuggestions();
	void setKeepLast(boolean use);
	boolean isKeepLast();
	boolean isBackSpaceBugFix();
	void setBackSpaceBugFix(boolean use);
	boolean isAutoLaunchEditor();
	void setAutoLaunchEditor(boolean use);
	boolean isDisableColor();
	void setDisableColor(boolean use);
	String HapticFeedbackMode();
	void setHapticFeedbackMode(String use);
	String getAvailableSet();
	String getHFOnPress();
	String getHFOnFlip();
	void setHFOnPress(String use);
	void setHFOnFlip(String use);
	void setDisplayDimensions(int rows,int cols);
	void reconnect();
	Map getTimers();
	TimerData getTimer(String ordinal);
	void startTimer(String ordinal);
	void pauseTimer(String ordinal);
	void stopTimer(String ordinal);
	void resetTimer(String ordinal);
	void updateTimer(in TimerData old,in TimerData newtimer);
	void addTimer(in TimerData newtimer);
	void removeTimer(in TimerData deltimer);
	int getNextTimerOrdinal();
	Map getTimerProgressWad();
	String getEncoding();
	void setEncoding(String input);
	String getConnectedTo();
	boolean isKeepScreenOn();
	void setKeepScreenOn(boolean use);
	boolean isLocalEcho();
	void setLocalEcho(boolean use);
	boolean isVibrateOnBell();
	void setVibrateOnBell(boolean use);
	boolean isNotifyOnBell();
	void setNotifyOnBell(boolean use);
	boolean isDisplayOnBell();
	void setDisplayOnBell(boolean use);	
	boolean isFullScreen();
	void setFullScreen(boolean use);	
	boolean isRoundButtons();
	void setRoundButtons(boolean use);
	int getBreakAmount();
	int getOrientation();
	boolean isWordWrap();
	void setBreakAmount(int pIn);
	void setOrientation(int pIn);
	void setWordWrap(boolean pIn);
	boolean isRemoveExtraColor();
	boolean isDebugTelnet();
	void setRemoveExtraColor(boolean pIn);
	void setDebugTelnet(boolean pIn);
	void updateAndRenameSet(String oldSet, String newSet,in ColorSetSettings settings);
}