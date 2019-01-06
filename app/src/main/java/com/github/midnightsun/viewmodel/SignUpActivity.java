package com.github.midnightsun;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private EditText nick_name_input;
    private EditText user_name_input;
    private EditText psd_input;
    private Button completed_btn;
    private Button cancled_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setCompletedButtonClickListener();
        setCancledButtonClickListener();
    }

    private void setCompletedButtonClickListener() {
        completed_btn = findViewById(R.id.sign_up_completed);
        nick_name_input = findViewById(R.id.user_nickname_input);
        user_name_input = findViewById(R.id.user_id_input);
        psd_input = findViewById(R.id.user_psd_input);
        completed_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = nick_name_input.getText().toString();
                String username = user_name_input.getText().toString();
                String psd = psd_input.getText().toString();
                Toast toast;

                //post data to remote host
                postSignUpDataToHost(nickname, username, psd);

                if (!isValid()) {
                    toast =  Toast.makeText(SignUpActivity.this,
                            "用户名或密码存在格式错误",Toast.LENGTH_SHORT);
                    toast.show();
                    user_name_input.setText("");
                    psd_input.setText("");
                }
                else {
                    toast =  Toast.makeText(SignUpActivity.this,
                            "注册成功",Toast.LENGTH_SHORT);
                    toast.show();
                    // save user infor
                    SharedPreferences sharedPreferences =
                            getSharedPreferences(username, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("psd", psd);
                    editor.putString("nickname",nickname);
                    editor.commit();

                    // switch to login activity
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        //通过Tag标签取消请求队列中对应的全部请求
        MyApplication.getHttpQueues().cancelAll("register");
    }

    private void postSignUpDataToHost(String nickname, String username, String psd) {
        Map<String, String> map = new HashMap<>();
        map.put("nickname", nickname);
        map.put("username", username);
        map.put("password", psd);

        MyApplication.volleyPost(SignUpActivity.this,
                "/midnightapisvr/api/user/userregister",
                "register", map);
    }

    private void setCancledButtonClickListener() {
        cancled_btn = findViewById(R.id.sign_up_cancled);
        cancled_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast =  Toast.makeText(SignUpActivity.this,
                        "注册已取消",Toast.LENGTH_SHORT);
                toast.show();
                // switch to login activity
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    Boolean isValid() {
        return true;
    }
}
