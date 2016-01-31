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


   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);


      mMainPageElements = getResources().getStringArray(R.array.main_page_list_items);

      mMainPageList = (LinearLayout) findViewById(R.id.main_page_list);
      mUserView = (ImageView) findViewById(R.id.user_icon_view);
      View mView = findViewById(R.id.walking_man);
      mView.setBackgroundResource(R.drawable.walking_stickman);
      AnimationDrawable sbu = (AnimationDrawable) mView.getBackground();
      sbu.start();

      mUserView.bringToFront();



      getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      getWindow().setStatusBarColor(Color.TRANSPARENT);

      String[] mMenuStrings = getResources().getStringArray(R.array.main_page_list_items);

      for(final String i: mMenuStrings) {

         final View v; // Creating an instance for View Object
         LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         v = inflater.inflate(R.layout.menu_fragment_item, null);
         TextView textTemp = (TextView) v.findViewById(R.id.item_text);
         textTemp.setText(i);

         v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               launchNextActivity(i);
            }
         });

         mMainPageList.addView(v);

}

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