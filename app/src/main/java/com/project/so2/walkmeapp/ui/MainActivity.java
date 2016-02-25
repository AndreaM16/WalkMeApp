package com.project.so2.walkmeapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.ORM.DatabaseHelper;
import com.project.so2.walkmeapp.core.SERVICE.GPS;

public class MainActivity extends Activity {
   private static final int THREAD_FINISH_MESSAGE = 1;
   private static final int ACCESS_FINE_LOCATION = 0;
   private LinearLayout mMainPageList;
   private String[] mMainPageElements;
   private ImageView mUserView;
   private GPS mService;
   private boolean mIsBound;
   private boolean positiveGPSPermission = false;


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

   private Runnable mUpdateTimeTask = new Runnable() {
      public void run() {
         activateGPS();
      }
   };


   private void disconnectLocalService() {
      if (mIsBound) {
         mService.removeOnNewGPSPointsListener();
         unbindService(mConnection);
         mIsBound = false;
      }
   }

   private void getGPSData() {

   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      mMainPageElements = getResources().getStringArray(R.array.main_page_list_items);

      mMainPageList = (LinearLayout) findViewById(R.id.main_page_list);
      mUserView = (ImageView) findViewById(R.id.user_icon_view);
      View mView = findViewById(R.id.walking_man);
      mView.setBackgroundResource(R.drawable.walking_stickman);
      AnimationDrawable animMan = (AnimationDrawable) mView.getBackground();

      animMan.start();

      mUserView.bringToFront();

      getWindow().getDecorView().setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                      | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      getWindow().setStatusBarColor(Color.TRANSPARENT);

      DatabaseHelper.initialize(this);

      String[] mMenuStrings = getResources().getStringArray(R.array.main_page_list_items);

      for (final String i : mMenuStrings) {

         final View v; // Creating an instance for View Object
         LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         v = inflater.inflate(R.layout.menu_fragment_item, null);
         TextView textTemp = (TextView) v.findViewById(R.id.item_text);
         textTemp.setText(i);
         connectLocalService();


         v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               launchNextActivity(i);
            }
         });

         mMainPageList.addView(v);

      }
      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
         ActivityCompat.requestPermissions(this,
                 new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                 ACCESS_FINE_LOCATION);
         return;
      }

   }

   @Override
   public boolean bindService(Intent service, ServiceConnection conn, int flags) {
      return super.bindService(service, conn, flags);
   }

   private ServiceConnection mConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName className, IBinder
              service) {

         GPS.LocalBinder binder = (GPS.LocalBinder) service;
         MainActivity.this.mService = (GPS) binder.getService();
         mIsBound = true;
         if (!mService.mLocationManager.isProviderEnabled("gps")) {
            Toast.makeText(MainActivity.this, "GPS e' attualmente disabilitato. E' possibile abilitarlo dal menu impostazioni.",
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

      }

   };

   @Override
   public void onRequestPermissionsResult(int requestCode,
                                          String permissions[], int[] grantResults) {
      switch (requestCode) {
         case ACCESS_FINE_LOCATION: {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               positiveGPSPermission = true;
            } else {
               Toast.makeText(this, "Permesso per il GPS negato, non sarai in grado di utilizzare le funzionalità che sfruttano la posizione", Toast.LENGTH_LONG).show();
            }
            return;
         }
      }
   }

   public void activateGPS() {
      Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
      startActivity(intent);
   }

   private void connectLocalService() {
      Intent service = new Intent(this, GPS.class);
      bindService(service, mConnection, Context.BIND_AUTO_CREATE);
   }

   private void launchNextActivity(String activityName) {
      Intent intent = null;

      switch (activityName) {

         case "History":
            intent = new Intent(this, History.class);
            break;

         case "Start":
            intent = new Intent(this, Training.class);
            break;

         case "Settings":
            intent = new Intent(this, Settings.class);
            break;

         case "About":
            intent = new Intent(this, About.class);
            break;


      }
      if (intent != null) {
         startActivity(intent);
      }

   }


}

