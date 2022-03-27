package com.jesen.sample_server;

import com.jesen.sample_server.handle.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements ClientHandler.ClientHandlerCallback {
    private final int port;
    private ClientListener mListener;
    // 需要考虑线程安全问题
    private List<ClientHandler> clientHandlerList = Collections.synchronizedList(new ArrayList<>());
    // 线程池用来转发收到的消息
    private final ExecutorService forwardThreadPool;

    public TCPServer(int port) {
        this.port = port;
        this.forwardThreadPool = Executors.newSingleThreadExecutor();
    }

    public boolean start() {
        try {
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (mListener != null) {
            mListener.exit();
        }

        synchronized (TCPServer.this) {
            for (ClientHandler clientHandler : clientHandlerList) {
                clientHandler.exit();
            }
        }

        clientHandlerList.clear();
        // 停止转发线程池
        forwardThreadPool.shutdownNow();
    }

    public synchronized void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    @Override
    public synchronized void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    @Override
    public void onNewMessageArrived(ClientHandler handler, String msg) {
        // 消息输出到屏幕
        System.out.println("收到：" + handler.getClientInfo() + ":" + msg);
        forwardThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (TCPServer.this) {
                    for (ClientHandler clientHandler : clientHandlerList) {
                        // 如果是发消息的客户端，就不用给它转发了，只转发到其他客户端
                        if (clientHandler.equals(handler)){
                            continue;
                        }
                        // 给其他客户端转发消息
                        clientHandler.send(msg);
                    }
                }
            }
        });
    }

    private class ClientListener extends Thread {
        private ServerSocket server;
        private boolean done = false;

        private ClientListener(int port) throws IOException {
            server = new ServerSocket(port);
            System.out.println("服务器信息：" + server.getInetAddress() + " P:" + server.getLocalPort());
        }

        @Override
        public void run() {
            super.run();

            System.out.println("服务器准备就绪～");
            // 等待客户端连接
            do {
                // 得到客户端
                Socket client;
                try {
                    client = server.accept();
                } catch (IOException e) {
                    continue;
                }
                try {
                    // 客户端构建异步线程
                    ClientHandler clientHandler = new ClientHandler(client,
                            TCPServer.this);
                    // 读取数据并打印
                    clientHandler.readToPrint();
                    synchronized (TCPServer.this) {
                        clientHandlerList.add(clientHandler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端连接异常：" + e.getMessage());
                }
            } while (!done);

            System.out.println("服务器已关闭！");
        }

        void exit() {
            done = true;
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
