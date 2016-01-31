package com.project.so2.walkmeapp.core.ORM;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.net.URL;

@DatabaseTable(tableName = "users")
public class DBUsers {

   @DatabaseField(id = true)
   public int id;
   @DatabaseField
   public String name;
   @DatabaseField
   public String surname;
   @DatabaseField
   public URL imageUrl; //TODO: check if there is a better type
   @DatabaseField
   public int age;
   @DatabaseField
   public int heightInCm;
   @DatabaseField
   public int weightInKg;
   @DatabaseField
   public int sex; // 0 is male, 1 is female


   public DBUsers() {
      // ORMLite needs a no-arg constructor
   }

   public DBUsers(int id, String name, String surname, URL imageUrl, int age, int heightInCm, int weightInKg, String sex) {


      this.id = id;
      this.name = name;
      this.surname = surname;
      this.imageUrl = imageUrl;
      this.age = age;
      this.heightInCm = heightInCm;
      this.weightInKg = weightInKg;
      this.sex = genderDetector(sex);


   }

   private int genderDetector(String sex) {
      int out = 0;
      if (sex.equalsIgnoreCase("Maschio")) {
         out = 0;
      }
      if (sex.equalsIgnoreCase("Femmina")) {
         out = 1;
      }
      return out;
   }

}


