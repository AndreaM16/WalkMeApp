package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.ConnectionService;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.project.so2.walkmeapp.core.ORM.DBManager;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;
import com.project.so2.walkmeapp.core.ORM.DatabaseHelper;
import com.project.so2.walkmeapp.core.POJO.TrainingPOJO;
import java.sql.SQLException;
import org.codehaus.jackson.map.ObjectMapper;
import com.project.so2.walkmeapp.core.JacksonUtils;
import java.io.IOException;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import java.io.File;

import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.PausableChronometer;
import com.project.so2.walkmeapp.core.SERVICE.GPS;
import com.project.so2.walkmeapp.core.SERVICE.Position;
import com.wnafee.vector.MorphButton;
import com.wnafee.vector.compat.AnimatedVectorDrawable;

import static android.app.PendingIntent.getActivity;
import static android.support.v4.content.FileProvider.getUriForFile;
import static java.security.AccessController.getContext;


/**
 * Created by Andrea on 24/01/2016.
 */
public class Training extends Activity{

   private static final String PREFS_NAME = "SETTINGS_PREFS";

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

   private boolean isInitialValueSet = false;
   private boolean isPaused = true;
   private boolean isStopped = true;
   private long startTime = -1000;
   private TrainingPOJO trainingData;
   private float actualSteps;
   private long actualTime;
   private DBManager db;

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
   private String formattedDate;
   private int index;
   private TextView lat;
   private TextView longit;
   private TextView distanza_text;
   double distanza=0.0;
   private ArrayList<Location> list=new ArrayList<Location>();


   private static final double MINUTE_IN_MILLIS = 60000.0;
   private static final int STEP_IN_CENTIMETERS_TEST = 100;
   private ContextWrapper context = this;
   private static final int THREAD_FINISH_MESSAGE = 1;
   private boolean mIsBound;
   private GPS mService;

   final Handler handleThreadMsg = new Handler(Looper.getMainLooper()) {
      @Override
      public void handleMessage(Message msg) {
         super.handleMessage(msg);
         switch( msg.what ) {
            case THREAD_FINISH_MESSAGE :
//Il worker thread ha terminato e lo notifico con un
//Toast eseguito nell’UI Thread
               Toast.makeText( getApplicationContext(),
                       "worker thread finished",
                       Toast.LENGTH_SHORT).show();
               break;
            default :
               break;
         }
      }
   };
   private Intent serviceIntent;


   @Override
   public boolean bindService(Intent service, ServiceConnection conn, int flags) {
      return super.bindService(service, conn, flags);
   }

   @Override
   protected void onStart() {
      super.onStart();

      Toast.makeText(this, "Mi connetto al servizio",
              Toast.LENGTH_SHORT).show();
      //Ci connettiamo al service
      connectLocalService();

   }
   @Override
   protected void onStop() {
      super.onStop();
      disconnectLocalService();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      db = new DBManager(this);
      //Binding Class to its View
      setContentView(R.layout.training_main);
      serviceIntent = new Intent(this, GPS.class);
//Inizializziamo le due TextView
      lat = (TextView) this.findViewById(R.id.tvLatitudine);
      longit = (TextView) this.findViewById(R.id.tvLongitudine);
      distanza_text=(TextView) this.findViewById(R.id.tvDistanza);

      lat.setText(format(getIntent().getExtras().getDouble("latitudine")));
      longit.setText(format(getIntent().getExtras().getDouble("longitudine")));






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
      //LocalBroadcastManager.getInstance(this).registerReceiver(
      //mMessageReceiver, new IntentFilter("GPSLocationUpdates"));

      setupActionbar();
     // setupPedometerService();
      index = db.setupDB();
      setValuesFromShared();
      Calendar c = Calendar.getInstance();
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      formattedDate = df.format(c.getTime());
      //trainingData = new TrainingPOJO(1, formattedDate, 10, 30, 2, 20, 2, 24, 10, 2, 4);
      //ObjectMapper mapper = JacksonUtils.mapper;
     /* File file = new File(Environment.DIRECTORY_DOWNLOADS, "training");

      try {
         mapper.writeValue(file, trainingData);
      } catch (IOException e) {
         e.printStackTrace();
      }*/


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
            //new GPS(Training.this).execute(coord, coord, coord);

            updateIsPaused();

         }
      });

      playButton.setOnLongClickListener(new View.OnLongClickListener() {
         @Override
         public boolean onLongClick(View v) {


            isStopped = true;
            //testCreateTraining();


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
            isInitialValueSet = false;
            Toast.makeText(Training.this, "RESET", Toast.LENGTH_SHORT).show();
            trainingDate = formattedDate;

            db.saveTrainingInDB(index, trainingDate, trainingSteps, trainingDuration, trainingDistance, lastMetersSettings, avgTotSpeed, avgXSpeed, avgTotSteps, avgXSteps, prefsstepLengthInCm);
            //db.saveTrainingInDB();
            String res = db.getTrainings();



            // how to save the file and share it by mail
            File path = new File(context.getFilesDir(), "training");
            File training = new File(path, "training.txt");
            try {
               ObjectMapper mapper = JacksonUtils.mapper;
               mapper.writeValue(training, res);
            } catch (IOException e) {
               e.printStackTrace();
            }
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            Uri contentUri = FileProvider.getUriForFile(context, "com.project.so2.walkmeapp", training);


// set the type to 'email'
            emailIntent.setType("vnd.android.cursor.dir/email");
            //String to[] = {"asd@gmail.com"};

            //emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
// the attachment
            emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
// the mail subject
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");

            startActivity(Intent.createChooser(emailIntent, "Send email..."));
            //emailIntent.setData(contentUri);
            unbindService(mConnection);

            return true;
         }
      });
   }
   private Runnable mUpdateTimeTask = new Runnable() {
      public void run() {
         // do what you need to do here after the delay
         activateGPS();
      }
   };
   public  void activateGPS() {

      Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
      startActivity(intent);
   }

   private ServiceConnection mConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName className, IBinder
              service) {
// This is called when the connection with the service has been
// established, giving us the service object we can use to
// interact with the service.
         GPS.LocalBinder binder = (GPS.LocalBinder) service;
         mService = binder.getService();
         mIsBound = true;
         if(!mService.mLocationManager.isProviderEnabled("gps")){
            Toast.makeText(Training.this, "GPS e' attualmente disabilitato. E' possibile abilitarlo dal menu impostazioni.",
                    Toast.LENGTH_LONG).show();
            handleThreadMsg.postDelayed(mUpdateTimeTask, 3000);


         }
         GPS.OnNewGPSPointsListener clientListener = new
                 GPS.OnNewGPSPointsListener() {
                    @Override
                    public void onNewGPSPoint() {
                       getGPSData();
                    }
                 };
//Registriamo il listener per ricevere gli aggiornamenti
         mService.addOnNewGPSPointsListener(clientListener);
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
         Log.i("ConnectionService", "Disconnected");
         mService = null;
         mIsBound=false;
      }

   };
   private void connectLocalService() {
      bindService(this.serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
   }

   private void disconnectLocalService() {
      if(mIsBound) {
//Deregistro il listener
         mService.removeOnNewGPSPointsListener();
// Detach our existing connection.
         unbindService(mConnection);
         mIsBound = false;
      }
   }
   private void getGPSData() {
      double latitude = 0.0;
      double longitude = 0.0;
      latitude = mService.getLatitude();
      longitude = mService.getLongitude();
      Location loc = new Location(mService.mLastLocation);
      list.add(loc);
      int i;
      if (list.size() > 1) {

         for (i = list.size() - 1; i > list.size() - 2; i--) {

            distanza = distanza + list.get(i).distanceTo(list.get(i - 1));

         }


// Visualizza i nuovi dati
         lat.setText(format(latitude));
         longit.setText(format(longitude));
         distanza_text.setText(format(distanza));

      }
   }

   public static String format(double value) {
      DecimalFormat decimalFormat = new DecimalFormat("0.0000000");
      return decimalFormat.format(value);
   }


   /*public void testCreateTraining( DBTrainings dbTrainingInstance) {


      dbTrainingInstance.id = db.getId()+1;
      dbTrainingInstance.trainingDate = "2015-12-11 18:00:23";
      dbTrainingInstance.trainingSteps = 100;
      dbTrainingInstance.trainingDuration = 500; //TODO: check if there is a better type
      dbTrainingInstance.trainingDistance = 1000;
      //this.lastMetersSettings = 30; set from real prefs, 10 is default value
      dbTrainingInstance.avgTotSpeed = 10;
      dbTrainingInstance.avgXSpeed = 4;
      dbTrainingInstance.avgTotSteps = 600;
      dbTrainingInstance.avgXSteps = 30;
      //this.prefsstepLengthInCm = 70; set from real prefs, 100 is default value
   }*/


   private void setValuesFromShared() {

      settings = getSharedPreferences(PREFS_NAME, 0);


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


   /*public void setupPedometerService() {

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

            if (isPaused != true && isStopped != true) {
               actualSteps = steps[0];
               actualTime = (System.currentTimeMillis() - startTime);
               int avg = (int) ((actualSteps - initialValue) / (actualTime / MINUTE_IN_MILLIS));

               stepsPerMin.setText(Integer.toString(avg));


               int kmh = (int) ((((actualSteps - initialValue) * (STEP_IN_CENTIMETERS_TEST / 100)) / (actualTime / 1000.0)) * (3.6));
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
   /*      }
      };

      sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      sensor = sensorService.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

      if (sensor != null) {
         sensorService.registerListener(sensorEventListener, sensor,
                 SensorManager.SENSOR_DELAY_FASTEST);
      }

   }

*/
   private void setupActionbar() {
      actionBar.setImageResource(R.drawable.btn_back);
      actionBar.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent intent;
            intent = new Intent(Training.this, MainActivity.class);
            intent.putExtra("latitudine",mService.getLatitude());
            intent.putExtra("longitudine",mService.getLongitude());
            startActivity(intent);
         }
      });

      actionBarText.setText(getResources().getString(R.string.main_training_title));
   }
}