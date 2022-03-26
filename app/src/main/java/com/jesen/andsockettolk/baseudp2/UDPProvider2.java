package com.jesen.andsockettolk.baseudp2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.UUID;

/**
 * UDP提供者 用于提供服务
 */
public class UDPProvider2 {

    public static void main(String[] args) throws IOException {
        // 生成唯一标识
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        // 启动线程
        provider.start();

        // 读取任意键盘信息退出
        System.in.read();
        provider.exit();

    }

    private static class Provider extends Thread {

        private final String sn;
        private boolean done = false;
        private DatagramSocket datagramSocket = null;

        public Provider(String sn) {
            super();

            this.sn = sn;
        }

        @Override
        public void run() {
            super.run();

            System.out.println("UDP Provider started.");
            try {
                // 数据接收者 监听20000端口
                datagramSocket = new DatagramSocket(20000);
                while (!done){

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

                    // 上面收到消息，下面进行回送到指定端口
                    // 解析端口
                    int responsePort = MessageCreator.parsePort(getData);
                    if (responsePort != -1){
                        String responseData = MessageCreator.buildWithSn(sn);
                        byte[] responseBytes = responseData.getBytes();
                        DatagramPacket responsePkt = new DatagramPacket(responseBytes, responseBytes.length,
                                receivePkt.getAddress(), // 此处是要给之前的发送者回复消息
                                responsePort
                        );
                        // 回送回去
                        datagramSocket.send(responsePkt);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                close();
            }
            System.out.println("Provider 结束");
        }

        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        void exit() {
            done = true;
            close();
        }
    }
}
