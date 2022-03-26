package com.jesen.andsockettolk.baseudp2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 局域网搜索案例
 * 先启动这个类 搜索监听多个socket的到来
 * */
public class UDPSearcher2 {

    private static final int LISTEN_PORT = 30000;

    private static Listener listen() throws InterruptedException {
        System.out.println("UDPSearcher listener start.");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT,countDownLatch);
        listener.start();

        // 等待启动完成
        countDownLatch.await();
        return listener;
    }

    private static void sendBoradcast() throws IOException {
        System.out.println("UDP Searcher sendBoradcast started.");
        // 作为搜索方 不需要指定端口 系统会自动分配
        DatagramSocket datagramSocket = new DatagramSocket();

        // 先发送消息
        String requestData = MessageCreator.buildWithPort(LISTEN_PORT);
        byte[] requestBytes = requestData.getBytes();
        DatagramPacket requestPkt = new DatagramPacket(requestBytes, requestBytes.length);
        // 指定20000端口发送广播
        requestPkt.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPkt.setPort(20000);
        // 发送数据回去
        datagramSocket.send(requestPkt);
        datagramSocket.close();
        System.out.println("发送广播已完成");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("开始");
        Listener listen = listen();
        sendBoradcast();

        // 按任意键退出
        System.in.read();
        List<Device> devices = listen.getDevicesAndClose();
        for (Device device : devices) {
            System.out.println("设备 ："+device.toString());
        }
        System.out.println("完成");
    }

    private static class Device{
       final int port;
       final String sn;
        final String ip;


        private Device(int port, String sn, String ip) {
            this.port = port;
            this.sn = sn;
            this.ip = ip;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", sn='" + sn + '\'' +
                    ", ip='" + ip + '\'' +
                    '}';
        }
    }

    // 监听类
    private static class Listener extends Thread{

        private final int listenPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> deviceList = new ArrayList<>();
        private boolean finish = false;
        private DatagramSocket datagramSocket = null;



        public Listener(int listenPort, CountDownLatch countDownLatch){
            super();
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            super.run();

            // 通知已启动
            countDownLatch.countDown();

            try {
                // 监听回送端口
                datagramSocket = new DatagramSocket(listenPort);

                while (!finish){
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

                    // 解析data并添加新设备
                    String sn = MessageCreator.parseSn(getData);
                    if (sn!= null){
                        Device device = new Device(sendPort,sendIp,sn);
                        deviceList.add(device);
                    }
                }
            }catch (Exception e){

            }finally {
                close();
            }
            System.out.println("UDP Search listener finished.");
        }

        private void close(){
            if (datagramSocket !=null){
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        List<Device> getDevicesAndClose(){
            finish = true;
            close();
            return deviceList;
        }

    }


}
