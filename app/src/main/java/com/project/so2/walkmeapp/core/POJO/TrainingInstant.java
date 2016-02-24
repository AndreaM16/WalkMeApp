package com.project.so2.walkmeapp.core.POJO;

/**
 * Created by loriz on 2/24/16.
 */
public class TrainingInstant {


   public int id;
   public double latitude;
   public double longitude;
   public double speed;
   public double pace;
   public double altitude;
   public long time;
   public double distance;


   public TrainingInstant(double latitude, double longitude, double speed, double altitude, long time, double distance) {

      this.latitude = latitude;
      this.longitude = longitude;
      this.speed = speed;
      this.altitude = altitude;
      this.time = time;
      this.distance = distance;

      if (speed == 0) {
         this.pace = 0;
      } else {
         this.pace = (1 / speed) * 60;
      }
   }

}