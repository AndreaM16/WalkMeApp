package com.project.so2.walkmeapp.core.SERVICE;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.project.so2.walkmeapp.ui.MainActivity;


public class GPS extends Service {

    private static final String TAG = "TEST GPS";
    private static final int THREAD_FINISH_MESSAGE = 1;
    public LocationManager mLocationManager;
    private static final int LOCATION_INTERVAL = 500;
    private static final float LOCATION_DISTANCE = 5;
    public Location mLastLocation;
    private Double lat;
    private Double longit;
    private final IBinder mBinder = new LocalBinder();
    private OnNewGPSPointsListener clientListener;
    private LocationListener mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);

    private class LocationListener implements android.location.LocationListener {


        public LocationListener(String provider) {
            Log.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
            if(mLastLocation.getAccuracy()<20) {
                lat = mLastLocation.getLatitude();
                longit = mLastLocation.getLongitude();
            }
            Log.d(TAG, "COORDINATE: " + " latitudine :" + mLastLocation.getLatitude() + "longitudine :" + mLastLocation.getLongitude());


        }


        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: " + location);
            if (location.getAccuracy()<20) {
                mLastLocation.set(location);
                lat = mLastLocation.getLatitude();
                longit = mLastLocation.getLongitude();
                // Aggiorna le coordinate

                if (clientListener != null) {
                    clientListener.onNewGPSPoint();
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {

            Log.d(TAG, "onProviderDisabled: " + provider);

        }


        @Override
        public void onProviderEnabled(String provider) {

            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged: " + provider);
        }
    }



    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }


    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {

            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    return;
                }
                mLocationManager.removeUpdates(mLocationListener);
                // sendMessageToActivity(mLastLocation,"Status");
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }

        }
    }

    private void initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            //Verifica se il GPS e' abilitato altrimenti avvisa l'utente

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            this.mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);
        }

    }

    public double getLatitude() {

        return lat;
    }

    public double getLongitude() {
        return longit;
    }

    public void addOnNewGPSPointsListener(OnNewGPSPointsListener listener) {
        clientListener = listener;
    }

    public void removeOnNewGPSPointsListener() {
        clientListener = null;
    }

    public class LocalBinder extends Binder {
        public GPS getService() {
            return GPS.this;
        }
    }

    public interface OnNewGPSPointsListener {
        public void onNewGPSPoint();
    }

    public boolean onUnbind(Intent intent) {
        return false;
    }
}
