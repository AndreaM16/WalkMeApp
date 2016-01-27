package com.project.so2.walkmeapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Andrea on 24/01/2016.
 */
public class Training extends Activity{

    //private String[] mMainTrainingElements;

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

    }
}
