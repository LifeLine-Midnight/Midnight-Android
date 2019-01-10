package com.github.midnightsun.structure;

import com.github.midnightsun.structure.MoreBeanType;

public class OnlineType extends MoreBeanType {
    public static final int TYPE_ONLINE = 2;

    private String name;

    public OnlineType(String mname, int type) {
        super(type);
        name = mname;
    }

    public void setName(String mname) {
        name = mname;
    }

    @Override
    public int getResourcesID() {
        return -1;
    }

    @Override
    public String getResourcesContent() {
        if (super.getViewType() == TYPE_ONLINE) {
            return (name + " is online");
        }
        else {
            return (name + " is offline");
        }
    }
}
