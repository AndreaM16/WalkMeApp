package com.project.so2.walkmeapp.core.SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.JacksonUtils;
import com.project.so2.walkmeapp.core.ORM.DBManager;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;
import com.project.so2.walkmeapp.core.ORM.DatabaseHelper;
import com.project.so2.walkmeapp.ui.Training;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 * Class that manages custom .walk file sharing
 */
public class FileHandling extends Activity {

   private DBManager db;
   public static DBTrainings training;
   private static final int ACCESS_FINE_LOCATION = 0;
   private boolean succImported = false;


   /**
    * On Create
    *
    * @param savedInstanceState
    */
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.import_xml);

      DatabaseHelper.initialize(this);
      DBManager.initialize(this);

      db = DBManager.getIstance();
      ObjectMapper mapper = JacksonUtils.mapper;

      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
              != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
              (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
              != PackageManager.PERMISSION_GRANTED) {

         ActivityCompat.requestPermissions(this,
                 new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE},
                 ACCESS_FINE_LOCATION);
      }


      if (getIntent().getData() != null && succImported == false) {


         String filePath = getIntent().getData().getPath();
         try {
            StringBuilder text = new StringBuilder();

            /* Read the file passed in the intent */
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            String line;

            while ((line = br.readLine()) != null) {
               text.append(line);
            }
            br.close();

            Log.d("BAU:training", text.toString());

            /* Convert the json file in a WorkoutItem object */
            training = mapper.readValue(text.toString(), DBTrainings.class);
            db.saveImportedTraining(training, getApplicationContext());


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            succImported = true;

            alertDialogBuilder
                    .setTitle("WalkMeApp Importer")
                    .setMessage("Training \"" + training.name + "\" successfully imported.\nIt is now in your WalkMeApp History.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                          finish();
                       }
                    });

            alertDialogBuilder.show();

         } catch (IOException e) {
            e.printStackTrace();

         }


      }

   }

   /**
    * Results of permission check. Needed by the file import
    *
    * @param requestCode
    * @param permissions
    * @param grantResults
    */
   @Override
   public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
      switch (requestCode) {
         case ACCESS_FINE_LOCATION: {
                /* If request is cancelled, the result arrays are empty. */
            if (grantResults.length > 1 && ((grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_DENIED) ||
                    (grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_DENIED) ||
                    (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED))) {

               AlertDialog.Builder alertDialogBuild = new AlertDialog.Builder(this);


               alertDialogBuild
                       .setTitle("WalkMeApp Importer")
                       .setMessage("You must open the app and grant the permissions before importing a workout.")
                       .setCancelable(false)
                       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
                             finish();
                          }
                       });
            }
         }
         return;
      }


   }
}






