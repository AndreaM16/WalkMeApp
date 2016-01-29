package com.project.so2.walkmeapp.POJO;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ale on 28/01/2016.
 */
public class TrainingPOJO implements Serializable {


    public int id;
    public String trainingDate;
    public int trainingSteps;
    public int trainingDuration; //TODO: check if there is a better type
    public int trainingDistance;
    public int lastMetersSettings;
    public float avgTotSpeed;
    public float avgXSpeed;
    public float avgTotSteps;
    public int avgXSteps;
    public int stepLenghtInCm; //in cm

    public TrainingPOJO(
            int id,
            String trainingDate,
            int trainingSteps,
            int trainingDuration, //TODO: check if there is a better type
            int trainingDistance,
            int lastMetersSettings,
            float avgTotSpeed,
            float avgXSpeed,
            float avgTotSteps,
            int avgXSteps,
            int stepLenghtInCm) {

        this.id = id;
        this.trainingDate = trainingDate;
        this.trainingSteps = trainingSteps;
        this.trainingDuration = trainingDuration;
        this.trainingDistance = trainingDistance;
        this.lastMetersSettings = lastMetersSettings;
        this.avgTotSpeed = avgTotSpeed;
        this.avgXSpeed = avgXSpeed;
        this.avgTotSteps = avgTotSteps;
        this.avgXSteps = avgXSteps;
        this.stepLenghtInCm = stepLenghtInCm;
    }


}
