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

   //private String[] mSettingsElements;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      //Binding Class to its View
      setContentView(R.layout.settings_layout);

      //Binding Strings to their View
      //mSettingsElements = getResources().getStringArray(R.array.settings_list_items);

      ImageView actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      stepLengthET = (EditText) findViewById(R.id.settings_step_length_edit);
      avgStepET = (EditText) findViewById(R.id.settings_average_step_edit);
      lastMetersET = (EditText) findViewById(R.id.settings_last_x_meters_edit);


      actionBar.setImageResource(R.drawable.btn_back);
      actionBar.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            finish();
         }
      });

      //Action Bar Title
      TextView actionBarText = (TextView) findViewById(R.id.action_bar_title);
      actionBarText.setText(getResources().getString(R.string.settings_title));

      // Restore preferences
      settings = getSharedPreferences(PREFS_NAME, 0);

      stepLengthInCm = checkAndSet("stepLength", stepLengthET);
      avgStepInM = checkAndSet("avgStepInM", avgStepET);
      lastMetersInM = checkAndSet("lastXMeters", lastMetersET);


   }

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

      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();

      editor.putInt("stepLength", Integer.parseInt(stepLengthET.getText().toString()));
      editor.putInt("avgStepInM", Integer.parseInt(avgStepET.getText().toString()));
      editor.putInt("lastXMeters", Integer.parseInt(lastMetersET.getText().toString()));

      editor.commit();
   }
}

