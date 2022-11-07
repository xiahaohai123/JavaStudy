package socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

public class TCPDetect {

    public static void main(String[] args) {
        String address = "www.baidu.com";
        int port = 80;
        try {
            Socket socket = new Socket(address, port);
            System.out.println("connect success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
