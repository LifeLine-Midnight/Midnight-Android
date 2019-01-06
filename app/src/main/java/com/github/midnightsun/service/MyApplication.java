package com.github.midnightsun.client;

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
    private static final String host = "http://golfqiu.cn";

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


    /**
     *  使用Post方式返回JsonObject类型的请求结果数据
     *
     *  new JsonObjectRequest(int method,String url,JsonObject jsonObject,Listener listener,ErrorListener errorListener)
     *  method：请求方式，Get请求为Method.GET，Post请求为Method.POST
     *  url：请求地址
     *  JsonObject：Json格式的请求参数。如果使用的是Get请求方式，请求参数已经包含在url中，所以可以将此参数置为null
     *  listener：请求成功后的回调
     *  errorListener：请求失败的回调
     */
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
