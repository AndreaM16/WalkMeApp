package com.project.so2.walkmeapp.core.ORM;

/**
 * Class used to handle all the operations involving DB management i.e. inserts, queries, ..
 * DB stores and manages all the information about trainings such as dates, pace, step length, ..
 */

import android.content.Context;
import android.content.ContextWrapper;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.project.so2.walkmeapp.ui.Training;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


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
   private static boolean isInitialized = false;

   /**
    * @param context Needed by DB Manager to work
    */
   public DBManager(Context context) {
      super(context);

      this.context = context;

   }

   /**
    * @param ctx context used to check if there is a current DB instance
    */
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
      isInitialized = true;
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

   /**
    * @param name             Training's name
    * @param formattedDate    Training's date
    * @param pref_pace        Training's set pace set by the user
    * @param pref_lastXMeters Training's last x meters set by the user
    * @param pref_stepLength  Training's step length set by the user
    * @param tiList           Training's parameters used to plot the training. It saves, for each point:
    *                         training's id, latitude, longitude, altitude, speed, pace, time and distance
    */

   public void createTraining(String name, String formattedDate, int pref_pace, int pref_lastXMeters, int pref_stepLength,
                              ArrayList<TrainingInstant> tiList) {

      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      Calendar date = Calendar.getInstance();
      try {
         date.setTime(format.parse(formattedDate));
      } catch (ParseException e) {
         e.printStackTrace();
      }

      this.name = name;
      this.date_year = date.get(Calendar.YEAR);
      this.date_month = date.get(Calendar.MONTH) + 1;   //calendar di merda é buggato, conta i mesi da 0 a 11
      this.date_day = date.get(Calendar.DAY_OF_MONTH);
      this.date_hour = date.get(Calendar.HOUR_OF_DAY);
      this.date_minutes = date.get(Calendar.MINUTE);
      this.date_seconds = date.get(Calendar.SECOND);
      this.pref_pace = pref_pace;
      this.pref_lastXMeters = pref_lastXMeters;
      this.pref_stepLength = pref_stepLength; //set from real prefs, 100 is default value
      this.tiList = tiList;
   }

   public void saveImportedTraining(DBTrainings training, Context ctx) {

      if (isInitialized == false) {
         initialize(ctx);
      }

      int newId = 0;
      try {
         if (getTrainings() != null && getTrainings().size() > 0) {
            newId = getLastTraining().id + 1;
         } else {
            newId = 0;
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }

      training.id = newId;

      ArrayList<TrainingInstant> instants = training.getInstants();

      for (TrainingInstant inst : instants) {
         inst.training = training;
      }

      try {
         training.setInstants(instants);
      } catch (SQLException e) {
         e.printStackTrace();
      }

      try {
         dbDao.create(training);
      } catch (SQLException e1) {
         e1.printStackTrace();
      }

   }

   /* Saving Trainings */
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

      /* Populating the instance to be saved in the DB */
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


   public void destroyTraining(DBTrainings training) {
      try {
         dbDao.delete(training);
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }


   public static DatabaseHelper getHelper() {
      return databaseHelper;
   }

}


