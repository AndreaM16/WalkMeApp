package com.project.so2.walkmeapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;

/**
 * Created by Andrea on 24/01/2016.
 */
public class LoadTrainings extends Activity{

    private String[] mLoadTrainingsElements;

    @Override
    //Bundle -> Allows passing values between Activities
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding Class to its View
        setContentView(R.layout.load_trainings);

        //Binding Strings to their View
        mLoadTrainingsElements = getResources().getStringArray(R.array.load_trainings_list_items);

    }
}
