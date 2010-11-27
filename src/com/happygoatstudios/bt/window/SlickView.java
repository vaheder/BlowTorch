package com.happygoatstudios.bt.window;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.happygoatstudios.bt.R;
//import com.happygoatstudios.bt.launcher.SlickButton;
import com.happygoatstudios.bt.button.SlickButton;
import com.happygoatstudios.bt.legacy.SlickButtonUtilities;
import com.happygoatstudios.bt.service.Colorizer;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
//import android.util.Log;

import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

public class SlickView extends SurfaceView implements SurfaceHolder.Callback {

	Pattern colordata = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?(([0-9]{1,2});)?([0-9]{1,2})m");
	Pattern lastcolordatainline = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)*?([0-9]{1,2})m.*$");
	
	private DrawRunner _runner;
	
	private int WINDOW_WIDTH = 0;
	private int WINDOW_HEIGHT = 0;
	
	private int colorDebugMode = 0;
	
	
	RelativeLayout parent_layout = null; //for adding buttons.
	EditText input = null; //for supporting buttons.
	Handler dataDispatch = null;
	
	SlickButtonUtilities sbu = new SlickButtonUtilities();
	
	TextView new_text_in_buffer_indicator = null;
	
	private Pattern newline = Pattern.compile("\n");
	private Pattern carriage = Pattern.compile("\\x0D");
	
	private Float scrollback = new Float(0);
	
	private Boolean touchLock = new Boolean(false);
	
	private Boolean drawn = false;
	private Boolean isdrawn = true;
	
	private Typeface PREF_FONT = Typeface.MONOSPACE;
	private int PREF_MAX_LINES = 300;
	public float PREF_FONTSIZE = 18;
	public float PREF_LINESIZE = 20;
	public String PREF_TYPEFACE = "monospace";
	public int CALCULATED_LINESINWINDOW = 20;
	public int CALCULATED_ROWSINWINDOW = 77;
	
	boolean buttondropstarted = false;
	
	public Vector<SlickButton> buttons = new Vector<SlickButton>();
	
	int bx = 0;
	int by = 0;
	
	private String encoding = "UTF-8";
	
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	final static public int MSG_BUTTONDROPSTART = 100;
	final static public int MSG_DELETEBUTTON = 666;
	final static public int MSG_CREATEBUTTON = 102;
	
	public static final int MSG_REALLYDELETEBUTTON = 667;

	protected static final int MSG_CREATEBUTTONWITHDATA = 103;
	final static public int MSG_CLEAR_NEW_TEXT_INDICATOR = 105;
	protected static final int MSG_UPPRIORITY = 200;
	protected static final int MSG_NORMALPRIORITY = 201;
	public Handler buttonaddhandler = null;
	public Handler realbuttonhandler = null;
	
	public SlickView(Context context) {
		super(context);
		getHolder().addCallback(this);
		_runner = new DrawRunner(getHolder(),this,touchLock);
		createhandler();
		//this.setZOrderOnTop(true);
	}
	
	public SlickView(Context context,AttributeSet attrib) {
		super(context,attrib);
		getHolder().addCallback(this);
		//_runner = new DrawRunner(getHolder(),this,touchLock);
		//Log.e("VIEW","VIEW STARTING UP!");
		createhandler();
		//this.setZOrderOnTop(true);
	} 
	
	//protected void onSizeChanged(int w, int h, int oldw, int oldh) { 
	//	statusBarHeight = h - windowHeight; 
	//	Log.e("WINDOW","STATUS BAR HEIGHT NOW:" + statusBarHeight);
	//} 
	
	private void createhandler() {
		buttonaddhandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MSG_UPPRIORITY:
					Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
					break;
				case MSG_NORMALPRIORITY:
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
					break;
				/*case MSG_CREATEBUTTON:
					int ix = msg.getData().getInt("X");
					int iy = msg.getData().getInt("Y");
					String text = msg.getData().getString("THETEXT");
					String label = msg.getData().getString("THELABEL");
					
					//Log.e("SLICK","ATTEMPTING TO ADD BUTTON AT XY:" + ix + "|" + iy + "|" + text);
					
					SlickButton bi = new SlickButton(parent_layout.getContext(),ix,iy);
					bi.setText(text);
					bi.setLabel(label);
					
					RelativeLayout.LayoutParams lpi = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
					
					//SlickButton b = new SlickButton(parent_layout.getContext(),bx,by);

					bi.setClickable(true);
					bi.setFocusable(true);
					
					//bi.setText(input.getText().toString());
					bi.setDispatcher(dataDispatch);
					bi.setDeleter(this);
					
					buttons.add(bi);
					int posi = buttons.lastIndexOf(bi);
					
					sbu.addButtonToSelectedSet(bi);
					
					parent_layout.addView(buttons.get(posi),lpi);
					
					break;*/
				case MSG_BUTTONDROPSTART:
					Message addbtn = realbuttonhandler.obtainMessage(MainWindow.MESSAGE_ADDBUTTON, bx, by);
					realbuttonhandler.sendMessage(addbtn);
					//wait for touchlock
					/*synchronized(touchLock) {
						while(touchLock.booleanValue()) {
							try {
								touchLock.wait();
							} catch (InterruptedException e) {
								
								e.printStackTrace();
							}
						}
						touchLock = true;
						Log.e("SLICK","TOUCHLOCK TRUE FOR NEW BUTTON CREATION")
					}*/
					/*if(parent_layout == null) {
						return;
					}

					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
				
					SlickButton b = new SlickButton(parent_layout.getContext(),bx,by);

					b.setClickable(true);
					b.setFocusable(true);
					
					b.setText(input.getText().toString());
					b.setDispatcher(dataDispatch);
					b.setDeleter(this);
					
					buttons.add(b);
					int pos = buttons.lastIndexOf(b);
					
					parent_layout.addView(buttons.get(pos),lp);
					parent_layout.invalidate();
					bx = 0;
					by = 0;*/
					buttondropstarted = false;
					

					break;
				/*case MSG_DELETEBUTTON:

					SlickButton the_b = (SlickButton)msg.obj;
					
					ButtonEditorDialog d = new ButtonEditorDialog(parent_layout.getContext(),the_b,this);
					
					d.show();
					
					break;
				case MSG_REALLYDELETEBUTTON:
					RelativeLayout lb = parent_layout;
					SlickButton del_b = (SlickButton)msg.obj;
					lb.removeView(del_b);
					buttons.remove(del_b);
					break;*/
				/*case MSG_CREATEBUTTONWITHDATA:

					
					SlickButton bip = new SlickButton(parent_layout.getContext(),0,0);
					SlickButtonData the_data = new SlickButtonData();
					the_data.setDataFromString((String)msg.obj);
					bip.setData(the_data);
					
					RelativeLayout.LayoutParams lpip = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
					
					bip.setClickable(true);
					bip.setFocusable(true);
					
					//bi.setText(input.getText().toString());
					bip.setDispatcher(dataDispatch);
					bip.setDeleter(this);
					
					buttons.add(bip);
					int posip = buttons.lastIndexOf(bip);
					
					parent_layout.addView(buttons.get(posip),lpip);
					break;*/
				case MSG_CLEAR_NEW_TEXT_INDICATOR:
					//Animation a = new AlphaAnimation(0.0f,0.0f);
					//a.setDuration(0);
					//a.setFillAfter(true);
					//a.setFillBefore(true);
					new_text_in_buffer_indicator.startAnimation(indicator_off);
					
					break;
				}
			}
		};
		
		Interpolator i = new CycleInterpolator(1);
		indicator_on.setDuration(1000);
		indicator_on.setInterpolator(i);
		indicator_on.setDuration(1000);
		indicator_on.setFillAfter(true);
		indicator_on.setFillBefore(true);
		
		indicator_off.setDuration(1000);
		indicator_off.setFillAfter(true);
		indicator_off.setFillBefore(true);
	}
	
	public void setButtonHandler(Handler useme) {
		realbuttonhandler = useme;
	}
	
	public void setNewTextIndicator(TextView e) {
		new_text_in_buffer_indicator = e;
	}
	
	public void setParentLayout(RelativeLayout l) {
		parent_layout = l;
	}
	
	public void setInputType(EditText t) {
		input = t;
	}
	
	public void setDispatcher(Handler h) {
		dataDispatch = h;
	}

	int prev_height = -1;
	int statusBarHeight = 1;
	
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int width, int height) {
		
		WINDOW_HEIGHT = height;
		WINDOW_WIDTH = width;
		
		calculateCharacterFeatures(WINDOW_WIDTH,WINDOW_HEIGHT);
		
		//if(prev_height < 0) {
		//	Log.e("SURFACE","SURFACE HEIGHT NOT SET");
			
		//} 
		//Log.e("SURFACE","SURFACE CHANGED: H="+WINDOW_HEIGHT);
		
		//calculate the number of rows and columns in this window.
		/*CALCULATED_LINESINWINDOW = (height / PREF_LINESIZE);
		
		Paint p = new Paint();
		p.setTypeface(Typeface.MONOSPACE);
		p.setTextSize(PREF_FONTSIZE);
		int one_char_is_this_wide = (int)Math.ceil(p.measureText("a")); //measure a single character
		CALCULATED_ROWSINWINDOW = (width / one_char_is_this_wide);
		*/
		}
	
	public void calculateCharacterFeatures(int width,int height) {
		
		if(height == 0 && width == 0) {
			return;
		}
		CALCULATED_LINESINWINDOW = (int) (height / PREF_LINESIZE);
		Paint p = new Paint();
		p.setTypeface(PREF_FONT);
		p.setTextSize(PREF_FONTSIZE);
		int one_char_is_this_wide = (int)Math.ceil(p.measureText("a")); //measure a single character
		CALCULATED_ROWSINWINDOW = (width / one_char_is_this_wide);
		
		//if(CALCULATED_ROWSINWINDOW > 0) {
			//Log.e("SLICK","surfaceChanged called, calculated" + CALCULATED_LINESINWINDOW + " lines and " + CALCULATED_ROWSINWINDOW + " rows.");
		//}
	}
	


	public void surfaceCreated(SurfaceHolder arg0) {
		_runner = new DrawRunner(getHolder(),this,touchLock);
		_runner.setRunning(true);
		_runner.start();
	}
	
	public String getBuffer() {
		StringBuilder build = new StringBuilder();
		for(String line : dlines) {
			build.append(line + "\n");
		}
		return build.toString();
		//return the_original_buffer.toString();
	}
	
	public void setBuffer(String input) {

		//synchronized(isdrawn) {
		//	while(isdrawn == false) {
		//		try {
		//			isdrawn.wait();
		//		} catch (InterruptedException e) {
		//			//Log.e("SLICK","setBuffer interrupted waiting for screen to be done drawing");
		//			e.printStackTrace();
		//		}
		//	}
		//}
		
		synchronized(dlines) {
			dlines.addAll(Arrays.asList(newline.split(input)));
		}
		
		//the_original_buffer.setLength(0);
		//the_buffer.setLength(0);
		//the_original_buffer = new StringBuffer(input);
		//the_buffer = new StringBuffer(betterBreakLines(the_original_buffer,77));
		
		//synchronized(drawn) {
		//	drawn.notify();
		//	drawn = false;
		//}
		if(_runner != null) {
			if(_runner.threadHandler != null) {
		
				if(!_runner.threadHandler.hasMessages(SlickView.DrawRunner.MSG_DRAW)) {
					_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
				}
			}
		}
	}
	
	public void jumpToZero() {
		//call this to scroll back to 0.
		//synchronized(drawn) {
		//	while(!drawn) {
		//		try {
		//			drawn.wait();
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//	}
		//}
		
		/*--------- set scrollBack to 0--------*/
		synchronized(scrollback) {
			scrollback = (float)0.0;
			fling_velocity = 0.0f;
		}
		/*synchronized(drawn) {
			if(drawn) {
				drawn.notify();
				drawn = false;
			}
		}*/
		if(!_runner.threadHandler.hasMessages(SlickView.DrawRunner.MSG_DRAW)) {
			_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
		}
 
	}
	
	public void startDrawing() {
		if(_runner != null) {
			_runner.setRunning(true);
			_runner.start();
			//wasRunning = true;
		}
		
		//synchronized(drawn) {
		//	drawn.notify();
		//}
		if(!_runner.threadHandler.hasMessages(SlickView.DrawRunner.MSG_DRAW)) {
			_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
		}
		
	}
	
	public void stopDrawing() {
		//Log.e("SLICK","Attempted to kill/stop thread at stopDrawing()");
		boolean retry = true;
		_runner.setRunning(false);
		while(retry) {
			try{
				_runner.join();
				retry = false;
			} catch (InterruptedException e) { }
		}

	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		boolean retry = true;
		

		while(retry) {
			try{
				_runner.setRunning(false);
				//synchronized(drawn) {
				//	drawn.notify();
				//	drawn = false;
				//	_runner.setRunning(false);
				//}
				//_runner.setRunning(false);
				
				
				//synchronized(touchLock) {

				//	if(touchLock) {
				//		touchLock.notify();
				//		touchLock = false;
				//	}
				//}
				_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_SHUTDOWN);
				
				_runner.join();
				retry = false;
			} catch (InterruptedException e) { }
		}
		
	}
	
	
	public void onAnimationStart() {

		super.onAnimationStart();
	}
	
	public void onAnimationEnd() {
		super.onAnimationEnd();
	}
	
	static final int MSG_UTIL_ADDTEXT = 400;
	static final int MSG_UTIL_PROCTOUCH = 401;
	
	
	//StringBuffer the_buffer = new StringBuffer();
	//StringBuffer the_original_buffer = new StringBuffer();
	
	Boolean utilinterlock = false;
	
	
	Animation indicator_on = new AlphaAnimation(1.0f,0.0f);
	Animation indicator_off = new AlphaAnimation(0.0f,0.0f);
	Boolean indicated = false;
		
	//Pattern endsonnewline = Pattern.compile(".*\n$");
	
	boolean endedonnewline = false;
	boolean prompted = false;
	StringBuffer the_prompt = new StringBuffer();
	StringBuffer color_free = new StringBuffer();
	Matcher colorfree = colordata.matcher("");
	public void addText(String input,boolean jumptoend) {
		
		Matcher carriagerock = carriage.matcher(input);
		color_free.setLength(0);
		//Matcher toLines = newline.matcher();
		StringBuffer carriage_free = new StringBuffer(carriagerock.replaceAll(""));
		//PREVIOUS CHARACTER WIDTH = 77
		
		//int numlines = countLines(broken_lines.toString());
		
		int size_before = 0;
		int size_after = 0;
		
		synchronized(dlines) {
			//int PREF_MAX_LINES = 200;
				//dlines.removeAllElements();
				//dlines.addAll(Arrays.asList(newline.split(the_buffer)));
				//dlines.
				
				//split the line into newlines and add it to the unbroken buffer.
				/*int was = dlines_unbroken.size();
				dlines_unbroken.addAll(Arrays.asList(newline.split(input)));
				if(dlines_unbroken.size() > PREF_MAX_LINES) {
					dlines_unbroken.removeRange(0, dlines_unbroken.size()-PREF_MAX_LINES);
					
				}
				Log.e("WINDOW","UNBROKEN WAS: " + was + " now " + dlines_unbroken.size());
				*/
				size_before = dlines.size();
				if(!endedonnewline) {

					if(dlines.size() > 0) {
						carriage_free.insert(0, dlines.get(dlines.size()-1));
						dlines.remove(dlines.size()-1);
						dlines.addAll(Arrays.asList(newline.split(betterBreakLines(carriage_free,CALCULATED_ROWSINWINDOW))));
					} else {
						dlines.addAll(Arrays.asList(newline.split(betterBreakLines(carriage_free,CALCULATED_ROWSINWINDOW))));
					}
					
				} else {
					dlines.addAll(Arrays.asList(newline.split(betterBreakLines(carriage_free,CALCULATED_ROWSINWINDOW))));
				}
				
				if(carriage_free.toString().endsWith("\n")){
					
					//unless the last line was a newline
						colorfree.reset(dlines.get(dlines.size()-1));
						color_free.append(colorfree.replaceAll(""));
						//Log.e("WINDOW","INPUT ENDED ON NEWLINE, LAST LINE IS:" + dlines.get(dlines.size()-1) + "| i debug thee into |" + color_free.toString() + "|hahaha");
						if(!color_free.toString().equals("")) {
					//if(!dlines.get(dlines.size()-1).equals("")) {
							dlines.add("");
							
							//endedonnewline = true;
						}
						endedonnewline=true;
					//} else {
						//Log.e("WINDOW","NOT ADDING A NEWLINE BECAUSE BUFFER ALREADY ENDS ON A NEWLINE!");
					//}
				} else {
					endedonnewline = false;
				}
				
				size_after = dlines.size();
				if(dlines.size() > PREF_MAX_LINES) {
					dlines.removeRange(0,dlines.size() - PREF_MAX_LINES);
				}
				
			}
		
		if(scrollback > 0) {
			synchronized(scrollback) {
			scrollback = scrollback + (size_after-size_before)*PREF_LINESIZE;
			}
			
			if(scrollback >= ((dlines.size() * PREF_LINESIZE) - (CALCULATED_LINESINWINDOW*PREF_LINESIZE))) {
				scrollback = ((dlines.size() * PREF_LINESIZE) - (CALCULATED_LINESINWINDOW*PREF_LINESIZE));
			}
			//EditText filler2 = (EditText) parent_layout.findViewById(R.id.filler2);
			//Animation a = new AlphaAnimation(1.0f,0.0f);
			//Interpolator i = new CycleInterpolator(1);
			//a.setDuration(1000);
			//a.setInterpolator(i);
			//a.setFillAfter(true);
			//a.setFillBefore(true);
			new_text_in_buffer_indicator.startAnimation(indicator_on);
			indicated = true;
			
		} else {
			
			
			//Animation a = new AlphaAnimation(0.0f,0.0f);
			//a.setDuration(0);
			//a.setFillAfter(true);
			//a.setFillBefore(true);
			if(indicated) {
				new_text_in_buffer_indicator.startAnimation(indicator_off);
				indicated = false;
			}
		}
		
		//the_buffer.append(broken_lines);
		
		//the_original_buffer.append(carriage_free);
		
		//int chars_per_row = CALCULATED_ROWSINWINDOW;
		//int rows = 25;
		//if(the_buffer.length() > (10*chars_per_row*rows)) {
		//	the_buffer.replace(0, the_buffer.length() - ((10*chars_per_row*rows)), "");
		//}
		//if(the_original_buffer.length() > (10*chars_per_row*rows)) {
		//	the_original_buffer.replace(0, the_original_buffer.length() - ((10*chars_per_row*rows)), "");
		//}
		
		//linetrap = newline.split(the_buffer);

		
		//synchronized(drawn) {
		//	drawn.notify();
		//	drawn = false;
		//}
		
		if(_runner != null) {
				//if(!_runner.threadHandler.hasMessages(DrawRunner.MSG_DRAW)) {
			//s//ynchronized(_runner) {
			if(!_runner.threadHandler.hasMessages(DrawRunner.MSG_DRAW))
					_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
				//}
			//}
		}
		
	}
	
	public void setFont(Typeface font) {
		synchronized(dlines) {
			PREF_FONT = font;
			//calculateCharacterFeatures(WINDOW_WIDTH,WINDOW_HEIGHT);
		}
	}
	
	public void setFontSize(int size) {
		synchronized(dlines) {
			PREF_FONTSIZE = size;
		}
	}
	
	public void setLineSpace(int space) {
		synchronized(dlines) {
			PREF_LINESIZE = PREF_FONTSIZE + space;
		}
	}
	
	public void setMaxLines(int amount) {
		synchronized(dlines) {
			PREF_MAX_LINES = amount;
		}
	}
	
	public void setCharacterSizes(float size,int space) {
		synchronized(dlines) {
			PREF_FONTSIZE = size;
			PREF_LINESIZE = size+space;
			
			calculateCharacterFeatures(WINDOW_WIDTH,WINDOW_HEIGHT);
		}
	}
	
	//StringBuffer sel_color = new StringBuffer(new Integer(0xFFBBBBBB).toString());
	//StringBuffer sel_bright = new StringBuffer(new Integer(0).toString());
	int selectedColor = 0;
	int selectedBright = 0;
	StringBuffer csegment = new StringBuffer();
	Canvas drawn_buffer = null;
	long prev_draw_time = 0;
	boolean finger_down_to_up = false;
	Matcher toLines = newline.matcher("");
	StringBuffer drawline = new StringBuffer();
	
	StringBuffer last_color = null;
	String[] linetrap = new String[0];
	BufferVector<String> dlines = new BufferVector<String>();
	StringBuffer dlines_unbroken = new StringBuffer();
	
	boolean optsInitialized = false;
	Paint opts;
	Paint bg_opts;
	
	boolean bgColorSpanning = false;
	int bgColorSpanStart = 0;
	//StringBuffer sel_bgcolor = new StringBuffer(new Integer(0xFF000000).toString());
	//StringBuffer sel_bgbright = new StringBuffer(new Integer(0).toString());
	int selectedBackgroundColor = 0;
	int selectedBackgroundBright = 0;
	//Matcher bleedfind = lastcolordatainline.matcher("");
	Matcher bleedfind = colordata.matcher("");
	Matcher colormatch = colordata.matcher("");
	@Override

	public void onDraw(Canvas canvas) {
		
		if(!optsInitialized) {
			opts = new Paint();
			bg_opts = new Paint();
			optsInitialized = true;
		}
		
		//dlines.addAll(Arrays.asList(linetrap));
		//dlines.get = "foo";
		
		
		
		int scrollbacklines = (int)Math.floor(scrollback / (float)PREF_LINESIZE);
		
		synchronized(scrollback) {
		
		if(prev_draw_time == 0) { //never drawn before
			if(finger_down) {
				scrollback = (float)Math.floor(scrollback + diff_amount);
				if(scrollback < 0) {
					scrollback = 0.0f;
				} else {
					//Log.e("WINDOW","CURRENT SCROLLBACK: " + scrollback + " MAX: " + ((dlines.size() * PREF_LINESIZE) - (CALCULATED_LINESINWINDOW*PREF_LINESIZE)));
					if(scrollback >= ((dlines.size() * PREF_LINESIZE) - (CALCULATED_LINESINWINDOW*PREF_LINESIZE))) {
						//Log.e("WINDOW","UPPER CAP OF THE BUFFER REACHED!");
						scrollback = ((dlines.size() * PREF_LINESIZE) - (CALCULATED_LINESINWINDOW*PREF_LINESIZE));
						//fling_velocity = 0;
						//prev_draw_time = 0;
						//Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
						//buttonaddhandler.sendEmptyMessage(SlickView.MSG_NORMALPRIORITY);
						
					}
				}
				diff_amount = 0;
				
				scrollbacklines = (int)Math.floor((scrollback) / (float)PREF_LINESIZE);

			} else {
				if(finger_down_to_up) {
					prev_draw_time = System.currentTimeMillis(); 
					Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
					buttonaddhandler.sendEmptyMessage(SlickView.MSG_UPPRIORITY);
					finger_down_to_up=false;
				}
			}
		} else {
			
			if(finger_down == true) {

			} else {
				
				long nowdrawtime = System.currentTimeMillis(); 
				
				float duration_since_last_frame = ((float)(nowdrawtime-prev_draw_time)) / 1000.0f; //convert to seconds
				prev_draw_time = System.currentTimeMillis();
				//compute change in velocity: v = vo + at;
				if(fling_velocity < 0) {
					fling_velocity = fling_velocity + fling_accel*duration_since_last_frame;
					scrollback = scrollback + fling_velocity*duration_since_last_frame;
					
				} else if (fling_velocity > 0) {
					
					fling_velocity = fling_velocity - fling_accel*duration_since_last_frame;
					scrollback = scrollback + fling_velocity*duration_since_last_frame;
					
				}
				
				if(Math.abs(new Double(fling_velocity)) < 15) {
					fling_velocity = 0;
					prev_draw_time = 0;
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
					buttonaddhandler.sendEmptyMessage(SlickView.MSG_NORMALPRIORITY);
				}
				
				/*if(scrollback.intValue() / PREF_LINESIZE < 1) {
					prev_draw_time = 0;
					fling_velocity = 0;
					buttonaddhandler.sendEmptyMessage(SlickView.MSG_CLEAR_NEW_TEXT_INDICATOR);
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
				}*/
				
					
				if(scrollback <= 0) {
					scrollback = 0.0f;
					fling_velocity = 0;
					prev_draw_time = 0;
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
					buttonaddhandler.sendEmptyMessage(SlickView.MSG_NORMALPRIORITY);

					buttonaddhandler.sendEmptyMessage(SlickView.MSG_CLEAR_NEW_TEXT_INDICATOR);
				}
				
				//check and see if we are above the upper limit.
				//Log.e("WINDOW","CURRENT SCROLLBACK: " + scrollback + " MAX: " + ((dlines.size() * PREF_LINESIZE) - (CALCULATED_LINESINWINDOW*PREF_LINESIZE)));
				if(scrollback >= ((dlines.size() * PREF_LINESIZE) - (CALCULATED_LINESINWINDOW*PREF_LINESIZE))) {
					//Log.e("WINDOW","UPPER CAP OF THE BUFFER REACHED!");
					scrollback = ((dlines.size() * PREF_LINESIZE) - (CALCULATED_LINESINWINDOW*PREF_LINESIZE));
					fling_velocity = 0;
					prev_draw_time = 0;
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
					buttonaddhandler.sendEmptyMessage(SlickView.MSG_NORMALPRIORITY);
					
				}
			
			
				scrollbacklines = (int)Math.floor(scrollback / (float)PREF_LINESIZE);
			}

			
		}
		}
		
		synchronized(dlines) {
			int remainder = (int) (canvas.getHeight() - (CALCULATED_LINESINWINDOW*PREF_LINESIZE)) - 6 + (int)Math.floor(scrollback % PREF_LINESIZE);
			if(remainder < 0) {
				//Log.e("SLICK","WE HAVE A PROBLEM WITH WINDOW SIZE");
			}
			
			//first clear the background
			canvas.drawColor(0xFF0A0A0A); //fill with black
			//canvas.drawColor(0xFF333333); //fill with grey
	        
	        opts.setAlpha(0);
	        opts.setAntiAlias(true);
	        //opts.setDither(true);
	        opts.setARGB(255, 255, 255, 255);
	        opts.setTextSize(PREF_FONTSIZE);
	        
	        //opts.setStyle(Style.)
	        opts.setTypeface(PREF_FONT);
	
	        //Matcher toLines = newline.matcher(the_buffer.toString());
	        //toLines.reset(the_buffer); 
	        //StringBuffer line = new StringBuffer();
	        //drawline.setLength(0);
	        
	        //linetrap = null;
	        //linetrap = newline.split(the_buffer);
	        //linetrap.clear();
	        //linetrap.removeAllElements();
	        //linetrap.
	        //Log.e("SLICK","Buffer contains: " + lines.length + " lines.");
	        //if(linetrap.length < 1) {
	       // 	return;
	        //}
	        
	       
	        
	        if(dlines.size() < 1) { return; }
	        
	        
	        int currentline = 1;
	        
	        //count lines
	       // int numlines = countLines();
	        
	        //
	        
	        int numlines = dlines.size();
	        int maxlines = CALCULATED_LINESINWINDOW; 
	        
	        
	        
	        int startDrawingAtLine = 1;
	        if(numlines > maxlines) {
	        	startDrawingAtLine = numlines - maxlines - scrollbacklines + 1;
	        } else {
	        	startDrawingAtLine = 1;
	        }
	        
	       
	        boolean endonnewline = false;
	        
	        int startpos = startDrawingAtLine -3;
	        if(startpos < 0) {
	        	startpos = 0;
	        }
	        
	        int endpos = startDrawingAtLine + maxlines;
	        if(endpos > dlines.size()) {
	        	endpos = dlines.size();
	        }
	        
	        //start from startpos backwards, try and find a color tag in the line
	        boolean notFound = true;
	        int colorBleed = startpos - 1;
	        //int bleedColor = 0;
	        //StringBuffer bleed_bright = new StringBuffer();
	        //StringBuffer bleed_color = new StringBuffer();
	        while(notFound) {
	        	if(colorBleed < 0) {
	        		//we made it all the way here and couldn't find it, yikes.
	        		//Log.e("SLICK","COULD NOT FIND A COLOR, DEFAULTING");
	            	//sel_bright.append("0");
	            	//sel_color.append("37");
	            	selectedBright = 0;
	            	selectedColor = 37;
	        		notFound = false;
	        	} else {
	        		//get the matcher
	        		
	        		bleedfind.reset(dlines.get(colorBleed));
	        		String line = dlines.get(colorBleed); //TODO: remove this line
	        		int count = line.length(); //TODO: remove this line
	        		while(bleedfind.find()) {
	        			//sel_bright.setLength(0);
	        			//sel_color.setLength(0);
	        			//keep the current group. or transform it to a color here.
	        			//sel_bright.append((bleedfind.group(2) == null) ? "0" : bleedfind.group(2));
	        			//sel_color.append(bleedfind.group(3));
	        			/*selectedBright = Integer.parseInt((bleedfind.group(2) == null) ? "0" : bleedfind.group(2));
	        			selectedColor = Integer.parseInt(bleedfind.group(3));
	        			
	        			if(selectedColor >= 40) {
	        				//we have a background color in the mix, skip it
	        				selectedBright = 0;
	        				selectedColor = 37;
	        			} else {
	        			//opts.setColor(0xFF000000 | Colorizer.getColorValue(sel_bright, sel_color));
	        				opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
	        				notFound = false;
	        			}*/
	        			
	        			//better block
	        			//Log.e("WINDOW","BLEED COLOR: " + bleedfind.group() + " | " + bleedfind.group(1) + " | " + bleedfind.group(2) + " | " + bleedfind.group(3) + " | " + bleedfind.group(4) + " | " + bleedfind.group(5) + " || ON LINE " + colorBleed);
	        			
	        			Integer brightVal = Integer.parseInt(((bleedfind.group(2) == null) ? "0" : bleedfind.group(2)));
	        			Integer firstVal = Integer.parseInt((bleedfind.group(4) == null) ? "37" : bleedfind.group(4));
	        			Integer secondVal = Integer.parseInt(bleedfind.group(5));
	        			
	        			boolean brightiscolor = false;
	        			if(brightVal > 2) {
	        				brightiscolor = true;
	        				firstVal = brightVal;
	        			} else {
	        				selectedBright = brightVal;
	        			}
	        			//test firstVal.
	        			Colorizer.COLOR_TYPE type = Colorizer.getColorType(firstVal);
	        			if(type == Colorizer.COLOR_TYPE.FOREGROUND) {
	        				if(bleedfind.group(4) != null || brightiscolor) { 
	        					selectedColor = firstVal;
	        					opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
	        					notFound = false;
	        				}
	        			} else if(type == Colorizer.COLOR_TYPE.BACKGROUND) {
	        				selectedBackgroundColor = firstVal;
	        				//bg_opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBackgroundBright, selectedBackgroundColor));
	        			}
	        			
	        			type = Colorizer.getColorType(secondVal);
	        			if(type == Colorizer.COLOR_TYPE.FOREGROUND) {
	        				selectedColor = secondVal;
	        				opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
	        				notFound = false;
	        			} else if(type == Colorizer.COLOR_TYPE.BACKGROUND) {
	        				selectedBackgroundColor = secondVal;
	        				//bg_opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBackgroundBright, selectedBackgroundColor));
	        			}
	        			
	        			
	        			if(selectedColor == 0 || selectedColor == 39) {
	        				selectedBright = 0;
	        				selectedColor = 37;
	        				opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
	        				notFound = false;
	        			}
	        			//Log.e("SLICK","Found "+bleedfind.group(0)+" in: " +(startpos - colorBleed) + " steps.");
	        			//Log.e("SLICK","On line:" + dlines.get(colorBleed));
	        			
	        		}
	        		if(notFound) {
	        			//if we got here than it means the line didnt have color data, decrement and try again
	        			colorBleed = colorBleed - 1; //working backwards.
	        		}
	        	}
	        	
	        }
	        
	        //if(notFound) {
	        	
	        //}
	        
	        //get regexp matcher
	        
	        //while found, keep the color group. @ end, the keep will be the last color code in the line
	        
	        for(int i=startpos;i<endpos;i++) {
	        	//Log.e("SLICK","Drawing line:" + i + ":" + lines[i]);
	        	int screenpos = i - startDrawingAtLine + 2;
	    		int y_position =  (int) ((screenpos*PREF_LINESIZE)+remainder);
	    		
	    		colormatch.reset(dlines.get(i));
	    		
	    		
	    		//Log.e("SLICK","Drawing line:" + i + ":" +":"+y_position +":" + lines[i]);
	    		
	    		float x_position = 0;
	    		
	    		boolean colorfound = false;
	    		
	    		while(colormatch.find()) {
	    			colorfound = true;
	    			if(colorDebugMode == 1 || colorDebugMode == 2) {
	    				colormatch.appendReplacement(csegment, colormatch.group());
	    			} else {
	    				colormatch.appendReplacement(csegment, "");
	    			}
	    			
	    			//colormatch.appendReplacement(csegment, colormatch.group());
	    			
	    			//get color data
	
	    			//int color = Colorizer.getColorValue(new Integer(sel_bright.toString()), new Integer(sel_color.toString()));
	    			//int color = Colorizer.getColorValue(sel_bright, sel_color);
	    			int color = Colorizer.getColorValue(selectedBright, selectedColor);
	    			if(color == 0) {
	    				//Log.e("SLICK","COLORLOOKUP RETURNED 0 for:" + colormatch.group() + " bright:" + selectedBright + " val: " + selectedColor);
	    			}
	    			
	    			//Colorizer.COLOR_TYPE fgbgType = Colorizer.getColorType(sel_color);
	    			if(colorDebugMode == 2 || colorDebugMode == 3) {
	    				opts.setColor(0xFFBBBBBB);
	    			} else {
	    				opts.setColor(0xFF000000 | color);
	    			}
	    			
	    			//canvas.drawText(csegment.toString(), x_position, y_position, opts);
	    			if(bg_opts.getColor() != 0xFF000000) {
	    				//Log.e("WINDOW","DRAWING BGCOLOR!!!!!");
	    				canvas.drawRect(x_position, y_position - opts.getTextSize(), x_position + opts.measureText(csegment,0,csegment.length()), y_position+5, bg_opts);
	    				
	    			} 
	    			//try {
					canvas.drawText(csegment, 0, csegment.length(), x_position, y_position, opts);
					//} catch (UnsupportedEncodingException e) {
					//	throw new RuntimeException(e);
					//}
	    			//Log.e("WINDOW","WRITING: " + csegment + " TO SCREEN - MID LINE");
	    			//x_position = x_position + opts.measureText(csegment.toString());
	    			x_position = x_position + opts.measureText(csegment,0,csegment.length());
	    			//opts.mea
	    			
					//sel_bgcolor.setLength(0);
					//sel_bgbright.setLength(0);
					//sel_bgcolor.append(colormatch.group(3));
					//sel_bgbright.append((colormatch.group(2) == null) ? "0" : colormatch.group(2)); 			
					
	    			/*************************
	    			 * NEW BLOCK, THIS IS GOOD CODE
	    			 */
	    			//Log.e("WINDOW","COLOR: " + colormatch.group() + " | " + colormatch.group(1) + " | " + colormatch.group(2) + " | " + colormatch.group(3) + " | " + colormatch.group(4) + " | " + colormatch.group(5));
        			
	    			Integer brightVal = Integer.parseInt(((colormatch.group(2) == null) ? "0" : colormatch.group(2)));
        			Integer firstVal = Integer.parseInt((colormatch.group(4) == null) ? "37" : colormatch.group(4));
        			Integer secondVal = Integer.parseInt(colormatch.group(5));
        			
        			boolean brightiscolor = false;
        			if(brightVal > 2) {
        				//bright is color
        				brightiscolor = true;
        				if(colormatch.group(4) == null) {
        					firstVal = brightVal;
        				}
        			} else {
        				selectedBright = brightVal;
        			}
        			csegment.setLength(0);
        			//test firstVal.
        			if(colormatch.group(4) != null || brightiscolor) {
	        			Colorizer.COLOR_TYPE type = Colorizer.getColorType(firstVal);
	        			if(type == Colorizer.COLOR_TYPE.FOREGROUND) {
	        				selectedColor = firstVal;
	        				//opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
	        			} else if(type == Colorizer.COLOR_TYPE.BACKGROUND) {
	        				selectedBackgroundColor = firstVal;
	        				bg_opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBackgroundBright, selectedBackgroundColor));
	        			}
        			}
        			
        			Colorizer.COLOR_TYPE type = Colorizer.getColorType(secondVal);
        			if(type == Colorizer.COLOR_TYPE.FOREGROUND) {
        				selectedColor = secondVal;
        				//opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
        			} else if(type == Colorizer.COLOR_TYPE.BACKGROUND) {
        				selectedBackgroundColor = secondVal;
        				bg_opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBackgroundBright, selectedBackgroundColor));
        			}
        			
        			//Log.e("WINDOW","PROCESSING COLOR: " + selectedColor + " BRIGHT: " + selectedBright);
	    			
        			if(selectedColor == 0 || selectedColor == 39) {  //if the color lookup failed, or the selected color is the "default" specified by the protocol (a 39 code is "default") 

	        				selectedBright = 0;
	        				selectedColor = 37;
	        		}
        			if(selectedColor == 1) {
        				selectedBright = 1;
        				selectedColor = 37;
        			}
	    			/****************************
	    			 * OLD BLOCK THIS IS BAD CODE
	    			 */
	    			
	    			/*selectedBackgroundColor = Integer.parseInt(colormatch.group(3));
					selectedBackgroundBright = Integer.parseInt((colormatch.group(2) == null) ? "0" : colormatch.group(2));
					Colorizer.COLOR_TYPE fgbgType = Colorizer.getColorType(selectedBackgroundColor);
	    			if(fgbgType == Colorizer.COLOR_TYPE.BACKGROUND) {
	    				//if(bgColorSpanning) {
	    					//draw the rectangle, set to the new color, reset the indicator.
	    					//bgColorSpanning = false;
	    				//} else {
	    					//Log.e("WINDOW","GOT BGCOLOR: " + colormatch.group(0));
	
	    					csegment.setLength(0);
	    					bg_opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBackgroundBright, selectedBackgroundColor));
	    				//	bgColorSpanning = true;
	    				//}
	    			} else {
	    			
	        			csegment.setLength(0);
	        			
	        			//sel_bright.setLength(0);
	        			//sel_color.setLength(0);
	        			//sel_bright.append((colormatch.group(2) == null) ? "0" : colormatch.group(2));
	        			//sel_color.append(colormatch.group(3));
	        			selectedBright = Integer.parseInt((colormatch.group(2) == null) ? "0" : colormatch.group(2));
	        			selectedColor = Integer.parseInt(colormatch.group(3));
	        			
	        			if(colormatch.groupCount() > 3) {
		        			Integer extraColor = Integer.parseInt((colormatch.group(4) == null) ? "40" : colormatch.group(4));
		        			
		        			
		        			//text the extraColor/selectedColor if they are foreground or background colors.
		        			Colorizer.COLOR_TYPE testtype = Colorizer.getColorType(selectedColor);
		        			if(testtype == Colorizer.COLOR_TYPE.FOREGROUND) {
		        				selectedColor = selectedColor;
		        			} else if(testtype == Colorizer.COLOR_TYPE.BACKGROUND) {
		        				selectedBackgroundColor = selectedColor;
		        			}
		        			
		        			//now test the extracolor
		        			testtype = Colorizer.getColorType(extraColor);
		        			if(testtype == Colorizer.COLOR_TYPE.FOREGROUND) {
		        				selectedColor = extraColor;
		        			} else if(testtype == Colorizer.COLOR_TYPE.BACKGROUND) {
		        				selectedBackgroundColor = extraColor;
		        				bg_opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBackgroundBright, selectedBackgroundColor));
		        			}
	        			}
	        			//if(sel_color.toString().equalsIgnoreCase("0")) {
	        			if(selectedColor == 0 || selectedColor == 39) {  //if the color lookup failed, or the selected color is the "default" specified by the protocol (a 39 code is "default") 
	        			//Log.e("SLICK","COLOLPARSE GOT 0 for:" + colormatch.group() + " bright:" + sel_bright + " val: " + sel_color);
	        				//sel_color.setLength(0);
	        				//sel_color.append("37");
	        				selectedBright = 0;
	        				selectedColor = 37;
	        			}
	    			} */
	    			/***************************
	    			 *  END BAD CODE BLOCK
	    			 */
	    		}
	    		//end of main loop.
	    		if(colorfound) {
	    			//int color = Colorizer.getColorValue(new Integer(sel_bright.toString()), new Integer(sel_color.toString()));
	    			//int color = Colorizer.getColorValue(sel_bright, sel_color);
	    			int color = Colorizer.getColorValue(selectedBright, selectedColor);
	    			if(colorDebugMode == 2 || colorDebugMode == 3) {
	    				opts.setColor(0xFFBBBBBB);
	    			} else {
	    				opts.setColor(0xFF000000 | color); //always alpha
	    			}
	    			colormatch.appendTail(csegment);
	    			
	    			if(bg_opts.getColor() != 0xFF000000) {
	    				
	    				canvas.drawRect(x_position, y_position - opts.getTextSize(), x_position + opts.measureText(csegment,0,csegment.length()), y_position+5, bg_opts);
	    			} 
	    			
	    			//canvas.drawText(csegment.toString(), x_position, y_position, opts);
	    			//csegment.
	    			//String str = new String();
	    			
	    			//try {
					canvas.drawText(csegment, 0, csegment.length(), x_position, y_position, opts);
					//} catch (UnsupportedEncodingException e) {
					//	throw new RuntimeException(e);
					//}
	    			//Log.e("WINDOW","WRITING: " + csegment + " TO SCREEN WITH COLOR!");
	    			csegment.setLength(0);
	    			
	    			bg_opts.setColor(0xFF000000);
	    		}
	    		
	    		if(!colorfound) {
	    			if(colorDebugMode == 2 || colorDebugMode == 3) {
	    				opts.setColor(0xFFBBBBBB);
	    			}
	    			//try {
					canvas.drawText(dlines.get(i), 0, y_position , opts);
					//} catch (UnsupportedEncodingException e) {
					//	throw new RuntimeException(e);
					//}
	    			//Log.e("WINDOW","WRITING: " + dlines.get(i) + " TO SCREEN NO COLOR!");
	    		}
	        }
	        
	        //draw scroller here.
	        //if(scrollback > PREF_LINESIZE) {
	        	showScroller(canvas);
	        //}
        }
        
        
	}
	
	public void showScroller(Canvas c) {
		//i am not sure this is going to work, so we are just going to fake something for now.
		
		Paint p = new Paint();
		
		p.setColor(0xFFFF0000);
		
		//need to calculate the percentage that this takes up.
		if(dlines.size() < 1) {
			return; //no scroller to show.
		}
		
		//lots to do for coloring
		
		Float scrollerSize = 0.0f;
		Float scrollerPos = 0.0f;
		Float scrollerTop = 0.0f;
		Float scrollerBottom = 0.0f;
		float range = 0.0f;
		float posPercent = 0.0f;
		Float windowPercent = WINDOW_HEIGHT / (dlines.size()*PREF_LINESIZE);
		if(windowPercent > 1) {
			//then we have but 1 page to show
			return;
		} else {
			scrollerSize = windowPercent*WINDOW_HEIGHT;
			
			//range = WINDOW_HEIGHT - (scrollerSize);
			
			//float topPercent = (scrollback+WINDOW_HEIGHT)/(dlines.size()*PREF_LINESIZE);
			//float bottomPercent = (scrollback+WINDOW_HEIGHT)/(dlines.size()*PREF_LINESIZE);
			//scrollerTop = WINDOW_HEIGHT * topPercent;
			//scrollerBottom = WINDOW_HEIGHT * bottomPercent;
			posPercent = (scrollback + (WINDOW_HEIGHT/2))/(dlines.size()*PREF_LINESIZE);
			
			scrollerPos = WINDOW_HEIGHT*posPercent;
			
			//scrollerPos = range*posPercent;
			//real position, since inverted
			scrollerPos = WINDOW_HEIGHT-scrollerPos;
			//real position is also offset because of the "useable range"
			//scrollerPos = scrollerPos;
			
			//Log.e("WINDOW","SCROLLER PARAMS: size: " + scrollerSize + " range: " + range + " positioin: " + scrollerPos);
		}
		
		//rect = left top right bottom, need to offset for the "useable range.
		//scroller colors, that's right, this is gonna be sick.
		int blue_value = (int) (-1*255*posPercent + 255);
		int red_value = (int) (255*posPercent);
		int alpha_value = (int) ((255-70)*posPercent+70);
		///Color color = new Color();
		
		int final_color = Color.argb(alpha_value, red_value, 100, blue_value);
		//String message = "COLOR: R=" + Integer.toHexString(red_value) + " G=" + Integer.toHexString(blue_value) + " A=" + Integer.toHexString(alpha_value) + " FINAL="+Integer.toHexString(final_color);
		//Log.e("WINDOW",message);
		p.setColor( final_color);
		float density = this.getResources().getDisplayMetrics().density;
		Rect r = new Rect(WINDOW_WIDTH-(int)(2*density),(int)(scrollerPos - scrollerSize/2),WINDOW_WIDTH,(int)(scrollerPos + scrollerSize/2));
		
		c.drawRect(r, p);
		
	}
	
	boolean finger_down = false;
	int diff_amount = 0;
	public Boolean is_in_touch = false;
	
	public boolean onTouchEvent(MotionEvent t) {
		
		synchronized(scrollback) {
		if(t.getAction() == MotionEvent.ACTION_DOWN) {
			buttonaddhandler.sendEmptyMessageDelayed(MSG_BUTTONDROPSTART, 2500);
			start_x = new Float(t.getX(t.getPointerId(0)));
			start_y = new Float(t.getY(t.getPointerId(0)));
			pre_event = MotionEvent.obtainNoHistory(t);
			fling_velocity = 0.0f;
			finger_down = true;
			finger_down_to_up = false;
			prev_draw_time = 0;
		}
		
		if(!increadedPriority) {
			_runner.dcbPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
			//_runner.
			increadedPriority = true;
		}
		
		if(t.getAction() == MotionEvent.ACTION_MOVE) {
			

			//compute distance;
			Float now_x = new Float(t.getX(t.getPointerId(0)));
			Float now_y = new Float(t.getY(t.getPointerId(0)));
			double distance = Math.sqrt(Math.pow(now_x-start_x, 2) + Math.pow(now_y-start_y,2));
			if(distance > 30) {
				buttonaddhandler.removeMessages(MSG_BUTTONDROPSTART);
			}
			

			float thentime = pre_event.getEventTime();
			float nowtime = t.getEventTime();
			
			float time = (nowtime - thentime) / 1000.0f; //convert to seconds
			
			//float prev_y = t.getHistoricalY(t.getPointerId(0),history-1);
			float prev_y = pre_event.getY(t.getPointerId(0));
			float dist = now_y - prev_y;
			diff_amount = (int)dist;
			
			float velocity = dist / time;
			float MAX_VELOCITY = 700;
			if(Math.abs(velocity) > MAX_VELOCITY) {
				if(velocity > 0) {
					velocity = MAX_VELOCITY;
				} else {
					velocity = MAX_VELOCITY * -1;
				}
			}
			fling_velocity = velocity;
			//Log.e("SLICK","MOTIONEVENT HISTORICAL Y: "+new Float(dist).toString() + " TIME: " + new Float(time).toString() + " VEL: " + new Float(velocity));
			
			
			if(Math.abs(diff_amount) > 5) {
				
				pre_event = MotionEvent.obtainNoHistory(t);
			}
			

		}
		
		int pointers = t.getPointerCount();
		//int the_last_pointer = t.getPointerId(0);
		float diff = 0;
		for(int i=0;i<pointers;i++) {
			
			Float y_val = new Float(t.getY(t.getPointerId(i)));
			Float x_val = new Float(t.getX(t.getPointerId(i)));
			bx = x_val.intValue();
			by = y_val.intValue();
			if(pre_event == null) {
				diff = 0;
			} else {
				diff = y_val - prev_y;	
			}
			
			//Log.e("SLICK","SLICK TOUCH AT PY:" + prev_y + " Y:" + y_val + " P:" + i + " DIF:" + diff);
			prev_y = y_val;
		}
		
		
		if(t.getAction() == (MotionEvent.ACTION_UP)) {
			
			pre_event = null;
			prev_y = new Float(0);
			buttonaddhandler.removeMessages(SlickView.MSG_BUTTONDROPSTART);
	        
	        //reset the priority
	        increadedPriority = false;
	        _runner.dcbPriority(Process.THREAD_PRIORITY_DEFAULT);
	        

	        pre_event = null;
	        finger_down=false;
	        finger_down_to_up = true;
	        
		}
		
        /*synchronized(drawn) {
        	if(drawn.booleanValue()) {
        		drawn.notify();
        		drawn = false;
        	} 
		}*/
		if(!_runner.threadHandler.hasMessages(SlickView.DrawRunner.MSG_DRAW)) {
			_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
		}
		}
		return true; //consumes
		
	}


	public int measuredX = 0;
	public int measuredY = 0;
	
	public void onMeasure(int wSpec,int hSpec) {
		int provided_x = 0;
		int provided_y = 0;
		switch(MeasureSpec.getMode(wSpec)) {
			case MeasureSpec.AT_MOST:
				
				provided_x = MeasureSpec.getSize(wSpec);
				//Log.e("SLICK","MEASURING WIDTH: AT MOST" + provided_x);
				break;
			case MeasureSpec.EXACTLY:
				
				provided_x = MeasureSpec.getSize(wSpec);
				//Log.e("SLICK","MEASURING WIDTH: EXACTLY" + provided_x);
				break;
			case MeasureSpec.UNSPECIFIED:
				
				provided_x = MeasureSpec.getSize(wSpec);
				//Log.e("SLICK","MEASURING WIDTH: UNSPECIFIED" + provided_x);
				break;
			default:
				break;
		}
		
		switch(MeasureSpec.getMode(hSpec)) {
		case MeasureSpec.AT_MOST:
			
			provided_y = MeasureSpec.getSize(hSpec);
			//Log.e("SLICK","MEASURING HEIGHT: AT MOST" + provided_y);
			break;
		case MeasureSpec.EXACTLY:
			
			provided_y = MeasureSpec.getSize(hSpec);
			//Log.e("SLICK","MEASURING HEIGHT: EXACTLY" + provided_y);
			break;
		case MeasureSpec.UNSPECIFIED:
			
			provided_y = MeasureSpec.getSize(hSpec);
			//Log.e("SLICK","MEASURING HEIGHT: UNSPECIFIED" + provided_y);
			break;
		default:
			break;
	}
		
		this.setMeasuredDimension(MeasureSpec.getSize(wSpec), MeasureSpec.getSize(hSpec));
	}
	
	
	
	MotionEvent pre_event = null;
	Float prev_y = new Float(0.1);
	boolean increadedPriority = false;
	float start_x = 0;
	float start_y = 0;
	float fling_velocity;
	float fling_accel = 200.0f; //(units per sec);
	float extra_accel = 0.0f;
	
	StringBuffer bholder = new StringBuffer();
	StringBuffer bline = new StringBuffer();
	StringBuffer betterBreakLines(StringBuffer input,int char_limit) {
		bholder.setLength(0);
		bline.setLength(0);
		
		Matcher l = newline.matcher(input);
		boolean found = false;
		while(l.find()) {
			l.appendReplacement(bline,"");
			found = true;
			bholder.append(splitLine(bline,char_limit) + "\n");
			bline.setLength(0);
		}
		if(found) {
			//handle tail (prompt)
			bline.setLength(0);
			l.appendTail(bline);
			bholder.append(splitLine(bline,char_limit));
		} else {
			//handle input that has no line breaks.
			bholder.append(splitLine(input,char_limit));
		}
		
		return bholder;
	}
	
	StringBuffer holder = new StringBuffer();
	StringBuffer segment = new StringBuffer();
	StringBuffer line = new StringBuffer();
	StringBuffer enddata = new StringBuffer();
	
	StringBuffer splitLine(StringBuffer input,int char_limit) {
		holder.setLength(0);
		segment.setLength(0);
		line.setLength(0);
		enddata.setLength(0);
		
		Matcher colormatch = colordata.matcher(input);
		
		boolean found = false;
		int normalchars = 0;
		while(colormatch.find()) {
			found = true;
			colormatch.appendReplacement(segment, colormatch.group());
			normalchars += segment.length() - colormatch.group().length();
			if(normalchars > char_limit) {
				int chars_already_drawn = normalchars - (segment.length() - colormatch.group().length());
				while(normalchars > char_limit) {
					//find out how many characters we are over.
					int leftover = normalchars-char_limit;
					int chopto = (segment.length() - colormatch.group().length()) - leftover;
					if(leftover > char_limit) {
						leftover = char_limit;
						chopto = char_limit - chars_already_drawn;
					}
					
					//int chopto = (segment.length() - colormatch.group().length()) - leftover;
					holder.append(segment.subSequence(0, chopto) + "\n");
					segment.replace(0, chopto,"");
					normalchars = segment.length() - colormatch.group().length();
					chars_already_drawn = 0;
				}
				if(segment.length() > 0) {
					holder.append(segment);
					normalchars = segment.length() - colormatch.group().length();
					if(normalchars < 0) {
						normalchars = 0;
					}
				}
			} else {
				//append the segment and zero it out
				holder.append(segment);
			}
			segment.setLength(0);
		}
		if(found) {
			//append tail stuff
			//breaking in the process
			line.setLength(0);
			colormatch.appendTail(line);

			if(line.length() > char_limit - normalchars) {
				while(line.length() > char_limit - normalchars) {
					holder.append(line.subSequence(0, char_limit - normalchars) + "\n");
					line.replace(0, char_limit-normalchars, "");
					normalchars = 0;
				}
				if(line.length() > 0) {
					holder.append(line);
				}
			} else {
				holder.append(line);
			}
			
			
			//holder.append(splitLine(line,77));
			//line.setLength(0);
			//colormatch.appendTail(holder);
		} else {
			//no colors found, perform normal break;
			line.append(input);
			if(line.length() > char_limit) {
				while(line.length() > char_limit) {
					holder.append(line.subSequence(0, char_limit) + "\n");
					line.replace(0, char_limit, "");
				}
				if(line.length() > 0) {
					holder.append(line);
				}
			} else {
				holder.append(line);
			}
		}
		
		
		
		return holder;
		
	}
	
	
	/*public StringBuffer prompt_string = new StringBuffer();
	public StringBuffer garbage_string = new StringBuffer();
	public int countLines() {
		Matcher toLines = newline.matcher(the_buffer.toString());
		
		boolean endonnewline = false;
		int found = 0;
		while(toLines.find()) {
			found++;
			toLines.appendReplacement(garbage_string, "");
			garbage_string.setLength(0);
			if(toLines.end() == the_buffer.length()) {
				//ended on a new line
				endonnewline = true;
				//toLines.appendReplacement()
			}
		}
		if(!endonnewline) {
			//make sure to include the last, un-newline-terminated string, which should be the prompt
			found++;
			String prev_prompt = prompt_string.toString();
			prompt_string.setLength(0); //clear the prompt
			toLines.appendTail(prompt_string);
			if(!prev_prompt.equals(prompt_string.toString())) {
				//Log.e("SLICK","NEW PROMT:" + prompt_string.toString());
			}
			
		}
		
		return found;
	}
	
	public int countLines(String input) {
		Matcher toLines = newline.matcher(input);
		
		int found = 0;
		boolean endonnewline = false;
		while(toLines.find()) {
			toLines.appendReplacement(garbage_string, "");
			found++;
			if(toLines.end() == input.length()) {
				endonnewline = true;
				Log.e("SLICK","Line count ended on a new line");
			}
		}
		if(!endonnewline) {
			found++;
			Log.e("SLICK","Line count not ended on a new line");
		}
		
		garbage_string.setLength(0);
		
		return found;
	}*/
	
	public void reBreakBuffer() {
		/*BufferVector<String> tmp = (BufferVector<String>) dlines_unbroken.clone();
		dlines_unbroken.clear();
		dlines.clear();
		
		for(String s : tmp) {
			if(s.endsWith("\n")) {
				addText(s+"\n",true);
			} else {
				addText(s+"\n",true);
			}
		}*/
		
	}
	
	public void forceDraw() {
		//Log.e("SLICK","ATTEMPTING FORCE DRAW");
		if(!_runner.threadHandler.hasMessages(SlickView.DrawRunner.MSG_DRAW)) {
			_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
		} else {
			//Log.e("SLICK","VIEW ALREADY HAS DRAW MESSAGES");
		}
	}
	
	public void setColorDebugMode(int colorDebugMode) {
		this.colorDebugMode = colorDebugMode;
	}

	public int getColorDebugMode() {
		return colorDebugMode;
	}


	public class DrawRunner extends Thread {
		private SurfaceHolder _surfaceHolder;
		private SlickView _sv;
		private boolean running = false;
		private boolean paused = false;
		private Boolean lock = null;
		
		public static final int MSG_DRAW = 100;
		public static final int MSG_SHUTDOWN = 101;
		
		private Handler threadHandler = null;
		
		public DrawRunner(SurfaceHolder parent,SlickView view,Boolean drawlock) {
			_surfaceHolder = parent;
			_sv = view;
			lock = drawlock;
		}
		
		
		
		public void dcbPriority(int val) {
			Process.setThreadPriority(val);
		}
		
		public void setRunning(boolean val) {
			//Log.e("SLICK","THREAD ATTEMPTED TO SET THE RUNNING VALUE");
			running = val;
		}
		
		public void setPause() {
			paused = true;
		}
		
		public void setResume() {
			paused = false;
		}
		
		@Override
		public void run() {
			Looper.prepare();
			
			threadHandler = new Handler() {
				public void handleMessage(Message msg) {
					switch(msg.what) {
					case MSG_DRAW:
						Canvas c = null;
						try{
							c = _surfaceHolder.lockCanvas(null);
							synchronized(_surfaceHolder) {
								_sv.onDraw(c);
								_surfaceHolder.notify();
								//Log.e("DRAW","DRAWING THE SCREEEEEEN!!!!");
							}
						} finally { 
							if(c != null) {
								_surfaceHolder.unlockCanvasAndPost(c);
							}
						}
						if(Math.abs(fling_velocity) > 0.0f) {
							this.sendEmptyMessageDelayed(MSG_DRAW, 3); //throttle myself, just a little bit.
						}
						break;
					case MSG_SHUTDOWN:
						this.getLooper().quit();
						break;
					default:
						break;
					}
				}
			};
			threadHandler.sendEmptyMessage(MSG_DRAW); //just to get us started.
			//threadHandler.getLooper();
			Looper.loop();
			
			//Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
			/*Canvas c;
			//Log.e("SLICK","VIEW THREAD RUNNING");
			while(running) {
				//if(!paused) {
					c = null;
					
					synchronized(drawn) {
						while(drawn && fling_velocity == 0.0f) {
							try {
								//Log.e("SLICK","ALREADY DRAWN VIEW, WAITING FOR EVENT TO RESUME!");
								drawn.wait();
								//Log.e("SLICK","WOKE FROM METHOD CALLING drawn.notify()");
							} catch (InterruptedException e) {
								Log.e("SLICK","Got interrupted while waiting for draw event.");
								e.printStackTrace();
								
							}
						}
						drawn = true;
						synchronized(isdrawn) {
							isdrawn = false;
						}
					}
						try{
							c = _surfaceHolder.lockCanvas(null);
							synchronized(_surfaceHolder) {
								_sv.onDraw(c);
								_surfaceHolder.notify();
							}
						} finally { 
							if(c != null) {
								_surfaceHolder.unlockCanvasAndPost(c);
							}
						}
					
					//synchronized(drawn) {
						//drawn.notify();
						//drawn = true;
					//}
					synchronized(isdrawn) {
						isdrawn.notify();
						isdrawn = true;
					}
			}*/
		}
		
	}

}
