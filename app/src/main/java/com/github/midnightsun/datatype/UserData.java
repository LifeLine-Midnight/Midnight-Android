package com.github.midnightsun.datatype;

import com.google.gson.annotations.SerializedName;

public class SignupData {
    @SerializedName("rtn")
    public int rtn;

    @SerializedName("msg")
    public String msg;

    @SerializedName("data")
    public Data data;

    public class Data {
        @SerializedName("username")
        public String usrname;

        @SerializedName("token")
        public String token;
    }
}
