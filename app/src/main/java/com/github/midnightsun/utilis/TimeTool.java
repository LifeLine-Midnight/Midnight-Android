package com.github.midnightsun.utilis;


import android.widget.SimpleAdapter;

import com.github.midnightsun.structure.DateType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeTool {
    // input format required: yy-MM-dd hh-mm-ss
    private static final long time_delay = 5 * 60 * 1000;
    public String timeConverter(String time) {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat ft2 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date time_input = ft.parse(time);
            Date right_now = new Date();
            String today_str = ft2.format(right_now) + " 00:00:00";
            Date today = ft.parse(today_str);
            String cur_time;
            if (time_input.after(today)) {
                cur_time = time.substring(11, time.length() - 3);
            } else {
                cur_time = time.substring(5, 11);
            }
            return cur_time;
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }
    }

    public String timeConverterLong(String time) {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat ft2 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date time_input = ft.parse(time);
            Date right_now = new Date();
            String today_str = ft2.format(right_now) + " 00:00:00";
            Date today = ft.parse(today_str);
            String cur_time;
            if (time_input.after(today)) {
                cur_time = time.substring(11, time.length() - 3);
            } else {
                cur_time = time.substring(5, time.length() - 3);
            }
            return cur_time;
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }
    }
    public boolean isLongEnough(String time) {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date time_input = ft.parse(time);
            Date right_now = new Date();
            if (right_now.getTime() - time_input.getTime() >= time_delay) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getTimeRightNow() {
        SimpleDateFormat ft = new SimpleDateFormat("MM-dd HH:mm:ss");
        Date date = new Date();
        return ft.format(date);
    }

    public String getTimeRightNowLong() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return ft.format(date);
    }
}
