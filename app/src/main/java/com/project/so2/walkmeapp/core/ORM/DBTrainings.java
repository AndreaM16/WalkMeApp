package com.project.so2.walkmeapp.core.ORM;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "trainings")
public class DBTrainings {

    @DatabaseField(id = true)
    public int id;
    @DatabaseField
    public String trainingDate;
    @DatabaseField
    public int trainingSteps;
    @DatabaseField
    public int trainingDuration; //TODO: check if there is a better type
    @DatabaseField
    public int trainingDistance;
    @DatabaseField
    public int lastMetersSettings;
    @DatabaseField
    public float avgTotSpeed;
    @DatabaseField
    public float avgXSpeed;
    @DatabaseField
    public float avgTotSteps;
    @DatabaseField
    public int avgXSteps;
    @DatabaseField
    public int stepLengthInCm; //in cm



    public DBTrainings() {
        // ORMLite needs a no-arg constructor
    }
    public DBTrainings(int id, String trainingDate, int trainingSteps, int trainingDuration, int trainingDistance, int lastMetersSettings, float avgTotSpeed, float avgXSpeed, float avgTotSteps, int avgXSteps, int stepLengthInCm) {


        this.id = id;
        this.trainingDate=trainingDate;
        this.trainingSteps=trainingSteps;
        this.trainingDuration=trainingDuration; //TODO: check if there is a better type
        this.trainingDistance=trainingDistance;
        this.lastMetersSettings=lastMetersSettings;
        this.avgTotSpeed=avgTotSpeed;
        this.avgXSpeed=avgXSpeed;
        this.avgTotSteps=avgTotSteps;
        this.avgXSteps=avgXSteps;
        this.stepLengthInCm = stepLengthInCm; //in cm

    }

}


