package socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    private static ServerSocket serverSocket;
    private static Socket socket;

    public static void main(String[] args) {
        startServer();
    }

    public static void startServer() {
        if (serverSocket == null) {
            new Thread(() -> {
                try {
                    serverSocket = new ServerSocket(8888);
                    System.out.println("启动服务端，等待客户端连接中");
                    socket = serverSocket.accept();
                    System.out.println("客户端连接上来了");
                    sendTcpMessage("你好，我是服务端");
                    InputStream inputStream = socket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    while ((len = inputStream.read(buffer)) != -1) {
                        String data = new String(buffer, 0, len);
                        System.out.println("收到客户端的数据:" + data);
                    }
//                        socket.getOutputStream().write("你好，服务端已接收到您的信息".getBytes());
//                        socket.getOutputStream().flush();
//                        socket.shutdownOutput();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    socket = null;
                    serverSocket = null;
                }
            }).start();
        }
    }

    public static void sendTcpMessage(final String msg) {
        if (socket != null && socket.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket.getOutputStream().write(msg.getBytes());
                        socket.getOutputStream().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.shutdownOutput();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

}
