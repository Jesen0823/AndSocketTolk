package com.jesen.andsockettolk.baseudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;

/**
 * 局域网搜索案例
 * */
public class UDPSearcher {

    public static void main(String[] args) throws IOException {
        System.out.println("UDP Searcher started.");

        // 作为搜索方 不需要指定端口 系统会自动分配
        DatagramSocket datagramSocket = new DatagramSocket();

        // 先发送消息
        String requestData = "Heloo..";
        byte[] requestBytes = requestData.getBytes();
        DatagramPacket requestPkt = new DatagramPacket(requestBytes, requestBytes.length);
        requestPkt.setAddress(Inet4Address.getLocalHost());
        requestPkt.setPort(20000);
        // 发送数据回去
        datagramSocket.send(requestPkt);



        // 构建接收实体
        final byte[] buf = new byte[512];
        DatagramPacket receivePkt = new DatagramPacket(buf, buf.length);
        // 接收
        datagramSocket.receive(receivePkt);
        // 打印来自发送者的信息
        String sendIp = receivePkt.getAddress().getHostAddress();
        int sendPort = receivePkt.getPort();
        int getDataLen = receivePkt.getLength();
        String getData = new String(receivePkt.getData(), 0, getDataLen);
        System.out.println("Searcher 接收到 来自【" + sendIp + "|" + sendPort + "】的回复："
                + getData);


        System.out.println("Searcher 结束");
        datagramSocket.close();
    }
}
