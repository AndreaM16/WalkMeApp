package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.project.so2.walkmeapp.R;

public class About extends Activity {

   private static int HEART_UNICODE = 0x2764;
   private String[] devNamesArray;
   private ImageView actionBar;
   private TextView about_body;
   private View mView;
   private LinearLayout devLinLayout;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.about_layout);

      //Get the needed Views
      actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      about_body = (TextView) findViewById(R.id.about_body);
      mView = findViewById(R.id.walking_man);
      devLinLayout = (LinearLayout) findViewById(R.id.about_grid);

      //ActionBar customization
      actionBar.setImageResource(R.drawable.btn_back);
      TextView actionBarText = (TextView) findViewById(R.id.action_bar_title);
      actionBarText.setText(getResources().getString(R.string.about_title));
      actionBar.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            finish();
         }
      });

      //Prepare Body text (with emoji! yay!)
      String about_text = (getString(R.string.about_text_body) + new String(Character.toChars(HEART_UNICODE)) + getString(R.string.about_text_body_cont));
      about_body.setText(about_text);

      //Animate the Stickman
      mView.setBackgroundResource(R.drawable.walking_stickman);
      AnimationDrawable wMan = (AnimationDrawable) mView.getBackground();
      wMan.start();

      //Create the dev badges
      devNamesArray = getResources().getStringArray(R.array.about_grid_items);

      for (String i : devNamesArray) {

         //Get a new View and the Inflater Service
         View v;
         LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         v = inflater.inflate(R.layout.about_user_template, null);

         //Customize the View
         TextView textTemp = (TextView) v.findViewById(R.id.about_user_name);
         ImageView imgTemp = (ImageView) v.findViewById(R.id.about_user_img);
         textTemp.setText(i);

         Drawable temp = null;

         switch (i) {
            case "Marco":
               temp = getDrawable(R.drawable.mar);
               break;

            case "Andrea":
               temp = getDrawable(R.drawable.and);
               break;

            case "Alessio":
               temp = getDrawable(R.drawable.ale);
               break;

            case "Cristin":
               temp = getDrawable(R.drawable.cri);
               break;

         }
         if (temp != null) {
            imgTemp.setBackground(temp);
         }

         //Add created View to the LinearLayout
         devLinLayout.addView(v);
      }

   }

}
