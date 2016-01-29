package com.project.so2.walkmeapp.POJO;
import

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ale on 28/01/2016.
 */
public class TrainingPOJO implements Serializable{



    public int id;
    public Date trainingDate;
    public int trainingSteps;
    public int trainingDuration; //TODO: check if there is a better type
    public int trainingDistance;
    public int lastMetersSettings;
    public float avgTotSpeed;
    public float avgXSpeed;
    public float avgTotSteps;
    public int avgXSteps;
    public int stepLenghtInCm; //in cm


}
