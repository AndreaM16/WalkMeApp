/**
 * Operating Systems 2 Project - WALKMEAPP
 * Made by: Marco Loriga, Alessio Pili, Andrea Medda and Cristin Sanna
 */

package com.project.so2.walkmeapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
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
import com.project.so2.walkmeapp.core.ORM.DBManager;
import com.project.so2.walkmeapp.core.ORM.DatabaseHelper;
import com.project.so2.walkmeapp.core.SERVICE.GPS;

/**
 * Main Activity handles all the set up operations, handles the main services, manages onCreate()
 * and onBundle()
 * <p/>
 * THREAD_FINISH_MESSAGE Sets a message used to check if the thread is done or not
 * ACCESS_FINE_LOCATION Sets permissions
 * mMainPageList Used to set up Main Activity's View
 * mUserView Used to set up User's View
 * mService Used to handle the GPS service
 * mIsBound Checks if there is a binding between the GPS Service and the Application
 * positiveGPSPermission Used to handle GPS permissions
 */
public class MainActivity extends Activity {
   private static final int THREAD_FINISH_MESSAGE = 1;
   private static final int ACCESS_FINE_LOCATION = 0;
   public static Context context;
   private LinearLayout mMainPageList;
   private String[] mMainPageElements;
   private ImageView mUserView;
   private GPS mService;
   private boolean mIsBound;
   private boolean positiveGPSPermission = false;

   /**
    * TODO
    *
    * @param msg
    */
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

   /**
    * Triggers GPS' Activation
    */
   private Runnable mUpdateTimeTask = new Runnable() {
      public void run() {
         activateGPS();
      }
   };

   /**
    * Disconnects GPS' service using unbindService
    */
   private void disconnectLocalService() {
      if (mIsBound) {
         mService.removeOnNewGPSPointsListener();
         unbindService(mConnection);
         mIsBound = false;
      }
   }

   /**
    * TODO
    */
   private void getGPSData() {

   }

   /**
    * @param savedInstanceState stored instances of DB's elements
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      context=this;
      setContentView(R.layout.activity_main);
      mMainPageElements = getResources().getStringArray(R.array.main_page_list_items);
      mMainPageList = (LinearLayout) findViewById(R.id.main_page_list);
      mUserView = (ImageView) findViewById(R.id.user_icon_view);
      View mView = findViewById(R.id.walking_man);
      mView.setBackgroundResource(R.drawable.walking_stickman);

      /* AnimationDrawable Handles Stickman's animation */
      AnimationDrawable animMan = (AnimationDrawable) mView.getBackground();
      animMan.start();

      mUserView.bringToFront();

      getWindow().getDecorView().setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      getWindow().setStatusBarColor(Color.TRANSPARENT);

      /* Initializing DBhelper and DBManager*/
      DatabaseHelper.initialize(this);
      DBManager.initialize(this);

      String[] mMenuStrings = getResources().getStringArray(R.array.main_page_list_items);

      for (final String i : mMenuStrings) {

         /* Creating an instance for View Object */
         final View v;
         LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         v = inflater.inflate(R.layout.menu_fragment_item, null);
         TextView textTemp = (TextView) v.findViewById(R.id.item_text);
         textTemp.setText(i);

         v.setOnClickListener(new View.OnClickListener() {

            /**
             * @param v View
             */
            @Override
            public void onClick(View v) {
               launchNextActivity(i);
            }
         });

         mMainPageList.addView(v);

      }

      /* Checking service's permissions, if not allowed, asks for them */
      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
              != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
              (this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
              != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
              (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

         ActivityCompat.requestPermissions(this,
                 new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION},
                 1);
         return;
      }

      connectLocalService();

   }

   /**
    * Binding GPS Service to the Application
    *
    * @param service Service used
    * @param conn    Used to  establishing the connection
    * @param flags   Flags used to establish the connection
    * @return returns the Bound Service
    */
   @Override
   public boolean bindService(Intent service, ServiceConnection conn, int flags) {
      return super.bindService(service, conn, flags);
   }

   /* Establishing Connection */
   private ServiceConnection mConnection = new ServiceConnection() {

      /**
       * Handling GPS
       * @param className ClassName passed for the connection
       * @param service Service passed in order to communicate
       */
      @Override
      public void onServiceConnected(ComponentName className, IBinder service) {

         GPS.LocalBinder binder = (GPS.LocalBinder) service;
         MainActivity.this.mService = (GPS) binder.getService();
         mIsBound = true;
         if (!mService.mLocationManager.isProviderEnabled("gps")) {
            Toast.makeText(MainActivity.this, "GPS Currently Disabled. It's possible Enabling it from Settings.",
                    Toast.LENGTH_LONG).show();
            handleThreadMsg.postDelayed(mUpdateTimeTask, 3000);
         }

         GPS.OnNewGPSPointsListener clientListener = new GPS.OnNewGPSPointsListener() {
            @Override
            public void onNewGPSPoint() {
               getGPSData();
            }
         };

         mService.addOnNewGPSPointsListener(clientListener);
      }

      /**
       * Handling GPS Service disconnection
       * @param name Id Service Passed
       */
      @Override
      public void onServiceDisconnected(ComponentName name) {
         Log.i("ConnectionService", "Disconnected");
         mService = null;

      }

   };

   /**
    * Check GPS Service Permissions
    *
    * @param requestCode  Service's request code
    * @param permissions  Service's Permissions
    * @param grantResults Permission Granted or Denied
    */
   @Override
   public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
      switch (requestCode) {
         case ACCESS_FINE_LOCATION: {
            /* If request is cancelled, the result arrays are empty. */
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               positiveGPSPermission = true;
            } else {
               Toast.makeText(this, "GPS Service Permissions Denied, " + "application will not be able to " +
                       "use functionality involving current position ", Toast.LENGTH_LONG).show();
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

   /**
    * Handling Activity Switching
    **/
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
         disconnectLocalService();
      }

   }


}

