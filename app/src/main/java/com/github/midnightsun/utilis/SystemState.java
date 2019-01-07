package com.github.midnightsun.utilis;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Jason on 2019/1/7.
 */

public class SystemState {
    private static final String name = "system_data";
    private SharedPreferences sharedPreferences;
    private Context context;

    public SystemState(Context mcontext) {
        context = mcontext;
    }
    public String getName() {
       return name;
    }

    public void setToken(String token) {
        sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public String getToken() {
        sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }

    public void clear() {
        sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
