package com.project.so2.walkmeapp.core.SERVICE;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.project.so2.walkmeapp.core.JacksonUtils;
import com.project.so2.walkmeapp.core.ORM.DBManager;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Class that manages custom .walk files sending and attaching
 */
public class AttachmentHandling {

   DBManager db;

   /**
    * Sharing the train
    *
    * @param context
    * @param training
    */
   public void share(Context context, DBTrainings training) {
      db = DBManager.getIstance();


      DBTrainings res = training;


      File path = new File(context.getFilesDir() + "/training");
      Log.d("Path", path.toString());
      path.mkdirs();
      File training_file = new File(path, res.name + ".walk");

      try {
         ObjectMapper mapper = JacksonUtils.mapper;
         mapper.writeValue(training_file, res);
      } catch (IOException e) {
         e.printStackTrace();
      }
      Intent emailIntent = new Intent(Intent.ACTION_SEND);
      emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

      Uri contentUri = FileProvider.getUriForFile(context, "com.project.so2.walkmeapp", training_file);

      emailIntent.setType("vnd.android.cursor.dir/email");
      emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
      emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");

      context.startActivity(Intent.createChooser(emailIntent, "Send email..."));


   }
}
