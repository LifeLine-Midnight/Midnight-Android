package com.github.midnightsun.structure;

public class DateType extends MoreBeanType {
    private String time;
    public DateType(String mtime, int type) {
        super(type);
        time = mtime;
    }
    public void setTime(String mtime) {
        time = mtime;
    }
    @Override
    public int getResourcesID() {
        return -1;
    }
    @Override
    public String getResourcesContent() {
       return time;
    }
}
