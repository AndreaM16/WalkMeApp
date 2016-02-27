package com.project.so2.walkmeapp.core.SERVICE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.project.so2.walkmeapp.core.JacksonUtils;
import com.project.so2.walkmeapp.core.ORM.DBManager;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;
import com.project.so2.walkmeapp.core.ORM.DatabaseHelper;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 * Created by alex_ on 26/02/2016.
 */
public class FileHandling extends Activity {

    private DBManager db;
    public static DBTrainings training;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

  /* Initializing DBhelper and DBManager*/
        DatabaseHelper.initialize(this);
        DBManager.initialize(this);


        db=DBManager.getIstance();
        ObjectMapper mapper = JacksonUtils.mapper;

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();

        if (getIntent().getData() != null) {


            String filePath = getIntent().getData().getPath();
            try {
                StringBuilder text = new StringBuilder();

                //read the file passed in the intent
                BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                }
                br.close();

                Log.d("BAU:training",text.toString());


                //convert the json file in a WorkoutItem object
                training = mapper.readValue(text.toString(), DBTrainings.class);

                //Intent intent_view_training=new Intent(this,ViewTraining.class);
               // startActivity(intent_view_training);

                        db.saveImportedTraining(training,getApplicationContext());



            } catch (IOException e) {
        e.printStackTrace();

        }
        }
        }
}





