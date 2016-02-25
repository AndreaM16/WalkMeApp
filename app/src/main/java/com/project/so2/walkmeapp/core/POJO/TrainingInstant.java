package com.project.so2.walkmeapp.core.POJO;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;

/**
 * Created by marco on 15/02/16.
 * This class models an instant of the workout
 */

@DatabaseTable(tableName = "training_instants")
public class TrainingInstant {

   @DatabaseField(foreign = true)
   public transient DBTrainings training;
   @DatabaseField(generatedId = true)
   public int id;
   @DatabaseField
   public double latitude;
   @DatabaseField
   public double longitude;
   @DatabaseField
   public double altitude;
   @DatabaseField
   public double speed;
   @DatabaseField
   public double pace;
   @DatabaseField
   public long time;
   @DatabaseField
   public double distance;


   public TrainingInstant() {
      // ORMLite needs a no-arg constructor
   }

   public TrainingInstant( DBTrainings training, double latitude, double longitude, double altitude, double speed, long time, double distance) {

      this.training = training;
      this.latitude = latitude;
      this.longitude = longitude;
      this.altitude = altitude;
      this.speed = speed;
      this.time = time;
      this.distance = distance;

      if (speed == 0) {
         this.pace = 0;
      } else {
         this.pace = (1 / speed) * 60;
      }
   }

}