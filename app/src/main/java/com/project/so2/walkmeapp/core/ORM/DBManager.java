package com.project.so2.walkmeapp.core.ORM;

/**
 * Created by Alessio on 05/02/2016.
 */

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;

import org.codehaus.jackson.map.ObjectMapper;

import com.project.so2.walkmeapp.core.JacksonUtils;
import com.project.so2.walkmeapp.core.POJO.TrainingInstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DBManager extends ContextWrapper {

    private static final String TAG = "Training";
    public DBTrainings dbTrainingInstance = new DBTrainings();
    private int id;
    private String trainingDate;
    private int pref_pace;
    private int pref_lastXMeters;
    private int pref_stepLength;
    private String name;
    private ArrayList<TrainingInstant> tiList;
    private ObjectMapper mapper;
    private DatabaseHelper databaseHelper;
    private Dao<DBTrainings, String> dbDao;
    private List<DBTrainings> results;

    public DBManager(ContextWrapper context) {

        super(context);
        mapper = JacksonUtils.mapper;

    }

    public String getTrainings() {

        try {
            results = dbDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String res = null;
        if (results.size() != 0) {

            try {
                res = mapper.writeValueAsString(results); // TODO: FIX TIPO JSON
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "risultati" + res + "\n");
        }
        return res;
    }

    public int setupDB() {
        try {
            this.dbDao = getHelper().getTrainingsDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //testCreateTraining(dbTrainingInstance.id);
        return dbTrainingInstance.id;


    }


    public void createTraining(int indice, String name, String date, int pref_pace, int pref_lastXMeters, int pref_stepLength, ArrayList<TrainingInstant> tiList) {


        this.id = indice;
        this.name = name;
        this.trainingDate = date;
        this.pref_pace = pref_pace;
        this.pref_lastXMeters = pref_lastXMeters;
        this.pref_stepLength = pref_stepLength; //set from real prefs, 100 is default value
        this.tiList = tiList;
    }

    //private void saveTrainingInDB(int id, String trainingDate, int trainingSteps, int trainingDuration, int trainingDistance, int lastMetersSettings, float avgTotSpeed, float avgXSpeed, float avgTotSteps, int avgXSteps, int stepLengthInCm) {
    public void saveTrainingInDB() throws SQLException {
        if (dbDao.queryForAll() == null || dbDao.queryForAll().size() == 0) {
            dbTrainingInstance.id = 0;
        } else {
            try {
                results = dbDao.queryForAll();
                dbTrainingInstance.id = results.get(results.size() - 1).id + 1;
            } catch (SQLException e1) {
                e1.printStackTrace();
            }


        }


        dbTrainingInstance.name = this.name;
        dbTrainingInstance.trainingDate = this.trainingDate;
        dbTrainingInstance.pref_pace = this.pref_pace;
        dbTrainingInstance.pref_lastXMeters = this.pref_lastXMeters;
        dbTrainingInstance.pref_stepLength = this.pref_stepLength;
        dbTrainingInstance.setInstants(tiList);


        try {
            dbDao.create(dbTrainingInstance);
        } catch (SQLException e1) {
            e1.printStackTrace();
        }


    }

    public int getId() {

        return this.id;
    }

    public DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

}


