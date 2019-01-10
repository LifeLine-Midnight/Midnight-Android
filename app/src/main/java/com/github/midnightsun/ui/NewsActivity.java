package com.github.midnightsun.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.github.midnightsun.R;
import com.github.midnightsun.model.NewsRecordModel;
import com.github.midnightsun.utilis.SystemState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar titlebar;
    private ListView listView;
    private NewsRecordModel newsRecordModel;
    private List<Map<String, Object>> news_data = new ArrayList<>();
    private SystemState state = new SystemState(NewsActivity.this);
    private SimpleAdapter adapter;
    private ImageButton back;
    private NewsBroadCastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        mBroadcastReceiver = new NewsBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NewsBroadCastReceiver.NEWS_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);

        newsRecordModel = new NewsRecordModel(NewsActivity.this, state.getUserName());
        //initial button click event
        setBackButtonClickListener();

        //initial tool bar
        titlebar = findViewById(R.id.title_bar);
        setSupportActionBar(titlebar);

        //initial list data
        initdata();
        binddata();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        newsRecordModel.flushUnReadNews();
        Intent intent = new Intent();
        intent.setAction("SWITCH_ACTION");
        sendBroadcast(intent);
        Log.i("news", "onDestroy: News");
    }

    private void setBackButtonClickListener() {
        back = findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewsActivity.this.finish();
            }
        });
    }

    private void initdata() {
        if (newsRecordModel == null) {
            newsRecordModel = new NewsRecordModel(NewsActivity.this, state.getUserName());
        }

        ArrayList<NewsRecordModel.NewsItem> items = newsRecordModel.getNewsItems(10);
        for (int i = items.size() - 1; i >= 0; --i) {
            Map<String, Object> map = new HashMap<>();
            NewsRecordModel.NewsItem item = items.get(i);
            map.put("title", item.Title);
            map.put("content", item.Content);
            news_data.add(map);
        }
    }

    private void binddata() {
        listView = findViewById(R.id.news_list);
        String[] from = new String[] {"title", "content"};
        int[] to = new int[] {R.id.news_title, R.id.news_content};

        adapter = new SimpleAdapter(this, news_data,
                R.layout.new_list_item, from, to);
        listView.setAdapter(adapter);
    }

    private class NewsBroadCastReceiver extends BroadcastReceiver {
        private static final String NEWS_ACTION = "NEWS_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == NEWS_ACTION) {
                Log.i("broad", "rev news");
                onNewsAction(intent.getStringExtra("title"),
                        intent.getStringExtra("content"));
            }
        }

        private void onNewsAction(String title, String content) {
            Map<String, Object> map = new HashMap<>();
            map.put("title", title);
            map.put("content", content);
            news_data.add(map);
            listView.deferNotifyDataSetChanged();
            listView.smoothScrollToPosition(news_data.size()-1);
        }
    }
}
