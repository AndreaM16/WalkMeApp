package com.project.so2.walkmeapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.so2.walkmeapp.R;

/**
 * Created by andream16 on 1/27/16.
 */
public class Presets extends Activity {

    //private String[] mPresetElements;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding Class to its View
        //setContentView(R.layout.presets_layout);

        //Binding Strings to their View
        //mPresetElements = getResources().getStringArray(R.array.preset_list_items);

        ImageView actionBar = (ImageView) findViewById(R.id.action_bar_icon);
        actionBar.setImageResource(R.drawable.btn_back);
        actionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //TextView actionBarText = (TextView) findViewById(R.id.presets_title);

        //actionBarText.setText(getResources().getString(R.string.main_training_title));

    }
}

