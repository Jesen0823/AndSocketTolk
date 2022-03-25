package com.jesen.andsockettolk.cs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.FileSystems;

public class Client {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(3000);
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), 2000), 3000);
        System.out.println("客户端" +
                "【" + socket.getLocalAddress() + "|" + socket.getLocalPort() +
                "】向 服务端【" + socket.getInetAddress() + "|" + socket.getPort() + "】" +
                "发起链接请求");
        try {
            send(socket);
        } catch (Exception e) {
            System.out.println("客户端异常关闭");
        }

        socket.close();
        System.out.println("客户端退出");
    }

    private static void send(Socket socket) throws IOException {
        /** 客户端发送 ：**/
        InputStream in = System.in;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        // 客户端输出流
        OutputStream outputStream = socket.getOutputStream();
        // 转换为打印流
        PrintStream sps = new PrintStream(outputStream);

        /** 服务端读取 ：**/
        // 得到socket输入流
        InputStream inputStream = socket.getInputStream();
        // 转换为BufferedReader
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        boolean running = true;
        do {
            // 从键盘读取一行并发送
            String str = reader.readLine();
            sps.println(str);

            // 读取来自服务端的一行
            String echo = br.readLine();
            if ("bye".equalsIgnoreCase(echo)) {
                running = false;
            } else {
                System.out.println("客户端收到：" + echo);
            }
        } while (running);

        // 资源释放
        sps.close();
        br.close();
    }
}













