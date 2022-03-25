package com.jesen.andsockettolk.cs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        // 服务端监听
        ServerSocket serverSocket = new ServerSocket(2000);
        System.out.println("OK 服务端准备就绪！");

        System.out.println("服务端" +
                "【" + serverSocket.getInetAddress() + "|" + serverSocket.getLocalPort() +
                "】等待客户端链接。。。");

        // 等待客户端链接
        for (; ; ) {
            Socket client = serverSocket.accept();
            HandlerClient handlerClient = new HandlerClient(client);
            handlerClient.start();
        }
    }

    // 处理客户端消息
    private static class HandlerClient extends Thread {

        private Socket socket;
        private boolean flag = true;

        HandlerClient(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("有客户端链接了, 他是：" + socket.getInetAddress() + "|" + socket.getPort());
            try {
                // 服务器回送数据
                PrintStream socketOutput = new PrintStream(socket.getOutputStream());
                // 服务端接收数据,输入流
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                do {
                    String fromClient = socketInput.readLine();
                    if ("bye".equalsIgnoreCase(fromClient)) { // 客户端主动退出
                        flag = flag;
                        socketOutput.println("服务端：bye");
                    } else {
                        System.out.println("服务端收到客户端消息：" + fromClient);
                        // 回送给客户端
                        socketOutput.println("服务端回送：你给我的长度是 " + fromClient.length());
                    }
                } while (flag);
                socketInput.close();
                socketOutput.close();

            } catch (Exception e) {
                System.out.println("服务端连接异常！");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("客户端【" + socket.getInetAddress() + "|" + socket.getPort() + "】已关闭");
        }
    }
}
