package com.happygoatstudios.bt.window;

import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;
import android.webkit.WebSettings.TextSize;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.happygoatstudios.bt.R;
//import com.happygoatstudios.bt.service.BaardTERMService;
import com.happygoatstudios.bt.service.*;

public class BaardTERMWindow extends Activity {
	
	//public static final String PREFS_NAME = "CONDIALOG_SETTINGS";
	public String PREFS_NAME;
	protected static final int MESSAGE_HTMLINC = 110;
	protected static final int MESSAGE_RAWINC = 111;
	protected static final int MESSAGE_BUFFINC = 112;
	protected static final int MESSAGE_PROCESS = 102;
	protected static final int MESSAGE_PROCESSED = 104;
	protected static final int MESSAGE_SENDDATAOUT = 105;
	
	
	String host;
	int port;
	
	Handler myhandler = null;
	boolean servicestarted = false;
	
	IBaardTERMService service = null;
	//Object ctrl_tag = new Object();
	Processor the_processor = null;
	

	
	GestureDetector gestureDetector = null;
	OnTouchListener gestureListener = null;
	
	
	//ScrollView screen2 = null;
	SlickView screen2 = null;
	
	EditText input_box = null;
	CommandKeeper history = null;
	

	ImageButton test_button = null;
	
	ImageButton up_button_c = null;
	ImageButton down_button_c = null;
	ImageButton enter_button_c  = null;
	boolean input_controls_expanded = false;
	
	boolean isBound = false;
	
	
	Boolean settingsLoaded = false; //synchronize or try to mitigate failures of writing button data, or failures to read data
	Boolean serviceConnected = false;
	Boolean isResumed = false;
	
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			service = IBaardTERMService.Stub.asInterface(arg1); //turn the binder into something useful
			
			//register callback
			try {
				service.registerCallback(the_callback);
				
			} catch (RemoteException e) {
				//do nothing here, as there isn't much we can do
			}
			synchronized(serviceConnected) {
				Log.e("WINDOW","SERVICE CONNECTED, SENDING NOTIFICATION");
				serviceConnected.notify();
				serviceConnected = true;
			}
			finishInitializiation();
			
		}

		public void onServiceDisconnected(ComponentName arg0) {
			try {
				//Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
				service.unregisterCallback(the_callback);
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
	
	public boolean finishStart = true;
	
	String html_buffer = new String();
	
	//public void onResized
	/*public void onSizeChanged(int w, int h,int oldw,int oldh) {
		Log.e("WINDOW","WINDOW RESIZED!!!!");
	}
	
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Log.e("WINDOW","RE-LAYOUT!!!!!");
	}*/
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.e("WINDOW","onCreate()");
		
		setContentView(R.layout.window_layout);
		
		String display_name = this.getIntent().getStringExtra("DISPLAY");
        Pattern nowhitespace = Pattern.compile("\\s");
        Matcher m = nowhitespace.matcher(display_name + ".PREFS");
        PREFS_NAME = m.replaceAll(""); //kill off all white space in the display name, use it as the preference file
        history = new CommandKeeper(10);
        
        screen2 = (SlickView)findViewById(R.id.slickview);
        RelativeLayout l = (RelativeLayout)findViewById(R.id.slickholder);
        screen2.setParentLayout(l);
        EditText fill2 = (EditText)findViewById(R.id.filler2);
        //View fill2 = findViewById(R.id.filler2);
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
		
        input_box = (EditText)findViewById(R.id.textinput);
        
        input_box.setOnKeyListener(new TextView.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
					
					String cmd = history.getNext();
					input_box.setText(cmd);
					return true;
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_UP) {
					String cmd = history.getPrev();
					input_box.setText(cmd);
					return true;
				}
				return false;
			}
   
        });
        
        
        input_box.setVisibility(View.VISIBLE);
        input_box.setEnabled(true);
        EditText filler = (EditText)findViewById(R.id.filler);
        filler.setFocusable(false);
        filler.setClickable(false);
        
        
        input_box.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        //input_box.setOnKeyListener(new EditText.OnKeyListener() {

        
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		//public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				//post message to conneciton thread
				//only do something if enter was pressed
				//InputConnection ic =null;
				//ic.
			
				//LOG.e("WINDOW","EDITOR ACTION" + actionId);
			
				if(event == null)  {
					//return false;
					//LOG.e("WINDOW","EDITOR EVENT HAD NULL EVENT");
				}
				
				if(actionId == EditorInfo.IME_ACTION_SEND) {

				//	return false;
					event = new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER);
					
				}
				
				if((event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)) {
					//Message tmp = ConnectionHandler.obtainMessage(105);
					String data = input_box.getText().toString();
					history.addCommand(data);
					Character cr = new Character((char)13);
					Character lf = new Character((char)10);
					String crlf = cr.toString() + lf.toString();
					String nosemidata = data.replace(";", crlf);
					nosemidata = nosemidata.concat(crlf);
					ByteBuffer buf = ByteBuffer.allocate(nosemidata.length());
		
					
					try {
						buf.put(nosemidata.getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						
						e.printStackTrace();
					}
				
					buf.rewind();
				
					byte[] buffbytes = buf.array();
					//Bundle bundle = new Bundle();
					//bundle.putByteArray("DATA", buffbytes);
					//tmp.setData(bundle);
					//ConnectionHandler.sendMessage(tmp);
					try {
						service.sendData(buffbytes);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					//send the box to the window, with newline because we are cool.
					//but delete that pesky carriage return, yuck
					data.replace(cr.toString(), "\n");
					data = data.concat("\n");
					screen2.addText(data,false);
					screen2.jumpToZero();
				
					input_box.setText("");
					if(actionId == EditorInfo.IME_ACTION_DONE) {

						//	return false;
							return true;
							
					} else { return true; }
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
					String cmd = history.getNext();
					input_box.setText(cmd);
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
				switch(msg.what) {
				case MESSAGE_RAWINC:
					//raw data incoming
					screen2.addText(msg.getData().getString("RAW"),false);
					break;
				case MESSAGE_BUFFINC:
					screen2.addText(msg.getData().getString("RAW"), true);
					break;
				case MESSAGE_SENDDATAOUT:
					byte[] data = msg.getData().getByteArray("DATA");
					try {
						String nosemidata = null;
						try {
							nosemidata = new String(data,"ISO-8859-1");
						} catch (UnsupportedEncodingException e) {
							
							e.printStackTrace();
						}
						Character cr = new Character((char)13);
						Character lf = new Character((char)10);
						String crlf = cr.toString() + lf.toString();
						nosemidata = nosemidata.replace(";", crlf);
						try {
							service.sendData(nosemidata.getBytes("ISO-8859-1"));
						} catch (UnsupportedEncodingException e) {
							
							e.printStackTrace();
						}
					} catch (RemoteException e) {
						
						e.printStackTrace();
					}
					screen2.addText(new String(msg.getData().getByteArray("DATA")),false);
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
						String cmd = history.getNext();
						input_box.setText(cmd);
					}
				});
				
				
				dc.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View arg0) {
						String cmd = history.getPrev();
						input_box.setText(cmd);
					}
				});
				
				ec.setOnClickListener(new View.OnClickListener() {

					public void onClick(View arg0) {
						String data = input_box.getText().toString();
						history.addCommand(data);
						Character cr = new Character((char)13);
						Character lf = new Character((char)10);
						String crlf = cr.toString() + lf.toString();
						String nosemidata = data.replace(";", crlf);
						nosemidata = nosemidata.concat(crlf);
						ByteBuffer buf = ByteBuffer.allocate(nosemidata.length());
			
						
						try {
							buf.put(nosemidata.getBytes("UTF-8"));
						} catch (UnsupportedEncodingException e) {
							
							e.printStackTrace();
						}
					
						buf.rewind();
					
						byte[] buffbytes = buf.array();
						try {
							service.sendData(buffbytes);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						
						//send the box to the window, with newline because we are cool.
						//but delete that pesky carriage return, yuck
						data.replace(cr.toString(), "\n");
						data = data.concat("\n");
						screen2.addText(data,false);
						screen2.jumpToZero();
					
						input_box.setText("");
					}
					
				});
				

				Animation a = new TranslateAnimation(-178,0,0,0);
				a.setDuration(120);
				AnimationSet set = new AnimationSet(true);
				set.addAnimation(a);
				
				LayoutAnimationController lac = new LayoutAnimationController(set,0.01f);
				
				rl.setLayoutAnimation(lac);
				
				if(input_controls_expanded) {
					//switch the image resource
					Animation outanim = new TranslateAnimation(178,0,0,0);
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
		screen2.setInputType(input_box);
		input_box.bringToFront();
		//icicile is out, prefs are in
		
		synchronized(settingsLoaded) {
		SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME,0);
		
		servicestarted = prefs.getBoolean("CONNECTED",false);
		finishStart = prefs.getBoolean("FINISHSTART", true);
		
		
		
		int count = prefs.getInt("BUTTONCOUNT", 0);
		for(int i = 0;i<count;i++) {
			//get button string
			String data = prefs.getString("BUTTON"+i, "");

			Message msg = screen2.buttonaddhandler.obtainMessage(103, data);
			screen2.buttonaddhandler.sendMessage(msg);
			
			
		}
		
		settingsLoaded.notify();
		settingsLoaded = true;
		} /*END SYNCHRONIZATION!*/
		//get the saved buffer from the prefs
		if(icicle != null) {
			CharSequence seq = icicle.getCharSequence("BUFFER");
			if(seq != null) {
				//the_buffer = new SpannableStringBuilder();
				screen2.setBuffer((new StringBuffer(seq).toString()));
			} else {
				//the_buffer = new SpannableStringBuilder();
				
			}
		} else {
			//the_buffer = new StringBuffer();
		}
		
		if(!servicestarted) {
			//start the service
			this.startService(new Intent(com.happygoatstudios.bt.service.IBaardTERMService.class.getName()));
			servicestarted = true;
		}
		
		
		//give it some time to launch
		synchronized(this) {
			try {
				this.wait(5);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0,100,0,"Triggers");
		menu.add(0,101,0,"Options");
		menu.add(0,102,0,"Slick Buttons");
		
		return true;
		
	}
	
	public void onBackPressed() {
		//Log.e("WINDOW","BACK PRESSED TRAPPED");
		
		//show dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(BaardTERMWindow.this);
		builder.setMessage("Keep service running in background?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                BaardTERMWindow.this.dirtyExit();
		                BaardTERMWindow.this.finish();
		           }
		       });
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //dialog.cancel();
		        	   BaardTERMWindow.this.cleanExit();
		        	   BaardTERMWindow.this.finish();
		           }
		       });
		//AlertDialog alert = builder.create();
		builder.create();
		builder.show();
		//alert.show();
		
		//super.onBackPressed();
	}
	
	public void cleanExit() {
		//we want to kill the service when we go.
		
		//shut down the service
		if(isBound) {
			try {
				//Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
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
		
		
		finishStart = true;
		servicestarted = false;
		
		saveSettings();
		
		stopService(new Intent(com.happygoatstudios.bt.service.IBaardTERMService.class.getName()));
		
	}
	
	public void dirtyExit() {
		//we dont want to kill the service
		if(isBound) {
			
			try {
				//Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
				SlickView sv = (SlickView)findViewById(R.id.slickview);
				service.saveBuffer(sv.getBuffer());
				service.unregisterCallback(the_callback);
			} catch (RemoteException e) {
				//e.printStackTrace();
			}
			
			unbindService(mConnection);
			//Log.e("WINDOW","Unbound connection at cleanExit");
			isBound = false;
		}
		
		//save settings
		finishStart = false;
		servicestarted = true;
		//this.onPause();
		saveSettings();
	}
	
	public void onSaveInstanceState(Bundle data) {
		SlickView sv = (SlickView)findViewById(R.id.slickview);
		data.putCharSequence("BUFFER", sv.getBuffer());
	}
	
	public void onRestoreInstanceState(Bundle data) {

		SlickView sv = (SlickView)findViewById(R.id.slickview);
		sv.setBuffer((new StringBuffer(data.getCharSequence("BUFFER")).toString()));


	}
	
	public void saveSettings() {
		//shared preferences
		synchronized(settingsLoaded) {
			while(settingsLoaded == false) {
				try {
					settingsLoaded.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		
		SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME,0);
		
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
			String str = new String(b.getData().x +"||" +b.getData().y + "||"+b.getData().the_text+"||"+b.getData().the_label);
			serialbuttons.append(str + "&&");
			
			
			bstrings.add(str);
		}
		
		editor.putInt("BUTTONCOUNT", buttoncount);
		if(buttoncount == 0) {
			Log.e("WINDOW","SAVED BUTTONCOUNT OF ZERO!!!");
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
		}
		
	}
	
	public void onStart() {
		Log.e("WINDOW","onStart()");
		super.onStart();
	}
	

	
	public void onPause() {
		Log.e("WINDOW","onPause()");
		
		if(isBound) {
			
			try {
				Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
				if(service != null) {
					service.unregisterCallback(the_callback);
					
					unbindService(mConnection);
					
					saveSettings();
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
		Log.e("WINDOW","onStop()");
		super.onStop();
	}
	
	public void onDestroy() {
		Log.e("WINDOW","onDestroy()");
		super.onDestroy();
	}
	
	public void onResume() {
		
		

		Log.e("WINDOW","onResume()");
		
		SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME,0);
		servicestarted = prefs.getBoolean("CONNECTED", false);
		finishStart = prefs.getBoolean("FINISHSTART", true);
		
		
		String str = "Load settings (onResume): ";
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
		
		//Log.e("WINDOW",str);
		
		
		if(!isBound) {
			bindService(new Intent(com.happygoatstudios.bt.service.IBaardTERMService.class.getName()), mConnection, 0); //do not auto create
			Log.e("WINDOW","Bound connection at onResume");
			isBound = true;
			
			
			isResumed = true;

		}
	
		super.onResume();
	}
	
	public void onDestroy(Bundle saveInstance) {
		Log.e("WINDOW","onDestroy()");
		super.onDestroy();
	}
	
	private void finishInitializiation() {
		Intent myintent = this.getIntent();
		
		if(!finishStart) {
			
			try {
				service.requestBuffer();
			} catch (RemoteException e) {
				
			}
			return;
		} else {
			
		}
		
		finishStart = false;
		
		String host = myintent.getStringExtra("HOST");
		String port = myintent.getStringExtra("PORT");
		
		//for now we are going to override:
		//host = "aardmud.org";
		//port = "4010";
		
		
		while(service == null) {
			host = "aardmud.org"; //waste time till onServiceConnected is called.
		}
		
		
		try {
			service.setConnectionData(host, new Integer(port).intValue());
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
	
	public void flingLeft() {
    	FixedViewFlipper tmp = (FixedViewFlipper)findViewById(R.id.flippdipper);
    	tmp.setInAnimation(AnimationUtils.loadAnimation(tmp.getContext(), R.anim.slide_left_in));
    	tmp.setOutAnimation(AnimationUtils.loadAnimation(tmp.getContext(), R.anim.slide_left_out));
    	tmp.showPrevious();
	}
	    
	public void flingRight() {
    	FixedViewFlipper tmp = (FixedViewFlipper)findViewById(R.id.flippdipper);
    	tmp.setOutAnimation(AnimationUtils.loadAnimation(tmp.getContext(), R.anim.slide_right_out));
    	tmp.setInAnimation(AnimationUtils.loadAnimation(tmp.getContext(), R.anim.slide_right_in));
    	tmp.showNext();
	}
	
	
	private IBaardTERMServiceCallback.Stub the_callback = new IBaardTERMServiceCallback.Stub() {

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

		public void rawDataIncoming(String raw) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_RAWINC);
			Bundle b = new Bundle();
			b.putString("RAW",raw);
			msg.setData(b);
			myhandler.sendMessage(msg);
			
		}
		
		public void rawBufferIncoming(String rawbuf) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_BUFFINC);
			Bundle b = msg.getData();
			b.putString("RAW", rawbuf);
			msg.setData(b);
			myhandler.sendMessage(msg);
		}
	};

}
