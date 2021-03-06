package com.project.so2.walkmeapp.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.HistoryExpListAdapter;
import com.project.so2.walkmeapp.core.ORM.DBManager;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import com.project.so2.walkmeapp.core.HistoryExpListAdapter;

/**
 * Class used to Handle trainings' history
 */
public class History extends Activity {

   private static final String TAG = "History";
   private Dao<DBTrainings, String> dbTrainingDao;
   private String[] mLoadTrainingsElements;

   private ExpandableListView historyListView;
   private List<DBTrainings> trainings;
   private TextView noTrainingsText;
   private ArrayList<String> listDataHeader;
   private HashMap<String, List<DBTrainings>> listDataChild;
   private HistoryExpListAdapter expListAdapter;

   /**
    * @param savedInstanceState Used to get DB resources' instances
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      /* Binding Class to its View */
      setContentView(R.layout.history_trainings);

      /* Binding Strings to their View */
      mLoadTrainingsElements = getResources().getStringArray(R.array.history_list_items);

      ImageView actionBar = (ImageView) findViewById(R.id.action_bar_icon);
      actionBar.setImageResource(R.drawable.btn_back);
      actionBar.setOnClickListener(new View.OnClickListener() {
         /**
          * @param v View
          */
         @Override
         public void onClick(View v) {
            finish();
         }
      });
      TextView actionBarText = (TextView) findViewById(R.id.action_bar_title);

      actionBarText.setText(getResources().getString(R.string.history_title));


      historyListView = (ExpandableListView) findViewById(R.id.load_trainings_list_view);
      noTrainingsText = (TextView) findViewById(R.id.no_trainings);

      /* Getting trainings' information from the DB */
      DBManager dbMan = DBManager.getIstance();
      dbTrainingDao = DBManager.dbDao;
      try {
         trainings = dbMan.getTrainings();
      } catch (SQLException e) {
         e.printStackTrace();
      }

      /* If there is at least one training it'll be shown */
      if (trainings != null && trainings.size() > 0) {
         noTrainingsText.setVisibility(View.GONE);
         historyListView.setVisibility(View.VISIBLE);

         expListAdapter = new HistoryExpListAdapter(this);
         expListAdapter.prepareListData(trainings, dbTrainingDao);
         historyListView.setAdapter(expListAdapter);
      }


   }

}

