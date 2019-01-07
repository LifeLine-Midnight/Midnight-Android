package com.github.midnightsun.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.midnightsun.datatype.MessageData;
import com.github.midnightsun.datatype.NewsData;
import com.github.midnightsun.datatype.OptionData;
import com.github.midnightsun.datatype.PostData;
import com.github.midnightsun.datatype.UserData;
import com.github.midnightsun.utilis.SystemState;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyService extends Service {
    private static final String TAG = "MyService";
    private static final String uri_act = "/midnightapisvr/api/action/getcurrentaction";
    private static final String uri_ack = "/midnightapisvr/api/action/normalmsgack";
    private static final String uri_choice = "/midnightapisvr/api/action/makechoice";

    private static final int ACTION_NULL = 0;
    private static final int ACTION_MSG = 1;
    private static final int ACTION_OPT = 2;
    private static final int ACTION_NEWS = 3;
    private static final int ACTION_POST = 4;
    private static final int ACTION_ON = 5;
    private static final int ACTION_OFF = 6;

    private Handler handler = new Handler();
    private Runnable runnable;
    private SystemState state = new SystemState(MyService.this);
    private Gson gson = new Gson();
    MessageData messageData;
    NewsData newsData;
    PostData postData;
    OptionData optionData;
    UserData userData;

    public MyService() {
        setRunnableTask();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "in onCreate");
    }
    @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "in onStartCommand");
        handler.postDelayed(runnable, 2000);
        return START_STICKY;
     }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "in onDestroy");
        handler.removeCallbacks(runnable);
        MyApplication.getHttpQueues().cancelAll("get-action");
        MyApplication.getHttpQueues().cancelAll("msg_ack");
        stopSelf();
    }

    private void setRunnableTask() {
        runnable = new Runnable() {
            @Override
            public void run() {
                getCurrentAction();
                handler.postDelayed(this, 1000);
            }
        };
    }

    private void getCurrentAction() {
        String url = MyApplication.host + String.format(uri_act + "?token=%s", state.getToken());
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url
            , null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    int type = -1;
                    try {
                        type = getActionType(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    switch (type) {
                        case ACTION_NULL:break;
                        case ACTION_MSG: messageSentAction(jsonObject);
                                         break;
                        case ACTION_OPT: optionSentAction(jsonObject);
                                         break;
                        case ACTION_NEWS:newsSentActon(jsonObject);
                                         break;
                        case ACTION_POST:postSentAction(jsonObject);
                                         break;
                        case ACTION_ON:  getOnlineAction(jsonObject);
                                         break;
                        case ACTION_OFF: getOfflineAction(jsonObject);
                                         break;
                        default: Log.i("action","error => type not found");
                                 break;
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

    private int getActionType(JSONObject jsonObject) throws JSONException {
        if (jsonObject.getInt("rtn") == 0) {
            return jsonObject.getJSONObject("data").getJSONObject("base_info").getInt("conjunction_msg_type");
        }
        return -1;
    }


    private void messageSentAction(JSONObject jsonObject) {
        messageData = gson.fromJson(jsonObject.toString(),MessageData.class);
        Log.i("action.message: ", messageData.data.conjunction_info.content);

        Map<String, Object> map = new HashMap<>();
        map.put("token", state.getToken());
        map.put("sid", messageData.data.base_info.sid);
        postACK(map, uri_ack, ACTION_MSG);
    }

    private void optionSentAction(JSONObject jsonObject) {
        optionData = gson.fromJson(jsonObject.toString(), OptionData.class);
        Log.i("action.option: ", optionData.data.conjunction_info.l_content
        + "  " + optionData.data.conjunction_info.r_content);

        Map<String, Object> map = new HashMap<>();
        map.put("token", state.getToken());
        map.put("sid", optionData.data.base_info.sid);
        map.put("option", 0);
        postACK(map, uri_choice, ACTION_OPT);
    }

    private void newsSentActon(JSONObject jsonObject) {
        newsData = gson.fromJson(jsonObject.toString(), NewsData.class);
        Log.i("action.news: ", newsData.data.conjunction_info.title);

        Map<String, Object> map = new HashMap<>();
        map.put("token", state.getToken());
        map.put("sid", newsData.data.base_info.sid);
        postACK(map, uri_ack, ACTION_NEWS);
    }

    private void postSentAction(JSONObject jsonObject) {
        postData = gson.fromJson(jsonObject.toString(), PostData.class);
        Log.i("action.post: ", postData.data.conjunction_info.author);

        Map<String, Object> map = new HashMap<>();
        map.put("token", state.getToken());
        map.put("sid", postData.data.base_info.sid);
        postACK(map, uri_ack, ACTION_POST);
    }

    private void getOnlineAction(JSONObject jsonObject) {
        messageData = gson.fromJson(jsonObject.toString(),MessageData.class);
        Log.i("action.message: ", "get online");

        Map<String, Object> map = new HashMap<>();
        map.put("token", state.getToken());
        map.put("sid", messageData.data.base_info.sid);
        postACK(map, uri_ack, ACTION_ON);
    }

    private void getOfflineAction(JSONObject jsonObject) {
        messageData = gson.fromJson(jsonObject.toString(),MessageData.class);
        Log.i("action.message: ", "get offline");

        Map<String, Object> map = new HashMap<>();
        map.put("token", state.getToken());
        map.put("sid", messageData.data.base_info.sid);
        postACK(map, uri_ack, ACTION_OFF);
    }

    private void postACK(Map<String, Object> map, String uri, final int type) {
        JSONObject object = new JSONObject(map);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                MyApplication.host + uri, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.i("msg-ack", jsonObject.toString());
                        userData = gson.fromJson(jsonObject.toString(), UserData.class);
                        UIrefresh(type);
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

    private void UIrefresh(int type) {
        Intent intent = new Intent();
        switch (type) {
            case ACTION_MSG:  intent.setAction("MESSAGE_ACTION");
                              sendBroadcast(intent);
                              Log.i("broad", "send message");
                              break;
            case ACTION_OPT:  intent.setAction("OPTION_ACTION");
                              sendBroadcast(intent);
                              Log.i("broad", "send opt");
                              break;
            case ACTION_NEWS: intent.setAction("NEWS_ACTION");
                              sendBroadcast(intent);
                              Log.i("broad", "send news");
                              break;
            case ACTION_POST: intent.setAction("POST_ACTION");
                              sendBroadcast(intent);
                              Log.i("broad", "send post");
                              break;
            case ACTION_ON:   intent.setAction("ONLINE_ACTION");
                              sendBroadcast(intent);
                              Log.i("broad", "send online");
                              break;
            case ACTION_OFF:  intent.setAction("OFFLINE_ACTION");
                              sendBroadcast(intent);
                              Log.i("broad", "send offline");
                              break;
            default: break;
        }
    }
}
