package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;
import com.project.so2.walkmeapp.R;

/**
 * Created by Andrea on 24/01/2016.
 */
public class History extends Activity{

    private static final String TAG = "History";
    private Dao<DBTrainings, String> dbTrainingDao;
    private String[] mLoadTrainingsElements;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding Class to its View
        setContentView(R.layout.history_trainings);

        //Binding Strings to their View
        mLoadTrainingsElements = getResources().getStringArray(R.array.history_list_items);

        ImageView actionBar = (ImageView) findViewById(R.id.action_bar_icon);
        actionBar.setImageResource(R.drawable.btn_back);
        actionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView actionBarText = (TextView) findViewById(R.id.action_bar_title);

        actionBarText.setText(getResources().getString(R.string.history_title));


    }
}


/*
{
        "id : "0",
        "trainingDate" : "Wed Jan 27 15:30:56 GMT+01:00 2016" ,
        "trainingSteps" : "500",
        "trainingDuration" : trainingDuration,
        "trainingDistance" : trainingDistance,
        "lastMetersSettings" : lastMetersSettings,
        "avgTotSpeed" : avgTotSpeed,
        "avgXSpeed" : avgXSpeed,
        "avgTotSteps" : avgTotSteps,
        "avgXSteps" : avgXSteps,
        "stepLengthInCm" : stepLengthInCm
}
 */
