package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.so2.walkmeapp.core.ORM.DBManager;

import org.codehaus.jackson.map.ObjectMapper;

import com.project.so2.walkmeapp.core.JacksonUtils;

import java.io.IOException;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.io.File;

import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.POJO.TrainingInstant;
import com.project.so2.walkmeapp.core.PausableChronometer;
import com.project.so2.walkmeapp.core.SERVICE.GPS;
import com.wnafee.vector.MorphButton;
import com.wnafee.vector.compat.AnimatedVectorDrawable;

import static android.app.PendingIntent.getActivity;


/**
 * Created by Andrea on 24/01/2016.
 */
public class Training extends Activity {

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
   LinearLayout linearChart;
   private ArrayList<TrainingInstant> trainingInsts;
   private TrainingInstant ti;

   private boolean isInitialValueSet = false;
   private boolean isPaused = true;
   private boolean isStopped = true;
   private long startTime = -1000;
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
   private double distance = 0.0;
   private ArrayList<Location> list = new ArrayList<Location>();


   private static final double MINUTE_IN_MILLIS = 60000.0;
   private static final int STEP_IN_CENTIMETERS_TEST = 100;
   private ContextWrapper context = this;
   private static final int THREAD_FINISH_MESSAGE = 1;
   private boolean mIsBound;
   private Intent serviceIntent;
   private GPS mService;

   private boolean isEnded = false;

   final Handler handleThreadMsg = new Handler(Looper.getMainLooper()) {
      @Override
      public void handleMessage(Message msg) {
         super.handleMessage(msg);
         switch (msg.what) {
            case THREAD_FINISH_MESSAGE:
               Toast.makeText(getApplicationContext(),
                       "worker thread finished",
                       Toast.LENGTH_SHORT).show();
               break;
            default:
               break;
         }
      }
   };
   private Location previousLoc;


   @Override
   public boolean bindService(Intent service, ServiceConnection conn, int flags) {
      return super.bindService(service, conn, flags);
   }


   @Override
   protected void onStart() {
      super.onStart();

      Toast.makeText(this, "Ricerca GPS",
              Toast.LENGTH_SHORT).show();
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
      setContentView(R.layout.training_main);

      serviceIntent = new Intent(this, GPS.class);
      lat = (TextView) this.findViewById(R.id.tvLatitudine);
      longit = (TextView) this.findViewById(R.id.tvLongitudine);
      distanza_text = (TextView) this.findViewById(R.id.tvDistanza);
      trainingInsts = new ArrayList<TrainingInstant>();

      //TEST GRAFICO
      linearChart = (LinearLayout) findViewById(R.id.linearChart);
      int colerloop[] = {1, 2, 2, 2, 3, 3, 3, 3, 1, 1};
      int heightLoop[] = {400, 300, 300, 300, 200, 200, 200, 200, 400, 400};
      for (int j = 0; j < colerloop.length; j++) {
         drawChart(1, colerloop[j], heightLoop[j]);
      }

      actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      actionBarText = (TextView) findViewById(R.id.action_bar_title);
      playButton = (MorphButton) findViewById(R.id.playPauseBtn);
      stepsPerMin = (TextView) findViewById(R.id.item_text2Left);
      kilometersPerHour = (TextView) findViewById(R.id.item_text2Right);
      runContainer = (RelativeLayout) findViewById(R.id.training_run_container);
      stopContainer = (RelativeLayout) findViewById(R.id.training_stop_container);
      chronometer = (PausableChronometer) findViewById(R.id.digital_clock);


      setupActionbar();


      index = db.setupDB();
      setValuesFromShared();
      Calendar c = Calendar.getInstance();
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      formattedDate = df.format(c.getTime());

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
               playButton.animate();
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

            endTrainingPrompt();

            Toast.makeText(Training.this, "RESET", Toast.LENGTH_SHORT).show();


            trainingDate = formattedDate;

            db.saveTrainingInDB(index, trainingDate, trainingSteps, trainingDuration, trainingDistance, lastMetersSettings, avgTotSpeed, avgXSpeed, avgTotSteps, avgXSteps, prefsstepLengthInCm);
            String res = db.getTrainings();


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


            emailIntent.setType("vnd.android.cursor.dir/email");
            emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");

            //startActivity(Intent.createChooser(emailIntent, "Send email..."));
            unbindService(mConnection);

            return true;
         }
      });
   }

   private void endTrainingPrompt() {

      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
              context);

      //alertDialogBuilder.setTitle("");

      alertDialogBuilder
              .setMessage("Vuoi terminare l'allenamento?")
              .setCancelable(false)
              .setPositiveButton("Si",new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog,int id) {

                    isEnded = true;
                    Intent intent = new Intent(Training.this, Settings.class);
                    startActivity(intent);
                 }
              })
              .setNegativeButton("No",new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog,int id) {

                    dialog.cancel();
                 }
              });

      // create alert dialog
      AlertDialog alertDialog = alertDialogBuilder.create();

      // show it
      alertDialog.show();
   }

   public void drawChart(int count, int color, int height) {
      System.out.println(count + color + height);
      if (color == 3) {
         color = Color.RED;
      } else if (color == 1) {
         color = Color.BLUE;
      } else if (color == 2) {
         color = Color.GREEN;
      }
      for (int k = 1; k <= count; k++) {
         View view = new View(this);
         view.setBackgroundColor(color);
         view.setLayoutParams(new LinearLayout.LayoutParams(25, height));
         LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
         params.setMargins(3, 0, 0, 0); // substitute parameters for left, // top, right, bottom
         view.setLayoutParams(params);
         linearChart.addView(view);
      }
   }


   private Runnable mUpdateTimeTask = new Runnable() {
      public void run() {
         activateGPS();
      }
   };

   public void activateGPS() {

      Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
      startActivity(intent);
   }

   private ServiceConnection mConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName className, IBinder service) {
         GPS.LocalBinder binder = (GPS.LocalBinder) service;
         mService = binder.getService();
         mIsBound = true;
         if (!mService.mLocationManager.isProviderEnabled("gps")) {
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
         mService.addOnNewGPSPointsListener(clientListener);
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
         Log.i("ConnectionService", "Disconnected");
         mService = null;
         mIsBound = false;
      }

   };

   private void connectLocalService() {
      bindService(this.serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
   }

   private void disconnectLocalService() {
      if (mIsBound) {
         mService.removeOnNewGPSPointsListener();
         //unbindService(mConnection);  //TODO: Forse non serve
         mIsBound = false;
      }
   }

   private void getGPSData() {

      if (isPaused != false || isStopped != false) {
         return;
      }
      Location loc = new Location(mService.mLastLocation);


      double latitude = mService.getLatitude();
      double longitude = mService.getLongitude();
      double altitude = loc.getAltitude();
      long time = loc.getTime();    //TODO: BUG - il cronometro e questo tempo non coincidono
      float speed = loc.getSpeed();

      if (trainingInsts.size() != 0) {
         distance = distance + previousLoc.distanceTo(loc);
      }
      previousLoc = loc;


         lat.setText(trainingInsts.size());
         longit.setText(format(distance));


      ti = new TrainingInstant(latitude, longitude, speed, altitude, time, distance);
      trainingInsts.add(ti);

   }


   public static String format(double value) {
      DecimalFormat decimalFormat = new DecimalFormat("0.0000000");
      return decimalFormat.format(value);
   }



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


   private void setupActionbar() {
      actionBar.setImageResource(R.drawable.btn_back);
      actionBar.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent intent;
            intent = new Intent(Training.this, MainActivity.class);
            intent.putExtra("latitudine", mService.getLatitude());
            intent.putExtra("longitudine", mService.getLongitude());
            startActivity(intent);
         }
      });

      actionBarText.setText(getResources().getString(R.string.main_training_title));
   }
}