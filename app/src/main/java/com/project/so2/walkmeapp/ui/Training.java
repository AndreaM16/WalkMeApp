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
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.so2.walkmeapp.core.ORM.DBManager;

import org.codehaus.jackson.map.ObjectMapper;

import com.project.so2.walkmeapp.core.JacksonUtils;

import java.io.IOException;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.io.File;

import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;
import com.project.so2.walkmeapp.core.POJO.TrainingInstant;
import com.project.so2.walkmeapp.core.PausableChronometer;
import com.project.so2.walkmeapp.core.SERVICE.AttachmentHandling;
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
    private boolean tiReset = false;
    private long startTime = -1000;
    private float actualSteps;
    private long actualTime;
    private DBManager db;
    private String name = "SBURRO";
    private int pref_pace;
    private int pref_lastXMeters;
    private int pref_stepLength;
    private String formattedDate;
    private TextView lat;
    private TextView longit;
    private TextView distanza_text;
    private double distance = 0.0;
    private ArrayList<Location> list = new ArrayList<Location>();


    private ContextWrapper context = this;
    private boolean mIsBound;
    private Intent serviceIntent;
    private GPS mService;
    public static File training;

    private boolean isEnded = false;


    private Location previousLoc;
    private boolean wasInPause = false;
    private boolean utenteAvvisato=false;
    private DBTrainings last_training;


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
                    if(utenteAvvisato==false) {

                        Toast.makeText(Training.this, "Tieni premuto per concludere l'allenamento", Toast.LENGTH_LONG).show();
                        utenteAvvisato=true;
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
                        builder.setTitle("Nome allenamento");

// Set up the input
                        final EditText input = new EditText(Training.this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        builder.setView(input);

// Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                name = input.getText().toString();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                       // builder.show();

                        isEnded = true;
                        //passare nome allenamento
                        db.createTraining(name, formattedDate, pref_pace, pref_lastXMeters, pref_stepLength, trainingInsts);
                        try {
                            db.saveTrainingInDB();
                            last_training=db.getLastTraining();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        Intent intent=new Intent(Training.this,ViewTraining.class);
                        intent.putExtra("id",last_training.id);
                        startActivity(intent);

                        chronometer.reset();

                        // potenzialmente non necessari, non serve resettare l'activity se la abbandoniamo
//                    stepsPerMin.setText("0");
//                    kilometersPerHour.setText("0");
//                    actualSteps = 0;
//                    actualTime = 0;
//                    isInitialValueSet = false;


                     /*   AttachmentHandling attachmentHandling = new AttachmentHandling();
                        attachmentHandling.share(Training.this);
*/
                        disconnectLocalService();
                       /* Intent intent = new Intent(Training.this, Settings.class);
                        startActivity(intent);*/
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
        previousLoc = loc;

        //lat.setText(trainingInsts.size());
        longit.setText(format(distance));

        ti = new TrainingInstant(db.dbTrainingInstance, latitude, longitude, speed, altitude, time, distance);

        //id_tInstance++;
        trainingInsts.add(ti);
        lat.setText(Integer.toString(trainingInsts.size()));
        Log.d("ti", "latitudine: " + ti.latitude + " longitudine: " + ti.longitude + " velocità: " + ti.speed + " altitudine: " + ti.altitude + " tempo: " + ti.time + " distanza: " + ti.distance);

    }


    public static String format(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0000000");
        return decimalFormat.format(value);
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
                finish();
            }
        });

        actionBarText.setText(getResources().getString(R.string.main_training_title));
    }
}