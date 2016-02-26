package com.project.so2.walkmeapp.core.SERVICE;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.project.so2.walkmeapp.core.JacksonUtils;
import com.project.so2.walkmeapp.core.ORM.DBManager;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;

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
    private DBTrainings training;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db=DBManager.getIstance();
        ObjectMapper mapper = JacksonUtils.mapper;

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
                Log.d("training",text.toString());


                //convert the json file in a WorkoutItem object
                training = mapper.readValue(text.toString(), DBTrainings.class);

                        db.saveImportedTraining(training);



            } catch (IOException e) {
        e.printStackTrace();

        }
        }
        }
}





