package com.jesen.sample_server;

import com.jesen.lib_clink.utils.CloseUtils;
import com.jesen.sample_server.handle.ClientHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements ClientHandler.ClientHandlerCallback {
    private final int port;
    private ClientListener listener;
    // 需要考虑线程安全问题
    private List<ClientHandler> clientHandlerList = Collections.synchronizedList(new ArrayList<>());
    // 线程池用来转发收到的消息
    private final ExecutorService forwardThreadPool;
    // NIO选择器
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public TCPServer(int port) {
        this.port = port;
        this.forwardThreadPool = Executors.newSingleThreadExecutor();
    }

    public boolean start() {
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            // 配置为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            // 绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            this.serverSocketChannel = serverSocketChannel;
            // 注册server监听客户端链接到达
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("服务器信息：" + serverSocketChannel.getLocalAddress());

            // 启动监听
            ClientListener listener = this.listener = new ClientListener();
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (listener != null) {
            listener.exit();
        }

        CloseUtils.close(serverSocketChannel);
        CloseUtils.close(selector);

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
                        if (clientHandler.equals(handler)) {
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
        private boolean done = false;

        @Override
        public void run() {
            super.run();

            Selector selector = TCPServer.this.selector;

            System.out.println("服务器准备就绪～");
            // 等待客户端连接
            do {
                // 得到客户端
                Socket client;
                try {
                    // 阻塞等待中
                    if (selector.select() == 0) {
                        if (done) {
                            break;
                        }
                        continue;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        if (done) {
                            break;
                        }
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            // 非阻塞状态拿到客户端连接
                            SocketChannel clientChannel = serverSocketChannel.accept();

                            try {
                                // 客户端构建异步线程
                                ClientHandler clientHandler = new ClientHandler(clientChannel,
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
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (!done);
            System.out.println("服务器已关闭！");
        }

        void exit() {
            done = true;
            // 取消阻塞状态
            selector.wakeup();
        }
    }
}
