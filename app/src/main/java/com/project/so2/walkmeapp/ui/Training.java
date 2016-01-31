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
   private int stepLenghtInCm;

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
   private Context context;
   private boolean secsCheckSet = false;
   private long oldTime;
   private double desideredTime;
   private float oldSteps;
   private double TIME_SAMPLE = 3.0;
   private float actualSteps;
   private long actualTime;

   private static final double MINUTE_IN_MILLIS = 60000.0;
   private static final int STEP_IN_CENTIMETERS_TEST = 100;


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
      stepsPerMin = (TextView) findViewById(R.id.item_text2Left);
      kilometersPerHour = (TextView) findViewById(R.id.item_text2Right);
      runContainer = (RelativeLayout) findViewById(R.id.training_run_container);
      stopContainer = (RelativeLayout) findViewById(R.id.training_stop_container);
      chronometer = (PausableChronometer) findViewById(R.id.digital_clock);

      setupActionbar();
      // setupChronometer();     //potentially useless
      setupPedometerService();

      try {
         this.dbDao = getHelper().getTrainingsDao();
      } catch (SQLException e) {
         e.printStackTrace();
      }

      testCreateTraining();
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


      colorGreen = Color.parseColor(getResources().getString(R.string.training_green));
      colorGrey = Color.parseColor(getResources().getString(R.string.training_grey));

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
            if (isPaused == false) {
               playButton.animate();   //TODO: try to trigger the play animation, there gotta be a way goddammit
               fadeTo(runContainer, colorGreen, colorGrey);
               fadeTo(stopContainer, colorGrey, colorGreen);
               isPaused = true;
            }
            chronometer.reset();
            stepsPerMin.setText("0");
            kilometersPerHour.setText("0");
            actualSteps = 0;
            actualTime = 0;
            Toast.makeText(Training.this, "RESET", Toast.LENGTH_SHORT).show();
            saveTrainingInDB(id, trainingDate, trainingSteps, trainingDuration, trainingDistance, lastMetersSettings, avgTotSpeed, avgXSpeed, avgTotSteps, avgXSteps, stepLenghtInCm);
            return true;
         }
      });
   }

   private void testCreateTraining() {

      this.id = 2;
      this.trainingDate = "2015-12-11 18:00:23";
      this.trainingSteps = 100;
      this.trainingDuration = 500; //TODO: check if there is a better type
      this.trainingDistance = 1000;
      this.lastMetersSettings = 30;
      this.avgTotSpeed = 10;
      this.avgXSpeed = 4;
      this.avgTotSteps = 600;
      this.avgXSteps = 30;
      this.stepLenghtInCm = 70; //in cm
   }

   private void saveTrainingInDB(int id, String trainingDate, int trainingSteps, int trainingDuration, int trainingDistance, int lastMetersSettings, float avgTotSpeed, float avgXSpeed, float avgTotSteps, int avgXSteps, int stepLenghtInCm) {

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
         this.stepLenghtInCm = stepLenghtInCm; //in cm

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
         dbTrainingInstance.stepLenghtInCm = this.stepLenghtInCm;


         dbDao.create(dbTrainingInstance);
      } catch (
              Exception e
              )

      {
         e.printStackTrace();
      }


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

            if (isPaused != true && isStopped != true ) {
               actualSteps = steps[0];
               actualTime = (System.currentTimeMillis() - startTime);
               int avg = (int) ((actualSteps - initialValue) / (actualTime / MINUTE_IN_MILLIS));

               stepsPerMin.setText(Integer.toString(avg));


               int kmh = (int)((actualSteps*(STEP_IN_CENTIMETERS_TEST/100))  /   ((actualTime/1000)*(3.6)));
               kilometersPerHour.setText(Integer.toString(kmh));

            }

           /* if ((System.currentTimeMillis() - startTime) < (60 * 1000)) {
               if (secsCheckSet == false ) {
                  oldTime = System.currentTimeMillis() - startTime;
                  oldSteps = steps[0];
                  desideredTime = (System.currentTimeMillis() - startTime) + (TIME_SAMPLE * 1000);

                  secsCheckSet = true;
               }
               if ((System.currentTimeMillis() - startTime) >= desideredTime) {
                  secsCheckSet = false;

                  float diffSteps = steps[0] - oldSteps;
                  double value = diffSteps/(TIME_SAMPLE/60);
                  stepsPerMin.setText(Double.toString(value));

               }
            } else {*/


       //     }
         }
      };

      sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      sensor = sensorService.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

      if (sensor != null) {
         sensorService.registerListener(sensorEventListener, sensor,
                 SensorManager.SENSOR_DELAY_FASTEST);
      }

   }

   private DatabaseHelper getHelper() {
      if (databaseHelper == null) {
         databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
      }
      return databaseHelper;
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

