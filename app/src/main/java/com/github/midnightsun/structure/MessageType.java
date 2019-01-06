package com.github.midnightsun;

/**
 * Created by Jason on 2019/1/5.
 */

public class MessageType extends MoreBeanType {
    private String msg;
    private int img_id;

    public MessageType(String message, int id, int mtype) {
        super(mtype);
        msg = message;
        img_id = id;
    }

    @Override
    public int getResourcesID() {
        return img_id;
    }

    @Override
    public String getResourcesContent(int kind) {
        return msg;
    }

    public void setMsg(String message) {
        msg = message;
    }

    public void setResourcesID(int id) {
        img_id = id;
    }
}
