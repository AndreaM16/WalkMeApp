package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.so2.walkmeapp.R;

public class MainActivity extends Activity {
   private LinearLayout mMainPageList;
   private String[] mMainPageElements;
   private ImageView mUserView;
   private View mView;


   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      //Get the needed Views & StringArray
      mMainPageElements = getResources().getStringArray(R.array.main_page_list_items);
      mMainPageList = (LinearLayout) findViewById(R.id.main_page_list);
      mUserView = (ImageView) findViewById(R.id.user_icon_view);

      //Animate the Stickman
      mView = findViewById(R.id.walking_man);
      mView.setBackgroundResource(R.drawable.walking_stickman);
      AnimationDrawable wMan = (AnimationDrawable) mView.getBackground();
      wMan.start();

      //Brings to front the User Avatar. TODO: BUG TO FIX - it's possible to tap through it
      mUserView.bringToFront();

      //Makes the System StatusBar transparent
      getWindow().getDecorView().setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                      | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      getWindow().setStatusBarColor(Color.TRANSPARENT);

      //Create the menu entries
      String[] mMenuStrings = getResources().getStringArray(R.array.main_page_list_items);

      for (final String i : mMenuStrings) {

         //Get a new View and the Inflater Service
         View v;
         LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         v = inflater.inflate(R.layout.menu_fragment_item, null);

         //Customize the View
         TextView textTemp = (TextView) v.findViewById(R.id.item_text);
         textTemp.setText(i);

         //Attach a listener to the View (useful for intent launching)
         v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               launchNextActivity(i);
            }
         });

         //Add created View to the Layout
         mMainPageList.addView(v);
      }

   }

   //"Choose" an intent based on input and launches corresponding activity.
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