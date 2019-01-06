package com.github.midnightsun.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.github.midnightsun.R;
import com.github.midnightsun.datatype.UserData;
import com.github.midnightsun.service.MyApplication;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private Button signUpButton;
    private Button signInButton;
    private EditText user_name;
    private EditText use_psd;
    private UserData userData;
    private static final String uri = "/midnightapisvr/api/session/userlogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setSignInButtonClickListener();
        setSignUpButtonClickListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //通过Tag标签取消请求队列中对应的全部请求
        MyApplication.getHttpQueues().cancelAll("log-in");
    }

    void setSignInButtonClickListener() {
        signInButton = findViewById(R.id.sign_in);
        user_name = findViewById(R.id.user_id_input);
        use_psd = findViewById(R.id.user_psd_input);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = user_name.getText().toString();
                String psd = use_psd.getText().toString();

                logIn(username, psd, "log-in");
            }
        });
    }

    void setSignUpButtonClickListener() {
        signUpButton = findViewById(R.id.sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    void setUserLoged() {
        SharedPreferences sharedPreferences =
                getSharedPreferences("system_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", userData.data.token);
        editor.apply();
    }

    private void logIn(String username, String psd, String tag) {
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", psd);

        JSONObject object = new JSONObject(map);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                MyApplication.host + uri, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.i("log-in", jsonObject.toString());
                        Gson gson = new Gson();
                        userData = gson.fromJson(jsonObject.toString(), UserData.class);
                        UIfresh();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.i("log-in", volleyError.toString());
                    }
                });

        request.setTag(tag);
        MyApplication.getHttpQueues().add(request);
    }

    void UIfresh() {
        if (userData.rtn != 0) {
            Toast.makeText(LoginActivity.this,
                    userData.msg, Toast.LENGTH_SHORT).show();
            user_name.setText("");
            use_psd.setText("");
        }
        else {
            Toast.makeText(LoginActivity.this,
                    "登陆成功",Toast.LENGTH_SHORT).show();

            setUserLoged();
            // switch to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            user_name.setText("");
            use_psd.setText("");
        }
    }
}
