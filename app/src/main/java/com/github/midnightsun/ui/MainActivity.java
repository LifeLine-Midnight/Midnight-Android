package com.github.midnightsun.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.midnightsun.R;
import com.github.midnightsun.datatype.UserData;
import com.github.midnightsun.model.ChatRecordModel;
import com.github.midnightsun.model.MomentRecordModel;
import com.github.midnightsun.model.NewsRecordModel;
import com.github.midnightsun.service.MyApplication;
import com.github.midnightsun.service.MyNetWorkService;
import com.github.midnightsun.utilis.BitmapCache;
import com.github.midnightsun.utilis.SystemState;
import com.github.midnightsun.utilis.TimeTool;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView navigation;
    private Toolbar mToolbarTb;
    private ListView listView;
    private ArrayList<Map<String, Object>> list_data;
    SimpleAdapter adapter;
    private ArrayList<Map<String, Object>> post_data;
    private static final String logOut_uri = "/midnightapisvr/api/session/userlogout";
    private static final String getInfo_uri = "/midnightapisvr/api/user/getuserinfo";
    private UserData userData;
    private SystemState state = new SystemState(MainActivity.this);
    private MsgBroadCastReceiver mBroadcastReceiver;
    ChatRecordModel chatRecordModel;
    NewsRecordModel newsRecordModel;
    MomentRecordModel momentRecordModel;
    TimeTool timeTool = new TimeTool();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    showMessagePage();
                    Intent intent = new Intent();
                    intent.setAction(MsgBroadCastReceiver.SWITCH_TO_MSG_ACTION);
                    sendBroadcast(intent);
                    return true;
                case R.id.navigation_dashboard:
                    showPostPage();
                    Intent intent2 = new Intent();
                    intent2.setAction(MsgBroadCastReceiver.SWITCH_TO_POST_ACTION);
                    sendBroadcast(intent2);
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
            Intent intent;
            if (position == 0) {
                intent = new Intent(MainActivity.this, ChatActivity.class);
            }
            else {
                intent = new Intent(MainActivity.this, NewsActivity.class);
            }
            intent.putExtra("username", username);
            intent.putExtra("img_id", img_id);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // registor broadcast
        mBroadcastReceiver = new MsgBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MsgBroadCastReceiver.MESSAGE_ACTION);
        intentFilter.addAction(MsgBroadCastReceiver.NEWS_ACTION);
        intentFilter.addAction(MsgBroadCastReceiver.POST_ACTION);
        intentFilter.addAction(MsgBroadCastReceiver.SWITCH_ACTION);
        intentFilter.addAction(MsgBroadCastReceiver.SWITCH_TO_MSG_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);

        // check if logged
        if (state.getToken().equals("")) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this, MyNetWorkService.class);
            startService(intent);
            chatRecordModel = new ChatRecordModel(MainActivity.this, state.getUserName());
            newsRecordModel =  new NewsRecordModel(MainActivity.this, state.getUserName());
            momentRecordModel = new MomentRecordModel(MainActivity.this, state.getUserName());
            intent = new Intent();
            intent.setAction(MsgBroadCastReceiver.SWITCH_TO_MSG_ACTION);
            sendBroadcast(intent);
        }

        // init navigation bar
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setBadge(R.id.navigation_home);

        // init Toolbar
        mToolbarTb = (Toolbar) findViewById(R.id.tb_toolbar);
        setSupportActionBar(mToolbarTb);
        // init ListView
        initListData();
        bindDataToListView();
        listView.setOnItemClickListener(onItemClickListener);
    }

    public void setBadge(int type) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        // set badge 0
        View tab = menuView.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) tab;
        View badge = LayoutInflater.from(this).inflate(R.layout.notification_msg, menuView, false);
        itemView.addView(badge);
        TextView msg_notification = findViewById(R.id.msg);
        // set badge 1
        tab = menuView.getChildAt(1);
        itemView = (BottomNavigationItemView) tab;
        badge = LayoutInflater.from(this).inflate(R.layout.notification_post, menuView, false);
        itemView.addView(badge);
        TextView post_notification = findViewById(R.id.post);

        if (chatRecordModel == null) {
            chatRecordModel = new ChatRecordModel(MainActivity.this, state.getUserName());
        }
        if (newsRecordModel == null) {
            newsRecordModel = new NewsRecordModel(MainActivity.this, state.getUserName());
        }
        if (momentRecordModel == null) {
            momentRecordModel = new MomentRecordModel(MainActivity.this, state.getUserName());
        }
        int msg_count = chatRecordModel.getUnReadMsgAmount();
        int news_count = newsRecordModel.getUnReadNewsAmount();
        int post_count = momentRecordModel.getUnReadPostAmount();
        switch (type) {
            case R.id.navigation_notifications:
                if (msg_count == 0 && news_count == 0) {
                    msg_notification.setVisibility(View.INVISIBLE);
                }
                if (post_count == 0) {
                    post_notification.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.navigation_home:
                if (msg_count == 0 && news_count == 0) {
                    msg_notification.setVisibility(View.INVISIBLE);
                }
                if (post_count == 0) {
                    post_notification.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.navigation_dashboard:
                post_notification.setVisibility(View.INVISIBLE);
                if (msg_count == 0 && news_count == 0) {
                    msg_notification.setVisibility(View.INVISIBLE);
                }
                break;
            default:break;
        }
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
        setBadge(R.id.navigation_home);

        // init Toolbar
        mToolbarTb = (Toolbar) findViewById(R.id.tb_toolbar);
        setSupportActionBar(mToolbarTb);

        // init ListView
        if (list_data != null) {
            list_data.clear();
        }
        initListData();
        bindDataToListView();
        listView.setOnItemClickListener(onItemClickListener);
    }

    // when back to Post Page
    private void showPostPage() {
        setContentView(R.layout.post_page);
        // init navigation bar
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_dashboard);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setBadge(R.id.navigation_dashboard);

        // init ListView
        if (post_data != null) {
            list_data.clear();
        }
        initPostData();
    }


    private void showUserPage() {
        setContentView(R.layout.user_page);
        // init navigation bar
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_notifications);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setBadge(R.id.navigation_notifications);
        Button quit_btn = findViewById(R.id.quit_btn);
        quit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage("确认退出吗？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logOut("log-out");
                                Intent intent = new Intent(MainActivity.this, MyNetWorkService.class);
                                stopService(intent);
                            }
                        })
                        .create().show();
            }
        });
        getUserInfo();
    }

    private void getUserInfo() {
        String url = MyApplication.host + String.format(getInfo_uri + "?token=%s", state.getToken());
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url
                , null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.i("get", jsonObject.toString());
                        try {
                            if (jsonObject.getInt("rtn") == 0) {
                                Log.i("get", "modify text");
                                TextView textView = findViewById(R.id.user_nickname);
                                textView.setText(jsonObject.getJSONObject("data").getString("nickname"));
                                textView = findViewById(R.id.user_username);
                                textView.setText(jsonObject.getJSONObject("data").getString("username"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("action","error => "+error.toString());
                    }
                });
        getRequest.addMarker("get-action");
        MyApplication.getHttpQueues().add(getRequest);
    }


    private void initListData() {
        if (chatRecordModel == null) {
            chatRecordModel = new ChatRecordModel(MainActivity.this,state.getUserName());
        }
        if (newsRecordModel == null) {
            newsRecordModel =  new NewsRecordModel(MainActivity.this, state.getUserName());
        }
        list_data = new ArrayList<Map<String, Object>>();
        // data of girl0
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", "Eva");
        map.put("img_id", R.drawable.dft0);
        if (chatRecordModel == null) {
            chatRecordModel = new ChatRecordModel(MainActivity.this, state.getUserName());
        }
        ChatRecordModel.ChattingItem item = chatRecordModel.getLastChattingItem();
        if (item != null) {
            map.put("last_msg", item.MsgContent);
            map.put("time", timeTool.timeConverter(item.Ctime));
        } else {
            map.put("last_msg", "");
            map.put("time", "");
        }
        list_data.add(map);
        // data of news
        Map<String, Object> news_data = new HashMap<String, Object>();
        NewsRecordModel.NewsItem news = newsRecordModel.getLastNewsItem();
        news_data.put("username", "News");
        news_data.put("img_id", R.drawable.news);
        if (news != null) {
            news_data.put("last_msg", news.Title);
            news_data.put("time", timeTool.timeConverter(item.Ctime));
        } else {
            news_data.put("last_msg", "");
            news_data.put("time", "");
        }
        list_data.add(news_data);
    }

    private void initPostData() {
        if (momentRecordModel == null) {
            momentRecordModel = new MomentRecordModel(MainActivity.this, state.getUserName());
        }
        post_data = new ArrayList<Map<String, Object>>();
        ArrayList<MomentRecordModel.MomentItem> items = momentRecordModel.getMomentPosts(5);
        for (int i = items.size() - 1; i >= 0; --i) {
            Map<String, Object> data = new HashMap<String, Object>();
            MomentRecordModel.MomentItem momentItem = items.get(i);
            data.put("content", momentItem.Content);
            data.put("author", momentItem.Author);
            data.put("date", timeTool.timeConverterLong(momentItem.Ctime));
            data.put("img_id", R.drawable.dft0);
            post_data.add(data);
        }

        String[] data = new String[] {"author", "date", "content", "img_id"};
        int[] container = new int[] {R.id.post_item_name, R.id.post_date, R.id.post_item_msg,
        R.id.post_item_image};
//        R.id.post_item_image
        SimpleAdapter post_adapter = new SimpleAdapter(this, post_data,
                R.layout.post_list_item, data, container);

        View headView = getLayoutInflater().inflate(R.layout.post_headerview, null);
        listView = (ListView) findViewById(R.id.post_list);
        listView.addHeaderView(headView, null, false);
        listView.setAdapter(post_adapter);
        listView.smoothScrollToPosition(post_data.size()-1);
        momentRecordModel.flushUnReadPosts();
    }

    private void bindDataToListView() {
        String[] from = new String[] {"username", "last_msg", "img_id", "time"};
        int[] to = new int[] {R.id.list_item_name, R.id.list_item_msg,
                R.id.list_item_image, R.id.list_item_date };
        adapter = new SimpleAdapter(this, list_data,
                R.layout.list_view_item, from, to);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
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
            state.clear("token");
            state.clear("username");
            // switch to log in activity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public class MsgBroadCastReceiver extends BroadcastReceiver {
        private static final String MESSAGE_ACTION = "MESSAGE_ACTION";
        private static final String POST_ACTION = "POST_ACTION";
        private static final String NEWS_ACTION = "NEWS_ACTION";
        private static final String SWITCH_ACTION = "SWITCH_ACTION";
        private static final String SWITCH_TO_MSG_ACTION = "SWITCH_TO_MSG_ACTION";
        private static final String SWITCH_TO_POST_ACTION = "SWITCH_TO_POST_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case MESSAGE_ACTION: Log.i("broad", "rev message");
                                         onMessageAction(intent.getStringExtra("content"));
                                         break;
                    case POST_ACTION: Log.i("broad", "rev post");
                                      onPostAction(intent.getStringExtra("author"),
                                              intent.getStringExtra("content"),
                                              intent.getStringExtra("date"),
                                              intent.getStringExtra("img_uri"));
                                      break;
                    case NEWS_ACTION: Log.i("broad", "rev news");
                                      onNewsAction(intent.getStringExtra("content"));
                                      break;
                    case SWITCH_ACTION: Log.i("broad", "rev switch");
                                         onSwitchAction();
                                         break;
                    case SWITCH_TO_MSG_ACTION: Log.i("broad", "rev switch_to_msg");
                                               onSwitchMsgAction();
                                               break;
                    case SWITCH_TO_POST_ACTION: Log.i("broad", "rev switch_to_post");
                                                onSwitchPostAction();
                                                break;
                }
            }
        }

        private void onMessageAction(String text) {
            if (MyApplication.isForeground(MainActivity.this)) {
                int count = chatRecordModel.getUnReadMsgAmount();
                if (count > 0) {
                    // set unread tag to message page
                    TextView textView = findViewById(R.id.msg);
                    textView.setVisibility(View.VISIBLE);

                  if (navigation.getSelectedItemId() == R.id.navigation_home) {
                      //update latest news
                      View view = listView.getChildAt(0 - listView.getFirstVisiblePosition());
                      if (view != null) {
                          TextView textview = view.findViewById(R.id.list_item_msg);
                          textview.setText(text);
                          //set unread tag to list
                          TextView textview2 = view.findViewById(R.id.message_unread);
                          textview2.setVisibility(View.VISIBLE);
                          textview2.setText(String.valueOf(count));
                          textview = view.findViewById(R.id.list_item_date);
                          textview.setText(timeTool.timeConverter(timeTool.getTimeRightNowLong()));
                      }
                  }
                }
            }
        }
        private void onPostAction(String author, String content, String date, String img_uri) {
            if (navigation.getSelectedItemId() == R.id.navigation_dashboard) {
                Map<String, Object> map = new HashMap<>();
                map.put("author", author);
                map.put("content", content);
                map.put("date", date);
                post_data.add(map);
                notify();
                listView.smoothScrollToPosition(post_data.size()-1);
                View view = listView.getChildAt(post_data.size()
                        - 1 - listView.getFirstVisiblePosition());
                if (view != null) {
                    ImageView imageView = view.findViewById(R.id.post_image);
                    imageView.setImageResource(R.drawable.post1);
                }
                momentRecordModel.flushUnReadPosts();
            }
            else {
                TextView textView = findViewById(R.id.post);
                textView.setVisibility(View.VISIBLE);
            }
        }

        private void onNewsAction(String title) {
            if (MyApplication.isForeground(MainActivity.this)) {
                int count = newsRecordModel.getUnReadNewsAmount();
                if (count > 0) {
                    // set unread tag to message page
                    TextView textView = findViewById(R.id.msg);
                    textView.setVisibility(View.VISIBLE);

                    //update latest news
                    View view = listView.getChildAt(1);
                    if (view != null) {
                        TextView textview = view.findViewById(R.id.list_item_msg);
                        textview.setText(title);
                        //set unread tag to list
                        TextView textview2 = view.findViewById(R.id.message_unread);
                        textview2.setVisibility(View.VISIBLE);
                        textview2.setText(String.valueOf(count));
                        // set time
                        textview = view.findViewById(R.id.list_item_date);
                        textview.setText(timeTool.timeConverter(timeTool.getTimeRightNowLong()));
                    }
                }
            }
        }

        private void onSwitchAction() {
            if (MyApplication.isForeground(MainActivity.this)) {

              if (navigation.getSelectedItemId() == R.id.navigation_home) {
                  if (chatRecordModel == null) {
                      chatRecordModel = new ChatRecordModel(MainActivity.this, state.getUserName());
                  }
                  if (newsRecordModel == null) {
                      newsRecordModel = new NewsRecordModel(MainActivity.this, state.getUserName());
                  }
                  listView = findViewById(R.id.list_view);

                  // set msg
                  int msg_count = chatRecordModel.getUnReadMsgAmount();
                  View view = listView.getChildAt(0);
                  if (msg_count > 0) {
                      //set unread tag to list
                      if (view != null) {
                          TextView textview2 = view.findViewById(R.id.message_unread);
                          textview2.setVisibility(View.VISIBLE);
                          textview2.setText(String.valueOf(msg_count));
                      }
                  }
                  else {
                      if (view != null) {
                          TextView textview2 = view.findViewById(R.id.message_unread);
                          textview2.setVisibility(View.INVISIBLE);
                      }
                  }
                  //update latest msg
                  if (view != null) {
                      TextView textview = view.findViewById(R.id.list_item_msg);
                      ChatRecordModel.ChattingItem item = chatRecordModel.getLastChattingItem();
                      if (item != null) {
                          textview.setText(item.MsgContent);
                          textview = view.findViewById(R.id.list_item_date);
                          textview.setText(timeTool.timeConverter(item.Ctime));
                      }
                  }
                  // set news
                  int news_count = newsRecordModel.getUnReadNewsAmount();
                  view = listView.getChildAt(1);
                  if (news_count > 0) {
                      //set unread tag to list
                      if (view != null) {
                          TextView textview2 = view.findViewById(R.id.message_unread);
                          textview2.setVisibility(View.VISIBLE);
                          textview2.setText(String.valueOf(news_count));
                      }
                  }
                  else {
                      if (view != null) {
                          TextView textview2 = view.findViewById(R.id.message_unread);
                          textview2.setVisibility(View.INVISIBLE);
                      }
                  }
                  //update latest news
                  if (view != null) {
                      TextView textview = view.findViewById(R.id.list_item_msg);
                      NewsRecordModel.NewsItem item = newsRecordModel.getLastNewsItem();
                      if (item != null) {
                          textview.setText(item.Title);
                          textview = view.findViewById(R.id.list_item_date);
                          textview.setText(timeTool.timeConverter(item.Ctime));
                      }
                  }

                  if (msg_count == 0 && news_count == 0) {
                      TextView textView = findViewById(R.id.msg);
                      textView.setVisibility(View.INVISIBLE);
                  }
              }
            }
        }

        private void onSwitchMsgAction() {
            int msg_count = chatRecordModel.getUnReadMsgAmount();
            int news_count = newsRecordModel.getUnReadNewsAmount();
            if (msg_count > 0) {
                if (msg_count > 0) {
                    //set unread tag to list
                    View view = listView.getChildAt(0);
                    Log.i("check-msg", String.valueOf(msg_count));
                    if (view != null) {
                        TextView textview = view.findViewById(R.id.message_unread);
                        textview.setVisibility(View.VISIBLE);
                        textview.setText(String.valueOf(msg_count));
                        textview = view.findViewById(R.id.list_item_date);
                    } else {
                        Log.i("check-msg", "view is null");
                    }
                }
            }
            if (news_count > 0) {
                if (news_count > 0) {
                    //set unread tag to list
                    View view = listView.getChildAt(1);
                    Log.i("check-news", String.valueOf(news_count));
                    if (view != null) {
                        TextView textview = view.findViewById(R.id.message_unread);
                        textview.setVisibility(View.VISIBLE);
                        textview.setText(String.valueOf(news_count));
                    } else {
                        Log.i("check-news", "view is null");
                    }
                }
            }
        }

        private void onSwitchPostAction() {
            if (momentRecordModel == null) {
                momentRecordModel = new MomentRecordModel(MainActivity.this, state.getUserName());
            }
            ArrayList<MomentRecordModel.MomentItem> momentItems = momentRecordModel.getMomentPosts(post_data.size() - 1);
            for (int i = momentItems.size(); i >= 1; --i) {
                View view = listView.getChildAt(momentItems.size() + 1 - i - listView.getFirstVisiblePosition());
                if (view != null) {
                    MomentRecordModel.MomentItem item = momentItems.get(i);
                    ImageView imageView = view.findViewById(R.id.post_image);
                    imageView.setImageResource(R.drawable.post1);
                } else {
                    Log.i("check-post", "view is null");
                }
            }
            momentRecordModel.flushUnReadPosts();
        }
    }

}
