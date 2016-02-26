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

import com.j256.ormlite.field.DatabaseField;
import com.project.so2.walkmeapp.core.JacksonUtils;
import com.project.so2.walkmeapp.core.POJO.TrainingInstant;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Task;
import bolts.TaskCompletionSource;


public class DBManager extends ContextWrapper {

   private static final String TAG = "Training";
   private static DBManager uniqInstance;
   private final Context context;
   public DBTrainings dbTrainingInstance = new DBTrainings();
   public int date_year;
   public int date_month;
   public int date_day;
   public int date_hour;
   public int date_minutes;
   public int date_seconds;
   private int pref_pace;
   private int pref_lastXMeters;
   private int pref_stepLength;
   private String name;
   private ArrayList<TrainingInstant> tiList;
   private static DatabaseHelper databaseHelper;
   public static Dao<DBTrainings, String> dbDao;
   private List<DBTrainings> results;

   public DBManager(Context context) {
      super(context);

      this.context = context;

   }

   public static void initialize(Context ctx) {
      if (uniqInstance == null) {
         uniqInstance = new DBManager(ctx.getApplicationContext());
      }
      if (databaseHelper == null) {
         databaseHelper = OpenHelperManager.getHelper(ctx.getApplicationContext(), DatabaseHelper.class);
      }

      try {
         dbDao = databaseHelper.getTrainingsDao();
      } catch (SQLException e) {
         e.printStackTrace();
      }

   }

   public static DBManager getIstance() {
      return uniqInstance;
   }

   public DBTrainings getLastTraining() throws SQLException {
            return getTrainings().get(getTrainings().size() - 1);
   }


   public List<DBTrainings> getTrainings() throws SQLException {
         return dbDao.queryForAll();
   }


   public int getLastTrainingId() {

      return dbTrainingInstance.id;
   }


   public void createTraining(String name, String formattedDate, int pref_pace, int pref_lastXMeters, int pref_stepLength, ArrayList<TrainingInstant> tiList) {

      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      Calendar date = Calendar.getInstance();
      try {
         date.setTime(format.parse(formattedDate));
      } catch (ParseException e) {
         e.printStackTrace();
      }

      this.name = name;
      this.date_year = date.get(Calendar.YEAR);
      this.date_month = date.get(Calendar.MONTH)+1;   //calendar di merda é buggato, conta i mesi da 0 a 11
      this.date_day = date.get(Calendar.DAY_OF_MONTH);
      this.date_hour = date.get(Calendar.HOUR_OF_DAY);
      this.date_minutes = date.get(Calendar.MINUTE);
      this.date_seconds = date.get(Calendar.SECOND);
      this.pref_pace = pref_pace;
      this.pref_lastXMeters = pref_lastXMeters;
      this.pref_stepLength = pref_stepLength; //set from real prefs, 100 is default value
      this.tiList = tiList;
   }


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

      //popolare l'istanza per il salvataggio nel db
      dbTrainingInstance.name = this.name;
      dbTrainingInstance.date_year = date_year;
      dbTrainingInstance.date_month = date_month;
      dbTrainingInstance.date_day = date_day;
      dbTrainingInstance.date_hour = date_hour;
      dbTrainingInstance.date_minutes = date_minutes;
      dbTrainingInstance.date_seconds = date_seconds;
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

   public static DatabaseHelper getHelper() {
      return databaseHelper;
   }

}


