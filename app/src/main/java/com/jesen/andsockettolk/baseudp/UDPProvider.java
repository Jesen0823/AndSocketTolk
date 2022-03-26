package com.jesen.andsockettolk.baseudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * UDP提供者 用于提供服务
 */
public class UDPProvider {

    public static void main(String[] args) throws IOException {
        System.out.println("UDP Provider started.");

        // 数据接收者 指定一个端口用于接收数据
        DatagramSocket datagramSocket = new DatagramSocket(20000);
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
        System.out.println("Provider 接收到 来自【" + sendIp + "|" + sendPort + "】的消息："
                + getData);

        // 上面收到消息，下面进行回送
        String responseData = "接收数据总长" + getDataLen;
        byte[] responseBytes = responseData.getBytes();
        DatagramPacket responsePkt = new DatagramPacket(responseBytes, responseBytes.length,
                receivePkt.getAddress(), // 此处是要给之前的发送者回复消息
                receivePkt.getPort()
        );
        // 回送回去
        datagramSocket.send(responsePkt);
        System.out.println("Provider 结束");
        datagramSocket.close();
    }
}
