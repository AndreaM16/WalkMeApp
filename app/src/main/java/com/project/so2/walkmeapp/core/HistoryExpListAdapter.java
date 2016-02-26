
package com.project.so2.walkmeapp.core;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.project.so2.walkmeapp.R;
import com.project.so2.walkmeapp.core.ORM.DBTrainings;

import java.util.HashMap;
import java.util.List;

/**
 * Class used to Handle the right order and format of trainings stored in the db
 */
public class HistoryExpListAdapter extends BaseExpandableListAdapter {

   private Context context;
   /* Header titles */
   private List<String> listDataHeader;
   /* Child data in format of header title, child title */
   private HashMap<String, List<DBTrainings>> listDataChild;

   /**
    * Defining History's information to be shown
    *
    * @param context        Context used by the DB
    * @param listDataHeader Header Titles
    * @param listChildData  Child data
    */
   public HistoryExpListAdapter(Context context, List<String> listDataHeader,
                                HashMap<String, List<DBTrainings>> listChildData) {
      this.context = context;
      this.listDataHeader = listDataHeader;
      this.listDataChild = listChildData;
   }

   /**
    * Handling Trainings' order in History View
    *
    * @param groupPosition  Training's Month
    * @param childPosititon Month's trainings
    * @return training's information
    */
   @Override
   public DBTrainings getChild(int groupPosition, int childPosititon) {
      return this.listDataChild.get(this.listDataHeader.get(groupPosition))
              .get(childPosititon);
   }

   /**
    * Getting child information
    *
    * @param groupPosition Training's Month
    * @param childPosition Month's Training Position
    * @return child information
    */
   @Override
   public long getChildId(int groupPosition, int childPosition) {
      return childPosition;
   }

   /**
    * Handling the view per each child
    *
    * @param groupPosition Training's Month
    * @param childPosition Child training ordered by data per each month
    * @param isLastChild   Used to check if it's the last child of a month
    * @param convertView   Needed by the adapter
    * @param parent        Child's parent
    * @return View per each Child
    */
   @Override
   public View getChildView(int groupPosition, final int childPosition,
                            boolean isLastChild, View convertView, ViewGroup parent) {

      final DBTrainings childText = getChild(groupPosition, childPosition);

      if (convertView == null) {
         LayoutInflater infalInflater = (LayoutInflater) this.context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         convertView = infalInflater.inflate(R.layout.history_day_element, null);
      }

      TextView txtListChild = (TextView) convertView
              .findViewById(R.id.item_text);

      txtListChild.setText("ID: " + childText.id + " - Data: " + childText.date_day + "/" +
              childText.date_month + " - " + childText.date_hour + ":" + childText.date_minutes);
      return convertView;
   }

   /**
    * Counts how many children are contained in each month
    *
    * @param groupPosition Month
    * @return Number or children per month
    */
   @Override
   public int getChildrenCount(int groupPosition) {
      return this.listDataChild.get(this.listDataHeader.get(groupPosition))
              .size();
   }

   /**
    * Getting groups
    *
    * @param groupPosition Month
    * @return Group
    */
   @Override
   public Object getGroup(int groupPosition) {
      return this.listDataHeader.get(groupPosition);
   }

   /**
    * Counts how many months store trainings' data
    *
    * @return Number of existing months
    */
   @Override
   public int getGroupCount() {
      return this.listDataHeader.size();
   }

   /**
    * Getting month's id
    *
    * @param groupPosition month
    * @return Month Id per each group
    */
   @Override
   public long getGroupId(int groupPosition) {
      return groupPosition;
   }

   /**
    * Managing Views per each month
    *
    * @param groupPosition Month
    * @param isExpanded    Checks if the group has been expanded or not
    * @param convertView   Needed by adapter
    * @param parent        Month's parent
    * @return View per month
    */
   @Override
   public View getGroupView(int groupPosition, boolean isExpanded,
                            View convertView, ViewGroup parent) {
      String headerTitle = (String) getGroup(groupPosition);
      if (convertView == null) {
         LayoutInflater infalInflater = (LayoutInflater) this.context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         convertView = infalInflater.inflate(R.layout.history_month_element, null);
      }

      TextView lblListHeader = (TextView) convertView
              .findViewById(R.id.history_item_month);
      lblListHeader.setTypeface(null, Typeface.BOLD);
      lblListHeader.setText(headerTitle);

      return convertView;
   }

   /**
    * Required by adapter
    *
    * @return false
    */
   @Override
   public boolean hasStableIds() {
      return false;
   }

   /**
    * Checks if a child is selectable
    *
    * @param groupPosition Month
    * @param childPosition Child per month
    * @return true
    */
   @Override
   public boolean isChildSelectable(int groupPosition, int childPosition) {
      return true;
   }
}