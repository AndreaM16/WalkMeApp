package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.ORM.DBManager;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;
import com.project.so2.walkmeapp.core.ORM.DatabaseHelper;
import com.project.so2.walkmeapp.core.ORM.TrainingInstant;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import lecho.lib.hellocharts.formatter.AxisValueFormatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;


/**
 * Class that manages end training stats
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

   private int avg_speed = 0;
   private int distance = 0;
   private int avg_X_speed = 0;
   private int avg_pace = 0;
   private int avg_X_pace = 0;
   private int total_steps_count = 0;
   private LineChartView graph;
   private xAxisType mXAxis;
   private yAxisType mYAxis;
   private AdapterView mSpinnerY;
   private AdapterView mSpinnerX;

   /**
    * On Create
    *
    * @param savedInstanceState instances
    */
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.training_end);

      /* Initializing DBHelper and DBManager*/
      DatabaseHelper.initialize(this);
      DBManager.initialize(this);

      db = DBManager.getIstance();
      dbTrainingDao = DBManager.dbDao;
      id = getIntent().getExtras().getInt("id");

      graph = (LineChartView) this.findViewById(R.id.chart);
      actionBar = (ImageView) this.findViewById(R.id.action_bar_icon);
      actionBarText = (TextView) this.findViewById(R.id.action_bar_title);
      tot_dist_view = (TextView) this.findViewById(R.id.tot_dist_value);
      tot_steps_view = (TextView) this.findViewById(R.id.total_steps_value);
      average_speed_view = (TextView) this.findViewById(R.id.average_speed_value);
      avg_x_speed_view = (TextView) this.findViewById(R.id.x_speed_label_value);
      avg_pace_view = (TextView) this.findViewById(R.id.avg_pace_value);
      avg_x_pace_view = (TextView) this.findViewById(R.id.avg_x_pace_value);
      mSpinnerX = (Spinner) this.findViewById(R.id.spinnerX);
      mSpinnerY = (Spinner) this.findViewById(R.id.spinnerY);

      boltsTask();


   }

   /**
    * Bolts used to make ordered tasks and ensure the correct operations workflow
    * Combination of 3 different tasks (ContinueWith)
    *
    * @return
    */
   public Task<Void> boltsTask() {

      Task<Void>.TaskCompletionSource tcs = Task.create();
      Task<Void> task = tcs.getTask();
      return Task.call(new Callable<Void>() {

         /**
          * Bolts Task (query the db)
          * @return
          * @throws Exception
          */
         @Override
         public Void call() throws Exception {
            QueryBuilder<DBTrainings, String> queryBuilder = dbTrainingDao.queryBuilder();

            List<DBTrainings> trainingList = null;

            try {
               queryBuilder.where().eq("id", id);
               PreparedQuery<DBTrainings> preparedQuery = queryBuilder.prepare();
               trainingList = dbTrainingDao.query(preparedQuery);
               training = trainingList.get(0);
            } catch (SQLException e) {
               e.printStackTrace();
            }

            setPreferencesValues();

            setupGraph();

            return null;
         }
      }, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Void, Void>() {

         /**
          * Bolts Task (calculate all the datas)
          * @param task
          * @return
          * @throws Exception
          */
         @Override
         public Void then(Task<Void> task) throws Exception {

            int size = training.getInstants().size() - 1;


            distance = (int) (training.getInstants().get(size).distance);
            total_steps_count = distance / (prefsstepLengthInCm / 100);

            for (int i = 0; i < training.getInstants().size() - 1; i++) {
               avg_speed += training.getInstants().get(i).speed;
            }
            avg_speed = avg_speed / (size);

            int speed = (int) ((training.getInstants().get(size - 1).speed + training.getInstants().get(size - 3).speed) / 2);
            int meters = (int) ((training.getInstants().get(size - 1).distance) - (training.getInstants().get(size - 3).distance));
            double speedMeters = ((double) speed) / ((double) meters);
            avg_X_speed = (int) ((speedMeters) * prefsLastMetersInM);

            int minutes = (int) ((training.getInstants().get(size - 1).time - training.getInstants().get(0).time) / (double) 60000);
            if (minutes == 0) {
               minutes = 1;
            }
            avg_pace = (int) ((double) total_steps_count / (double) minutes);

            double pace = ((training.getInstants().get(size - 1).pace + training.getInstants().get(size - 3).pace) / (double) 2);
            double meters_pace = ((training.getInstants().get(size - 1).distance) - (training.getInstants().get(size - 3).distance));
            avg_X_pace = (int) ((pace / meters_pace) * prefsLastMetersInM);

            return null;

         }
      }, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Void, Void>() {

         /**
          * Bolts Task (associate datas to views)
          * @param task
          * @return
          * @throws Exception
          */
         @Override
         public Void then(Task<Void> task) throws Exception {


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

         /**
          * Setting up the view
          * @param v
          */
         @Override
         public void onClick(View v) {
            finish();
         }
      });
      actionBarText.setText("\"" + training.name + "\" | " + training.date_day + "/" + training.date_month + " - " + training.date_hour + ":" + training.date_minutes);
   }

   /* user preferences extracted from the training */
   private void setPreferencesValues() {
      prefsstepLengthInCm = training.pref_stepLength;
      prefsLastMetersInM = training.pref_lastXMeters;
      prefsAvgStepInM = training.pref_pace;
   }


   /* setup all the options to correctly display the graph */
   private void setupGraph() {

      mXAxis = xAxisType.TIME;
      ArrayAdapter<CharSequence> adapterY = ArrayAdapter.createFromResource(this, R.array.y_array, R.layout.spinner_center_item);
      adapterY.setDropDownViewResource(R.layout.spinner_center_item);
      mSpinnerY.setAdapter(adapterY);

      ArrayAdapter<CharSequence> adapterX = ArrayAdapter.createFromResource(this, R.array.x_array, R.layout.spinner_center_item);
      adapterX.setDropDownViewResource(R.layout.spinner_center_item);
      mSpinnerX.setAdapter(adapterX);

      mSpinnerY.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
               case 0:
                  mYAxis = yAxisType.SPEED;
                  break;
               case 1:
                  mYAxis = yAxisType.PACE;
                  break;
               case 2:
                  mYAxis = yAxisType.ALTITUDE;
                  break;
            }
            plot();
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent) {
            mYAxis = yAxisType.SPEED;
         }
      });


      /* chose the value on the x axis using the enum and call plot to re-draw the graph */
      mSpinnerX.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
               case 0:
                  mXAxis = xAxisType.TIME;
                  break;
               case 1:
                  mXAxis = xAxisType.DISTANCE;
                  break;
            }
            plot();
         }


         @Override
         public void onNothingSelected(AdapterView<?> parent) {
            mXAxis = xAxisType.TIME;
         }
      });
   }

   /* get all the remaining data and plot it */
   private void plot() {

      List<PointValue> values = new ArrayList<>();
      Axis axisX = new Axis();
      Axis axisY = new Axis().setHasLines(true);

      if (mXAxis == xAxisType.TIME) {

         axisX.setFormatter(new AxisValueFormatter() {
            @Override
            public int formatValueForAutoGeneratedAxis(char[] formattedValue, float value, int autoDecimalDigits) {

               //needed to hide the time from the x axis
               SimpleDateFormat simpleDateFormat = new SimpleDateFormat("");
               simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
               String label = simpleDateFormat.format(value);
               label.getChars(0, label.length(), formattedValue, formattedValue.length - label.length());

               return label.length();
            }

            @Override
            public int formatValueForManualAxis(char[] formattedValue, AxisValue axisValue) {
               //needed by the class, breaks otherwise
               return 0;
            }
         });

         /* needed to hide the time labels */
         axisX.setMaxLabelChars(5);
      }


      for (int i = 0; i < training.getInstants().size(); i++) {
         Float x, y = 0f;

         if (mXAxis == xAxisType.TIME) {
            x = (float) ((training.getInstants().get(i).time - training.getInstants().get(0).time));
            axisX.setName(getString(R.string.axisX_time));
         } else {
            x = (float) training.getInstants().get(i).distance;
            axisX.setName(getString(R.string.axisX_distance));
         }

         switch (mYAxis) {
            case SPEED:
               y = (float) training.getInstants().get(i).speed;
               axisY.setName(getString(R.string.axisY_speed));
               break;
            case PACE:
               y = (float) training.getInstants().get(i).pace;
               axisY.setName(getString(R.string.axisY_pace));
               break;
            case ALTITUDE:
               y = (float) training.getInstants().get(i).altitude;
               axisY.setName(getString(R.string.axisY_altitude));
               break;
         }

         /*prepare the values (x,y)*/
         values.add(new PointValue(x, y));
      }


      /*map the values to the graph*/
      Line line = new Line(values);
      line.setColor(ContextCompat.getColor(ViewTraining.this, R.color.colorPrimary));
      line.setHasPoints(false);
      line.setFilled(true);
      List<Line> lines = new ArrayList<>();
      lines.add(line);

      axisX.setTextColor(Color.BLACK);
      axisY.setTextColor(Color.BLACK);

      LineChartData data = new LineChartData();
      data.setLines(lines);
      data.setAxisXBottom(axisX);
      data.setAxisYLeft(axisY);
      graph.setZoomEnabled(false);
      graph.setContainerScrollEnabled(false, null);
      graph.setLineChartData(data);
      graph.setInteractive(true);
   }


   private enum xAxisType {
      TIME, DISTANCE
   }

   private enum yAxisType {
      SPEED, PACE, ALTITUDE
   }


}
