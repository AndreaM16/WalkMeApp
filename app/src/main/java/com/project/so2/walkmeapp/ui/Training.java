package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.so2.walkmeapp.R;

import java.io.Console;

/**
 * Created by Andrea on 24/01/2016.
 */
public class Training extends Activity{

   private static final String TAG = "Training";
   SensorManager sMgr;
   Sensor steps;
   private SensorManager sensorService;
   private Sensor sensor;
   private TextView itemTest;
   private boolean isInitialValueSet = false;
   private float initialValue;
   private ImageView actionBar;
   private TextView actionBarText;
   private SensorEventListener mySensorEventListener;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      //Binding Class to its View
      setContentView(R.layout.training_main);

      //Binding Strings to their View
      //mMainTrainingElements = getResources().getStringArray(R.array.main_training_list_items);

      actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      actionBarText = (TextView) findViewById(R.id.action_bar_title);

      setupActionbar();

      setupPedometer();


      itemTest = (TextView) findViewById(R.id.item_text2Left);

      sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      sensor = sensorService.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

      if (sensor != null) {
         sensorService.registerListener(mySensorEventListener, sensor,
               SensorManager.SENSOR_DELAY_FASTEST);
         Log.i("Compass MainActivity", "Registerered for STEP Sensor");
      } else {
         Log.e("Compass MainActivity", "Registerered for STEP Sensor");
         Toast.makeText(this, "STEP Sensor not found",
               Toast.LENGTH_LONG).show();
      }
   }




   public void setupPedometer() {
      mySensorEventListener =new SensorEventListener() {

         @Override
         public void onAccuracyChanged(Sensor sensor, int accuracy) {
         }

         @Override
         public void onSensorChanged(SensorEvent event) {
            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West
            float[] steps = event.values;

            if (isInitialValueSet != true) {
               isInitialValueSet = true;
               initialValue = steps[0];
            }

            itemTest.setText(Integer.toString((int)(steps[0]-initialValue)));
            Log.d(TAG, "Steps value = " + (steps[0]-initialValue));

         }
      };

   }



   private void setupActionbar() {
      actionBar.setImageResource(R.drawable.btn_back);
      actionBar.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            finish();
         }
      });

      actionBarText.setText(getResources().getString(R.string.main_training_title));
   }

}

