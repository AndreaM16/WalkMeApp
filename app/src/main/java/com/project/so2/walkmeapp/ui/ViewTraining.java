package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.project.so2.walkmeapp.core.POJO.TrainingInstant;

import java.sql.SQLException;
import java.text.DecimalFormat;
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
    private TextView total_distance;
    private TextView total_steps;
    private TextView average_speed;
    private TextView avg_x_speed;
    private TextView total_avg_steps;
    private TextView avg_x_steps;
    private int prefsAvgStepInM;
    private int prefsLastMetersInM;

    private SharedPreferences settings;
    private double total_speed;
    private double total_pace;
    private double distance;
    private int avg_X_speed;
    private int avg_step;
    private int avg_X_pace;


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
        total_distance = (TextView) this.findViewById(R.id.tot_dist_value);
        total_steps = (TextView) this.findViewById(R.id.total_steps_value);
        average_speed = (TextView) this.findViewById(R.id.average_speed_value);
        avg_x_speed = (TextView) this.findViewById(R.id.x_speed_label_value);
        total_avg_steps = (TextView) this.findViewById(R.id.avg_pace_value);
        avg_x_steps = (TextView) this.findViewById(R.id.avg_x_pace_value);

        setValuesFromShared();




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


                return null;
            }
        }, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {

                int size = training.getInstants().size() - 1;
                distance = training.getInstants().get(size).distance;


                int total_pace = 0;
                for (int i = 0; i < size; i++) {
                    total_pace += training.getInstants().get(i).pace;
                }


                for (int i = 0; i < training.getInstants().size() - 1; i++) {
                    total_speed += training.getInstants().get(i).speed;
                }
                total_speed = total_speed / (size);


                int speed = (int) training.getInstants().get(size - 1).speed;
                int meters = (int) training.getInstants().get(size - 1).distance;
                avg_X_speed = (speed / meters) * prefsLastMetersInM;



                avg_step = total_pace / (size);


                int pace = (int) training.getInstants().get(size - 1).pace;
                int meters_pace = (int) training.getInstants().get(size - 1).distance;
                avg_X_pace = pace / meters_pace * prefsLastMetersInM;
                return null;
            }
        },Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {

                Log.d("RESULTS", "Distance:" + distance + " | AvgTotSpd: " + total_speed + " | TotalSteps: " + total_pace + " | AvgXSpd: " + avg_X_speed);

                setupActionbar();

                total_steps.setText(format(total_pace));
                total_distance.setText(format(distance));
                average_speed.setText(format(total_speed));
                avg_x_speed.setText(format(avg_X_speed));
                total_avg_steps.setText(format(avg_step));
                avg_x_steps.setText(format(avg_X_pace));


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

    private void setValuesFromShared() {

        settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.contains("stepLength")) {
            prefsstepLengthInCm = settings.getInt("stepLength", 100);
        } else {
            prefsstepLengthInCm = 100;
        }
        if (settings.contains("lastXMeters")) {
            prefsLastMetersInM = settings.getInt("lastXMeters", 10);
        } else {
            prefsLastMetersInM = 10;
        }
        if (settings.contains("avgStepInM")) {
            prefsAvgStepInM = settings.getInt("avgStepInM", 1);
        } else {
            prefsAvgStepInM = 120;
        }

        Log.d("OOOO", "Values from prefs ->  " + "stepLength: " + prefsstepLengthInCm + "cm ||  LastXMeters: " + prefsLastMetersInM + "m ||  AvgStep: " + prefsAvgStepInM + "m");
    }

    public static String format(double value) {
        //DecimalFormat decimalFormat = new DecimalFormat("000000");
        String temp = Integer.toString((int) value);
        return temp;
    }


}
