package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.so2.walkmeapp.POJO.TrainingPOJO;
import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.JacksonUtils;
import com.project.so2.walkmeapp.core.PausableChronometer;
import com.wnafee.vector.MorphButton;
import com.wnafee.vector.compat.AnimatedVectorDrawable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.codehaus.jackson.map.ObjectMapper;
import java.io.IOException;
import java.util.Date;

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
   private SensorEventListener sensorEventListener;
   private MorphButton playButton;
   private RelativeLayout runContainer;
   private RelativeLayout stopContainer;
   private int colorGreen;
   private int colorGrey;
   private PausableChronometer chronometer;
   private ObjectMapper mapper;


   private boolean isInitialValueSet = false;
   private boolean isPaused = true;
   private boolean isStopped = true;

   private final String CHRONO_FORMAT =  "H:MM:SS";
   private long startTime = -1000;
   private TrainingPOJO trainingData;
   private Context context;


   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      //Binding Class to its View
      setContentView(R.layout.training_main);

      //Binding Strings to their View
      //mMainTrainingElements = getResources().getStringArray(R.array.main_training_list_items);

      actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      actionBarText = (TextView) findViewById(R.id.action_bar_title);
      playButton = (MorphButton) findViewById(R.id.playPauseBtn);
      itemTest = (TextView) findViewById(R.id.item_text2Left);
      runContainer = (RelativeLayout) findViewById(R.id.training_run_container);
      stopContainer = (RelativeLayout) findViewById(R.id.training_stop_container);
      chronometer = (PausableChronometer) findViewById(R.id.digital_clock);

      setupActionbar();
     // setupChronometer();     //potentially useless
      setupPedometerService();
      Calendar c = Calendar.getInstance();
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String formattedDate = df.format(c.getTime());
      trainingData=new TrainingPOJO(1,formattedDate,10,30,2,20,2,24,10,2,4);
      File file = new File(Environment.DIRECTORY_DOWNLOADS, "training");
      mapper =JacksonUtils.mapper;
      try {
         mapper.writeValue(file,trainingData);
      } catch (IOException e) {
         e.printStackTrace();
      }


      colorGreen =Color.parseColor(getResources().getString(R.string.training_green));
      colorGrey =Color.parseColor(getResources().getString(R.string.training_grey));

      final AnimatedVectorDrawable startDrawable = AnimatedVectorDrawable.getDrawable(playButton.getContext(), R.drawable.ic_play_to_pause);
      AnimatedVectorDrawable endDrawable = AnimatedVectorDrawable.getDrawable(playButton.getContext(), R.drawable.ic_pause_to_play);      //STRANGE FIX TO ANDROID 6 BUG

      playButton.setStartDrawable(startDrawable);
      playButton.setEndDrawable(endDrawable);


      playButton.setOnClickListener(new View.OnClickListener() {           //TODO: find out why the play/pause animation occours even on long touch //bug is gone? wtf
         @Override
         public void onClick(View v) {

            if (isStopped != false) {
               isStopped = false;
            }


            if (isPaused == true) {
               // TIME OF START TRAINING
               if (startTime == -1000) {
                  startTime = System.currentTimeMillis();
               }

               chronometer.start();
               fadeTo(runContainer, colorGrey, colorGreen);
               fadeTo(stopContainer, colorGreen, colorGrey);

            } else {

               chronometer.stop();
               fadeTo(runContainer, colorGreen, colorGrey);
               fadeTo(stopContainer, colorGrey, colorGreen);


            }

            updateIsPaused();


         }
      });

      playButton.setOnLongClickListener(new View.OnLongClickListener() {
         @Override
         public boolean onLongClick(View v) {
            isStopped = true;
            if ( isPaused == false ) {
               playButton.animate();   //TODO: try to trigger the play animation, there gotta be a way goddammit
               fadeTo(runContainer, colorGreen, colorGrey);
               fadeTo(stopContainer, colorGrey, colorGreen);
               isPaused = true;
            }
            chronometer.reset();
            Toast.makeText(Training.this, "RESET", Toast.LENGTH_SHORT).show();
            return true;
         }
      });
   }

/*   private void setupChronometer() {
      chronometer.setOnChronometerTickListener(new PausableChronometer.OnChronometerTickListener() {
         public void onChronometerTick(Chronometer cArg) {
            long t =  SystemClock.elapsedRealtime() - cArg.getBase();
            cArg.setText(DateFormat.format("kk:mm:ss", t));
         }
      });
   }*/


   private void fadeTo(RelativeLayout layout, int actualColor, int color) {
      ColorDrawable[] colour = {new ColorDrawable(actualColor), new ColorDrawable(color)};

      TransitionDrawable trans = new TransitionDrawable(colour);
      layout.setBackground(trans);
      trans.startTransition(200);
   }

   private void updateIsPaused() {

      if (isPaused != true) {
         isPaused = true;
      } else {
         isPaused = false;
      }

   }


   public void setupPedometerService() {

      sensorEventListener = new SensorEventListener() {

         @Override
         public void onAccuracyChanged(Sensor sensor, int accuracy) {
         }

         @Override
         public void onSensorChanged(SensorEvent event) {

            float[] steps = event.values;

            if (isInitialValueSet != true) {          //TODO: FIX THIS MESS
               isInitialValueSet = true;
               initialValue = steps[0];
            }

            if ((System.currentTimeMillis()-startTime) < (60*1000) ) {
               itemTest.setText("--");
               Log.d("SBU", "Non ancora!");
            } else {
               itemTest.setText(Integer.toString((int) ( ( (float)(steps[0] - initialValue) ) / ( ( (float)(System.currentTimeMillis()-startTime) ) /60 ) ) ) );
            }
         }
      };

      sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      sensor = sensorService.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

      if (sensor != null) {
         sensorService.registerListener(sensorEventListener, sensor,
                 SensorManager.SENSOR_DELAY_FASTEST);
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

