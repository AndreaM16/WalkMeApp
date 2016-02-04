package com.project.so2.walkmeapp.core.POJO;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * Created by Ale on 28/01/2016.
 */
public class TrainingPOJO implements Serializable {

@DatabaseField(columnName = "id")
    public int id;
    @DatabaseField(columnName = "trainingDate")
    public String trainingDate;
    @DatabaseField(columnName = "trainingSteps")
    public int trainingSteps;
    @DatabaseField(columnName = "trainingDuration")
    public int trainingDuration;
    @DatabaseField(columnName = "trainingDistance")
    public int trainingDistance;
    @DatabaseField(columnName = "lastMetersSettings")
    public int lastMetersSettings;
    @DatabaseField(columnName = "avgTotSpeed")
    public float avgTotSpeed;
    @DatabaseField(columnName = "avgXSpeed")
    public float avgXSpeed;
    @DatabaseField(columnName = "avgTotSteps")
    public float avgTotSteps;
    @DatabaseField(columnName = "avgXSteps")
    public int avgXSteps;
    @DatabaseField(columnName = "stepLengthInCm")
    public int stepLengthInCm;

    public TrainingPOJO(
            int id,
            String trainingDate,
            int trainingSteps,
            int trainingDuration,
            int trainingDistance,
            int lastMetersSettings,
            float avgTotSpeed,
            float avgXSpeed,
            float avgTotSteps,
            int avgXSteps,
            int stepLengthInCm) {

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
        this.stepLengthInCm = stepLengthInCm;
    }


}
