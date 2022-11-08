package netproxy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class Socks5Detector {
    /** 日志记录 */
    private final Log log = LogFactory.getLog(this.getClass());
    /** 代理服务器地址 */
    private final String proxyHost;
    /** 代理服务监听端口 */
    private final int proxyPort;

    /** 代理服务器用户名 */
    private String username;
    /** 代理服务器密码 */
    private String password;

    public static void main(String[] args) {
        Socks5Detector socks5Detector = new Socks5Detector("10.13.0.51", 1080);
        socks5Detector.setUserPasswd("test", "12345678");
        socks5Detector.detect("10.12.2.36", 22, 10000);
    }

    public Socks5Detector(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    /**
     * 加入代理服务器的用户名与密码
     * 这会让代理使用用户名与密码认证
     * @param username 用户名
     * @param password 密码
     */
    public void setUserPasswd(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * 探测目标服务器的IP与端口是否能连接上
     * @param host    目标服务器IP
     * @param port    目标服务器端口
     * @param timeout 探测超时时间
     */
    public void detect(String host, int port, int timeout) {
        if (StringUtils.isBlank(host) || port <= 0) {
            throw new IllegalArgumentException("illegal param of host and port");
        }
        Socks5Proxy socks5Proxy = new Socks5Proxy(proxyHost, proxyPort);
        socks5Proxy.setUserPasswd(username, password);
        try {
            socks5Proxy.connect(host, port, timeout);
        } catch (ProxyException e) {
            throw new RuntimeException("problem detect server: " + e.getMessage(), e);
        } finally {
            try {
                socks5Proxy.close();
            } catch (IOException e) {
                log.warn("problem close proxy: " + e.getMessage(), e);
            }
        }
    }
}
