package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.so2.walkmeapp.R;

/**
 * Created by andream16 on 1/27/16.
 */
public class Settings extends Activity {
   private static final String PREFS_NAME = "SETTINGS_PREFS";
   private int stepLengthInCm;
   private int avgStepInM;
   private int lastMetersInM;
   private EditText stepLengthET;
   private EditText avgStepET;
   private EditText lastMetersET;
   private SharedPreferences settings;
   private ImageView actionBar;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.settings_layout);

      //Get the needed Views
      actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      stepLengthET = (EditText) findViewById(R.id.settings_step_length_edit);
      avgStepET = (EditText) findViewById(R.id.settings_average_step_edit);
      lastMetersET = (EditText) findViewById(R.id.settings_last_x_meters_edit);

      //ActionBar customization
      actionBar.setImageResource(R.drawable.btn_back);
      actionBar.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            finish();
         }
      });
      TextView actionBarText = (TextView) findViewById(R.id.action_bar_title);
      actionBarText.setText(getResources().getString(R.string.settings_title));

      // Get Preferences and restore them
      settings = getSharedPreferences(PREFS_NAME, 0);
      stepLengthInCm = checkAndSet("stepLength", stepLengthET);
      avgStepInM = checkAndSet("avgStepInM", avgStepET);
      lastMetersInM = checkAndSet("lastXMeters", lastMetersET);


   }


   //This handy function checks if the chosen pref from the Preferences exists.
   //If exists, returns the value and sets it to the specified View, else returns 0.
   private int checkAndSet(String prefsKey, EditText correspondingET) {

      if (settings.contains(prefsKey)) {
         int auxInt = settings.getInt(prefsKey, 0);
         correspondingET.setText(Integer.toString(auxInt));
         return auxInt;
      } else {
         return 0;
      }

   }


   @Override
   protected void onPause() {
      super.onPause();

      //Get the Preferences editor
      SharedPreferences.Editor editor = settings.edit();

      //If possible, get the value from the EditText and put into SharedPrefs
      if (stepLengthET.getText() != null && !(stepLengthET.getText().toString().equals(""))) {
         editor.putInt("stepLength", Integer.parseInt(stepLengthET.getText().toString()));
      }

      if (avgStepET.getText() != null && !(avgStepET.getText().toString().equals(""))) {
         editor.putInt("avgStepInM", Integer.parseInt(avgStepET.getText().toString()));
      }

      if (lastMetersET.getText() != null && !(lastMetersET.getText().toString().equals(""))) {
         editor.putInt("lastXMeters", Integer.parseInt(lastMetersET.getText().toString()));
      }

      editor.commit();
   }
}

