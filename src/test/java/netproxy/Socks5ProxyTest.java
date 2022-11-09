package netproxy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class Socks5ProxyTest {

    private Socks5Proxy socks5Proxy;
    private final String desHostIpv4 = "10.12.2.36";
    private final String desHostIpv6 = "fc00:1012::250:56ff:fe9a:af7d";
    private final String desHostDomain = "rdfs.xxxx.com";
    private final int desPort = 443;
    private final int timeout = 10000;
    private final String proxyHostNoAuth = "10.13.0.52";
    private final String proxyHostNeedAuth = "10.13.0.51";
    private final String proxyUser = "test";
    private final String proxyPwd = "12345678";
    private final int proxyPort = 1080;


    @BeforeEach
    void setUp() {
        socks5Proxy = new Socks5Proxy(proxyHostNeedAuth, proxyPort);
    }

    @Test
    @DisplayName("need auth proxy, connect to ipv4")
    void connectNeedAuthIpv4Server() {
        socks5Proxy.setUserPasswd(proxyUser, proxyPwd);
        Assertions.assertDoesNotThrow(() -> {
            connectAndClose(socks5Proxy, desHostIpv4, desPort, timeout);
        });
    }

    @Test
    @DisplayName("need auth proxy, connect to ipv6")
    void connectNeedAuthIpv6Server() {
        socks5Proxy.setUserPasswd(proxyUser, proxyPwd);
        Assertions.assertDoesNotThrow(() -> {
            connectAndClose(socks5Proxy, desHostIpv6, desPort, timeout);
        });
    }

    @Test
    @DisplayName("need auth proxy, connect to domain")
    void connectNeedAuthDomainServer() {
        socks5Proxy.setUserPasswd(proxyUser, proxyPwd);
        Assertions.assertDoesNotThrow(() -> {
            connectAndClose(socks5Proxy, desHostDomain, desPort, timeout);
        });
    }

    @Test
    @DisplayName("no auth proxy, connect to ipv4")
    void connectWithoutAuthIpv4Server() {
        Socks5Proxy socks5Proxy = createNoAuthProxy();
        Assertions.assertDoesNotThrow(() -> {
            connectAndClose(socks5Proxy, desHostIpv4, desPort, timeout);
        });
    }

    @Test
    @DisplayName("no auth proxy, connect to ipv6")
    void connectWithoutAuthIpv6Server() {
        Socks5Proxy socks5Proxy = createNoAuthProxy();
        Assertions.assertDoesNotThrow(() -> {
            connectAndClose(socks5Proxy, desHostIpv6, desPort, timeout);
        });
    }

    @Test
    @DisplayName("no auth proxy, connect to domain")
    void connectWithoutAuthDomainServer() {
        Socks5Proxy socks5Proxy = createNoAuthProxy();
        Assertions.assertDoesNotThrow(() -> {
            connectAndClose(socks5Proxy, desHostDomain, desPort, timeout);
        });
    }

    private Socks5Proxy createNoAuthProxy() {
        return new Socks5Proxy(proxyHostNoAuth, proxyPort);
    }

    private void connectAndClose(Socks5Proxy socks5Proxy, String host, int port, int timeout)
            throws ProxyException, IOException {
        socks5Proxy.connect(host, port, timeout);
        socks5Proxy.close();
    }
}