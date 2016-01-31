package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;
import com.project.so2.walkmeapp.core.ORM.DatabaseHelper;
import com.project.so2.walkmeapp.core.POJO.TrainingPOJO;
import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.JacksonUtils;
import com.project.so2.walkmeapp.core.PausableChronometer;
import com.wnafee.vector.MorphButton;
import com.wnafee.vector.compat.AnimatedVectorDrawable;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by Andrea on 24/01/2016.
 */
public class Training extends Activity {


   private static final String PREFS_NAME = "SETTINGS_PREFS";
   private int id;
   private String trainingDate;
   private int trainingSteps;
   private int trainingDuration;
   public int trainingDistance;
   private int lastMetersSettings;
   private float avgTotSpeed;
   private float avgXSpeed;
   private float avgTotSteps;
   private int avgXSteps;
   private int prefsstepLengthInCm;

   private int prefsAvgStepInM;
   private int prefsLastMetersInM;

   private SharedPreferences settings;


   private static final String TAG = "Training";
   SensorManager sMgr;
   Sensor steps;
   private SensorManager sensorService;
   private Sensor sensor;
   private TextView stepsPerMin;
   private TextView kilometersPerHour;
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
   private DatabaseHelper databaseHelper;
   private Dao<DBTrainings, String> dbDao;

   private boolean isInitialValueSet = false;
   private boolean isPaused = true;
   private boolean isStopped = true;

   private long startTime = -1000;
   private TrainingPOJO trainingData;
   private float actualSteps;
   private long actualTime;

   private static final double MINUTE_IN_MILLIS = 60000.0;
   private static final int STEP_IN_CENTIMETERS_TEST = 100;


   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.training_main);

      //Get the needed Views & some Colors
      actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      actionBarText = (TextView) findViewById(R.id.action_bar_title);
      playButton = (MorphButton) findViewById(R.id.playPauseBtn);
      stepsPerMin = (TextView) findViewById(R.id.item_text2Left);
      kilometersPerHour = (TextView) findViewById(R.id.item_text2Right);
      runContainer = (RelativeLayout) findViewById(R.id.training_run_container);
      stopContainer = (RelativeLayout) findViewById(R.id.training_stop_container);
      chronometer = (PausableChronometer) findViewById(R.id.digital_clock);
      colorGreen = Color.parseColor(getResources().getString(R.string.training_green));
      colorGrey = Color.parseColor(getResources().getString(R.string.training_grey));

      //Some specialized setups, to keep the code as clean as possible
      setupActionbar();
      setupPedometerService();
      setupDB();
      setValuesFromShared();

      //Test for ORM functionality
      testCreateTraining();

      //Test for Jackson functionality
      testJSON();

      //Deprecated fix for Play/Pause animation.
      //It appears that MarshMallow has a bug where the animation can't be reloaded from cache, so it needs this explicit declaration.
      AnimatedVectorDrawable startDrawable = AnimatedVectorDrawable.getDrawable(playButton.getContext(), R.drawable.ic_play_to_pause);
      AnimatedVectorDrawable endDrawable = AnimatedVectorDrawable.getDrawable(playButton.getContext(), R.drawable.ic_pause_to_play);
      playButton.setStartDrawable(startDrawable);
      playButton.setEndDrawable(endDrawable);

      //Core functionality of stopwatch/step counter in the listener
      playButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            if (isStopped != false) {
               isStopped = false;
            }

            //if the Train Session is paused, start it, else stop it
            if (isPaused == true) {

               //Save the start time of training
               //startTime is set to this number to ensure that it can be set only once per training
               if (startTime == -1000) {
                  startTime = System.currentTimeMillis();
               }

               chronometer.start();

               //Color fade animations
               fadeTo(runContainer, colorGrey, colorGreen);
               fadeTo(stopContainer, colorGreen, colorGrey);

            } else {

               chronometer.stop();

               //Color fade animations
               fadeTo(runContainer, colorGreen, colorGrey);
               fadeTo(stopContainer, colorGrey, colorGreen);


            }
            //as the name says, this updates the "isPaused" status
            updateIsPaused();

         }
      });


      //Stops the training on long click (still beta behaviour)
      playButton.setOnLongClickListener(new View.OnLongClickListener() {
         @Override
         public boolean onLongClick(View v) {
            isStopped = true;

            if (isPaused == false) {

               //TODO: BUG to fix - try to trigger the play animation programmatically
               playButton.animate();

               //Color fade animations
               fadeTo(runContainer, colorGreen, colorGrey);
               fadeTo(stopContainer, colorGrey, colorGreen);

               //set the isPaused according to status
               isPaused = true;
            }

            //Training reset
            chronometer.reset();
            stepsPerMin.setText("0");
            kilometersPerHour.setText("0");
            actualSteps = 0;
            actualTime = 0;
            isInitialValueSet = false;

            //BETA - graphical notification of training reset
            Toast.makeText(Training.this, "RESET", Toast.LENGTH_SHORT).show();

            //TEST - save example training in DB
            saveTrainingInDB(id, trainingDate, trainingSteps, trainingDuration, trainingDistance, lastMetersSettings, avgTotSpeed, avgXSpeed, avgTotSteps, avgXSteps, prefsstepLengthInCm);
            return true;
         }
      });
   }

   //This TEST function is needed to test the functionality of the Jackson library
   private void testJSON() {
      Calendar c = Calendar.getInstance();
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String formattedDate = df.format(c.getTime());
      trainingData = new TrainingPOJO(1, formattedDate, 10, 30, 2, 20, 2, 24, 10, 2, 4);

      File file = new File(Environment.DIRECTORY_DOWNLOADS, "training");
      mapper = JacksonUtils.mapper;
      try {
         mapper.writeValue(file, trainingData);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   //Sets the user preferences from SgaredPrefs
   private void setValuesFromShared() {

      settings = getSharedPreferences(PREFS_NAME, 0);

      //Check if pref exists, if so gets it
      if (settings.contains("stepLength")) {
         prefsstepLengthInCm = settings.getInt("stepLength", 100);
      }
      if (settings.contains("lastXMeters")) {
         prefsLastMetersInM = settings.getInt("lastXMeters", 10);
      }
      if (settings.contains("avgStepInM")) {
         prefsAvgStepInM = settings.getInt("avgStepInM", 1);
      }

      Log.d(TAG, "Values from prefs ->  " + "stepLength: " + prefsstepLengthInCm + "cm ||  LastXMeters: " + prefsLastMetersInM + "m ||  AvgStep: " + prefsAvgStepInM + "m");
   }

   //needed setup for ORMLite
   private void setupDB() {
      try {
         this.dbDao = getHelper().getTrainingsDao();
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

   //test values for Training
   private void testCreateTraining() {

      this.id = 2;
      this.trainingDate = "2015-12-11 18:00:23";
      this.trainingSteps = 100;
      this.trainingDuration = 500;
      this.trainingDistance = 1000;
      //this.lastMetersSettings = 30; set from real prefs, 10 is default value
      this.avgTotSpeed = 10;
      this.avgXSpeed = 4;
      this.avgTotSteps = 600;
      this.avgXSteps = 30;
      //this.prefsstepLengthInCm = 70; set from real prefs, 100 is default value
   }

   //test the ORMLite saving capabilities
   private void saveTrainingInDB(int id, String trainingDate, int trainingSteps, int trainingDuration, int trainingDistance, int lastMetersSettings, float avgTotSpeed, float avgXSpeed, float avgTotSteps, int avgXSteps, int stepLengthInCm) {

      DBTrainings dbTrainingInstance = new DBTrainings();


      try {
         this.id = id;
         this.trainingDate = trainingDate;
         this.trainingSteps = trainingSteps;
         this.trainingDuration = trainingDuration; //TODO: check if there is a better type
         this.trainingDistance = trainingDistance;
         this.lastMetersSettings = lastMetersSettings;
         this.avgTotSpeed = avgTotSpeed;
         this.avgXSpeed = avgXSpeed;
         this.avgTotSteps = avgTotSteps;
         this.avgXSteps = avgXSteps;
         this.prefsstepLengthInCm = stepLengthInCm; //in cm

         dbTrainingInstance.id = this.id;
         dbTrainingInstance.trainingDate = this.trainingDate;
         dbTrainingInstance.trainingSteps = this.trainingSteps;
         dbTrainingInstance.trainingDuration = this.trainingDuration;
         dbTrainingInstance.trainingDistance = this.trainingDistance;
         dbTrainingInstance.lastMetersSettings = this.lastMetersSettings;
         dbTrainingInstance.avgTotSpeed = this.avgTotSpeed;
         dbTrainingInstance.avgXSpeed = this.avgXSpeed;
         dbTrainingInstance.avgTotSteps = this.avgTotSteps;
         dbTrainingInstance.avgXSteps = this.avgXSteps;
         dbTrainingInstance.stepLengthInCm = this.prefsstepLengthInCm;


         dbDao.create(dbTrainingInstance);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   //needed to create a transition effect between active and inactive colors in training status
   private void fadeTo(RelativeLayout layout, int actualColor, int color) {
      ColorDrawable[] colour = {new ColorDrawable(actualColor), new ColorDrawable(color)};

      TransitionDrawable trans = new TransitionDrawable(colour);
      layout.setBackground(trans);
      trans.startTransition(200);
   }

   //updates the isPaused status on call
   private void updateIsPaused() {

      if (isPaused != true) {
         isPaused = true;
      } else {
         isPaused = false;
      }

   }

   //setups all the Step Counting part of the class
   public void setupPedometerService() {

      //creates a new SensorEventListener for the TYPE_STEP_COUNTER
      sensorEventListener = new SensorEventListener() {

         //needed by Android
         @Override
         public void onAccuracyChanged(Sensor sensor, int accuracy) {
         }

         //when the sensor status changed, the listener gets called
         @Override
         public void onSensorChanged(SensorEvent event) {

            float[] steps = event.values;

            //this zeroes the step counter to count correctly only the steps walked during the training.
            if (isInitialValueSet != true) {
               isInitialValueSet = true;
               initialValue = steps[0];
            }


            if (isPaused != true && isStopped != true) {
               actualSteps = steps[0];
               actualTime = (System.currentTimeMillis() - startTime);

               //calculates the steps/minute ratio with a double precision, then truncates to int for readability and sets to the view
               int avg = (int) ((actualSteps - initialValue) / (actualTime / MINUTE_IN_MILLIS));
               stepsPerMin.setText(Integer.toString(avg));

               //calculates the km/h ratio with a double precision, then truncates to int for readability and sets to the view
               int kmh = (int) ((((actualSteps - initialValue) * (STEP_IN_CENTIMETERS_TEST / 100)) / (actualTime / 1000.0)) * (3.6));
               kilometersPerHour.setText(Integer.toString(kmh));
            }
         }
      };

      //gets the correct sensor service (TYPE_STEP_COUNTER)
      sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      sensor = sensorService.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

      //sets the precision of the sensor
      if (sensor != null) {
         sensorService.registerListener(sensorEventListener, sensor,
                 SensorManager.SENSOR_DELAY_FASTEST);
      }

   }

   //gets the Helper
   private DatabaseHelper getHelper() {
      if (databaseHelper == null) {
         databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
      }
      return databaseHelper;
   }

   //setups the ActionBar
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

