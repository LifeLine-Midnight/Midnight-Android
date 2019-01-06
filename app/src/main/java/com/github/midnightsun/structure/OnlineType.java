package com.github.midnightsun;

/**
 * Created by Jason on 2019/1/5.
 */

public class OnlineType extends MoreBeanType {
    public static final int TYPE_ONLINE = 3;
    public static final int TYPE_OFFLINE = 4;

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
    public String getResourcesContent(int kind) {
        if (super.getViewType() == TYPE_ONLINE) {
            return (name + " is online");
        }
        else {
            return (name + " is offline");
        }
    }
}
