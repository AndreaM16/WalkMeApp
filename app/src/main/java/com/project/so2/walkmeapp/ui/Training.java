package com.project.so2.walkmeapp.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.so2.walkmeapp.R;
import com.wnafee.vector.MorphButton;

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
   private float initialValue;
   private ImageView actionBar;
   private TextView actionBarText;
   private SensorEventListener mySensorEventListener;
   private MorphButton playPauseButton;

   private boolean isInitialValueSet = false;
   private boolean isWalking = false;
   private RelativeLayout runContainer;
   private RelativeLayout stopContainer;


   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      //Binding Class to its View
      setContentView(R.layout.training_main);

      //Binding Strings to their View
      //mMainTrainingElements = getResources().getStringArray(R.array.main_training_list_items);

      actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      actionBarText = (TextView) findViewById(R.id.action_bar_title);
      playPauseButton = (MorphButton) findViewById(R.id.playPauseBtn);
      itemTest = (TextView) findViewById(R.id.item_text2Left);
      runContainer = (RelativeLayout) findViewById(R.id.training_run_container);
      stopContainer = (RelativeLayout) findViewById(R.id.training_stop_container);

      playPauseButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            updateStatus();
            if (isWalking == true) {
               fadeTo(runContainer, Color.parseColor(getResources().getString(R.string.training_green)), Color.parseColor(getResources().getString(R.string.training_grey)));
               fadeTo(stopContainer, Color.parseColor(getResources().getString(R.string.training_grey)), Color.parseColor(getResources().getString(R.string.training_green)));

            } else {
               fadeTo(runContainer, Color.parseColor(getResources().getString(R.string.training_grey)), Color.parseColor(getResources().getString(R.string.training_green)));
               fadeTo(stopContainer, Color.parseColor(getResources().getString(R.string.training_green)), Color.parseColor(getResources().getString(R.string.training_grey)));

            }
         }
      });




      setupActionbar();

      setupPedometer();




   }

   private void fadeTo(RelativeLayout layout, int actualColor, int color) {
      ColorDrawable[] colour = {new ColorDrawable(actualColor), new ColorDrawable(color)};

      TransitionDrawable trans = new TransitionDrawable(colour);
      layout.setBackgroundDrawable(trans);
      trans.startTransition(200);
   }

   private void updateStatus() {

      if (isWalking != true) {
         isWalking = true;
      } else {
         isWalking = false;
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

