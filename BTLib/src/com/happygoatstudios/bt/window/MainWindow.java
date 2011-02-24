package com.happygoatstudios.bt.window;

import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
//import android.util.Log;
//import android.util.Log;
//import android.util.Log;
//import android.util.Log;
//import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.alias.AliasDialogDoneListener;
import com.happygoatstudios.bt.alias.AliasEditorDialog;
import com.happygoatstudios.bt.button.ButtonEditorDialog;
import com.happygoatstudios.bt.button.ButtonSetSelectorDialog;
import com.happygoatstudios.bt.button.SlickButton;
import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.launcher.Launcher;
import com.happygoatstudios.bt.launcher.Launcher.LAUNCH_MODE;
import com.happygoatstudios.bt.service.*;
import com.happygoatstudios.bt.settings.ColorSetSettings;
import com.happygoatstudios.bt.settings.HyperSettingsActivity;
import com.happygoatstudios.bt.timer.TimerSelectionDialog;
import com.happygoatstudios.bt.trigger.TriggerSelectionDialog;

public class MainWindow extends Activity implements AliasDialogDoneListener {
	
	public static String TEST_MODE = "blowTorchTestMode";
	public static String NORMAL_MODE = "blowTorchNormalMode";
	
	//public static final String PREFS_NAME = "CONDIALOG_SETTINGS";
	//public String PREFS_NAME;
	protected static final int MESSAGE_HTMLINC = 110;
	protected static final int MESSAGE_RAWINC = 111;
	protected static final int MESSAGE_BUFFINC = 112;
	protected static final int MESSAGE_PROCESS = 102;
	protected static final int MESSAGE_PROCESSED = 104;
	public static final int MESSAGE_SENDDATAOUT = 105;
	protected static final int MESSAGE_RESETINPUTWINDOW = 106;
	protected static final int MESSAGE_PROCESSINPUTWINDOW = 107;
	protected static final int MESSAGE_LOADSETTINGS = 200;
	protected static final int MESSAGE_ADDBUTTON = 201;
	public static final int MESSAGE_MODIFYBUTTON = 202;
	public static final int MESSAGE_NEWBUTTONSET = 205;
	public static final int MESSAGE_CHANGEBUTTONSET = 206;
	public static final int MESSAGE_RELOADBUTTONSET = 208;
	protected static final int MESSAGE_BUTTONREQUESTINGSETCHANGE = 207;
	protected static final int MESSAGE_XMLERROR = 397;
	protected static final int MESSAGE_COLORDEBUG = 675;
	protected static final int MESSAGE_DIRTYEXITNOW = 943;
	protected static final int MESSAGE_DOHAPTICFEEDBACK = 856;
	public static final int MESSAGE_DELETEBUTTONSET = 867;
	public static final int MESSAGE_CLEARBUTTONSET = 868;
	protected static final int MESSAGE_SHOWTOAST = 869;
	protected static final int MESSAGE_SHOWDIALOG = 870;
	public static final int MESSAGE_HFPRESS = 871;
	public static final int MESSAGE_HFFLIP = 872;
	public static final int MESSAGE_LOCKUNDONE = 873;
	public static final int MESSAGE_BUTTONFIT = 874;
	protected static final int MESSAGE_BELLTOAST = 876;
	protected static final int MESSAGE_DOSCREENMODE = 877;
	protected static final int MESSAGE_KEYBOARD = 878;
	protected static final int MESSAGE_DODISCONNECT = 879;
	public static final int MESSAGE_SENDBUTTONDATA = 880;
	private static final int MESSAGE_LINEBREAK = 881;
	private static final int MESSAGE_HIDEKEYBOARD =882;
	//protected static final int MESSAGE_BUTTONRELOAD = 882;
	
	private TextTree tree = new TextTree();

	protected boolean settingsDialogRun = false;
	
	private boolean autoLaunch = true;
	private String overrideHF = "auto";
	private String overrideHFFlip = "auto";
	private String overrideHFPress = "auto";
	
	private boolean isFullScreen = false;
	
	String host;
	int port;
	
	Handler myhandler = null;
	//boolean servicestarted = false;
	
	IStellarService service = null;
	Processor the_processor = null;
	private int statusBarHeight = 1;
	//GestureDetector gestureDetector = null;
	OnTouchListener gestureListener = null;
	ByteView screen2 = null;
	CommandKeeper history = null;
	ImageButton test_button = null;
	ImageButton up_button_c = null;
	ImageButton down_button_c = null;
	ImageButton enter_button_c  = null;
	boolean input_controls_expanded = false;
	boolean isBound = false;
	boolean isKeepLast = false; //for keeping last
	boolean historyWidgetKept = false;
	Boolean settingsLoaded = false; //synchronize or try to mitigate failures of writing button data, or failures to read data
	Boolean serviceConnected = false;
	Boolean isResumed = false;
	
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			service = IStellarService.Stub.asInterface(arg1); //turn the binder into something useful
			
			//register callback
			try {
				service.registerCallback(the_callback);
				
			} catch (RemoteException e) {
				//do nothing here, as there isn't much we can do
			}
			synchronized(serviceConnected) {
				//Log.e("WINDOW","SERVICE CONNECTED, SENDING NOTIFICATION");
				serviceConnected.notify();
				serviceConnected = true;
			}
			finishInitializiation();
			
		}

		public void onServiceDisconnected(ComponentName arg0) {
			try {
				//Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
				if(service != null) service.unregisterCallback(the_callback);
			} catch (RemoteException e) {
				//do nothing here, as there isn't much we can do
			}
			
			service = null;
			
			synchronized(serviceConnected) {
				serviceConnected.notify();
				serviceConnected = false;
			}
		}
		
	};
	
	Pattern newline = Pattern.compile("\n");
	//Pattern newline = Pattern.compile("[\\x0D][\\x0A]");

	public Bundle CountNewLine(String ISOLATINstring,int maxlines) {
		
		Matcher match = newline.matcher(ISOLATINstring);
		
		int prunelocation = 0;
		int numberfound = 0;
		//boolean found = false;
		while(match.find()) {
			numberfound++;	
		}
		
		
		
		if(numberfound > maxlines) {
			int numtoprune = numberfound - maxlines;
			match.reset();
			for(int i = 0;i < numtoprune;i++) {
				if(match.find()) { //shouldalways be true
					prunelocation = match.start();
				}
			}
			//by the time we are here, the prunelocation is known
		}
		//LOG.e("WINDOW","FOUND: " + numberfound + " with prune location at: " + prunelocation);
		
		Bundle dat = new Bundle();
		dat.putInt("TOTAL", numberfound);
		dat.putInt("PRUNELOC", prunelocation);
		
		return dat;
	}
	
	//public boolean finishStart = true;
	

	//private int statusBarHeight = 1;
	
	LAUNCH_MODE mode = LAUNCH_MODE.FREE;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.e("BlowTorch","INTENT: " + this.getIntent().getAction());
		if("com.happygoatstudios.bt.window.MainWindow.NORMAL_MODE".equals(this.getIntent().getAction())) {
			Log.e("BlowTorch","Free/Pro mode launch");
			mode = LAUNCH_MODE.FREE;
		} else if("com.happygoatstudios.bt.window.MainWindow.TEST_MODE".equals(this.getIntent().getAction())) {
			Log.e("BlowTorch","Test mode launch");
			mode = LAUNCH_MODE.TEST;
		}
		//TODO: REMOVE THE CRASH HANDLER BEFORE RELEASES.
		//Thread.setDefaultUncaughtExceptionHandler(new com.happygoatstudios.bt.crashreport.CrashReporter(this.getApplicationContext()));
		
		SharedPreferences sprefs = this.getSharedPreferences("STATUS_BAR_HEIGHT", 0);
		statusBarHeight = sprefs.getInt("STATUS_BAR_HEIGHT", 1);
		
		setContentView(R.layout.window_layout);
		
		//String display_name = this.getIntent().getStringExtra("DISPLAY");
		//Pattern invalidchars = Pattern.compile("\\W"); 
		//Matcher replacebadchars = invalidchars.matcher(display_name);
		//String prefsname = replacebadchars.replaceAll("") + ".PREFS";
		//prefsname = prefsname.replaceAll("/", "");
        //Log.e("WINDOW","LAUNCHING WITH CONFIGURATION FILE NAME:" + tmpstr);
       // PREFS_NAME = prefsname; //kill off all white space in the display name, use it as the preference file
        history = new CommandKeeper(10);
        
        screen2 = (ByteView)findViewById(R.id.slickview);
        RelativeLayout l = (RelativeLayout)findViewById(R.id.slickholder);
        screen2.setParentLayout(l);
        TextView fill2 = (TextView)findViewById(R.id.filler2);
        fill2.setFocusable(false);
        fill2.setClickable(false);
        screen2.setNewTextIndicator(fill2);
        
        Animation alphaout = new AlphaAnimation(1.0f,0.0f);
        alphaout.setDuration(100);
        alphaout.setFillBefore(true);
        alphaout.setFillAfter(true);
        fill2.startAnimation(alphaout);
        
        screen2.setZOrderOnTop(false);
        screen2.setOnTouchListener(gestureListener);
		
        EditText input_box = (EditText)findViewById(R.id.textinput);
        
        input_box.setOnKeyListener(new TextView.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				EditText input_box = (EditText)findViewById(R.id.textinput);
				if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
					
					String cmd = history.getNext();
					try {
						if(service.isKeepLast()) {
							if(historyWidgetKept) {
								String tmp = history.getNext();
								input_box.setText(tmp);
								input_box.setSelection(tmp.length());
								historyWidgetKept=false;
							} else {
								input_box.setText(cmd);
								//input_box.setText(cmd);
								input_box.setSelection(cmd.length());
							}
						} else {
							input_box.setText(cmd);
							input_box.setSelection(cmd.length());
						}
					} catch (RemoteException e) {
						throw new RuntimeException(e);
					}
					return true;
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_UP) {
					String cmd = history.getPrev();
					input_box.setText(cmd);
					input_box.setSelection(cmd.length());
					return true;
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_UP) {
					myhandler.sendEmptyMessage(MainWindow.MESSAGE_PROCESSINPUTWINDOW);
					screen2.jumpToZero();
					return true;
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					return true;
				}
				
				return false;
			}
   
        });
        
        input_box.setDrawingCacheEnabled(true);
        input_box.setVisibility(View.VISIBLE);
        input_box.setEnabled(true);
        TextView filler = (TextView)findViewById(R.id.filler);
        filler.setFocusable(false);
        filler.setClickable(false);
        
        
        input_box.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        

        
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		
				EditText input_box = (EditText)findViewById(R.id.textinput);
				
			
				if(event == null)  {
					
				}
				
				if(actionId == EditorInfo.IME_ACTION_SEND) {
					event = new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER);
				}
				
				if(((event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) && event.getAction() == KeyEvent.ACTION_UP)) {
					myhandler.sendEmptyMessage(MainWindow.MESSAGE_PROCESSINPUTWINDOW);
					screen2.jumpToZero();

					if(actionId == EditorInfo.IME_ACTION_DONE) {
							return true;
					} else { return true; }
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
					String cmd = history.getNext();
					input_box.setText(cmd);
					input_box.setSelection(cmd.length());
					if(actionId == EditorInfo.IME_ACTION_DONE) {

						//	return false;
							return true;
							
					} else { return true; }
				} else {
					return true;
				}
				//return false;
			}
		});
        
        

		
		//assign my handler
		myhandler = new Handler() {
			public void handleMessage(Message msg) {
				EditText input_box = (EditText)findViewById(R.id.textinput);
				switch(msg.what) {
				case MESSAGE_HIDEKEYBOARD:
					HideKeyboard();
					break;
				case MESSAGE_LINEBREAK:
					screen2.setLineBreaks((Integer)msg.obj);
					break;
				case MESSAGE_SENDBUTTONDATA:
					/*ByteBuffer bbuf = null;
					try {
						bbuf = ByteBuffer.allocate(((String)msg.obj).getBytes(service.getEncoding()).length);
					} catch (UnsupportedEncodingException e6) {
						// TODO Auto-generated catch block
						e6.printStackTrace();
					} catch (RemoteException e6) {
						// TODO Auto-generated catch block
						e6.printStackTrace();
					}
					
					try {
						bbuf.put(((String)msg.obj).getBytes(service.getEncoding()));
					} catch (UnsupportedEncodingException e) {
						
						e.printStackTrace();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					bbuf.rewind();
				
					byte[] bbuffbytes = bbuf.array();*/
					try {
						service.sendData(((String)msg.obj).getBytes(service.getEncoding()));
						
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					screen2.jumpToZero();
					break;
				case MESSAGE_DODISCONNECT:
					//Log.e("WINDOW","SHOW MESSAGE");
					DoDisconnectMessage();
					break;
				case MESSAGE_KEYBOARD:
					boolean add = (msg.arg2 > 0) ? true : false;
					boolean popup = (msg.arg1 > 0) ? true : false;
					String text = (String)msg.obj;
					
					if(!add) {
						//reset text
						input_box.setText(text);
						input_box.setSelection(input_box.getText().toString().length());
					} else {
						//append text
						input_box.setText(input_box.getText().toString() + text);
						input_box.setSelection(input_box.getText().toString().length());
					}
					
					if(popup) {
						InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						mgr.showSoftInput(input_box, InputMethodManager.SHOW_FORCED);
					}
				
					break;
				case MESSAGE_DOSCREENMODE:
					boolean fullscreen = false;
					if(msg.arg1 == 1) {
						fullscreen = true;
					}
					boolean needschange = false;
					if(fullscreen && !isFullScreen) {
						//switch to fullscreen.
						try {
							service.setFullScreen(true);
							isFullScreen = true;
						    MainWindow.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
						    MainWindow.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
						    needschange = true;
						} catch (RemoteException e) {
							throw new RuntimeException(e);
						}
					}
					
					if(!fullscreen && isFullScreen) {
						//switch to non full screen.
						try {
							service.setFullScreen(false);
							isFullScreen = false;
							MainWindow.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
							MainWindow.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
						
							//MainWindow.this.findViewById(R.id.window_container).requestLayout();
							needschange = true;
						} catch (RemoteException e) {
							throw new RuntimeException(e);
						}
					}
					
					if(needschange) {
						RelativeLayout modb = (RelativeLayout)MainWindow.this.findViewById(R.id.slickholder);
						//modb is the slickview/button container.
						for(int i=0;i<modb.getChildCount();i++) {
							View tmp = modb.getChildAt(i);
							if(tmp instanceof SlickButton) {
								if(isFullScreen) {
									((SlickButton)tmp).setFullScreenShift(statusBarHeight); 
								} else {
									((SlickButton)tmp).setFullScreenShift(0); 
								}	
							}
							
						}
						MainWindow.this.findViewById(R.id.window_container).requestLayout();
						screen2.doDelayedDraw(100);
					}
					
					//try {
					//	this.sendMessage(this.obtainMessage(MESSAGE_CHANGEBUTTONSET,service.getLastSelectedSet()));
					//} catch (RemoteException e5) {
					//	throw new RuntimeException(e5);
					//}
					
					
					break;
				case MESSAGE_BELLTOAST:
					Toast belltoast = Toast.makeText(MainWindow.this, "No actual message.", Toast.LENGTH_LONG);
					//t.setView(view);
					
					
					LayoutInflater li = (LayoutInflater) MainWindow.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View v = li.inflate(R.layout.bell_toast, null);
					//TextView tv = (TextView) v.findViewById(R.id.message);
					//tv.setText(message);
					
					belltoast.setView(v);
					float density = MainWindow.this.getResources().getDisplayMetrics().density;
					belltoast.setGravity(Gravity.TOP|Gravity.RIGHT, (int)(40*density), (int)(30*density));
					belltoast.setDuration(Toast.LENGTH_SHORT);
					belltoast.show();
					break;
				case MESSAGE_LOCKUNDONE:
					//MainWindow.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					//screen2.forceDraw();
					//screen2.invalidate();
					//Log.e("WINDOW","ATTEMPTING TO FORCE REDRAW THE SCREEN");
					break;
				case MESSAGE_HFPRESS:
					DoHapticFeedbackPress();
					break;
				case MESSAGE_HFFLIP:
					DoHapticFeedbackFlip();
					break;
				case MESSAGE_SHOWDIALOG:
					AlertDialog.Builder dbuilder = new AlertDialog.Builder(MainWindow.this);
					dbuilder.setTitle("ERROR");
					dbuilder.setMessage((String)msg.obj);
					dbuilder.setCancelable(true);
					//dbuilder.set
					dbuilder.setPositiveButton("Close Window", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							cleanExit();
							MainWindow.this.finish();
							
						}
					});
					
					AlertDialog dlg = dbuilder.create();
					dlg.show();
					
					break;
				case MESSAGE_SHOWTOAST:
					Toast t = null;
					if(msg.arg1 == 1) {
						t = Toast.makeText(MainWindow.this, (String)msg.obj, Toast.LENGTH_LONG);
					} else {
						t = Toast.makeText(MainWindow.this, (String)msg.obj, Toast.LENGTH_SHORT);		
					}
					t.show();
					break;
				case MESSAGE_DELETEBUTTONSET:
					try {
						int count = service.deleteButtonSet((String)msg.obj);
						String message = "Deleted " + (String)msg.obj + " button set ";
						if(count > 0) {
							message +=  "with " + count + " buttons.";
						} else {
							message += ".";
						}
						
						Message reloadset = this.obtainMessage(MESSAGE_CHANGEBUTTONSET);
						reloadset.obj = service.getAvailableSet();
						reloadset.arg1 = 10;
						
						if(service.getLastSelectedSet().equals((String)msg.obj)) {
							message += "\nLoaded default button set.";
						}
						Toast cleared = Toast.makeText(MainWindow.this,message, Toast.LENGTH_SHORT);
						cleared.show();
						
						this.sendMessage(reloadset);
						

						
					} catch (RemoteException e4) {
						throw new RuntimeException(e4);
					}
					break;
				case MESSAGE_CLEARBUTTONSET:
					try {
						int count = service.clearButtonSet((String)msg.obj);
						Toast cleared = Toast.makeText(MainWindow.this,"Cleared " + count+" buttons from " + (String)msg.obj + " button set.", Toast.LENGTH_SHORT);
						cleared.show();
						if(service.getLastSelectedSet().equals((String)msg.obj)) {
							Message reloadset = this.obtainMessage(MESSAGE_CHANGEBUTTONSET);
							reloadset.obj = msg.obj;
							reloadset.arg1 = 10;
							this.sendMessage(reloadset);
						}
						
					} catch (RemoteException e4) {
						throw new RuntimeException(e4);
					}
					break;
				case MESSAGE_DOHAPTICFEEDBACK:
					DoHapticFeedback();
					break;
				case MESSAGE_DIRTYEXITNOW:
					//the service via an entered command ".closewindow" or something, to bypass the window asking if you want to close
					dirtyExit();
					MainWindow.this.finish();
					break;
				case MESSAGE_COLORDEBUG:
					//execute color debug.
					screen2.setColorDebugMode(msg.arg1);
					break;
				case MESSAGE_XMLERROR:
					//got an xml error, need to display it.
					String xmlerror = (String)msg.obj;
					AlertDialog.Builder builder = new AlertDialog.Builder(MainWindow.this);
					builder.setPositiveButton("Acknowledge.", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					});
					
					builder.setMessage("XML Error: " + xmlerror + "\nSettings have not been loaded.");
					builder.setTitle("Problem with XML File.");
					
					
					//tvtmp.setText("TESTING");
					//builder.setView(tvtmp);
					
					
					AlertDialog error = builder.create();
					error.show();
					TextView tvtmp = (TextView)error.findViewById(android.R.id.message);
					tvtmp.setTypeface(Typeface.MONOSPACE);
					
					break;
				case MESSAGE_RELOADBUTTONSET:
					
					break;
				case MESSAGE_NEWBUTTONSET:
					try {
						service.addNewButtonSet((String)msg.obj);
					} catch (RemoteException e3) {
						throw new RuntimeException(e3);
					}
					RelativeLayout clearb = (RelativeLayout)MainWindow.this.findViewById(R.id.slickholder);
					int pos = clearb.indexOfChild(screen2);
					int count = clearb.getChildCount();
					if(pos == 0) {
						clearb.removeViews(1, count-1);
					} else {
						clearb.removeViews(0,pos);
						clearb.removeViews(pos+1,count - pos);
					}
					makeFakeButton();
					showNoButtonMessage(true);
					
					break;
				case MESSAGE_CHANGEBUTTONSET:
					RelativeLayout modb = (RelativeLayout)MainWindow.this.findViewById(R.id.slickholder);
					//get the new list
					try {
						
						List<SlickButtonData> newset = service.getButtonSet((String)msg.obj);
						
						if(newset != null) {
							
							int posm = modb.indexOfChild(screen2);
							int countm = modb.getChildCount();
							if(posm == 0) {
								modb.removeViews(1, countm-1);
							} else {
								modb.removeViews(0,posm);
								modb.removeViews(posm+1,countm - posm);
							}
							
							if(newset.size() > 0) {
								for(SlickButtonData tmp : newset) {
									SlickButton new_button = new SlickButton(modb.getContext(),0,0);
									new_button.setData(tmp);
									new_button.setDispatcher(this);
									new_button.setDeleter(this);
									
									if(isFullScreen) {
										new_button.setFullScreenShift(statusBarHeight);
									} else {
										new_button.setFullScreenShift(0);
									}
									
									if(!service.isRoundButtons()) {
										new_button.setDrawRound(false);
									}
									modb.addView(new_button);
								}
							} else {
								makeFakeButton();
								if(msg.arg1 != 10) showNoButtonMessage(false);
							}
						}
					} catch (RemoteException e3) {
						throw new RuntimeException(e3);
					}
					break;
				case MESSAGE_MODIFYBUTTON:
					SlickButtonData orig = msg.getData().getParcelable("ORIG_DATA");
					SlickButtonData mod = msg.getData().getParcelable("MOD_DATA");
					
					try {
						if(orig != null && mod != null) {
							//Log.e("WINDOW","MODIFY BUTTON " +orig.toString() + " TO " + mod.toString() + " attempting service call now");
							service.modifyButton(service.getLastSelectedSet(),orig,mod);
						} else {
							//Log.e("WINDOW","ATTEMPTED TO MODIFY BUTTON, BUT GOT NULL DATA");
						}
					} catch (RemoteException e2) {
						throw new RuntimeException(e2);
					}
					
					//we modified the button, now load the set again to make the changes appear on the screen.
					Message reloadset = myhandler.obtainMessage(MESSAGE_CHANGEBUTTONSET);
					try {
						reloadset.obj = service.getLastSelectedSet();
					} catch (RemoteException e3) {
						throw new RuntimeException(e3);
					}
					myhandler.sendMessage(reloadset);
					break;
				case MESSAGE_LOADSETTINGS:
					//the service is connected at this point, so the service is alive and settings are loaded
					//TODO: SETTINGS LOAD PLACE
					//attemppt to load button sets.
					@SuppressWarnings("unused")
					boolean fontSizeChanged = false;
					//boolean fullscreen_now = false;
					if(settingsDialogRun) {
						//so, if we a are here, then the dialog screen has been run.
						//we need to read in the values and supply them to the service
						settingsDialogRun = false;
						//Log.e("WINDOW","SETTINGS DIALOG HAS BEEN RUN! LOAD CHANGES!");
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainWindow.this);
						//Integer font_size = new Integer(prefs.getString("FONT_SIZE", "18"));
						Integer font_size = Integer.parseInt(prefs.getString("FONT_SIZE", "18"));
						Integer line_space = new Integer(prefs.getString("FONT_SIZE_EXTRA", "2"));
						Integer max_lines = new Integer(prefs.getString("MAX_LINES", "300"));
						String font_name = prefs.getString("FONT_NAME", "monospace");
						boolean process_periods = prefs.getBoolean("PROCESS_PERIOD", true);
						boolean use_semi = prefs.getBoolean("PROCESS_SEMI", true);
						boolean use_extractui = prefs.getBoolean("USE_EXTRACTUI", false);
						boolean throttle_background = prefs.getBoolean("THROTTLE_BACKGROUND", false);
						boolean wifi_keepalive = prefs.getBoolean("WIFI_KEEPALIVE", true);
						boolean use_suggestions = prefs.getBoolean("USE_SUGGESTIONS", true);
						boolean keeplast = prefs.getBoolean("KEEPLAST", false);
						boolean bsbugfix = prefs.getBoolean("BACKSPACE_BUGFIX", false);
						boolean autolaunch = prefs.getBoolean("AUTOLAUNCH_EDITOR",true);
						boolean disablecolor = prefs.getBoolean("DISABLE_COLOR", false);
						String overrideHF = prefs.getString("OVERRIDE_HAPTICFEEDBACK","auto");
						String overrideHFPress = prefs.getString("HAPTIC_PRESS", "auto");
						String overrideHFFlip = prefs.getString("HAPTIC_FLIP", "none");
						String sel_encoding = prefs.getString("ENCODING", "ISO-8859-1");
						
						boolean keepscreen = prefs.getBoolean("KEEP_SCREEN_ON",true);
						boolean localecho = prefs.getBoolean("LOCAL_ECHO",true);
						boolean bellvibrate = prefs.getBoolean("BELL_VIBRATE",true);
						boolean bellnotify = prefs.getBoolean("BELL_NOTIFY",false);
						boolean belldisplay = prefs.getBoolean("BELL_DISPLAY",false);
						boolean fullscreen_now = prefs.getBoolean("WINDOW_FULLSCREEN", false);
						boolean roundbutt = prefs.getBoolean("ROUND_BUTTONS",true);
						
						int breakvalue = prefs.getInt("BREAK_AMOUNT", 0);
						int orientationvalue = prefs.getInt("ORIENTATION", 0);
						boolean wordwrapvalue = prefs.getBoolean("WORD_WRAP", true);
						
						boolean removeextracolor = prefs.getBoolean("REMOVE_EXTRA_COLOR",true);
						boolean debugtelnet = prefs.getBoolean("DEBUG_TELNET", false);
						
						//boolean fitmessage = prefs.getBoolean("FIT_MESSAGE", true);
						
						//Log.e("WINDOW","LOADED KEEPLAST AS " + keeplast);
						
						try {
							if(font_size != service.getFontSize()) {
								fontSizeChanged = true;
							}
							
							service.setFontSize(font_size);
							service.setFontSpaceExtra(line_space);
							service.setMaxLines(max_lines);
							service.setFontName(font_name);
							service.setProcessPeriod(process_periods);
							service.setUseExtractUI(use_extractui);
							service.setThrottleBackground(throttle_background);
							service.setSemiOption(use_semi);
							service.setKeepWifiActive(wifi_keepalive);
							service.setAttemptSuggestions(use_suggestions);
							service.setKeepLast(keeplast);
							service.setBackSpaceBugFix(bsbugfix);
							service.setAutoLaunchEditor(autolaunch);
							service.setDisableColor(disablecolor);
							service.setHapticFeedbackMode(overrideHF);
							service.setHFOnPress(overrideHFPress);
							service.setHFOnFlip(overrideHFFlip);
							service.setEncoding(sel_encoding);
							service.setKeepScreenOn(keepscreen);
							service.setLocalEcho(localecho);
							service.setVibrateOnBell(bellvibrate);
							service.setNotifyOnBell(bellnotify);
							service.setDisplayOnBell(belldisplay);
							service.setFullScreen(fullscreen_now);
							service.setRoundButtons(roundbutt);
							service.setOrientation(orientationvalue);
							service.setWordWrap(wordwrapvalue);
							service.setBreakAmount(breakvalue);
							service.setRemoveExtraColor(removeextracolor);
							service.setDebugTelnet(debugtelnet);
							//service.setShowFitMessage(fitmessage);
							service.saveSettings();
						} catch (RemoteException e) {
							throw new RuntimeException(e);
						}
						
						String importPath = prefs.getString("IMPORT_PATH","");
						String exportPath = prefs.getString("EXPORT_PATH", "");
						String defaultSettings = prefs.getString("SETTINGS_TO_DEFAULT", "");
						

						
						SharedPreferences.Editor editor = prefs.edit();
						
						if(defaultSettings.equals("doit")) {
							editor.putString("SETTINGS_TO_DEFAULT", "");
							try {
								service.resetSettings();
							} catch (RemoteException e) {
								throw new RuntimeException(e);
							}
						}
						
						if(!importPath.equals("")) {
							try {
								service.LoadSettingsFromPath(importPath);
							} catch (RemoteException e) {
								throw new RuntimeException(e);
							}
							editor.putString("IMPORT_PATH", "");
						}
						
						if(!exportPath.equals("")) {
							//export needed
							String fullPath = "/BlowTorch/" + exportPath;
							try {
								service.ExportSettingsToPath(fullPath);
							} catch (RemoteException e) {
								throw new RuntimeException(e);
							}
							editor.putString("EXPORT_PATH","");
						}
						
						editor.commit();
						
					}
					
					
					try {
						if(service.isFullScreen()) {
						    MainWindow.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
						    MainWindow.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
						} else {
							MainWindow.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
							MainWindow.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
						}

						MainWindow.this.findViewById(R.id.window_container).requestLayout();
						isFullScreen = service.isFullScreen();
						
						input_box.setKeepScreenOn(service.isKeepScreenOn());
					
						
						screen2.setEncoding(service.getEncoding());
						
						screen2.setCullExtraneous(service.isRemoveExtraColor());
						
						//int or = MainWindow.this.getRequestedOrientation();
						switch(service.getOrientation()) {
						case 0:
							MainWindow.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
							break;
						case 1:
							MainWindow.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
							break;
						case 2:
							MainWindow.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
							break;
						default:
							break;
						}
						screen2.setWordWrap(service.isWordWrap());
						screen2.setLineBreaks(service.getBreakAmount());
						
						//current_button_views.clear();
						List<SlickButtonData> buttons =  service.getButtonSet(service.getLastSelectedSet());
						
						RelativeLayout button_layout = (RelativeLayout)MainWindow.this.findViewById(R.id.slickholder);
						
						int posl = button_layout.indexOfChild(screen2);
						int countl = button_layout.getChildCount();
						if(posl == 0) {
							button_layout.removeViews(1, countl-1);
						} else {
							button_layout.removeViews(0,posl);
							button_layout.removeViews(posl+1,countl - posl);
						}
						
						if(buttons != null) {
							if(buttons.size() > 0) {
								for(SlickButtonData button : buttons) {
									SlickButton tmp = new SlickButton(MainWindow.this,0,0);
									tmp.setData(button);
									tmp.setDispatcher(this);
									tmp.setDeleter(this);
									//adjust for full screen.
									if(isFullScreen) {
										tmp.setFullScreenShift(statusBarHeight);
									} else {
										tmp.setFullScreenShift(0);
									}
									
									if(!service.isRoundButtons()) {
										tmp.setDrawRound(false);
									}
									button_layout.addView(tmp);
									//current_button_views.add(tmp);
								}
							} else {
								makeFakeButton();
								//show that the loaded set has no buttons.
								showNoButtonMessage(false);
							}
						}
						
						//screen2.setFontSize(service.getFontSize());
						//screen2.setLineSpace(service.getFontSpaceExtra());
						screen2.setCharacterSizes(service.getFontSize(), service.getFontSpaceExtra());
						screen2.setMaxLines(service.getMaxLines());
						
						//get the font name 
						String tmpname = service.getFontName();
						Typeface font = Typeface.MONOSPACE;
						//Log.e("WINDOW","FONT SELECTION IS:" + tmpname);
						if(tmpname.contains("/")) {
							//string is a path
							if(tmpname.contains(Environment.getExternalStorageDirectory().getPath())) {
								
								String sdstate = Environment.getExternalStorageState();
								if(Environment.MEDIA_MOUNTED.equals(sdstate) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdstate)) {
									font = Typeface.createFromFile(tmpname);
								} else {
									font = Typeface.MONOSPACE;
								}
								
							} else {
								//path is a system path
								font = Typeface.createFromFile(tmpname);
							}
							
						} else {
							if(tmpname.equals("monospace")) {
								font = Typeface.MONOSPACE;
							} else if(tmpname.equals("sans serif")) {
								font = Typeface.SANS_SERIF;
							} else if (tmpname.equals("default")) {
								font = Typeface.DEFAULT;
							}
						}
						
						screen2.setFont(font);
						
						service.setDisplayDimensions(screen2.CALCULATED_LINESINWINDOW, screen2.CALCULATED_ROWSINWINDOW);
						
						//if(fontSizeChanged) {
						//	screen2.reBreakBuffer();
						//}
						
						
						if(service.getUseExtractUI()) {
							
							
							int current = input_box.getImeOptions();
							int wanted = current & (0xFFFFFFFF^EditorInfo.IME_FLAG_NO_EXTRACT_UI);
							
							//Log.e("WINDOW","ATTEMPTING TO SET FULL SCREEN IME| WAS: "+ Integer.toHexString(current) +" WANT: " + Integer.toHexString(wanted));
							input_box.setImeOptions(wanted);
							input_box.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
							BetterEditText better = (BetterEditText)input_box;
							better.setUseFullScreen(true);
						} else {
							int current = input_box.getImeOptions();
							int wanted = current | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
							//Log.e("WINDOW","ATTEMPTING TO SET NO EXTRACT IME| WAS: "+ Integer.toHexString(current) +" WANT: " + Integer.toHexString(wanted));
							input_box.setImeOptions(wanted);
							if(service.isAttemptSuggestions()) {
								input_box.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
							} else {
								input_box.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS|InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
									
							}
							BetterEditText better = (BetterEditText)input_box;
							better.setUseFullScreen(false);
							//Log.e("WINDOW","SETTINGS NOW "+Integer.toHexString(input_box.getImeOptions()));
						}
						
						
						if(service.isKeepLast()) {
							isKeepLast = true;
						} else {
							isKeepLast = false;
						}
						
						//handle auto launch
						autoLaunch = service.isAutoLaunchEditor();
						//handle disable color
						if(service.isDisableColor()) {
							//set the slick view debug mode to 3.
							screen2.setColorDebugMode(3);
						} else {
							screen2.setColorDebugMode(0);
						}
						//handle overridehf.
						overrideHF = service.HapticFeedbackMode();
						
						overrideHFPress = service.getHFOnPress();
						overrideHFFlip = service.getHFOnFlip();
						
						if(service.isBackSpaceBugFix()) {
							//Log.e("WINDOW","APPLYING BACK SPACE BUG FIX");
							BetterEditText tmp_bar = (BetterEditText)input_box;
							tmp_bar.setBackSpaceBugFix(true);
						} else {
							BetterEditText tmp_bar = (BetterEditText)input_box;
							tmp_bar.setBackSpaceBugFix(false);
							//Log.e("WINDOW","NOT APPLYING BACK SPACE BUG FIX");
						}
						
						InputMethodManager imm = (InputMethodManager) input_box.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.restartInput(input_box);
						//imm.
						//im
						//get the rest of the window options that are necessary to function
						
					} catch (RemoteException e1) {
						throw new RuntimeException(e1);
					}
					break;
				case ByteView.MSG_DELETEBUTTON:
					ButtonEditorDialog d = new ButtonEditorDialog(MainWindow.this,R.style.SuperSweetDialog,(SlickButton)msg.obj,this);
					d.show();
					break;
				case ByteView.MSG_REALLYDELETEBUTTON:
					try {
						service.removeButton(service.getLastSelectedSet(), ((SlickButton)msg.obj).orig_data);
					} catch (RemoteException e1) {
						throw new RuntimeException(e1);
					}
					RelativeLayout layout = (RelativeLayout) MainWindow.this.findViewById(R.id.slickholder);
					layout.removeView((SlickButton)msg.obj);
					
					if(layout.getChildCount() == 1) {
						//if the last button was removed manually, we need to make the fake button to properly draw the screen.
						makeFakeButton();
					}
					break;
				case MESSAGE_ADDBUTTON:
					SlickButtonData tmp = new SlickButtonData();
					tmp.setX(msg.arg1);
					tmp.setY(msg.arg2);
					
					//if(OREINTATION == Configuration.ORIENTATION_PORTRAIT) {
					//	tmp.setX(msg.arg2);
					//	tmp.setY(msg.arg1);
					//}
					
					
					tmp.setText(input_box.getText().toString());
					tmp.setLabel("LABEL");
					
					ColorSetSettings colorset = null;
					try {
						colorset = service.getCurrentColorSetDefaults();
					} catch (RemoteException e2) {
						throw new RuntimeException(e2);
					}
					
					tmp.setLabelColor(colorset.getLabelColor());
					tmp.setPrimaryColor(colorset.getPrimaryColor());
					tmp.setFlipColor(colorset.getFlipColor());
					tmp.setSelectedColor(colorset.getSelectedColor());
					tmp.setLabelSize(colorset.getLabelSize());
					
					tmp.setWidth(colorset.getButtonWidth());
					tmp.setHeight(colorset.getButtonHeight());
					
					SlickButton new_button = new SlickButton(MainWindow.this,0,0);
					if(OREINTATION == Configuration.ORIENTATION_PORTRAIT) {
						new_button.setPortraiteMode(true);
					}
					if(isFullScreen) {
						tmp.setY(msg.arg2 - statusBarHeight);
						new_button.setFullScreenShift(statusBarHeight);
					} 
					
					try {
						if(!service.isRoundButtons()) {
							new_button.setDrawRound(false);
						}
					} catch (RemoteException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
					new_button.setData(tmp);
					new_button.setDeleter(this);
					new_button.setDispatcher(this);
					
					try {
						service.addButton(service.getLastSelectedSet(), tmp);
					} catch (RemoteException e1) {
						throw new RuntimeException(e1);
					}
					
					RelativeLayout hold = (RelativeLayout)MainWindow.this.findViewById(R.id.slickholder);
					hold.addView(new_button);
					
					/*int aflags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
					if(overrideHF) {
						aflags |= HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
					}
					new_button.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, aflags);
					*/
					DoHapticFeedback();
					
					//this may look real funky, but MSG_DELETEBUTTON really means to launch the editor.
					if(autoLaunch) {
						new_button.prepareToLaunchEditor();
						
						Message launcheditor = this.obtainMessage(ByteView.MSG_DELETEBUTTON);
						launcheditor.obj = new_button;
						this.sendMessage(launcheditor);
					}
					//current_button_views.add(new_button);
					
					break;
				case MESSAGE_PROCESSINPUTWINDOW:
					
					//input_box.debug(5);
					
					String pdata = input_box.getText().toString();
					history.addCommand(pdata);
					Character cr = new Character((char)13);
					Character lf = new Character((char)10);
					String crlf = cr.toString() + lf.toString();
					pdata = pdata.concat(crlf);
					//ByteBuffer buf = ByteBuffer.allocate(pdata.length());
					ByteBuffer buf = null;
					try {
						buf = ByteBuffer.allocate(pdata.getBytes(service.getEncoding()).length);
					} catch (UnsupportedEncodingException e2) {
						throw new RuntimeException(e2);
					} catch (RemoteException e2) {
						throw new RuntimeException(e2);
					}
					
					
					try {
						buf.put(pdata.getBytes(service.getEncoding()));
					} catch (UnsupportedEncodingException e) {
						
						e.printStackTrace();
					} catch (RemoteException e) {
						
						e.printStackTrace();
					}
				
					buf.rewind();
				
					byte[] buffbytes = buf.array();

					try {
						service.sendData(buffbytes);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					myhandler.sendEmptyMessage(MainWindow.MESSAGE_RESETINPUTWINDOW);
					break;
				case MESSAGE_RESETINPUTWINDOW:
					//Log.e("WINDOW","Attempting to reset input bar.");
					
					try {
						if(service.isKeepLast()) {
							input_box.setSelection(0, input_box.getText().length());
							historyWidgetKept = true;
						} else {
							input_box.clearComposingText();
							input_box.setText("");
						}
					} catch (RemoteException e1) {
						throw new RuntimeException(e1);
					}
					break;
				case MESSAGE_RAWINC:
					//screen2.addText((String)msg.obj,false);
					screen2.addBytes((byte[])msg.obj, false);
					//try {
					//	tree.addBytes(((String)msg.obj).getBytes("ISO-8859-1"));
					//} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
					///	e1.printStackTrace();
					//}
					break;
				case MESSAGE_BUFFINC:
					//String message = "\n" + Colorizer.colorCyanBright + "Buffer received: " +  ((String)msg.obj).getBytes().length + Colorizer.colorWhite + "\n";
					//screen2.addText((String)msg.obj,true);
					screen2.addBytes((byte[])msg.obj,true);
					break;
				case MESSAGE_SENDDATAOUT:
					try {
						service.sendData((byte[])msg.obj);
						
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					screen2.jumpToZero();
					
					
					break;
				default:
					break;
				}
			}

			
		};
		
		test_button = (ImageButton)findViewById(R.id.test_btn);
				
		test_button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				//change my layout parameters and add a new button.
				RelativeLayout rl = (RelativeLayout)findViewById(R.id.input_bar);
				LinearLayout l = (LinearLayout)findViewById(R.id.ctrl_target);
				if(l == null) {
					return;
				}

				
				LayoutInflater lf = LayoutInflater.from(l.getContext());
				RelativeLayout target = (RelativeLayout)lf.inflate(R.layout.input_controls, null);
				
				ImageButton dc = (ImageButton)target.findViewById(R.id.down_btn_c);
				ImageButton uc = (ImageButton)target.findViewById(R.id.up_btn_c);
				ImageButton ec = (ImageButton)target.findViewById(R.id.enter_btn_c);
				
				uc.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View arg0) {
						EditText input_box = (EditText)findViewById(R.id.textinput);
						String cmd = history.getNext();
						try {
							if(service.isKeepLast()) {
								if(historyWidgetKept) {
									String tmp = history.getNext();
									input_box.setText(tmp);
									input_box.setSelection(tmp.length());
									historyWidgetKept = false;
								} else {
									input_box.setText(cmd);
									input_box.setSelection(cmd.length());
								}
							} else {
								input_box.setText(cmd);
								input_box.setSelection(cmd.length());
							}
						} catch (RemoteException e) {
							throw new RuntimeException(e);
						}
					}
				});
				
				
				dc.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View arg0) {
						EditText input_box = (EditText)findViewById(R.id.textinput);
						String cmd = history.getPrev();
						input_box.setText(cmd);
					}
				});
				
				ec.setOnClickListener(new View.OnClickListener() {

					public void onClick(View arg0) {
						myhandler.sendEmptyMessage(MainWindow.MESSAGE_PROCESSINPUTWINDOW);
						screen2.jumpToZero();
					}
					
				});
				
				//pre multi screen support animation value 178px
				float amount = 180*MainWindow.this.getResources().getDisplayMetrics().density;
				Animation a = new TranslateAnimation(-1*amount,0,0,0);
				a.setDuration(120);
				AnimationSet set = new AnimationSet(true);
				set.addAnimation(a);
				
				LayoutAnimationController lac = new LayoutAnimationController(set,0.01f);
				
				rl.setLayoutAnimation(lac);
				
				if(input_controls_expanded) {
					//switch the image resource
					Animation outanim = new TranslateAnimation(amount,0,0,0);
					outanim.setDuration(120);
					LayoutAnimationController lac2 = new LayoutAnimationController(outanim,0.0f);
					rl.setLayoutAnimation(lac2);
					l.removeAllViews();
					input_controls_expanded = false;
					test_button.setImageResource(R.drawable.sliderwidgetout);
				} else {
					l.addView(target);
					input_controls_expanded = true;
					test_button.setImageResource(R.drawable.sliderwidgetin);
				}

			}
		});
		
		screen2.setDispatcher(myhandler);
		screen2.setButtonHandler(myhandler);
		screen2.setInputType(input_box);
		//input_box.bringToFront();
		//icicile is out, prefs are in
		
		synchronized(settingsLoaded) {
		//Log.e("WINDOW","CHECKING SETTINGS FROM: " + PREFS_NAME);
		//SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME,0);
		
		//servicestarted = prefs.getBoolean("CONNECTED",false);
		//finishStart = prefs.getBoolean("FINISHSTART", true);
		
		
		
		//int count = prefs.getInt("BUTTONCOUNT", 0);
		//for(int i = 0;i<count;i++) {
		//	//get button string
		//	String data = prefs.getString("BUTTON"+i, "");
//
		//	Message msg = screen2.buttonaddhandler.obtainMessage(103, data);
		//	screen2.buttonaddhandler.sendMessage(msg);
			
			
		//}
		
		//settingsLoaded.notify();
		//settingsLoaded = true;
		} 
		//if(icicle != null) {
		//	CharSequence seq = icicle.getCharSequence("BUFFER");
		//	if(seq != null) {
		//		screen2.setBuffer((new StringBuffer(seq).toString()));
		//	} else {
		//	}
		//} else {
		//}
		
		if(!isServiceRunning()) {
			//start the service
			//if("com.happygoatstudios.bt.MainWindow.NORMAL_MODE".equals(this.getIntent().getAction())) {
			//	mode = LAUNCH_MODE.FREE;
			//} else if("com.happygoatstudios.bt.MainWindow.NORMAL_MODE".equals(this.getIntent().getAction())) {
			//	mode = LAUNCH_MODE.TEST;
			//}
			if(mode == LAUNCH_MODE.FREE) {
				this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_NORMAL"));
			} else if(mode == LAUNCH_MODE.TEST) {
				this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_TEST"));
			}
			//this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName()));
			//servicestarted = true;
		}
		
		//register screenlock thingie.
		//IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		//filter.addAction(Intent.ACTION_SCREEN_OFF);
		///filter.addAction(Intent.ACTION_USER_PRESENT);
		//BroadcastReceiver mReceiver = new ScreenState(myhandler);
		//registerReceiver(mReceiver, filter);
		
		
		//give it some time to launch
		synchronized(this) {
			try {
				this.wait(5);
			} catch (InterruptedException e) {
			}
		}
	}
	
	/*boolean showsettingsoptions = false;
	boolean settingsmenuclosed  = true;
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		menu.clear();
		if(!showsettingsoptions) {
			menu.add(0,99,0,"Aliases");
			menu.add(0,100,0,"Triggers");
			menu.add(0,101,0,"Options");
			menu.add(0,102,0,"Button Sets");
		} else {
			menu.add(0,103,0,"Edit Settings");
			menu.add(0,104,0,"Import Settings");
			menu.add(0,105,0,"Export Settings");
		}
		
		return true;
	}*/
	
	private void DoDisconnectMessage() {
		AlertDialog.Builder err = new AlertDialog.Builder(this);
		err.setTitle("Disconnected");
		err.setMessage("The connection has closed. Reconnect?");
		err.setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				try {
					service.reconnect();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		err.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				cleanExit();
				dialog.dismiss();
				MainWindow.this.finish();
			}
		});
		
		AlertDialog d = err.create();
		d.show();
	}
	
	private void makeFakeButton() {
		
		//the uglyest of ugly hacks.
		RelativeLayout modb = (RelativeLayout)findViewById(R.id.slickholder);
		//Log.e("WINDOW","ADDING FAKE BUTTON!");
		SlickButton fakey = new SlickButton(modb.getContext(),0,0);
		SlickButtonData fakedatay = new SlickButtonData();
		fakedatay.setPrimaryColor(0x00FFFFFF);
		fakedatay.setLabelColor(0x00FFFFFF);
		fakedatay.setFlipColor(0x00FFFFFF);
		fakedatay.setFlipLabelColor(0x00FFFFFF);
		fakedatay.setSelectedColor(0x00FFFFFF);
		fakedatay.setText("");
		fakedatay.setFlipCommand("");
		fakedatay.setLabel("");
		fakedatay.setFlipLabel("");
		fakedatay.setWidth(2);
		fakedatay.setHeight(2);
		fakedatay.setX(-10);
		fakedatay.setY(-10);
		
		fakey.setData(fakedatay);
		
		modb.addView(fakey);
		

		
	}
	
	private void showNoButtonMessage(boolean newset) {
		
		//if we are here then we loaded a blank button set, show the button set info message
		try {
			String message = "";
			if(newset) {
				message = "Button set " + service.getLastSelectedSet() + " created!";
			} else {
				message = "No buttons loaded for " + service.getLastSelectedSet() + " set.";
			}
			message += "\nNew buttons can be made by long pressing the window.";
			message += "\nButtons may be moved after press + hold.";
			message += "\nButtons may be edited after press + hold + hold.";
			Toast t = Toast.makeText(MainWindow.this, message, Toast.LENGTH_LONG);
			t.show();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem tmp = null;
		tmp = menu.add(0,99,0,"Aliases").setIcon(R.drawable.ic_menu_alias);
		menu.add(0,100,0,"Triggers").setIcon(R.drawable.ic_menu_triggers);
		menu.add(0,105,0,"Timers").setIcon(R.drawable.ic_menu_timers);
		menu.add(0,103,0,"Options").setIcon(R.drawable.ic_menu_options);
		menu.add(0,102,0,"Button Sets").setIcon(R.drawable.ic_menu_button_sets);
		//SubMenu sm = menu.addSubMenu(0, 900, 0, "More");
		menu.add(0, 901, 0, "Reconnect");
		menu.add(0, 902, 0, "Disconnect");
		menu.add(0, 903, 0, "Quit");
		
		
		return true;
		
	}
	
	RotatableDialog d = null;
	
	@SuppressWarnings("unchecked")
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case 903:
			this.cleanExit();
			this.finish();
			break;
		case 902:
			myhandler.sendEmptyMessage(MESSAGE_DODISCONNECT);
			break;
		case 901:
			try {
				service.reconnect();
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case 105:
			TimerSelectionDialog tsel = null;
			tsel = new TimerSelectionDialog(MainWindow.this,service);
			//tsel.
			tsel.show();
			
			break;
		case 99:
			AliasEditorDialog d = null;
			try {
				d = new AliasEditorDialog(this,(HashMap<String,AliasData>)service.getAliases(),this,service);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			d.setTitle("Edit Aliases:");
			d.show();
			break;
		case 102:
			//show the button set selector dialog
			ButtonSetSelectorDialog buttoneditor = null;
			try{
				buttoneditor = new ButtonSetSelectorDialog(this,myhandler,(HashMap<String,Integer>)service.getButtonSetListInfo(),service.getLastSelectedSet(),service);
				buttoneditor.setTitle("Select Button Set");
				buttoneditor.show();
			} catch(RemoteException e) {
				e.printStackTrace();
			}
			break;
		case 101:
			//SettingEditorDialog sedit = new SettingEditorDialog(this,service);
			//sedit.setTitle("Modify Settings...");
			//sedit.show();
			//showsettingsoptions = true;
			//settingsmenuclosed = false;
			MainWindow.this.myhandler.postDelayed(new Runnable() { public void run() { openOptionsMenu();}}, 1);
			//Intent settingintent = new Intent(this,HyperSettingsActivity.class);
			//this.startActivityForResult(settingintent, 0);
			break;
		case 103:
			//fix up the shared preferences so that it will actually work.
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainWindow.this);
			
			SharedPreferences.Editor edit = prefs.edit();
			
			try {
				//Log.e("WINDOW","FUCKING FUCK FUCK SERVICE IS MODIFYING THE FUCKING FILE");
				edit.putBoolean("THROTTLE_BACKGROUND",service.isThrottleBackground());
				edit.putBoolean("USE_EXTRACTUI", service.getUseExtractUI());
				edit.putBoolean("PROCESS_PERIOD", service.isProcessPeriod());
				edit.putBoolean("PROCESS_SEMI", service.isSemiNewline());
				edit.putBoolean("WIFI_KEEPALIVE", service.isKeepWifiActive());
				edit.putBoolean("USE_SUGGESTIONS", service.isAttemptSuggestions());
				edit.putBoolean("BACKSPACE_BUGFIX", service.isBackSpaceBugFix());
				edit.putBoolean("AUTOLAUNCH_EDITOR", service.isAutoLaunchEditor());
				edit.putBoolean("DISABLE_COLOR",service.isDisableColor());
				edit.putString("OVERRIDE_HAPTICFEEDBACK", service.HapticFeedbackMode());
				edit.putString("HAPTIC_PRESS", service.getHFOnPress());
				edit.putString("HAPTIC_FLIP", service.getHFOnFlip());
				edit.putString("ENCODING", service.getEncoding());
				
				edit.putInt("BREAK_AMOUNT", service.getBreakAmount());
				edit.putInt("ORIENTATION", service.getOrientation());
				edit.putBoolean("WORD_WRAP",service.isWordWrap());
				edit.putInt("CALCULATED_WIDTH", screen2.CALCULATED_ROWSINWINDOW);
				
				edit.putBoolean("REMOVE_EXTRA_COLOR", service.isRemoveExtraColor());
				edit.putBoolean("DEBUG_TELNET", service.isDebugTelnet());
				
				edit.putBoolean("KEEPLAST", service.isKeepLast());
				edit.putString("FONT_SIZE", Integer.toString((service.getFontSize())));
				edit.putString("FONT_SIZE_EXTRA", Integer.toString(service.getFontSpaceExtra()));
				edit.putString("MAX_LINES", Integer.toString(service.getMaxLines()));
				edit.putString("FONT_NAME", service.getFontName());
				
				edit.putBoolean("KEEP_SCREEN_ON",service.isKeepScreenOn());
				edit.putBoolean("LOCAL_ECHO", service.isLocalEcho());
				edit.putBoolean("BELL_VIBRATE", service.isVibrateOnBell());
				edit.putBoolean("BELL_NOTIFY", service.isNotifyOnBell());
				edit.putBoolean("BELL_DISPLAY", service.isDisplayOnBell());
				edit.putBoolean("WINDOW_FULLSCREEN",service.isFullScreen());
				edit.putBoolean("ROUND_BUTTONS",service.isRoundButtons());
				//edit.putBoolean("FIT_MESSAGE", service.isShowFitMessage());
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			
			edit.commit();
			
			Intent settingintent = new Intent(this,HyperSettingsActivity.class);
			this.startActivityForResult(settingintent, 0);
			//settingsmenuclosed = true;
			//showsettingsoptions = false;
			break;
		case 100:
			//launch the sweet trigger dialog.
			TriggerSelectionDialog trigger_selector = new TriggerSelectionDialog(this,service);
			trigger_selector.show();
			break;
		default:
			break;
		}
		return true;
	}
	
	
	Handler extporthandler = new Handler() {
		public void handleMessage(Message msg) {
			//so we are kludging out the new button set dialog to just be a "string enterer" dialog.
			//should be a full path /sdcard/something.xml
			String filename = (String)msg.obj;
			try {
				//Log.e("WINDOW","TRYING TO GET SERVICE TO WRITE A FILE FOR ME!");
				service.ExportSettingsToPath(filename);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		}
	};
	
	public void onBackPressed() {
		//Log.e("WINDOW","BACK PRESSED TRAPPED");
		
		//show dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(MainWindow.this);
		builder.setMessage("Keep service running in background?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                MainWindow.this.dirtyExit();
		                MainWindow.this.finish();
		           }
		       });
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //dialog.cancel();
		        	   MainWindow.this.cleanExit();
		        	   MainWindow.this.finish();
		           }
		       });
		//AlertDialog alert = builder.create();
		builder.create();
		builder.show();
		//alert.show();
		
		//super.onBackPressed();
	}
	
	int OREINTATION = Configuration.ORIENTATION_LANDSCAPE;
	
	public void onConfigurationChanged(Configuration newconfig) {
		//Log.e("WINDOW","CONFIGURATION CHANGING");
		if(service == null) {
			super.onConfigurationChanged(newconfig);
			return;
		}
		//Log.e("WINDOW","CONFIGURATION CHANGED");
		//RelativeLayout container = (RelativeLayout)this.findViewById(R.id.window_container);
		//RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)container.getLayoutParams();
		switch(newconfig.orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			
		//	container.requestLayout();
			//DoButtonPortraitMode(true);
			//OREINTATION = Configuration.ORIENTATION_PORTRAIT;
			myhandler.sendEmptyMessageDelayed(MESSAGE_HIDEKEYBOARD, 10);
			try {
				if(service.getOrientation() == 1) { //if we are selected as landscape
					newconfig.orientation = Configuration.ORIENTATION_LANDSCAPE;
					//HideKeyboard();
					//myhandler.sendEmptyMessageDelayed(MESSAGE_HIDEKEYBOARD, 1000);
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
		//	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		//	container.requestLayout();
			//DoButtonPortraitMode(false);
			//OREINTATION = Configuration.ORIENTATION_LANDSCAPE;
			myhandler.sendEmptyMessageDelayed(MESSAGE_HIDEKEYBOARD, 10);
			try {
				if(service.getOrientation() == 2) { //if we are selected as landscape
					newconfig.orientation = Configuration.ORIENTATION_PORTRAIT;
					//HideKeyboard();
					//myhandler.sendEmptyMessageDelayed(MESSAGE_HIDEKEYBOARD, 1000);
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		
		super.onConfigurationChanged(newconfig);
		
	}
	
	private void HideKeyboard() {
		InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
		EditText input_box = (EditText)findViewById(R.id.textinput);
		imm.hideSoftInputFromWindow(input_box.getWindowToken(), 0);
		//Log.e("WINDOW","ATTEMPTING TO HIDE THE KEYBOARD");
	}
	
	private void DoButtonPortraitMode(boolean use) {
		RelativeLayout l = (RelativeLayout)findViewById(R.id.slickholder);
		for(int i=0;i<l.getChildCount();i++) {
			if(l.getChildAt(i) instanceof SlickButton) {
				((SlickButton)l.getChildAt(i)).setPortraiteMode(use);
			}
		}
	}
	
	private void DoHapticFeedback() {
		if(overrideHF.equals("none")) {
			return;
		}
		
		int aflags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
		if(overrideHF.equals("always")) {
			aflags |= HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
		}
		
		BetterEditText input_box = (BetterEditText) this.findViewById(R.id.textinput);
		input_box.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, aflags);
	}
	
	private void DoHapticFeedbackPress() {
		if(overrideHFPress.equals("none")) {
			return;
		}
		
		//Log.e("WINDOW","D")
		int aflags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
		if(overrideHFPress.equals("always")) {
			aflags |= HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
		}
		//Log.e("WINDOW","DISPATCHING HAPTIC FEEDBACK FOR PRESS!");
		BetterEditText input_box = (BetterEditText) this.findViewById(R.id.textinput);
		input_box.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, aflags);
	}
	
	private void DoHapticFeedbackFlip() {
		if(overrideHFFlip.equals("none")) {
			return;
		}
		
		int aflags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
		if(overrideHFFlip.equals("always")) {
			aflags |= HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
		}
		
		BetterEditText input_box = (BetterEditText) this.findViewById(R.id.textinput);
		input_box.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, aflags);
	}
	
	private boolean isServiceRunning() {
	/*	ActivityManager activityManager = (ActivityManager)MainWindow.this.getSystemService(Context.ACTIVITY_SERVICE);
    	List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
    	boolean found = false;
    	for(RunningServiceInfo service : services) {
    		//Log.e("LAUNCHER","FOUND:" + service.service.getClassName());
    		//service.service.
    		if(com.happygoatstudios.bt.service.StellarService.class.getName().equals(service.service.getClassName())) {
    			//service is running, don't do anything.
    			found = true;
    		} else {

    			
    		}
    	}
		return found;*/
	ActivityManager activityManager = (ActivityManager)MainWindow.this.getSystemService(Context.ACTIVITY_SERVICE);
	List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
	boolean found = false;
	for(RunningServiceInfo service : services) {
		Log.e("LAUNCHER","FOUND:" + service.service.getClassName());
		//service.service.
		if(com.happygoatstudios.bt.service.StellarService.class.getName().equals(service.service.getClassName())) {
			//service is running, don't do anything.
			Log.e(":Launcher","Service lives in: " + service.process);
			if(mode == LAUNCH_MODE.FREE) {
				
				if(service.process.equals("com.happygoatstudios.btfree:stellar_free")) found = true;
			} else if(mode == LAUNCH_MODE.TEST) {
				if(service.process.equals("com.happygoatstudios.bttest:stellar_test")) found = true;
			}
			
		} else {

			
		}
	}
	return found;
	}
	
	private boolean isServiceConnected() {
		try {
			if(service.isConnected()) {
				return true;
			} else {
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void cleanExit() {
		//we want to kill the service when we go.
		
		//shut down the service
		if(isBound) {
			try {
				if(service != null) {
					service.unregisterCallback(the_callback);
				}
			} catch (RemoteException e) {
				//e.printStackTrace();
			}
			
			unbindService(mConnection);
			
			
			
			isBound = false;
			//Log.e("WINDOW","Unbound connection at cleanExit");
		}
		
		
		//finishStart = true;
		//servicestarted = false;
		
		//saveSettings();
		//
		//if("com.happygoatstudios.bt.MainWindow.NORMAL_MODE".equals(this.getIntent().getAction())) {
		//	mode = LAUNCH_MODE.FREE;
		//} else if("com.happygoatstudios.bt.MainWindow.NORMAL_MODE".equals(this.getIntent().getAction())) {
		//	mode = LAUNCH_MODE.TEST;
		//}
		if(mode == LAUNCH_MODE.FREE) {
			this.stopService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_NORMAL"));
		} else if(mode == LAUNCH_MODE.TEST) {
			this.stopService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_TEST"));
		}
		//stopService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName()));
		
	}
	
	public void dirtyExit() {
		//we dont want to kill the service
		if(isBound) {
			
			try {
				//Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
				saveBuffer();
			} catch (RemoteException e) {
				//e.printStackTrace();
			}
			
			unbindService(mConnection);
			//Log.e("WINDOW","Unbound connection at cleanExit");
			isBound = false;
		}
		
		//save settings
		//finishStart = false;
		//servicestarted = true;
		//this.onPause();
		saveSettings();
	}

	private void saveBuffer() throws RemoteException {
		//ByteView sv = (ByteView)findViewById(R.id.slickview);
		//Log.e("WINDOW","SAVING BUFFER:" + sv.getBuffer().length());
		//String message = "\n" + Colorizer.colorYeollowBright + "Saving buffer: " + sv.getBuffer().getBytes().length + Colorizer.colorWhite + "\n";
		//service.saveBuffer(sv.getBuffer());
		service.unregisterCallback(the_callback);
		//sv.clearBuffer();
	}
	
	public void onSaveInstanceState(Bundle data) {
		//SlickView sv = (SlickView)findViewById(R.id.slickview);
		//data.putCharSequence("BUFFER", sv.getBuffer());
	}
	
	public void onRestoreInstanceState(Bundle data) {

		//SlickView sv = (SlickView)findViewById(R.id.slickview);
		//sv.setBuffer((new StringBuffer(data.getCharSequence("BUFFER")).toString()));


	}
	
	public void saveSettings() {
		//shared preferences
		/*synchronized(settingsLoaded) {
			while(settingsLoaded == false) {
				try {
					settingsLoaded.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		
		//SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME,0);
		
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putBoolean("CONNECTED", servicestarted);
		editor.putBoolean("FINISHSTART", finishStart);
		
		//build the slickviews string arrays for buttons.
		SlickView sv = (SlickView)findViewById(R.id.slickview);
		Vector<SlickButton> btns = sv.buttons;
		Iterator<SlickButton> itrator = btns.listIterator();
		
		Vector<String> bstrings = new Vector<String>();
		StringBuffer serialbuttons = new StringBuffer();
		int buttoncount = 0;
		while(itrator.hasNext()) {
			SlickButton b = itrator.next();
			Parcel p = Parcel.obtain();
			Bundle tmp = new Bundle();
			tmp.putParcelable("DATA", b.getData());
			p.writeBundle(tmp);
			//p.unmarshall(arg0, arg1, arg2)

			editor.putString("BUTTON" + buttoncount,b.getData().toString());
			//LOG.e("WINDOW","SERIALIZING: " + b.toString());
			buttoncount++;
			String str = new String(b.getData().getX() +"||" +b.getData().getY() + "||"+b.getData().getText()+"||"+b.getData().getLabel());
			serialbuttons.append(str + "&&");
			
			
			bstrings.add(str);
		}
		
		editor.putInt("BUTTONCOUNT", buttoncount);
		if(buttoncount == 0) {
			//Log.e("WINDOW","SAVED BUTTONCOUNT OF ZERO!!!");
		}
		
		editor.commit();
		
		String str = "Save settings: ";
		if(servicestarted) {
			str = str + " servicestatred=true";
		} else {
			str = str + " servicestatred=false";
		}
		
		if(finishStart) {
			str = str + " finishStart=true";
		} else {
			str = str + " finishStart=false";
		}
		}*/
		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode == RESULT_OK) {
			//Log.e("WINDOW","onActivityResult()");
			settingsDialogRun = true;
		}
	}
	
	
	//public void onWindowFocusChanged() {
		//Log.e("WINDOW","onStart()");
	//	super.onStart();
	//	windowHeight = this.getWindowManager().getDefaultDisplay().getHeight();
	//	int full_height = findViewById(R.id.window_container).getHeight();
	//	Log.e("WINDOW","STATUS BAR HEIGHT IS: " + (windowHeight - full_height) + " = WH:" + windowHeight + " - FH:" + full_height );
		
	//}
	

	public void onStart() {
		super.onStart();
		if("com.happygoatstudios.bt.window.MainWindow.NORMAL_MODE".equals(this.getIntent().getAction())) {
			mode = LAUNCH_MODE.FREE;
		} else if("com.happygoatstudios.bt.window.MainWindow.TEST_MODE".equals(this.getIntent().getAction())) {
			mode = LAUNCH_MODE.TEST;
		}
		
		if(!isServiceRunning()) {
			//start the service
			if(mode == LAUNCH_MODE.FREE) {
				this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_NORMAL"));
			} else if(mode == LAUNCH_MODE.TEST) {
				this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_TEST"));
			}
			//servicestarted = true;
		}
	}
	public void onPause() {
		//Log.e("WINDOW","onPause()");
		
		if(isBound) {
			
			try {
				//Log.e("WINDOW","SAVING BUFFER IN SERVICE");
				
				if(service != null) {
					saveBuffer();
					
					service.unregisterCallback(the_callback);
					service.unregisterCallback(the_callback);
					
					unbindService(mConnection);
					
					//saveSettings();
				} else {
					//uh oh, pausing with a null service, this should not happen
					
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				
			}
			isBound = false;
			
		} else {
			//calling pause without being bound, should not happen
			
		}

		isResumed = false;
		super.onPause();
	
	}
	
	public void onStop() {
		//Log.e("WINDOW","onStop()");
		super.onStop();
	}
	
	public void onDestroy() {
		//Log.e("WINDOW","onDestroy()");
		super.onDestroy();
	}
	
	public void onResume() {
		
		

		//Log.e("WINDOW","onResume()");
		
		//SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME,0);
		//servicestarted = prefs.getBoolean("CONNECTED", false);
		//finishStart = prefs.getBoolean("FINISHSTART", true);
		
		
		//String str = "Load settings (onResume): ";
		//if(servicestarted) {
		//	str = str + " servicestatred=true";
		//} else {
		//	str = str + " servicestatred=false";
		//}
		
		//if(finishStart) {
		//	str = str + " finishStart=true";
		//} else {
		//	str = str + " finishStart=false";
		//}
		
		//Log.e("WINDOW",str);
		
		
		if(!isBound) {
			if("com.happygoatstudios.bt.window.MainWindow.NORMAL_MODE".equals(this.getIntent().getAction())) {
				mode = LAUNCH_MODE.FREE;
			} else if("com.happygoatstudios.bt.window.MainWindow.TEST_MODE".equals(this.getIntent().getAction())) {
				mode = LAUNCH_MODE.TEST;
			}
			if(mode == LAUNCH_MODE.FREE) {
				//this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_NORMAL"));
				bindService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName()+".MODE_NORMAL"), mConnection, 0);
			} else if(mode == LAUNCH_MODE.TEST) {
				//this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_TEST"));
				this.bindService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName()+".MODE_TEST"), mConnection, 0);
			}
			//bindService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName()), mConnection, 0); //do not auto create
			//Log.e("WINDOW","Bound connection at onResume");
			isBound = true;
			
			
			isResumed = true;

		}
	
		super.onResume();
	}
	
	public void onDestroy(Bundle saveInstance) {
		//Log.e("WINDOW","onDestroy()");
		super.onDestroy();
	}
	
	private void finishInitializiation() {
		Intent myintent = this.getIntent();
		
		
		//TODO: check if there is a buffer to fetch, and if so, fetch it.
		/*if(!finishStart) {
			
			try {
				service.requestBuffer();
			} catch (RemoteException e) {
				
			}
			return;
		} else {
			
		}
		
		finishStart = false;*/
		try {
			if(service.hasBuffer()) {
				//Log.e("WINDOW","REQUESTING BUFFER");
				service.requestBuffer();
			} else {
				//Log.e("WINDOW","SERVICE RESPONDED THAT IT HAS NO BUFFER");
			}
		} catch (RemoteException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		if(isServiceConnected()) {
			return;
		}
		
		String host = myintent.getStringExtra("HOST");
		String port = myintent.getStringExtra("PORT");
		
		//for now we are going to override:
		//host = "aardmud.org";
		//port = "4010";
		
		
		while(service == null) {
			host = "aardmud.org"; //waste time till onServiceConnected is called.
		}
		
		
		try {
			service.setConnectionData(host, new Integer(port).intValue(),myintent.getStringExtra("DISPLAY"));
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		} catch (RemoteException e1) {
			
		}
		
		
		//connect up the service and start pumping data
		try {
			service.initXfer();
		} catch (RemoteException e) {
			//e.printStackTrace();
		}
	}
	
	private IStellarServiceCallback.Stub the_callback = new IStellarServiceCallback.Stub() {

		public void dataIncoming(byte[] seq) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_PROCESS);
			Bundle b = new Bundle();
			b.putByteArray("SEQ", seq);
			msg.setData(b);
			myhandler.sendMessage(msg);
		}

		public void processedDataIncoming(CharSequence seq) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_PROCESSED); 
			Bundle b = new Bundle();
			b.putCharSequence("SEQ", seq);
			msg.setData(b);
			myhandler.sendMessage(msg);
		}

		public void htmlDataIncoming(String html) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_HTMLINC);
			Bundle b = new Bundle();
			b.putString("HTML", html);
			msg.setData(b);
			myhandler.sendMessage(msg);
			
		}

		public void rawDataIncoming(byte[] raw) throws RemoteException {
			
			Message msg = myhandler.obtainMessage(MESSAGE_RAWINC,raw);
			//Log.e("WINDOW","RECIEVING RAW");
			myhandler.sendMessage(msg);
			
		}
		
		public void rawBufferIncoming(byte[] rawbuf) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_BUFFINC,rawbuf);
			myhandler.sendMessage(msg);
			//Log.e("WINDOW","RECEIVING BUFFER: " + rawbuf.length());
		}

		public void loadSettings() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_LOADSETTINGS);
		}

		public void displayXMLError(String error) throws RemoteException {
			Message xmlerror = myhandler.obtainMessage(MESSAGE_XMLERROR);
			xmlerror.obj = error;
			myhandler.sendMessage(xmlerror);
			
		}

		public void executeColorDebug(int arg) throws RemoteException {
			Message colordebug = myhandler.obtainMessage(MESSAGE_COLORDEBUG);
			colordebug.arg1 = arg;
			myhandler.sendMessage(colordebug);
		}

		public void invokeDirtyExit() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_DIRTYEXITNOW);
			
		}

		public void showMessage(String message,boolean longtime) throws RemoteException {
			Message showmessage = myhandler.obtainMessage(MESSAGE_SHOWTOAST);
			showmessage.obj = message;
			if(longtime) {
				showmessage.arg1 = 1;
			} else {
				showmessage.arg1 = 0;
			}
			myhandler.sendMessage(showmessage);
			
		}

		public void showDialog(String message) throws RemoteException {
			Message showdlg = myhandler.obtainMessage(MESSAGE_SHOWDIALOG);
			showdlg.obj = message;
			myhandler.sendMessage(showdlg);
		}

		public void doVisualBell() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_BELLTOAST);
		}

		public void setScreenMode(boolean fullscreen) throws RemoteException {
			Message doScreenMode = myhandler.obtainMessage(MESSAGE_DOSCREENMODE);
			if(fullscreen) {
				doScreenMode.arg1 = 1;
			} else {
				doScreenMode.arg1 = 0;
			}
			
			myhandler.sendMessage(doScreenMode);
		}

		public void showKeyBoard(String txt,boolean popup,boolean add,boolean flush) throws RemoteException {
			if(flush) {
				myhandler.sendEmptyMessage(MESSAGE_PROCESSINPUTWINDOW);
				return;
			}
			int p = (popup) ? 1 : 0;
			int a = (add) ? 1 : 0;
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_KEYBOARD,p,a,txt));
		}

		public void doDisconnectNotice() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_DODISCONNECT);
			
		}

		public void doLineBreak(int i) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_LINEBREAK,new Integer(i)));
		}

		public void reloadButtons(String setName) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_CHANGEBUTTONSET,setName));
		}
	};
	
	public void aliasDialogDone(ArrayList<AliasData> items) {
		HashMap<String,AliasData> map = new HashMap<String,AliasData>();
		
		if(items.size() > 0) {
			for(AliasData d : items) {
				map.put(d.getPre(), d);
			}
			try {
				service.setAliases(map);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			try {
				service.setAliases(map);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		}
	}	

		
	

}