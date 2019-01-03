package com.github.midnightsun;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<Map<String, Object>> list_data;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    showMessagePage();
                    return true;
                case R.id.navigation_dashboard:
                    showPostPage();
                    return true;
                case R.id.navigation_notifications:
                    showUserPage();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Toolbar mToolbarTb = (Toolbar) findViewById(R.id.tb_toolbar);
        setSupportActionBar(mToolbarTb);

        initListData();
        String[] from = new String[] {"username", "last_msg", "img_id", "date"};
        int[] to = new int[] {R.id.list_item_name, R.id.list_item_msg,
                R.id.list_item_image, R.id.list_item_date };

        SimpleAdapter adapter = new SimpleAdapter(this, list_data,
                R.layout.list_view_item, from, to);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
    }

    void showMessagePage() {}
    void showPostPage() {}
    void showUserPage() {}

    void initListData() {
        int[] img_ids = new int[] {R.drawable.dft0, R.drawable.dft1, R.drawable.dft2};

        list_data = new ArrayList<Map<String, Object>>();
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("username", "girl" + i);
            map.put("last_msg", "Hello, I am Number " + i);
            map.put("img_id", img_ids[i]);
            map.put("date", random.nextInt(24) + ":" + (random.nextInt(59-i) + i));
            list_data.add(map);
        }
    }

}
