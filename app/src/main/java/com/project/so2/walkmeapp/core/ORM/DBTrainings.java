package com.project.so2.walkmeapp.core.ORM;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.project.so2.walkmeapp.core.POJO.TrainingInstant;

import java.util.ArrayList;
import java.util.Collection;

@DatabaseTable(tableName = "trainings")
public class DBTrainings {

    @DatabaseField(id = true)
    public int id;
    @DatabaseField
    public String name;
    @DatabaseField
    public String trainingDate;
    @DatabaseField
    public int pref_pace;
    @DatabaseField
    public int pref_lastXMeters;
    @DatabaseField
    public int pref_stepLength;
    @ForeignCollectionField(eager = true)
    Collection<TrainingInstant> tiList;



    public DBTrainings() {
        // ORMLite needs a no-arg constructor
    }
    public DBTrainings(int id, String trainingDate, int trainingSteps, int lastMetersSettings, int stepLengthInCm) {

        this.id = id;
        this.trainingDate=trainingDate;
        this.pref_pace=trainingSteps;
        this.pref_lastXMeters=lastMetersSettings;
        this.pref_stepLength = stepLengthInCm; //in cm

    }

    public ArrayList<TrainingInstant> getInstants() {
        ArrayList<TrainingInstant> tiArray = new ArrayList<>();
        for (TrainingInstant ti : tiList) {
            tiArray.add(ti);
        }
        return tiArray;
    }

    public void setInstants(ArrayList<TrainingInstant> instants) throws java.sql.SQLException {
        if (this.tiList == null) {
            Dao<DBTrainings, String> dao = DatabaseHelper.getIstance().getTrainingsDao();
            this.tiList = dao.getEmptyForeignCollection("tiList");
        }
        this.tiList.addAll(instants);
    }

}


