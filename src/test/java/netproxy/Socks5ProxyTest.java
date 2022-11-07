package netproxy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Socks5ProxyTest {

    private Socks5Proxy socks5Proxy;
    private final String desHostIpv4 = "10.12.2.36";
    private final int desPort = 22;
    private final int timeout = 10000;

    @BeforeEach
    void setUp() {
        String proxyHost = "10.13.0.51";
        int proxyPort = 1080;
        socks5Proxy = new Socks5Proxy(proxyHost, proxyPort);
    }

    @Test
    void connect() {
        String proxyUser = "test";
        String proxyPwd = "12345678";
        socks5Proxy.setUserPasswd(proxyUser, proxyPwd);

        Assertions.assertDoesNotThrow(() -> {
            socks5Proxy.connect(desHostIpv4, desPort, timeout);
            socks5Proxy.close();
        });
    }
}