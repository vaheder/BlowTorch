package com.happygoatstudios.aardwolf;

import com.offsetnull.bt.R;
import com.offsetnull.bt.settings.ConfigurationLoader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class AardLauncher extends AppCompatActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_launcher_layout);
        
        String windowAction = ConfigurationLoader.getConfigurationValue("windowAction", this);
        
        Intent launch = new Intent(windowAction);
        launch.putExtra("DISPLAY","Aardwolf RPG");
        launch.putExtra("HOST", "aardmud.org");
        launch.putExtra("PORT", "7777");
        
        SharedPreferences prefs = this.getSharedPreferences("SERVICE_INFO",0);
    	Editor edit = prefs.edit();
    	
    	
    	edit.putString("SETTINGS_PATH", "Aardwolf RPG");
    	edit.commit();
        
        this.startActivity(launch);
        
        this.finish();
    }
}