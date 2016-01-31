package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.project.so2.walkmeapp.R;

/**
 * Created by Andrea on 30/01/2016.
 */
public class About extends Activity {

   //private String[] mSettingsElements;
   private static int HEART_UNICODE = 0x2764;
   private String[] devNamesArray;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      //Binding Class to its View
      setContentView(R.layout.about_layout);

      //Binding Strings to their View


      devNamesArray = getResources().getStringArray(R.array.about_grid_items);

      ImageView actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      TextView about_body = (TextView) findViewById(R.id.about_body);
      View mView = findViewById(R.id.walking_man);
      LinearLayout devLinLayout = (LinearLayout) findViewById(R.id.about_grid);

      //setup
      mView.setBackgroundResource(R.drawable.walking_stickman);
      AnimationDrawable wMan = (AnimationDrawable) mView.getBackground();
      wMan.start();

      for (String i: devNamesArray) {

         final View v; // Creating an instance for View Object
         LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         v = inflater.inflate(R.layout.about_user_template, null);
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
         if ( temp != null ) {
            imgTemp.setBackground(temp);
         }

         devLinLayout.addView(v);
      }




      actionBar.setImageResource(R.drawable.btn_back);
      actionBar.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            finish();
         }
      });

      //Action Bar Title
      TextView actionBarText = (TextView) findViewById(R.id.action_bar_title);
      actionBarText.setText(getResources().getString(R.string.about_title));

      String about_text = (getString(R.string.about_text_body) + getEmijoByUnicode(HEART_UNICODE) + getString(R.string.about_text_body_cont));
      about_body.setText(about_text);


        /*//External Link
        TextView link = (TextView) findViewById(R.id.about_external_link);
        String linkText = "<a href=\"https://github.com/AndreaM16/WalkMeApp\">View Project on GitHub</a>";
        link.setText(Html.fromHtml(linkText));
        link.setMovementMethod(LinkMovementMethod.getInstance());*/

   }

   public String getEmijoByUnicode(int unicode) {
      return new String(Character.toChars(unicode));
   }

}
