package com.jesen.andsockettolk.server;

import com.jesen.andsockettolk.constants.TCPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {

    public static void main(String[] args) throws IOException {

        // TCP服务端先开启
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }

        // 然后监听客户端消息，监听后回送ip信息给客户端，客户端会发起TCP连接
        UDPServerProvider.start(TCPConstants.PORT_SERVER);

        // 读取键盘的输入
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String input;
        do {
            input = bufferedReader.readLine();
            // 发给所有的客户端
            tcpServer.broadcast(input);
        } while (!"00bye00".equalsIgnoreCase(input));

        UDPServerProvider.stop();
        tcpServer.stop();
    }
}
