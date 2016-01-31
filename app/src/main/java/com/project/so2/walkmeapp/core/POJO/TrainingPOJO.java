package com.project.so2.walkmeapp.core.POJO;

import java.io.Serializable;

/**
 * Created by Ale on 28/01/2016.
 */
public class TrainingPOJO implements Serializable {


    public int id;
    public String trainingDate;
    public int trainingSteps;
    public int trainingDuration;
    public int trainingDistance;
    public int lastMetersSettings;
    public float avgTotSpeed;
    public float avgXSpeed;
    public float avgTotSteps;
    public int avgXSteps;
    public int stepLenghtInCm;

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
