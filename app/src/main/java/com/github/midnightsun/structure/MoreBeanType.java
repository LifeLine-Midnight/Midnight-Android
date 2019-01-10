package com.github.midnightsun.structure;

public abstract class MoreBeanType {
    private int viewType;

    public MoreBeanType(int type) {viewType = type;}
    public MoreBeanType(){}
    public int getViewType() {
        return viewType;
    }
    public abstract int getResourcesID();
    public abstract String getResourcesContent();

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}
