package com.happygoatstudios.bt.crashreport;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.text.format.Time;

public class CrashReporter implements Thread.UncaughtExceptionHandler {

	private Thread.UncaughtExceptionHandler defaultUEH;
	//private Context app = null;
	
	public CrashReporter(Context crash_watch) {
		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		//app = crash_watch;
	}
	
	public void uncaughtException(Thread t, Throwable e) {
		//do stuff here
		//Log.e("TESTUEH","CUSTOM UEH HANDLER AWAY!");
		
		//build the report
		final Writer result = new StringWriter();
		
		final PrintWriter printWriter = new PrintWriter(result);
		
		e.printStackTrace(printWriter);
		
		String reportmeat = result.toString();
		printWriter.close();

		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		HttpPost httpPost = new HttpPost("http://bt.happygoatstudios.com/upload_crash_report.php");
				
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		
		//versionname
		//packagename
		//phonemodel
		//phoneversion
		//androidversion
		
		String versionName = android.os.Build.BRAND;
		String phoneModel = android.os.Build.MODEL;
		String phoneVersion = android.os.Build.DEVICE;
		String androidVersion = android.os.Build.VERSION.RELEASE;
		
		//random
		Random generator = new Random();
		generator.setSeed(java.lang.System.currentTimeMillis());
		int random = generator.nextInt(99999);
		
		String formatted_random = String.format("%05d", random);
		
		Time cur_time = new Time();
		cur_time.set(java.lang.System.currentTimeMillis());
		
		String filename = phoneModel + "_" + phoneVersion + "_" + versionName + "_ANDVER" + androidVersion + "_" + formatted_random + "_"+cur_time.hour+"h_"+cur_time.minute+"m_"+".CRASH";
		
		pairs.add(new BasicNameValuePair("filename",filename));
		pairs.add(new BasicNameValuePair("crashreport",reportmeat));
		
		try{
			httpPost.setEntity(new UrlEncodedFormEntity(pairs,HTTP.UTF_8));
			
			httpClient.execute(httpPost);
			//Log.e("REPORTER","SENDING REPORT! file:"+filename+" mess:" +reportmeat);
		} catch (IOException error) {
			error.printStackTrace();
		}
		
		e.printStackTrace();
		
		defaultUEH.uncaughtException(t, e);
		
	}
	
}


