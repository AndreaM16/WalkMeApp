package com.project.so2.walkmeapp.core.ORM;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.project.so2.walkmeapp.core.POJO.TrainingInstant;

import java.sql.SQLException;

/**
 * Database helper which creates and upgrades the database and provides DAOs for the app.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

   /* Standard Initialization*/
   private static final String DATABASE_NAME = "database.db";
   private static final int DATABASE_VERSION = 14;

   private Dao<DBTrainings, String> dbTrainingsDao;
   private static DatabaseHelper mDatabaseHelper;

   public DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }

   /**
    * @param sqliteDatabase   DB
    * @param connectionSource Connection used for the communication
    */
   @Override
   public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
      try {

         /** Create tables. This onCreate() method will be invoked only once of the application
          *  life time i.e. the first time when the application starts.
          */
         TableUtils.createTableIfNotExists(connectionSource, DBTrainings.class);
         TableUtils.createTableIfNotExists(connectionSource, TrainingInstant.class);

      } catch (SQLException e) {
         Log.e(DatabaseHelper.class.getName(), "Unable to create Database", e);
      }
   }

   @Override
   public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
      try {

         /** In case of change in database of next version of application, please increase
          *  the value of DATABASE_VERSION variable, then this method will be invoked
          *  automatically. Developer needs to handle the upgrade logic here, i.e. create
          *  a new table or a new column to an existing table, take the backups of the
          *  existing database etc.
          */

         TableUtils.dropTable(connectionSource, DBTrainings.class, true);
         TableUtils.dropTable(connectionSource, TrainingInstant.class, true);

         onCreate(sqliteDatabase, connectionSource);

      } catch (SQLException e) {
         Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new "
                 + newVer, e);
      }
   }

   /**
    * Create getDao methods of all database tables to access those from android code.
    * Insert, delete, read, update everything will be happened through DAOs
    */

   public Dao<DBTrainings, String> getTrainingsDao() throws SQLException {
      if (dbTrainingsDao == null) {
         dbTrainingsDao = getDao(DBTrainings.class);
      }
      return dbTrainingsDao;
   }

   public static void initialize(Context ctx) {
      if (mDatabaseHelper == null) {
         mDatabaseHelper = new DatabaseHelper(ctx.getApplicationContext());
      }
   }

   public static DatabaseHelper getIstance() {
      return mDatabaseHelper;
   }


}