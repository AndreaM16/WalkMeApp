package com.project.so2.walkmeapp.pedometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by loriz on 1/28/16.
 */
public class Pedometer {

    private static Pedometer mInstance;
    private SensorManager sensorService;
    private Sensor sensor;
    private Context mContext;


    public static Pedometer getInstance (Context context) {

        if(mInstance == null)
            mInstance = new Pedometer(context);
        else {
            mInstance.mContext = context;
        }

        return mInstance;
    }


   private Pedometer (Context context) {
       sensorService = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
       sensor = sensorService.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

       SensorEventListener mySensorEventListener =new SensorEventListener() {

           @Override
           public void onAccuracyChanged(Sensor sensor, int accuracy) {
           }

           @Override
           public void onSensorChanged(SensorEvent event) {

               float[] steps = event.values;

           }
       };


       if (sensor != null) {
           sensorService.registerListener(mySensorEventListener, sensor,
                   SensorManager.SENSOR_DELAY_FASTEST);
       }


       return mySensorEventListener ;
   }



}

