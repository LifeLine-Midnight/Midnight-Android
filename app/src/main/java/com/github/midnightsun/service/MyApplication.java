package com.github.midnightsun.service;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Map;


public class MyApplication extends Application {
    private static RequestQueue queues ;
    public static final String host = "http://golfqiu.cn";

    @Override
    public void onCreate() {
        super.onCreate();
        queues = Volley.newRequestQueue(getApplicationContext());
    }

    public static RequestQueue getHttpQueues() {
        return queues;
    }




    public static void volleyGet(final Context context, String uri, final String tag) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, host + uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {//jsonObject为请求返回的Json格式数据
//                        Toast.makeText(context,jsonObject.toString(),Toast.LENGTH_LONG).show();
                        Log.i(tag, jsonObject.toString());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
//                        Toast.makeText(context, volleyError.toString(),Toast.LENGTH_LONG).show();
                          Log.i(tag, volleyError.toString());
                    }
                });

        //设置请求的Tag标签，可以在全局请求队列中通过Tag标签进行请求的查找
        request.setTag(tag);
        //将请求加入全局队列中
        MyApplication.getHttpQueues().add(request);
    }

    public static void volleyPost(final Context context, String uri, final String tag, Map<String, String> map) {
        //将map转化为JSONObject对象
        JSONObject jsonObject = new JSONObject(map);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, host + uri, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {//jsonObject为请求返回的Json格式数据
//                        Toast.makeText(context,jsonObject.toString(), Toast.LENGTH_LONG).show();
                        Log.i(tag, jsonObject.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
//                        Toast.makeText(context,volleyError.toString(),Toast.LENGTH_LONG).show();
                        Log.i(tag, volleyError.toString());
                    }
                });
        //设置请求的Tag标签，可以在全局请求队列中通过Tag标签进行请求的查找
        request.setTag(tag);
        //将请求加入全局队列中
        MyApplication.getHttpQueues().add(request);
    }
}
