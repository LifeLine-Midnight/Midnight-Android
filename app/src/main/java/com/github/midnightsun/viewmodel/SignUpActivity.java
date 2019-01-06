package com.github.midnightsun.viewmodel;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.midnightsun.datatype.UserData;
import com.github.midnightsun.service.MyApplication;
import com.github.midnightsun.R;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private EditText nick_name_input;
    private EditText user_name_input;
    private EditText psd_input;
    private Button completed_btn;
    private Button cancled_btn;
    private UserData data_receive;
    private static final String uri = "/midnightapisvr/api/user/userregister";

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
                signUp(nickname, username, psd, "sign-up");
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        //通过Tag标签取消请求队列中对应的全部请求
        MyApplication.getHttpQueues().cancelAll("sign-up");
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

    private void signUp(String nickname, String username, String psd, String tag) {
        Map<String, String> map = new HashMap<>();
        map.put("nickname", nickname);
        map.put("username", username);
        map.put("password", psd);

        JSONObject object = new JSONObject(map);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                MyApplication.host + uri, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.i("sign-up", jsonObject.toString());
                        Gson gson = new Gson();
                        data_receive = gson.fromJson(jsonObject.toString(), UserData.class);
                        UIfresh();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.i("sign-up", volleyError.toString());
                    }
                });

        request.setTag(tag);
        MyApplication.getHttpQueues().add(request);
    }

    void UIfresh() {
        if (data_receive.rtn != 0) {
            Toast.makeText(SignUpActivity.this,
                    data_receive.msg, Toast.LENGTH_SHORT).show();
            user_name_input.setText("");
            psd_input.setText("");
        }
        else {
            Toast.makeText(SignUpActivity.this,
                    "注册成功",Toast.LENGTH_SHORT).show();

            // switch to login activity
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }
}
