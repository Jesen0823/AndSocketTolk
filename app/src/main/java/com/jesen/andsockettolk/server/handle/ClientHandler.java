package com.jesen.andsockettolk.server.handle;

import com.jesen.andsockettolk.clink.net.jesen.clink.utils.CloseUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private final Socket socket;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final CloseNotify closeNotify;

    public ClientHandler(Socket socket, CloseNotify closeNotify) throws IOException {
        this.socket = socket;
        this.readHandler = new ClientReadHandler(socket.getInputStream());
        this.writeHandler = new ClientWriteHandler(socket.getOutputStream());
        this.closeNotify = closeNotify;

        System.out.println("新客户端连接：" + socket.getInetAddress() +
                " P:" + socket.getPort());
    }


    public void readToPrint() {
        readHandler.start();
    }

    public void exit() {
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socket);
        System.out.println("客户端【" + socket.getInetAddress() + "|" + socket.getPort() + "】已退出");
    }

    private void exitBySelf() {
        exit();
        closeNotify.onSelfClosed(this);
    }

    public void send(String input) {
        writeHandler.send(input);
    }

    // 用来处理客户端
    class ClientReadHandler extends Thread {

        private boolean done = false;
        private final InputStream inputStream;

        ClientReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }


        @Override
        public void run() {
            super.run();

            try {
                // 得到输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do {
                    // 客户端拿到一条数据
                    String str = socketInput.readLine();
                    if (str == null) {
                        System.out.println("客户端无法读取数据");
                        // 退出当前客户端
                        ClientHandler.this.exitBySelf();
                        break;
                    }
                    // 打印到屏幕
                    System.out.println(str);

                } while (!done);

            } catch (Exception e) {
                if (!done) {
                    System.out.println("连接异常断开");
                    ClientHandler.this.exitBySelf();
                }
            } finally {
                CloseUtils.close(inputStream);
            }
        }

        void exit() {
            done = true;
            CloseUtils.close(inputStream);
        }
    }

    // 发送消息处理
    class ClientWriteHandler {

        private boolean done = false;
        private final PrintStream printStream;
        private final ExecutorService executorService;

        ClientWriteHandler(OutputStream outputStream) {
            this.printStream = new PrintStream(outputStream);
            this.executorService = Executors.newSingleThreadExecutor();
        }

        public void exit() {
            done = true;
            CloseUtils.close(printStream);
            executorService.shutdownNow();
        }

        public void send(String input) {
            executorService.execute(new WriteTask(input));
        }


        class WriteTask implements Runnable {

            private final String msg;

            WriteTask(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                if (ClientWriteHandler.this.done) {
                    return;
                }
                try {
                    ClientWriteHandler.this.printStream.println(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface CloseNotify {
        void onSelfClosed(ClientHandler handler);
    }
}
