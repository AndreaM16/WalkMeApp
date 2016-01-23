package com.project.so2.walkmeapp;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private String[] mDrawerElements;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ImageView mMenuIcon;
    private LinearLayout mMainPageList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mDrawerElements = getResources().getStringArray(R.array.drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mMainPageList = (LinearLayout) findViewById(R.id.main_page_list);


        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerElements));
        // Set the list's click listener

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        mMenuIcon = (ImageView) findViewById(R.id.menu_icon);
        mMenuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerToggle();
            }
        });

        String[] mMenuStrings = getResources().getStringArray(R.array.main_page_list_items);

        for(String i: mMenuStrings) {

            View v; // Creating an instance for View Object
            LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.menu_fragment_item, null);
            TextView textTemp = (TextView) v.findViewById(R.id.item_text);
            textTemp.setText(i);
            mMainPageList.addView(v);

        }

    }


    private void drawerToggle () {

       if ( mDrawerLayout.isDrawerOpen(GravityCompat.START) ) {
           mDrawerLayout.closeDrawer(Gravity.LEFT);
       } else {
           mDrawerLayout.openDrawer(Gravity.LEFT);
       }
    }

}