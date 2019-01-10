package com.github.midnightsun.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.midnightsun.model.ChatRecordModel;
import com.github.midnightsun.service.MyApplication;
import com.github.midnightsun.utilis.ChatAdapter;
import com.github.midnightsun.structure.DateType;
import com.github.midnightsun.structure.MessageType;
import com.github.midnightsun.structure.MoreBeanType;
import com.github.midnightsun.structure.OnlineType;
import com.github.midnightsun.R;
import com.github.midnightsun.utilis.SystemState;
import com.github.midnightsun.utilis.TimeTool;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    public static final int MSG_OTHER = 0;
    public static final int MSG_SELF = 1;
    public static final int MSG_ONLINE = 2;
    public static final int MSG_OFFLINE = 3;
    public static final int MSG_TIME = 4;
    private String unkonw_name;

    private android.support.v7.widget.Toolbar titlebar;
    private ImageButton back;
    private Button send;
    private TextView selectionOne;
    private TextView selectionTwo;
    private EditText editText;

    private ChatAdapter adapter;
    private List<MoreBeanType> messages = new ArrayList<>();
    private RecyclerView recyclerView;
    private SystemState state = new SystemState(ChatActivity.this);
    private ChatRecordModel chatRecordModel;
    private TimeTool timeTool = new TimeTool();
    private static final String uri_choice = "/midnightapisvr/api/action/makechoice";
    ChatBroadCastReceiver mBroadcastReceiver;
    private int sid;
    private int option;
    private int img_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("chat", "onCreate: Chat");
        setContentView(R.layout.activity_chat);

        chatRecordModel = new ChatRecordModel(ChatActivity.this, state.getUserName());
        //initial button click event
        setBackButtonClickListener();

        //initial tool bar
        titlebar = findViewById(R.id.title_bar);
        setSupportActionBar(titlebar);

        //initial list
        initdata();
        LinearLayoutManager layoutManager =new LinearLayoutManager(this);
        adapter = new ChatAdapter(messages);
        recyclerView = findViewById(R.id.chat_list);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(messages.size()-1);

        //initial selection textview
        selectionOne = findViewById(R.id.selection_one);
        selectionTwo = findViewById(R.id.selection_two);
        editText = findViewById(R.id.edit_query);

        //set onclick event
        setSelectionClickListener(selectionOne);
        setSelectionClickListener(selectionTwo);
        setSendButtonClickListender();

        mBroadcastReceiver = new ChatBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ChatBroadCastReceiver.MESSAGE_ACTION);
        intentFilter.addAction(ChatBroadCastReceiver.OFFLINE_ACTION);
        intentFilter.addAction(ChatBroadCastReceiver.ONLINE_ACTION);
        intentFilter.addAction(ChatBroadCastReceiver.OPTION_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        chatRecordModel.flushUnReadMsg();
        Intent intent = new Intent();
        intent.setAction("SWITCH_ACTION");
        sendBroadcast(intent);
        Log.i("chat", "onDestroy: Chat");
    }

    private void setBackButtonClickListener() {
        back = findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivity.this.finish();
            }
        });
    }

    private void setSelectionClickListener(final TextView textView) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(textView.getText());
                if (textView.getId() == R.id.selection_one) {
                    option = 0;
                } else {
                    option = 1;
                }
            }
        });
    }

    private void setSendButtonClickListender() {
        send = findViewById(R.id.send_btn);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("tag", "send button cliked!");
                String msg = editText.getText().toString();
                if (!msg.equals("")) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("token", state.getToken());
                    map.put("sid", sid);
                    map.put("option", option);
                    postACK(map, uri_choice);
                }
            }
        });
    }

    private void initdata() {
        Intent intent = getIntent();
        img_id = intent.getIntExtra("img_id", R.drawable.dft0);
        unkonw_name = intent.getStringExtra("username");

        TextView textView = findViewById(R.id.chat_title);
        textView.setText(unkonw_name);

        ArrayList<ChatRecordModel.ChattingItem> chattingItems = chatRecordModel.getChattingItems(25);

        for (int i = chattingItems.size() - 1; i >= 0; --i) {
            switch (chattingItems.get(i).MsgType) {
                case MSG_OTHER:
                    MessageType otherType = new MessageType(chattingItems.get(i).MsgContent, img_id,
                            MSG_OTHER);
                    messages.add(otherType);
                    break;
                case MSG_SELF:
                    MessageType selfType = new MessageType(chattingItems.get(i).MsgContent, R.drawable.user_default,
                            MSG_SELF);
                    messages.add(selfType);
                    break;
                case MSG_TIME:
                    DateType dateType = new DateType(timeTool.timeConverterLong(chattingItems.get(i).MsgContent), MSG_TIME);
                    messages.add(dateType);
                    break;
                case MSG_ONLINE:
                    OnlineType onlineType = new OnlineType(unkonw_name, MSG_ONLINE);
                    messages.add(onlineType);
                    break;
                case MSG_OFFLINE:
                    OnlineType offlineType = new OnlineType(unkonw_name, MSG_OFFLINE);
                    messages.add(offlineType);
                    break;
            }
        }
    }

    public class ChatBroadCastReceiver extends BroadcastReceiver {
        private static final String OPTION_ACTION = "OPTION_ACTION";
        private static final String ONLINE_ACTION = "ONLINE_ACTION";
        private static final String OFFLINE_ACTION = "OFFLINE_ACTION";
        private static final String MESSAGE_ACTION = "MESSAGE_ACTION";

        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction() != null) {
                   Runnable runnable = new Runnable() {
                       @Override
                       public void run() {
                           switch (intent.getAction()) {
                               case OPTION_ACTION:
                                   Log.i("broad", "rev opt");
                                   onOptionAction(intent.getStringExtra("l_content"),
                                           intent.getStringExtra("r_content"), intent.getIntExtra("sid", 0));
                                   break;
                               case ONLINE_ACTION:
                                   Log.i("broad", "rev online");
                                   onOnlineAction(intent.getBooleanExtra("show_time", false),
                                           intent.getStringExtra("time"));
                                   break;
                               case OFFLINE_ACTION:
                                   Log.i("broad", "rev offline");
                                   onOfflineAction();
                                   break;
                               case MESSAGE_ACTION:
                                   Log.i("broad", "rev message");
                                   onMessageAction(intent.getStringExtra("content"),
                                           intent.getBooleanExtra("show_time", false),
                                           intent.getStringExtra("time"));
                                   break;
                           }
                       }
                   };
                   runnable.run();
            }

        }
    }

        private void onOptionAction(String l_text, String r_text, int s) {
            if (MyApplication.isForeground(ChatActivity.this)) {
                selectionOne.setText(l_text);
                selectionOne.setVisibility(View.VISIBLE);
                selectionTwo.setText(r_text);
                selectionTwo.setVisibility(View.VISIBLE);
                sid = s;
                recyclerView.scrollToPosition(messages.size()-1);
            }
        }

        private void onOnlineAction(boolean is_show, String time) {
            if (MyApplication.isForeground(ChatActivity.this)) {
                if (is_show) {
                    DateType dateType = new DateType(timeTool.timeConverterLong(time),
                            MSG_ONLINE);
                    messages.add(dateType);
                }
                OnlineType onlineType = new OnlineType(unkonw_name, MSG_ONLINE);
                messages.add(onlineType);
                adapter.notifyItemInserted(messages.size()-1);
                recyclerView.scrollToPosition(messages.size()-1);
            }
        }

        private void onOfflineAction() {
            if (MyApplication.isForeground(ChatActivity.this)) {
                OnlineType onlineType = new OnlineType(unkonw_name, MSG_OFFLINE);
                messages.add(onlineType);
                adapter.notifyItemInserted(messages.size()-1);
                recyclerView.scrollToPosition(messages.size()-1);
            }
        }

        private void onMessageAction(String content, boolean is_show, String time) {
            if (MyApplication.isForeground(ChatActivity.this)) {
                if (is_show) {
                    DateType dateType = new DateType(timeTool.timeConverterLong(time),
                            MSG_ONLINE);
                    messages.add(dateType);
                }
                MessageType onlineType = new MessageType(content, img_id, MSG_OTHER);
                messages.add(onlineType);
                adapter.notifyItemInserted(messages.size()-1);
                recyclerView.scrollToPosition(messages.size()-1);
            }
        }

    private void postACK(Map<String, Object> map, String uri) {
        JSONObject object = new JSONObject(map);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                MyApplication.host + uri, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.i("msg-ack", jsonObject.toString());
                        UIrefresh(option);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.i("msg-ack", volleyError.toString());
                    }
                });

        request.setTag("msg_ack");
        MyApplication.getHttpQueues().add(request);
    }

    void UIrefresh(int option) {
        String msg;
        if (option == 0) {
            msg = selectionOne.getText().toString();
        } else {
            msg = selectionTwo.getText().toString();
        }
        if (timeTool.isLongEnough(chatRecordModel.getLastChattingItem().Ctime)) {
            String time = timeTool.getTimeRightNowLong();
            chatRecordModel.addChattingItem(MSG_TIME, time, true);
            DateType dateType = new DateType(timeTool.timeConverterLong(time), MSG_TIME);
            messages.add(dateType);
        }
        MessageType mine = new MessageType(msg,
                R.drawable.user_default, MSG_SELF);
        messages.add(mine);
        chatRecordModel.addChattingItem(MSG_SELF, msg, true);
        adapter.notifyItemChanged(messages.size()-1, " ");
        recyclerView.scrollToPosition(messages.size()-1);
        editText.setText("");
        selectionOne.setVisibility(View.GONE);
        selectionTwo.setVisibility(View.GONE);
    }
}
