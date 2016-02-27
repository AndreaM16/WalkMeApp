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
import android.location.Location;
import android.os.Bundle;

import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.so2.walkmeapp.core.ORM.DBManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.io.File;
import java.util.List;
import java.util.TimeZone;

import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.POJO.TrainingInstant;
import com.project.so2.walkmeapp.core.PausableChronometer;
import com.project.so2.walkmeapp.core.SERVICE.GPS;
import com.wnafee.vector.MorphButton;
import com.wnafee.vector.compat.AnimatedVectorDrawable;

import lecho.lib.hellocharts.formatter.AxisValueFormatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;


/**
 * Created by Andrea on 24/01/2016.
 */
public class Training extends Activity {

   private static final String PREFS_NAME = "SETTINGS_PREFS";

   private int prefsStepLengthInCm = 100;
   private int prefsAvgStepPerMin = 120;
   private int prefsLastMetersInM = 15;

   private SharedPreferences settings;


   private static final String TAG = "Training";
   private TextView stepsPerMin;
   private TextView kilometersPerHour;
   private ImageView actionBar;
   private TextView actionBarText;
   private MorphButton playButton;
   private RelativeLayout runContainer;
   private RelativeLayout stopContainer;
   private int colorGreen;
   private int colorGrey;
   private PausableChronometer chronometer;
   private ArrayList<TrainingInstant> trainingInsts;
   private TrainingInstant ti;

   private boolean isInitialValueSet = false;
   private boolean isPaused = true;
   private boolean isStopped = true;
   private long startTime = -1000;
   private DBManager db;
   private String name = "Allenamento";
   private String formattedDate;
   private double distance = 0.0;

   private ContextWrapper context = this;
   private boolean mIsBound;
   private Intent serviceIntent;
   private GPS mService;
   public static File training;

   private Location previousLoc;
   private boolean wasInPause = false;
   private boolean utenteAvvisato = false;
   private xAxisType mXAxis;
   private yAxisType mYAxis;
   private LineChartView graph;
   private int stepsMinValue = 0;


   @Override
   public boolean bindService(Intent service, ServiceConnection conn, int flags) {
      return super.bindService(service, conn, flags);
   }


   @Override
   protected void onStart() {
      super.onStart();
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
      db = DBManager.getIstance();
      setContentView(R.layout.training_main);

      serviceIntent = new Intent(this, GPS.class);
      trainingInsts = new ArrayList<TrainingInstant>();

      actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      actionBarText = (TextView) findViewById(R.id.action_bar_title);
      playButton = (MorphButton) findViewById(R.id.playPauseBtn);
      stepsPerMin = (TextView) findViewById(R.id.item_text2Left);
      kilometersPerHour = (TextView) findViewById(R.id.item_text2Right);
      runContainer = (RelativeLayout) findViewById(R.id.training_run_container);
      stopContainer = (RelativeLayout) findViewById(R.id.training_stop_container);
      chronometer = (PausableChronometer) findViewById(R.id.digital_clock);
      graph = (LineChartView) findViewById(R.id.chart);


      setupActionbar();

      mXAxis = xAxisType.TIME;
      mYAxis = yAxisType.PACE;


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

               wasInPause = true;
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
               if (utenteAvvisato == false) {

                  Toast.makeText(Training.this, "Tieni premuto per concludere l'allenamento", Toast.LENGTH_LONG).show();
                  utenteAvvisato = true;
               }


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


            endTrainingPrompt();


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
              .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Training.this);
                    builder.setTitle("Title");

                    final EditText input = new EditText(Training.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {

                          name = input.getText().toString();
                          Log.d("TYEST", name);

                          db.createTraining(name, formattedDate, prefsAvgStepPerMin, prefsLastMetersInM, prefsStepLengthInCm, trainingInsts);
                          try {
                             db.saveTrainingInDB();
                          } catch (SQLException e) {
                             e.printStackTrace();
                          }
                          disconnectLocalService();

                       }
                    });

                    builder.show();
                    chronometer.reset();


                 }
              })
              .setNegativeButton("No", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {

                    dialog.cancel();
                 }
              });

      // create alert dialog
      AlertDialog alertDialog = alertDialogBuilder.create();

      // show it
      alertDialog.show();
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
         unbindService(mConnection);  //TODO: Forse non serve
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
         if (wasInPause != true) {

            distance = distance + Math.abs(previousLoc.distanceTo(loc));

         } else {
            wasInPause = false;
         }
      }

      ti = new TrainingInstant(db.dbTrainingInstance, latitude, longitude, speed, altitude, time, distance);

      //id_tInstance++;

      if (previousLoc == null) {
         updateView(0, 0, 0);
      } else {
         updateView(loc.getTime() - previousLoc.getTime(), Math.abs(previousLoc.distanceTo(loc)), speed);
      }

      previousLoc = loc;

      trainingInsts.add(ti);

      Log.d("ti", "latitudine: " + ti.latitude + " longitudine: " + ti.longitude + " velocità: " + ti.speed + " altitudine: " + ti.altitude + " tempo: " + ti.time + " distanza: " + ti.distance);

   }

   private void updateView(long deltaTime, double deltaDistance, float speed) {

         if (deltaTime == 0 && deltaDistance == 0 && speed == 0) {
            stepsPerMin.setText("--");
            kilometersPerHour.setText("--");
            stepsMinValue = 0;

         } else {
            double numberSteps = deltaDistance / (prefsStepLengthInCm / 100);
            stepsMinValue = (int) ((numberSteps / (deltaTime / 1000)) * 60);
            stepsPerMin.setText(Integer.toString(stepsMinValue));
            kilometersPerHour.setText(Integer.toString((int) (speed * 3.6)));
         }

      plot(trainingInsts);

   }


   @Override
   protected void onPause() {
      super.onPause();
      wasInPause = true;
      disconnectLocalService();

   }

   @Override
   protected void onResume() {
      super.onResume();
      connectLocalService();
   }

   private void setValuesFromShared() {

      settings = getSharedPreferences(PREFS_NAME, 0);


      if (settings.contains("stepLength")) {
         prefsStepLengthInCm = settings.getInt("stepLength", 100);
      }
      if (settings.contains("lastXMeters")) {
         prefsLastMetersInM = settings.getInt("lastXMeters", 10);
      }
      if (settings.contains("avgStepInM")) {
         prefsAvgStepPerMin = settings.getInt("avgStepInM", 1);
      }

      Log.d(TAG, "Values from prefs ->  " + "stepLength: " + prefsStepLengthInCm + "cm ||  LastXMeters: " + prefsLastMetersInM + "m ||  AvgStep: " + prefsAvgStepPerMin + "m");
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
            finish();
         }
      });
      actionBarText.setText(getResources().getString(R.string.main_training_title));
   }

   private void plot(ArrayList<TrainingInstant> train) {
      //plot the graph with the axis determined by the spinners
      List<PointValue> values = new ArrayList<>();
      List<PointValue> values2 = new ArrayList<>();
      Axis axisX = new Axis();
      Axis axisY = new Axis().setHasLines(true);

      if (mXAxis == xAxisType.TIME) {

         //if the axis x type is time, format the unix timestamp a date
         axisX.setFormatter(new AxisValueFormatter() {
            @Override
            public int formatValueForAutoGeneratedAxis(char[] formattedValue, float value, int autoDecimalDigits) {

               SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
               simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

               String label = simpleDateFormat.format(value);

               //The library need the label string at the end of the formattedValue array
               label.getChars(0, label.length(), formattedValue, formattedValue.length - label.length());
               //Log.d("Formatter", new String(formattedValue));

               return label.length();
            }

            @Override
            public int formatValueForManualAxis(char[] formattedValue, AxisValue axisValue) {
               //not used-
               return 0;
            }
         });

         //show max 3 date label for space issues
         axisX.setMaxLabelChars(4);
      }


      float oldTime = 0;
      for (int i = 0; i < train.size(); i++) {
         Float x, y = 0f;

         if (mXAxis == xAxisType.TIME) {
            oldTime = oldTime + (float) trainingInsts.get(i).time;
            x = oldTime;
            axisX.setName(getString(R.string.axisX_time));
         } else {
            x = (float) train.get(i).distance;
            axisX.setName(getString(R.string.axisX_distance));
         }

         switch (mYAxis) {
            case SPEED:
               y = (float) train.get(i).speed;
               axisY.setName(getString(R.string.axisY_speed));
               break;
            case PACE:
               y = (float) stepsMinValue;
               axisY.setName(getString(R.string.axisY_pace));
               break;
            case ALTITUDE:
               y = (float) train.get(i).altitude;
               axisY.setName(getString(R.string.axisY_altitude));
               break;
         }

         values.add(new PointValue(x, y));
         values2.add(new PointValue(x, prefsAvgStepPerMin));
      }

      Line line = new Line(values);
      Line desired = new Line(values2);
      line.setColor(ContextCompat.getColor(Training.this, R.color.colorPrimary));
      desired.setColor(ContextCompat.getColor(Training.this, R.color.black));
      line.setHasPoints(false);
      desired.setHasPoints(false);
      line.setFilled(true);
      desired.setFilled(false);
      List<Line> lines = new ArrayList<>();
      lines.add(line);
      lines.add(desired);

      axisX.setTextColor(Color.BLACK);
      axisY.setTextColor(Color.BLACK);

      LineChartData data = new LineChartData();
      data.setLines(lines);
      data.setAxisXBottom(axisX);
      data.setAxisYLeft(axisY);
      graph.setZoomEnabled(false);
      graph.setContainerScrollEnabled(false, null);
      graph.setLineChartData(data);
      graph.setInteractive(true);
     // graph.setViewportCalculationEnabled(true);

      Viewport v = new Viewport(graph.getMaximumViewport());
      v.top = prefsAvgStepPerMin *2;
      graph.setMaximumViewport(v);
      graph.setMinimumHeight(0);
      graph.setCurrentViewport(v);
   }

   /**
    * Enum used for memorizing the x axis data type
    */
   private enum xAxisType {
      TIME, DISTANCE
   }

   /**
    * Enum used for memorizing the y axis data type
    */
   private enum yAxisType {
      SPEED, PACE, ALTITUDE
   }


}