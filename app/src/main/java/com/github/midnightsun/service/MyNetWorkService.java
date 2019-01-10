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
import com.github.midnightsun.model.ChatRecordModel;
import com.github.midnightsun.model.MomentRecordModel;
import com.github.midnightsun.model.NewsRecordModel;
import com.github.midnightsun.utilis.SystemState;
import com.github.midnightsun.utilis.TimeTool;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyNetWorkService extends Service {
    private static final String TAG = "MyNetWorkService";
    private static final String uri_act = "/midnightapisvr/api/action/getcurrentaction";
    private static final String uri_ack = "/midnightapisvr/api/action/normalmsgack";

    private static final int ACTION_NULL = 0;
    private static final int ACTION_MSG = 1;
    private static final int ACTION_OPT = 2;
    private static final int ACTION_NEWS = 3;
    private static final int ACTION_POST = 4;
    private static final int ACTION_ON = 5;
    private static final int ACTION_OFF = 6;

    private Handler handler = new Handler();
    private Runnable runnable;
    private SystemState state = new SystemState(MyNetWorkService.this);
    private Gson gson = new Gson();
    private TimeTool timeTool = new TimeTool();
    ChatRecordModel chatRecordModel;
    NewsRecordModel newsRecordModel;
    MomentRecordModel momentRecordModel;
    MessageData messageData;
    NewsData newsData;
    PostData postData;
    OptionData optionData;
    UserData userData;

    public MyNetWorkService() {
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
        chatRecordModel = new ChatRecordModel(
                MyNetWorkService.this, state.getUserName());
        newsRecordModel = new NewsRecordModel(
                MyNetWorkService.this, state.getUserName());
        momentRecordModel = new MomentRecordModel(
                MyNetWorkService.this, state.getUserName());

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
//        Log.i("action.option: ", optionData.data.conjunction_info.l_content
//        + "  " + optionData.data.conjunction_info.r_content);
        UIrefresh(ACTION_OPT);
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
                        if (userData.rtn == 0) {
                            UIrefresh(type);
                        }
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
            case ACTION_MSG:
                intent.setAction("MESSAGE_ACTION");
                intent.putExtra("content", messageData.data.conjunction_info.content);
                chatRecordModel.addChattingItem(ChatRecordModel.MSG_OTHER,
                        messageData.data.conjunction_info.content, false);
                if (checkTimeDelay(intent)) {
                    intent.putExtra("show_time", true);
                } else {
                    intent.putExtra("show_time", false);
                }
                sendBroadcast(intent);
                Log.i("broad", "send message");
                break;
            case ACTION_OPT:
                intent.setAction("OPTION_ACTION");
                intent.putExtra("l_content", optionData.data.conjunction_info.l_content);
                intent.putExtra("r_content", optionData.data.conjunction_info.r_content);
                intent.putExtra("sid", optionData.data.base_info.sid);
                sendBroadcast(intent);
//                Log.i("broad", "send opt");
                break;

            case ACTION_NEWS:
                intent.setAction("NEWS_ACTION");
                intent.putExtra("title", newsData.data.conjunction_info.title);
                intent.putExtra("content", newsData.data.conjunction_info.content);
                newsRecordModel.addNews(newsData.data.conjunction_info.title,
                        newsData.data.conjunction_info.content, false);
                sendBroadcast(intent);
                Log.i("broad", "send news");
                break;

            case ACTION_POST:
                intent.setAction("POST_ACTION");
                momentRecordModel.addPostItem(postData.data.conjunction_info.author,
                        postData.data.conjunction_info.content,
                        postData.data.conjunction_info.img_uri, false);
                intent.putExtra("author", postData.data.conjunction_info.author);
                intent.putExtra("date", timeTool.timeConverterLong(timeTool.getTimeRightNowLong()));
                intent.putExtra("content", postData.data.conjunction_info.content);
                intent.putExtra("img_uri", postData.data.conjunction_info.img_uri);
                sendBroadcast(intent);
                Log.i("broad", "send post");
                break;

            case ACTION_ON:
                intent.setAction("ONLINE_ACTION");
                chatRecordModel.addChattingItem(ChatRecordModel.MSG_ONLINE,
                        "", true);
                if (checkTimeDelay(intent)) {
                    intent.putExtra("show_time", true);
                } else {
                    intent.putExtra("show_time", false);
                }
                sendBroadcast(intent);
                Log.i("broad", "send online");
                break;

            case ACTION_OFF:
                intent.setAction("OFFLINE_ACTION");
                chatRecordModel.addChattingItem(ChatRecordModel.MSG_OFFLINE,
                        "", true);
                sendBroadcast(intent);
                Log.i("broad", "send offline");
                break;
            default: break;
        }
    }

    private boolean checkTimeDelay(Intent intent) {
        ArrayList<ChatRecordModel.ChattingItem>
                items = chatRecordModel.getChattingItems(1);
        if (!items.isEmpty()) {
            ChatRecordModel.ChattingItem item = items.get(0);
            if (timeTool.isLongEnough(item.Ctime)) {
                String time = timeTool.getTimeRightNowLong();
                chatRecordModel.addChattingItem(ChatRecordModel.MSG_TIME,
                        time, true);
                intent.putExtra("time", time);
                return true;
            }
        }
        return false;
    }
}
