package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.ORM.DBManager;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;
import com.project.so2.walkmeapp.core.ORM.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;


/**
 * Created by alex_ on 27/02/2016.
 */
public class ViewTraining extends Activity {

   private DBTrainings training;
   private static final String PREFS_NAME = "SETTINGS_PREFS";
   private int prefsstepLengthInCm;

   private Dao<DBTrainings, String> dbTrainingDao;
   private DBManager db;
   private TextView trainingName;
   private int id;
   private ImageView actionBar;
   private TextView actionBarText;
   private TextView tot_dist_view;
   private TextView tot_steps_view;
   private TextView average_speed_view;
   private TextView avg_x_speed_view;
   private TextView avg_pace_view;
   private TextView avg_x_pace_view;
   private int prefsAvgStepInM;
   private int prefsLastMetersInM;

   private int avg_speed;
   private int distance;
   private int avg_X_speed;
   private int avg_pace;
   private int avg_X_pace;
   private int total_steps_count = 0;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.training_end);

         /* Initializing DBhelper and DBManager*/
      DatabaseHelper.initialize(this);
      DBManager.initialize(this);

      db = DBManager.getIstance();
      dbTrainingDao = db.dbDao;
      id = getIntent().getExtras().getInt("id");

      actionBar = (ImageView) this.findViewById(R.id.action_bar_icon);
      actionBarText = (TextView) this.findViewById(R.id.action_bar_title);
      tot_dist_view = (TextView) this.findViewById(R.id.tot_dist_value);
      tot_steps_view = (TextView) this.findViewById(R.id.total_steps_value);
      average_speed_view = (TextView) this.findViewById(R.id.average_speed_value);
      avg_x_speed_view = (TextView) this.findViewById(R.id.x_speed_label_value);
      avg_pace_view = (TextView) this.findViewById(R.id.avg_pace_value);
      avg_x_pace_view = (TextView) this.findViewById(R.id.avg_x_pace_value);

      boltsTask();
   }

   public Task<Void> boltsTask() {

      Task<Void>.TaskCompletionSource tcs = Task.create();
      Task<Void> task = tcs.getTask();
      return task.call(new Callable<Void>() {
         @Override
         public Void call() throws Exception {
            QueryBuilder<DBTrainings, String> queryBuilder = dbTrainingDao.queryBuilder();

            List<DBTrainings> trainingList = null;

            try {
               queryBuilder.where().eq("id", id);
               PreparedQuery<DBTrainings> preparedQuery = queryBuilder.prepare();
               trainingList = dbTrainingDao.query(preparedQuery);
               training = trainingList.get(0);
               Log.d("TEST", trainingList.toString());
            } catch (SQLException e) {
               e.printStackTrace();
            }

            setPreferencesValues();


            return null;
         }
      }, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Void, Void>() {
         @Override
         public Void then(Task<Void> task) throws Exception {

            int size = training.getInstants().size() - 1;


            distance = (int) (training.getInstants().get(size).distance);

            for (int i = 0; i < size; i++) {
               total_steps_count += training.getInstants().get(i).pace;
            }

            for (int i = 0; i < training.getInstants().size() - 1; i++) {
               avg_speed += training.getInstants().get(i).speed;
            }
            avg_speed = avg_speed / (size);


            int speed = (int) ((training.getInstants().get(size - 1).speed) + (training.getInstants().get(size - 1).speed) / 2);
            int meters = (int) ((training.getInstants().get(size - 1).distance) - (training.getInstants().get(size - 3).distance));
            avg_X_speed = (int) (speed / meters) * prefsLastMetersInM;

            avg_pace = (int) (total_steps_count / (size));

            int pace = (int) ((training.getInstants().get(size - 1).pace + training.getInstants().get(size - 3).pace) / 2);
            int meters_pace = (int) ((training.getInstants().get(size - 1).distance) - (training.getInstants().get(size - 3).distance));
            avg_X_pace =(int) (pace / meters_pace * prefsLastMetersInM);

            return null;

         }
      }, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Void, Void>() {
         @Override
         public Void then(Task<Void> task) throws Exception {

            Log.d("RESULTS", "Distance:" + distance + " | AvgTotSpd: " + avg_speed + " | TotalSteps: " + total_steps_count + " | AvgXSpd: " + avg_X_speed);

            setupActionbar();

            tot_dist_view.setText(Integer.toString(distance));
            tot_steps_view.setText(Integer.toString(total_steps_count));

            average_speed_view.setText(Integer.toString(avg_speed));
            avg_x_speed_view.setText(Integer.toString(avg_X_speed));

            avg_pace_view.setText(Integer.toString(avg_pace));
            avg_x_pace_view.setText(Integer.toString(avg_X_pace));


            return null;
         }
      });
   }

   private void setupActionbar() {
      actionBar.setImageResource(R.drawable.btn_back);
      actionBar.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            finish();
         }
      });
      actionBarText.setText(training.name + training.date_day);
   }

   private void setPreferencesValues() {

      prefsstepLengthInCm = training.pref_stepLength;
      prefsLastMetersInM = training.pref_lastXMeters;
      prefsAvgStepInM = training.pref_pace;

   }


}
