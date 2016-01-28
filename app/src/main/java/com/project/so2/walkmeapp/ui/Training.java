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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.pedometer.Pedometer;

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



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding Class to its View
        setContentView(R.layout.training_main);

        //Binding Strings to their View
        //mMainTrainingElements = getResources().getStringArray(R.array.main_training_list_items);

        ImageView actionBar = (ImageView) findViewById(R.id.action_bar_icon);
        actionBar.setImageResource(R.drawable.btn_back);
        actionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView actionBarText = (TextView) findViewById(R.id.action_bar_title);

        actionBarText.setText(getResources().getString(R.string.main_training_title));
        itemTest = (TextView) findViewById(R.id.item_text2Left);

        /*sMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        steps = sMgr.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        s*/


        Pedometer.
    }

}

