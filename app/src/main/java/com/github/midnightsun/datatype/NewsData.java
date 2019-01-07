package com.github.midnightsun.datatype;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jason on 2019/1/7.
 */

public class NewsData {
    @SerializedName("rtn")
    public int rtn;

    @SerializedName("msg")
    public String msg;

    @SerializedName("data")
    public Data data;

    public class Data {
        @SerializedName("base_info")
        public Base_info base_info;

        @SerializedName("conjunction_info")
        public Conjunction_info conjunction_info;

        public class Base_info {
            @SerializedName("sid")
            public int sid;

            @SerializedName("conjunction_msg_type")
            public int conjunction_msg_type;
        }

        public class Conjunction_info {
            @SerializedName("title")
            public String title;

            @SerializedName("content")
            public String content;
        }
    }
}
