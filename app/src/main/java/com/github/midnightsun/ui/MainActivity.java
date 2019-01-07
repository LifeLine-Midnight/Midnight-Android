package com.github.midnightsun.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.midnightsun.R;
import com.github.midnightsun.datatype.UserData;
import com.github.midnightsun.service.MyApplication;
import com.github.midnightsun.service.MyService;
import com.github.midnightsun.utilis.SystemState;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView navigation;
    private Toolbar mToolbarTb;
    private ListView listView;
    private ArrayList<Map<String, Object>> list_data;
    SimpleAdapter adapter;
    private ArrayList<Map<String, Object>> post_data;
    private static final String logOut_uri = "/midnightapisvr/api/session/userlogout";
    private UserData userData;
    private SystemState state = new SystemState(MainActivity.this);
    private MsgBroadCastReceiver mBroadcastReceiver;

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

    private ListView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String username = (String)list_data.get(position).get("username");
            int img_id = (int)list_data.get(position).get("img_id");
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("img_id", img_id);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if logged
        if (state.getToken().equals("")) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this, MyService.class);
            startService(intent);
        }

        mBroadcastReceiver = new MsgBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MsgBroadCastReceiver.MESSAGE_ACTION);
        intentFilter.addAction(MsgBroadCastReceiver.NEWS_ACTION);
        intentFilter.addAction(MsgBroadCastReceiver.POST_ACTION);
        // 3. 动态注册：调用Context的registerReceiver（）方法
        registerReceiver(mBroadcastReceiver, intentFilter);

        // init navigation bar
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setBadge();

        // init Toolbar
        mToolbarTb = (Toolbar) findViewById(R.id.tb_toolbar);
        setSupportActionBar(mToolbarTb);
        // init ListView
        initListData(R.id.navigation_home);
        bindDataToListView(R.id.navigation_home);
        listView.setOnItemClickListener(onItemClickListener);
    }

    public void setBadge() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        // set badge 0
        View tab = menuView.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) tab;
        View badge = LayoutInflater.from(this).inflate(R.layout.notification_msg, menuView, false);
        itemView.addView(badge);
        TextView textView = findViewById(R.id.msg);
        textView.setVisibility(View.INVISIBLE);
        // set badge 1
        tab = menuView.getChildAt(1);
        itemView = (BottomNavigationItemView) tab;
        badge = LayoutInflater.from(this).inflate(R.layout.notification_post, menuView, false);
        itemView.addView(badge);
        textView = findViewById(R.id.post);
        textView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("tag", "onDestroy: Main");
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //通过Tag标签取消请求队列中对应的全部请求
        MyApplication.getHttpQueues().cancelAll("log-out");
    }

    // when back to MessageType Page
    private void showMessagePage() {
        setContentView(R.layout.activity_main);
        // init navigation bar
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setBadge();

        // init Toolbar
        mToolbarTb = (Toolbar) findViewById(R.id.tb_toolbar);
        setSupportActionBar(mToolbarTb);

        // init ListView
        if (list_data == null) {
            initListData(R.id.navigation_home);
        }
        bindDataToListView(R.id.navigation_home);
        listView.setOnItemClickListener(onItemClickListener);
    }

    // when back to Post Page
    private void showPostPage() {
        setContentView(R.layout.post_page);
        // init navigation bar
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_dashboard);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setBadge();

        // init ListView
        if (post_data == null) {
            initListData(R.id.navigation_dashboard);
        }
        bindDataToListView(R.id.navigation_dashboard);
    }


    private void showUserPage() {
        setContentView(R.layout.user_page);
        // init navigation bar
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_notifications);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setBadge();

        Button quit_btn = findViewById(R.id.quit_btn);
        quit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               logOut("log-out");
               Intent intent = new Intent(MainActivity.this, MyService.class);
               stopService(intent);
            }
        });
    }

    private void initListData(int type) {
        switch (type) {
            case R.id.navigation_home:
                list_data = new ArrayList<Map<String, Object>>();
                int[] img_ids = new int[] {R.drawable.dft0, R.drawable.dft1, R.drawable.dft2};
                Random random = new Random();
                for (int i = 0; i < img_ids.length; i++) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("username", "girl" + i);
                    map.put("last_msg", "Hello, I am Number " + i);
                    map.put("img_id", img_ids[i]);
                    map.put("date", random.nextInt(24) + ":"
                            + (random.nextInt(59-i) + i));
                    list_data.add(map);
                }
                break;

            case R.id.navigation_dashboard:
                post_data = new ArrayList<Map<String, Object>>();
                int[] post_ids = new int[] {R.drawable.dft0, R.drawable.dft1, R.drawable.dft2};
                Random rand = new Random();
                for (int i = 0; i < post_ids.length; i++) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("username", "girl" + i);
                    map.put("post_id", post_ids[i]);
                    map.put("date", rand.nextInt(60) + "分钟前");
                    post_data.add(map);
                }
                break;
        }
    }

    private void bindDataToListView(int type) {
        switch (type) {
            case R.id.navigation_home:
                String[] from = new String[] {"username", "last_msg", "img_id", "date"};
                int[] to = new int[] {R.id.list_item_name, R.id.list_item_msg,
                        R.id.list_item_image, R.id.list_item_date };

                adapter = new SimpleAdapter(this, list_data,
                        R.layout.list_view_item, from, to);
                listView = (ListView) findViewById(R.id.list_view);
                listView.setAdapter(adapter);
                break;

            case R.id.navigation_dashboard:
                Log.i("tag", "init Data");
                String[] data = new String[] {"username", "post_id", "date"};
                int[] container = new int[] {R.id.post_item_name, R.id.post_item_image,
                       R.id.post_date };

                SimpleAdapter post_adapter = new SimpleAdapter(this, post_data,
                        R.layout.post_list_item, data, container);

                View headView = getLayoutInflater().inflate(R.layout.post_headerview, null);
                listView = (ListView) findViewById(R.id.post_list);
                listView.addHeaderView(headView, null, false);
                listView.setAdapter(post_adapter);
                break;
        }
    }

    private void logOut(String tag) {
        Map<String, String> map = new HashMap<>();
        map.put("token", state.getToken());
        JSONObject object = new JSONObject(map);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                MyApplication.host + logOut_uri, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.i("log-out", jsonObject.toString());
                        Gson gson = new Gson();
                        userData = gson.fromJson(jsonObject.toString(), UserData.class);
                        UIfresh();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.i("log-out", volleyError.toString());
                    }
                });

        request.setTag(tag);
        MyApplication.getHttpQueues().add(request);
    }

    private void UIfresh() {
        if (userData.rtn != 0) {
            Toast.makeText(MainActivity.this,
                    userData.msg, Toast.LENGTH_SHORT).show();
        }
        else {
            state.clear();

            // switch to log in activity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public class MsgBroadCastReceiver extends BroadcastReceiver {
        private static final String MESSAGE_ACTION = "MESSAGE_ACTION";
        private static final String POST_ACTION = "POST_ACTION";
        private static final String NEWS_ACTION = "NEWS_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case MESSAGE_ACTION: Log.i("broad", "rev message");
                                         onMessageAction();
                                         break;
                    case POST_ACTION: Log.i("broad", "rev post");
                                      onPostAction();
                                      break;
                    case NEWS_ACTION: Log.i("broad", "rev news");
                                      onMessageAction();
                                      break;
                }
            }
        }

        private void onMessageAction() {
            if (navigation.getSelectedItemId() != R.id.navigation_home) {
                TextView textView = findViewById(R.id.msg);
                textView.setVisibility(View.VISIBLE);
            }

        }
        private void onPostAction() {
            if (navigation.getSelectedItemId() != R.id.navigation_dashboard) {
                TextView textView = findViewById(R.id.post);
                textView.setVisibility(View.VISIBLE);
            }
        }
    }

}
