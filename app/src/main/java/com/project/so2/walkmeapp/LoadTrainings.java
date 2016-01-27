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
public class LoadTrainings extends Activity{

    private String[] mLoadTrainingsElements;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding Class to its View
        setContentView(R.layout.load_trainings);

        //Binding Strings to their View
        mLoadTrainingsElements = getResources().getStringArray(R.array.load_trainings_list_items);

        ImageView actionBar = (ImageView) findViewById(R.id.action_bar_icon);
        actionBar.setImageResource(R.drawable.btn_back);
        actionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView actionBarText = (TextView) findViewById(R.id.action_bar_title);

        actionBarText.setText(getResources().getString(R.string.load_trainings_title));

    }
}
