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
import com.project.so2.walkmeapp.core.ORM.DBTrainings;
import com.project.so2.walkmeapp.core.ORM.TrainingInstant;
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
 * Class that manages all the training features
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
   private String name = "Training";
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
   private DBTrainings last_training;
   public static boolean comingFromTraining = false;
   private ImageView arrow_up;
   private ImageView arrow_down;
   private static AlertDialog.Builder alertDialogBuilder;
   private static AlertDialog alertDialog;
   private long deltaTime;

   /**
    * Binding GPS Service
    *
    * @param service Service
    * @param conn    Connection
    * @param flags   Flags used for the binding
    * @return Bound Service
    */
   @Override
   public boolean bindService(Intent service, ServiceConnection conn, int flags) {
      return super.bindService(service, conn, flags);
   }

   /**
    * Connecting to Service
    */
   @Override
   protected void onStart() {
      super.onStart();
      connectLocalService();
   }

   /**
    * Destroying the Service
    */
   @Override
   protected void onDestroy() {
      super.onDestroy();
   }

   /**
    * Going Back
    */
   @Override
   public void onBackPressed() {
      comingFromTraining = true;
      finish();
   }

   /**
    * Stop
    */
   @Override
   protected void onStop() {
      super.onStop();
      disconnectLocalService();
   }

   /**
    * onCreate
    *
    * @param savedInstanceState instances
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      db = DBManager.getIstance();
      setContentView(R.layout.training_main);


      alertDialogBuilder = new AlertDialog.Builder(
              context);

      serviceIntent = new Intent(this, GPS.class);
      trainingInsts = new ArrayList<TrainingInstant>();

      actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      arrow_up = (ImageView) findViewById(R.id.arrow_up);
      arrow_down = (ImageView) findViewById(R.id.arrow_down);
      actionBarText = (TextView) findViewById(R.id.action_bar_title);
      playButton = (MorphButton) findViewById(R.id.playPauseBtn);
      stepsPerMin = (TextView) findViewById(R.id.item_text2Left);
      kilometersPerHour = (TextView) findViewById(R.id.item_text2Right);
      runContainer = (RelativeLayout) findViewById(R.id.training_run_container);
      stopContainer = (RelativeLayout) findViewById(R.id.training_stop_container);
      chronometer = (PausableChronometer) findViewById(R.id.digital_clock);
      graph = (LineChartView) findViewById(R.id.chart);

      setupActionbar();

      /* Used by the Graph */
      mXAxis = xAxisType.TIME;
      mYAxis = yAxisType.PACE;


      setValuesFromShared();
      Calendar c = Calendar.getInstance();
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      formattedDate = df.format(c.getTime());

      colorGreen = Color.parseColor(getResources().getString(R.string.training_green));
      colorGrey = Color.parseColor(getResources().getString(R.string.training_grey));

      final AnimatedVectorDrawable startDrawable = AnimatedVectorDrawable.getDrawable(playButton.getContext(), R.drawable.ic_play_to_pause);
      AnimatedVectorDrawable endDrawable = AnimatedVectorDrawable.getDrawable(playButton.getContext(), R.drawable.ic_pause_to_play);

      playButton.setStartDrawable(startDrawable);
      playButton.setEndDrawable(endDrawable);


      playButton.setOnClickListener(new View.OnClickListener() {
         /**
          * Handling Play/Pause/Stop
          * @param v View
          */
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

                  Toast.makeText(Training.this, "Hold to stop the training", Toast.LENGTH_LONG).show();
                  utenteAvvisato = true;
               }


            }

            updateIsPaused();

         }
      });

      /**
       * Handling long press to Stop the training
       */
      playButton.setOnLongClickListener(new View.OnLongClickListener() {
         @Override
         public boolean onLongClick(View v) {


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

   /**
    * Handling training end
    */
   private void endTrainingPrompt() {

      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
              context);

      alertDialogBuilder
              .setMessage("Do you want to end the training?")
              .setCancelable(false)
              .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {

                    isStopped = true;
                    isPaused = true;

                    chronometer.stop();

                    AlertDialog.Builder builder = new AlertDialog.Builder(Training.this);
                    builder.setTitle("Pick a name:");

                    final EditText input = new EditText(Training.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                       /**
                        * Setting the name
                        * @param dialog
                        * @param which
                        */
                       @Override
                       public void onClick(DialogInterface dialog, int which) {


                          if (input.getText().toString() != null && !(input.getText().toString().equals(""))) {
                             name = input.getText().toString();
                          } else {
                             name = "Training";
                          }

                          db.createTraining(name, formattedDate, prefsAvgStepPerMin, prefsLastMetersInM, prefsStepLengthInCm, trainingInsts);
                          try {
                             db.saveTrainingInDB();
                             last_training = db.getLastTraining();
                          } catch (SQLException e) {
                             e.printStackTrace();
                          }


                          Intent intent = new Intent(Training.this, ViewTraining.class);
                          intent.putExtra("id", last_training.id);
                          startActivity(intent);
                          finish();
                          disconnectLocalService();
                       }
                    });

                    builder.show();


                 }
              })
              .setNegativeButton("No", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {

                    dialog.cancel();
                 }
              });

      /* Create alert dialog */
      AlertDialog alertDialog = alertDialogBuilder.create();

      /* Show it */
      alertDialog.show();
   }

   private ServiceConnection mConnection = new ServiceConnection() {
      /**
       * Handling GPS
       * @param className
       * @param service
       */
      @Override
      public void onServiceConnected(ComponentName className, IBinder service) {
         GPS.LocalBinder binder = (GPS.LocalBinder) service;
         mService = binder.getService();
         mIsBound = true;

         if (!mService.mLocationManager.isProviderEnabled("gps") && Training.comingFromTraining == false && alertDialog == null) {

            dialogGps();

         }
         GPS.OnNewGPSPointsListener clientListener = new
                 GPS.OnNewGPSPointsListener() {
                    /**
                     * Handling new GPS Points
                     */
                    @Override
                    public void onNewGPSPoint() {
                       getGPSData();
                    }
                 };
         mService.addOnNewGPSPointsListener(clientListener);
      }

      public void activateGPS() {
         Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
         startActivity(intent);
      }


      public void dialogGps() {

         alertDialogBuilder
                 .setMessage("GPS Disabled. Do you want to activate it from Settings?")
                 .setCancelable(false)
                 .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    /**
                     * Dialog
                     * @param dialog
                     * @param id
                     */
                    public void onClick(DialogInterface dialog, int id) {

                       activateGPS();

                    }
                 })
                 .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    /**
                     * Dialog
                     * @param dialog
                     * @param id
                     */
                    public void onClick(DialogInterface dialog, int id) {
                       Toast.makeText(Training.this, "GPS permissions denied, " + "some functionalities will not be supported", Toast.LENGTH_LONG).show();

                       dialog.cancel();
                    }
                 });

         /* Create alert dialog */
         alertDialog = alertDialogBuilder.create();

         /* Show it */
         alertDialog.show();


      }

      /**
       * Handling Disconnection
       * @param name Service Name
       */
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
         unbindService(mConnection);
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
      long time = loc.getTime();
      float speed = loc.getSpeed();

      if (trainingInsts.size() != 0) {
         if (wasInPause != true) {
            distance = distance + Math.abs(previousLoc.distanceTo(loc));
         } else {
            wasInPause = false;
         }
      }

      if (distance != 0) {
         double numberSteps = Math.abs(previousLoc.distanceTo(loc)) / (prefsStepLengthInCm / 100);
         deltaTime = time - previousLoc.getTime();
         stepsMinValue = (int) ((numberSteps / (deltaTime / 1000)) * 60);
      } else {
         stepsMinValue = 0;
      }

      ti = new TrainingInstant(db.dbTrainingInstance, latitude, longitude, altitude, speed, time, distance, stepsMinValue);

      if (previousLoc == null) {
         updateView(0, 0);
      } else {
         updateView(stepsMinValue, speed);
      }

      previousLoc = loc;

      trainingInsts.add(ti);


   }

   /**
    * Updating the View for changes
    *
    * @param paceValue pace
    * @param speed     speed
    */
   private void updateView(int paceValue, float speed) {

      if (paceValue == 0 && speed == 0) {
         stepsPerMin.setText("--");
         kilometersPerHour.setText("--");

      } else {

         stepsPerMin.setText(Integer.toString(paceValue));
         kilometersPerHour.setText(Integer.toString((int) (speed * 3.6)));
      }

      if (paceValue < prefsAvgStepPerMin - 10) {
         accelerate();
      } else if (paceValue >= prefsAvgStepPerMin - 10 && paceValue <= prefsAvgStepPerMin + 10) {
         both();
      } else {
         decelerate();
      }

      plot(trainingInsts);

   }

   /**
    * Handling on Pause
    */
   @Override
   protected void onPause() {
      super.onPause();
      wasInPause = true;
      disconnectLocalService();

   }

   /**
    * Handling on Resume
    */
   @Override
   protected void onResume() {
      super.onResume();
      connectLocalService();
   }

   /**
    * Setting values from Shared Preferences
    */
   private void setValuesFromShared() {

      settings = getSharedPreferences(PREFS_NAME, 0);


      if (settings.contains("stepLength")) {
         prefsStepLengthInCm = settings.getInt("stepLength", 100);
      }
      if (settings.contains("lastXMeters")) {
         prefsLastMetersInM = settings.getInt("lastXMeters", 15);
      }
      if (settings.contains("avgStepPerMin")) {
         prefsAvgStepPerMin = settings.getInt("avgStepPerMin", 60);
      }

   }

   /**
    * Switching Play and Pause Animation Colors
    *
    * @param layout
    * @param actualColor
    * @param color
    */
   private void fadeTo(RelativeLayout layout, int actualColor, int color) {
      ColorDrawable[] colour = {new ColorDrawable(actualColor), new ColorDrawable(color)};

      TransitionDrawable trans = new TransitionDrawable(colour);
      layout.setBackground(trans);
      trans.startTransition(200);
   }

   private void updateIsPaused() {

      isPaused = isPaused != true;
   }

   private void setupActionbar() {
      actionBar.setImageResource(R.drawable.btn_back);
      actionBar.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            comingFromTraining = true;
            finish();
         }
      });
      actionBarText.setText(getResources().getString(R.string.main_training_title));
   }

   private void plot(ArrayList<TrainingInstant> train) {
      /* Plot the graph with the axis determined by the spinners */
      List<PointValue> values = new ArrayList<>();
      Axis axisX = new Axis();
      Axis axisY = new Axis().setHasLines(true);

      if (mXAxis == xAxisType.TIME) {

         /* If the axis x type is time, format the unix timestamp a date */
         axisX.setFormatter(new AxisValueFormatter() {
            @Override
            public int formatValueForAutoGeneratedAxis(char[] formattedValue, float value, int autoDecimalDigits) {

               SimpleDateFormat simpleDateFormat = new SimpleDateFormat("");
               simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

               String label = simpleDateFormat.format(value);

               /* The library need the label string at the end of the formattedValue array */
               label.getChars(0, label.length(), formattedValue, formattedValue.length - label.length());

               return label.length();
            }

            /**
             * Format Values for the Plot
             * @param formattedValue
             * @param axisValue
             * @return Formatted values
             */
            @Override
            public int formatValueForManualAxis(char[] formattedValue, AxisValue axisValue) {

               return 0;
            }
         });

         /* Show max 3 date label for space issues */
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
               y = (float) train.get(i).pace;
               axisY.setName(getString(R.string.axisY_pace));
               break;
            case ALTITUDE:
               y = (float) train.get(i).altitude;
               axisY.setName(getString(R.string.axisY_altitude));
               break;
         }

         if (values.size() < 30) {
            values.add(new PointValue(x, y));
         } else {
            values.remove(0);
            values.add(new PointValue(x, y));
         }
      }

      Line line = new Line(values);
      line.setColor(ContextCompat.getColor(Training.this, R.color.colorPrimary));
      line.setHasPoints(false);
      line.setFilled(true);
      List<Line> lines = new ArrayList<>();
      lines.add(line);

      axisX.setTextColor(Color.BLACK);
      axisY.setTextColor(Color.BLACK);

      /* Plot the data */
      LineChartData data = new LineChartData();
      data.setLines(lines);
      data.setAxisXBottom(axisX);
      data.setAxisYLeft(axisY);
      graph.setZoomEnabled(false);
      graph.setContainerScrollEnabled(false, null);
      graph.setLineChartData(data);
      graph.setInteractive(true);

      Viewport v = new Viewport(graph.getMaximumViewport());
      v.top = prefsAvgStepPerMin * 2;
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

   private void accelerate() {
      arrow_up.setBackgroundResource(R.drawable.colored);
      arrow_down.setBackgroundResource(R.drawable.grey_down);
   }

   private void decelerate() {
      arrow_up.setBackgroundResource(R.drawable.grey);
      arrow_down.setBackgroundResource(R.drawable.color_down);
   }

   private void both() {
      arrow_up.setBackgroundResource(R.drawable.colored);
      arrow_down.setBackgroundResource(R.drawable.color_down);

   }

}