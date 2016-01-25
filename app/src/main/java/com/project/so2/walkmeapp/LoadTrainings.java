package com.project.so2.walkmeapp;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;

/**
 * Created by Andrea on 24/01/2016.
 */
public class LoadTrainings extends MainActivity{

    private String[] mLoadTrainingsElements;
    //private DrawerLayout mLoadTrainingsLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_trainings);

        mLoadTrainingsElements = getResources().getStringArray(R.array.load_trainings_list_items);
        //mLoadTrainingsLayout = (LoadTrainingsLayout) findViewById(R.id.load_trainings);
    }
}
