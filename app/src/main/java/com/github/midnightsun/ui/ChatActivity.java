package com.github.midnightsun.ui;

import android.content.Intent;
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

import com.github.midnightsun.utilis.ChatAdapter;
import com.github.midnightsun.structure.DateType;
import com.github.midnightsun.structure.MessageType;
import com.github.midnightsun.structure.MoreBeanType;
import com.github.midnightsun.structure.OnlineType;
import com.github.midnightsun.R;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar titlebar;
    private ImageButton back;
    private Button send;
    private TextView selectionOne;
    private TextView selectionTwo;
    private EditText editText;

    private ChatAdapter adapter;
    private List<MoreBeanType> messages = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

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

        //initial selection textview
        selectionOne = findViewById(R.id.selection_one);
        selectionTwo = findViewById(R.id.selection_two);
        editText = findViewById(R.id.edit_query);

        //set onclick event
        setMenuOnClickListener();
        setSelectionClickListener(selectionOne);
        setSelectionClickListener(selectionTwo);
        setSendButtonClickListender();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("tag", "onDestroy: Chat");
    }

    private void setMenuOnClickListener() {
        ImageButton btn = findViewById(R.id.menu_button);
        btn.setOnClickListener(new View.OnClickListener() {

           @Override
           public void onClick(View v) {
               Log.i("tag", "menu clicked!");
               selectionOne.setVisibility(View.VISIBLE);
               selectionTwo.setVisibility(View.VISIBLE);
           }
        });
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
                MessageType mine = new MessageType(msg,
                        R.drawable.back_img,ChatAdapter.TYPE_MSG_RIGHT);
                messages.add(mine);
                adapter.notifyItemChanged(messages.size()-1, " ");
                recyclerView.scrollToPosition(messages.size()-1);
            }
        });
    }

    private void initdata() {
        Intent intent = getIntent();
        int img_id = intent.getIntExtra("img_id", R.drawable.dft0);
        String name = intent.getStringExtra("username");

        TextView textView = findViewById(R.id.chat_title);
        textView.setText(name);

        MessageType messageType = new MessageType("你好！", img_id,
                ChatAdapter.TYPE_MSG_LEFT);
        messages.add(messageType);
        messageType = new MessageType("你好, 请问你是？", R.drawable.back_img,
                ChatAdapter.TYPE_MSG_RIGHT);
        messages.add(messageType);
        OnlineType onlineType = new OnlineType(name, ChatAdapter.TYPE_OFFLINE);
        DateType dateType = new DateType("周四", "15:35", ChatAdapter.TYPE_MSG_DATE);
        messages.add(onlineType);
        messageType = new MessageType("哎，别走啊，我们还没开始说话呢？···不会是骗子吧", R.drawable.back_img,
                ChatAdapter.TYPE_MSG_RIGHT);
        messages.add(messageType);
        messages.add(dateType);
    }
}
