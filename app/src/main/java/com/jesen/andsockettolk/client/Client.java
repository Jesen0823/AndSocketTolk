package com.jesen.andsockettolk.client;

import com.jesen.andsockettolk.client.bean.ServerInfo;

import java.io.IOException;

public class Client {
    public static void main(String[] args) {
        ServerInfo info = ClientSearcher.searchServer(10000);
        System.out.println("Server:" + info);

        // 上面两行拿到服务端IP信息，下面开始先服务端开启TCP点对点连接：
        if (info != null) {
            try {
                TCPClient.linkWith(info);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
