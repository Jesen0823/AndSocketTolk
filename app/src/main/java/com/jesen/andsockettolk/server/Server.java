package com.jesen.andsockettolk.server;

import com.jesen.andsockettolk.constants.TCPConstants;

import java.io.IOException;

public class Server {

    public static void main(String[] args) {

        // TCP服务端先开启
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }

        // 然后监听客户端消息，监听后回送ip信息给客户端，客户端会发起TCP连接
        UDPServerProvider.start(TCPConstants.PORT_SERVER);

        try {
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UDPServerProvider.stop();
        tcpServer.stop();
    }
}
