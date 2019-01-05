package com.github.midnightsun;

/**
 * Created by Jason on 2019/1/5.
 */

public class DateType extends MoreBeanType {
    private String date, time;
    public DateType(String mdate, String mtime, int type) {
        super(type);
        date = mdate;
        time = mtime;
    }
    public void setDate(String mdate) {
        date = mdate;
    }
    public void setTime(String mtime) {
        time = mtime;
    }
    @Override
    public int getResourcesID() {
        return -1;
    }
    @Override
    public String getResourcesContent(int kind) {
        if (kind == 0) {
            return date;
        }
        else {
            return  time;
        }
    }
}
