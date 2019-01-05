package com.github.midnightsun;
/**
 * Created by Jason on 2019/1/5.
 */

public abstract class MoreBeanType {
    private int viewType;

    public MoreBeanType(int type) {viewType = type;}
    public MoreBeanType(){}
    public int getViewType() {
        return viewType;
    }
    public abstract int getResourcesID();
    public abstract String getResourcesContent(int kind);

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}
