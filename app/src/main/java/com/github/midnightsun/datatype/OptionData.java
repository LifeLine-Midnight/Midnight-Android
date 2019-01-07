package com.github.midnightsun.datatype;
import com.google.gson.annotations.SerializedName;

public class OptionData {
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
            @SerializedName("l_content")
            public String l_content;

            @SerializedName("r_content")
            public String r_content;
        }
    }
}
