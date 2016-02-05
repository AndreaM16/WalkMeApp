package com.project.so2.walkmeapp.core.ORM;

/**
 * Created by Alessio on 05/02/2016.
 */
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;
import com.project.so2.walkmeapp.core.ORM.DatabaseHelper;
import com.project.so2.walkmeapp.core.POJO.TrainingPOJO;
import java.sql.SQLException;
import org.codehaus.jackson.map.ObjectMapper;
import com.project.so2.walkmeapp.core.ORM.DatabaseHelper;
import com.project.so2.walkmeapp.core.JacksonUtils;
import java.io.IOException;
import java.util.List;


public class DBManager extends ContextWrapper {

    private static final String TAG = "Training";
    private DBTrainings dbTrainingInstance=new DBTrainings();
    private int id;
    private String trainingDate;
    private int trainingSteps;
    private int trainingDuration;
    public int trainingDistance;
    private int lastMetersSettings;
    private float avgTotSpeed;
    private float avgXSpeed;
    private float avgTotSteps;
    private int avgXSteps;

    private ObjectMapper mapper;
    private DatabaseHelper databaseHelper;
    private Dao<DBTrainings, String> dbDao;
    private List<DBTrainings> results;
    private List<DBTrainings> lista;
    private Context context;

    public DBManager(ContextWrapper context){
        super(context);
        mapper=JacksonUtils.mapper;

    }















    public String getTrainings() {

        try {
            results=dbDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String res = null;
       if(results.size()!=0){

            try {
                res = mapper.writeValueAsString(results);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "risultati" + res + "\n");}
        return res;
    }

    public int setupDB() {
        try {
            this.dbDao = getHelper().getTrainingsDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        testCreateTraining(dbTrainingInstance.id);
        return dbTrainingInstance.id;


    }


    public void testCreateTraining(int indice) {


        this.id = indice;
        this.trainingDate = "2015-12-11 18:00:23";
        this.trainingSteps = 100;
        this.trainingDuration = 500; //TODO: check if there is a better type
        this.trainingDistance = 1000;
        //this.lastMetersSettings = 30; set from real prefs, 10 is default value
        this.avgTotSpeed = 10;
        this.avgXSpeed = 4;
        this.avgTotSteps = 600;
        this.avgXSteps = 30;
        //this.prefsstepLengthInCm = 70; set from real prefs, 100 is default value
    }

    //private void saveTrainingInDB(int id, String trainingDate, int trainingSteps, int trainingDuration, int trainingDistance, int lastMetersSettings, float avgTotSpeed, float avgXSpeed, float avgTotSteps, int avgXSteps, int stepLengthInCm) {
    public void saveTrainingInDB() {


        try {
        /* this.id = id+ 1;
         this.trainingDate = trainingDate;
         this.trainingSteps = trainingSteps;
         this.trainingDuration = trainingDuration; //TODO: check if there is a better type
         this.trainingDistance = trainingDistance;
         this.lastMetersSettings = lastMetersSettings;
         this.avgTotSpeed = avgTotSpeed;
         this.avgXSpeed = avgXSpeed;
         this.avgTotSteps = avgTotSteps;
         this.avgXSteps = avgXSteps;
         this.prefsstepLengthInCm = stepLengthInCm; //in cm
*/          results=dbDao.queryForAll();
            dbTrainingInstance.id = (results.get(results.size()-1).id) +1;
            dbTrainingInstance.trainingDate = this.trainingDate;
            dbTrainingInstance.trainingSteps = this.trainingSteps;
            dbTrainingInstance.trainingDuration = this.trainingDuration;
            dbTrainingInstance.trainingDistance = this.trainingDistance;
            dbTrainingInstance.lastMetersSettings = this.lastMetersSettings;
            dbTrainingInstance.avgTotSpeed = this.avgTotSpeed;
            dbTrainingInstance.avgXSpeed = this.avgXSpeed;
            dbTrainingInstance.avgTotSteps = this.avgTotSteps;
            dbTrainingInstance.avgXSteps = this.avgXSteps;
            dbTrainingInstance.stepLengthInCm = this.lastMetersSettings;


            dbDao.create(dbTrainingInstance);
        } catch (
                Exception e
                )

        {
            e.printStackTrace();
        }


    }
    public int getId(){

        return this.id;
    }

    public DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

}


