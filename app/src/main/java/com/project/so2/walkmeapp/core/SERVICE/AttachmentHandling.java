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
 * Created by alex_ on 27/02/2016.
 */
public class AttachmentHandling {

DBManager db;



   public void share( Context context){
       db=DBManager.getIstance();


       DBTrainings res = null;
       try {
           res = db.getLastTraining();
       } catch (SQLException e) {
           e.printStackTrace();
       }


       File path = new File(context.getFilesDir() + "/training");
       Log.d("percorso", path.toString());
       path.mkdirs();
       File training = new File(path, "training.walk");

       try {
           ObjectMapper mapper = JacksonUtils.mapper;
           mapper.writeValue(training, res);
       } catch (IOException e) {
           e.printStackTrace();
       }
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        Uri contentUri = FileProvider.getUriForFile(context, "com.project.so2.walkmeapp", training);


        emailIntent.setType("vnd.android.cursor.dir/email");
        emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");

        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));



    }
}
