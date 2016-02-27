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
 * Class used to handle all application's settings
 * stepLengthInCm used to set user's step length in order to calculate the different metrics
 * avgStepPerMin used to set user's average steps preferences
 * lastMetersInM used to set user's last meters preferences
 * SharedPreferences used to share these resources within the application
 */
public class Settings extends Activity {
   private static final String PREFS_NAME = "SETTINGS_PREFS";
   private int stepLengthInCm;
   private int avgStepPerMin;
   private int lastMetersInM;
   private EditText stepLengthET;
   private EditText avgStepET;
   private EditText lastMetersET;
   private SharedPreferences settings;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      /* Binding Class to its View */
      setContentView(R.layout.settings_layout);

      ImageView actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      stepLengthET = (EditText) findViewById(R.id.settings_step_length_edit);
      avgStepET = (EditText) findViewById(R.id.settings_average_step_edit);
      lastMetersET = (EditText) findViewById(R.id.settings_last_x_meters_edit);

      actionBar.setImageResource(R.drawable.btn_back);
      actionBar.setOnClickListener(new View.OnClickListener() {
         /**
          * @param v View
          */
         @Override
         public void onClick(View v) {
            finish();
         }
      });

      /* Action Bar Title */
      TextView actionBarText = (TextView) findViewById(R.id.action_bar_title);
      actionBarText.setText(getResources().getString(R.string.settings_title));

      /* Restore preferences */
      settings = getSharedPreferences(PREFS_NAME, 0);

      stepLengthInCm = checkAndSet("stepLength", stepLengthET);
      avgStepPerMin = checkAndSet("avgStepPerMin", avgStepET);
      lastMetersInM = checkAndSet("lastXMeters", lastMetersET);

   }

   /* Checks if preferences' input value has been set and if true sets it */
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

      if (stepLengthET.getText() != null && !(stepLengthET.getText().toString().equals(""))) {
         editor.putInt("stepLength", Integer.parseInt(stepLengthET.getText().toString()));
      }

      if (avgStepET.getText() != null && !(avgStepET.getText().toString().equals(""))) {
         editor.putInt("avgStepPerMin", Integer.parseInt(avgStepET.getText().toString()));
      }

      if (lastMetersET.getText() != null && !(lastMetersET.getText().toString().equals(""))) {
         editor.putInt("lastXMeters", Integer.parseInt(lastMetersET.getText().toString()));
      }

      editor.commit();

   }
}

