package com.jesen.andsockettolk.baseudp2;

public class MessageCreator {

    private static final String SN_HEADER = "收到了，我是(SN):";
    private static final String POST_HEADER = "好，请回复端口号Port:";

    public static String buildWithPort(int port){
        return POST_HEADER + port;
    }

    public static int parsePort(String data){
        if (data.startsWith(POST_HEADER)){
            return Integer.parseInt(data.substring(POST_HEADER.length()));
        }
        return -1;
    }

    public static String buildWithSn(String sn){
        return SN_HEADER+sn;
    }

    public static String parseSn(String data){
        if (data.startsWith(SN_HEADER)){
            return data.substring(SN_HEADER.length());
        }
        return  null;
    }

}
