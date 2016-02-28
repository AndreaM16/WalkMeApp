package com.project.so2.walkmeapp.core.ORM;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class containing DB structure and managing
 */
@DatabaseTable(tableName = "trainings")
public class DBTrainings {

   @DatabaseField(id = true)
   public int id;
   @DatabaseField
   public String name;
   @DatabaseField
   public int date_year;
   @DatabaseField
   public int date_month;
   @DatabaseField
   public int date_day;
   @DatabaseField
   public int date_hour;
   @DatabaseField
   public int date_minutes;
   @DatabaseField
   public int date_seconds;
   @DatabaseField
   public int pref_pace;
   @DatabaseField
   public int pref_lastXMeters;
   @DatabaseField
   public int pref_stepLength;
   @ForeignCollectionField(eager = true)
   Collection<TrainingInstant> tiList;

   /* ORMLite needs a no-arg constructor */
   public DBTrainings() {

   }

   public DBTrainings(int id, int date_year, int date_month, int date_day, int date_hour,
                      int date_minutes, int date_seconds, int trainingSteps, int lastMetersSettings,
                      int stepLengthInCm) {

      this.id = id;
      this.date_year = date_year;
      this.date_month = date_month;
      this.date_day = date_day;
      this.date_hour = date_hour;
      this.date_minutes = date_minutes;
      this.date_seconds = date_seconds;
      this.pref_pace = trainingSteps;
      this.pref_lastXMeters = lastMetersSettings;
      this.pref_stepLength = stepLengthInCm; //in cm

   }

   public ArrayList<TrainingInstant> getInstants() {
      ArrayList<TrainingInstant> tiArray = new ArrayList<>();
      for (TrainingInstant ti : tiList) {
         tiArray.add(ti);
      }
      return tiArray;
   }

   /**
    * Setting DB instances per each point of the training (used for the plot)
    *
    * @param instants one per each point of the training
    * @throws java.sql.SQLException SQLException
    */
   public void setInstants(ArrayList<TrainingInstant> instants) throws java.sql.SQLException {
      if (this.tiList == null) {
         Dao<DBTrainings, String> dao = DatabaseHelper.getIstance().getTrainingsDao();
         this.tiList = dao.getEmptyForeignCollection("tiList");
      }
      this.tiList.clear();
      this.tiList.addAll(instants);
   }

}


