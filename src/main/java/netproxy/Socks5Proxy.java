package netproxy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Socks5Proxy {

    /** 日志记录 */
    private final Log log = LogFactory.getLog(this.getClass());
    /** socks5代理服务默认监听端口 */
    private static final int DEFAULT_PORT = 1080;
    /** 代理服务器地址 */
    private final String proxyHost;
    /** 代理服务监听端口 */
    private final int proxyPort;

    /** 代理服务器用户名 */
    private String username;
    /** 代理服务器密码 */
    private String password;

    /** 连接到代理服务器的套接字 */
    private Socket socket;

    /**
     * 构造器
     * @param proxyHost 代理服务器地址
     * @param proxyPort 代理服务监听端口
     */
    public Socks5Proxy(String proxyHost, int proxyPort) {
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
     * 通过代理连接目标服务器
     * https://www.rfc-editor.org/rfc/rfc1928.html SOCKS Protocol Version 5
     * https://www.rfc-editor.org/rfc/rfc1929.html Username/Password Authentication for SOCKS V5
     * @param host    目标服务器地址
     * @param port    目标服务器端口
     * @param timeout 连接超时时长: ms
     * @throws ProxyException 异常信息
     */
    public void connect(String host, int port, long timeout) throws ProxyException {
        if (StringUtils.isBlank(host) || port <= 0) {
            throw new IllegalArgumentException("illegal param of host and port");
        }
        try {
            socket = createSocket(proxyHost, proxyPort, timeout);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            if (timeout > 0) {
                socket.setSoTimeout((int) timeout);
            }
            socket.setTcpNoDelay(true);

            byte[] buf = new byte[1024];
            int index = 0;

            /*
               The client connects to the server, and sends a version
               identifier/method selection message:

                               +----+----------+----------+
                               |VER | NMETHODS | METHODS  |
                               +----+----------+----------+
                               | 1  |    1     | 1 to 255 |
                               +----+----------+----------+

               The VER field is set to X'05' for this version of the protocol.  The
               NMETHODS field contains the number of method identifier octets that
               appear in the METHODS field.

               The server selects from one of the methods given in METHODS, and
               sends a METHOD selection message:

                                     +----+--------+
                                     |VER | METHOD |
                                     +----+--------+
                                     | 1  |   1    |
                                     +----+--------+

               If the selected METHOD is X'FF', none of the methods listed by the
               client are acceptable, and the client MUST close the connection.

               The values currently defined for METHOD are:

                      o  X'00' NO AUTHENTICATION REQUIRED
                      o  X'01' GSSAPI
                      o  X'02' USERNAME/PASSWORD
                      o  X'03' to X'7F' IANA ASSIGNED
                      o  X'80' to X'FE' RESERVED FOR PRIVATE METHODS
                      o  X'FF' NO ACCEPTABLE METHODS

               The client and server then enter a method-specific sub-negotiation.
             */
            buf[index++] = 5;
            // 本客户端支持两种认证方式: NO AUTHENTICATION REQUIRED 和 USERNAME/PASSWORD
            if (username == null && password == null) {
                // 在没有账号密码的时候需要请求仅支持一种认证方式，防止服务端要求使用账号密码认证
                buf[index++] = 1;
                buf[index++] = 0;
                if (log.isDebugEnabled()) {
                    log.debug("connect to socks server use 'NO AUTHENTICATION REQUIRED', buf: "
                            + Arrays.toString(cloneBuf(buf, index)));
                }
            } else {
                buf[index++] = 2;
                buf[index++] = 0;
                buf[index++] = 2;
                if (log.isDebugEnabled()) {
                    log.debug("connect to socks server use 'NO AUTHENTICATION REQUIRED' and 'USERNAME/PASSWORD', buf: "
                            + Arrays.toString(cloneBuf(buf, index)));
                }
            }
            out.write(buf, 0, index);

            fill(in, buf, 2);
            boolean authed = false;
            switch (buf[1] & 0xff) {
                case 0:                 // NO AUTHENTICATION REQUIRED
                    authed = true;
                    break;
                case 2:                 // USERNAME/PASSWORD
                    if (username == null && password == null) {
                        break;
                    }
                    /*
                       Once the SOCKS V5 server has started, and the client has selected the
                       Username/Password Authentication protocol, the Username/Password
                       subnegotiation begins.  This begins with the client producing a
                       Username/Password request:
                               +----+------+----------+------+----------+
                               |VER | ULEN |  UNAME   | PLEN |  PASSWD  |
                               +----+------+----------+------+----------+
                               | 1  |  1   | 1 to 255 |  1   | 1 to 255 |
                               +----+------+----------+------+----------+
                       The VER field contains the current version of the subnegotiation,
                       which is X'01'. The ULEN field contains the length of the UNAME field
                       that follows. The UNAME field contains the username as known to the
                       source operating system. The PLEN field contains the length of the
                       PASSWD field that follows. The PASSWD field contains the password
                       association with the given UNAME.
                     */
                    index = 0;
                    buf[index++] = 1;
                    int uLength = username.length();
                    buf[index++] = (byte) uLength;
                    System.arraycopy(username.getBytes(StandardCharsets.UTF_8), 0, buf, index, uLength);
                    index += uLength;
                    int pLength = password.length();
                    buf[index++] = (byte) pLength;
                    System.arraycopy(password.getBytes(StandardCharsets.UTF_8), 0, buf, index, pLength);
                    index += pLength;
                    out.write(buf, 0, index);
                    if (log.isDebugEnabled()) {
                        log.debug("auth with 'USERNAME/PASSWORD', buf: " + Arrays.toString(cloneBuf(buf, index)));
                    }
                    /*
                       The server verifies the supplied UNAME and PASSWD, and sends the
                       following response:

                                            +----+--------+
                                            |VER | STATUS |
                                            +----+--------+
                                            | 1  |   1    |
                                            +----+--------+

                       A STATUS field of X'00' indicates success. If the server returns a
                       `failure' (STATUS value other than X'00') status, it MUST close the
                       connection.
                     */
                    fill(in, buf, 2);
                    if (buf[1] == 0) {
                        authed = true;
                    }
                    break;
                default:
                    throw new ProxyException("Unsupported identifier method: " + (buf[1] & 0xff));
            }

            if (!authed) {
                throw new ProxyException("Proxy authentication failed");
            }

            /*
               Once the method-dependent subnegotiation has completed, the client
               sends the request details.  If the negotiated method includes
               encapsulation for purposes of integrity checking and/or
               confidentiality, these requests MUST be encapsulated in the method-
               dependent encapsulation.

               The SOCKS request is formed as follows:

                    +----+-----+-------+------+----------+----------+
                    |VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
                    +----+-----+-------+------+----------+----------+
                    | 1  |  1  | X'00' |  1   | Variable |    2     |
                    +----+-----+-------+------+----------+----------+

                 Where:

                      o  VER    protocol version: X'05'
                      o  CMD
                         o  CONNECT X'01'
                         o  BIND X'02'
                         o  UDP ASSOCIATE X'03'
                      o  RSV    RESERVED
                      o  ATYP   address type of following address
                         o  IP V4 address: X'01'
                         o  DOMAINNAME: X'03'
                         o  IP V6 address: X'04'
                      o  DST.ADDR       desired destination address
                      o  DST.PORT desired destination port in network octet
                         order

               The SOCKS server will typically evaluate the request based on source
               and destination addresses, and return one or more reply messages, as
               appropriate for the request type.
             */
            index = 0;
            buf[index++] = 5;
            buf[index++] = 1;
            buf[index++] = 0;

            InetAddress addr = null;
            boolean isDomainName = false;
            try {
                addr = InetAddress.getByName(host);
            } catch (UnknownHostException e) {
                log.debug("unknown host", e);
                if (host.equals(e.getMessage())) {
                    isDomainName = true;
                } else {
                    throw new ProxyException("invalid host: " + host, e);
                }
            }
            if (!isDomainName) {
                int len = addr.getAddress().length;
                buf[index++] = (byte) ((addr instanceof Inet6Address) ? 4 : 1);
                System.arraycopy(addr.getAddress(), 0, buf, index, len);
                index += len;
            } else {
                byte[] hostB = host.getBytes(StandardCharsets.UTF_8);
                buf[index++] = (byte) 3;
                int len = hostB.length;
                buf[index++] = (byte) len;
                System.arraycopy(hostB, 0, buf, index, len);
                index += len;
            }
            buf[index++] = (byte) (port >>> 8);
            buf[index++] = (byte) (port & 0xff);
            if (log.isDebugEnabled()) {
                log.debug("send connect buf: " + Arrays.toString(cloneBuf(buf, index)));
            }
            out.write(buf, 0, index);

            /*
            6.  Replies
               The SOCKS request information is sent by the client as soon as it has
               established a connection to the SOCKS server, and completed the
               authentication negotiations.  The server evaluates the request, and
               returns a reply formed as follows:
                    +----+-----+-------+------+----------+----------+
                    |VER | REP |  RSV  | ATYP | BND.ADDR | BND.PORT |
                    +----+-----+-------+------+----------+----------+
                    | 1  |  1  | X'00' |  1   | Variable |    2     |
                    +----+-----+-------+------+----------+----------+
                 Where:
                      o  VER    protocol version: X'05'
                      o  REP    Reply field:
                         o  X'00' succeeded
                         o  X'01' general SOCKS server failure
                         o  X'02' connection not allowed by ruleset
                         o  X'03' Network unreachable
                         o  X'04' Host unreachable
                         o  X'05' Connection refused
                         o  X'06' TTL expired
                         o  X'07' Command not supported
                         o  X'08' Address type not supported
                         o  X'09' to X'FF' unassigned
                      o  RSV    RESERVED
                      o  ATYP   address type of following address
                         o  IP V4 address: X'01'
                         o  DOMAINNAME: X'03'
                         o  IP V6 address: X'04'
                      o  BND.ADDR       server bound address
                      o  BND.PORT       server bound port in network octet order
             */
            fill(in, buf, 4);
            if (buf[1] != 0) {
                throw new ProxyException(String.format("Failed to connect to host: %s, The server returns %d", host,
                        buf[1]));
            } else {
                switch (buf[3] & 0xff) {
                    case 1:
                        this.fill(in, buf, 4 + 2);
                        if (log.isDebugEnabled()) {
                            log.debug("receive ipv4 data buf: " + Arrays.toString(cloneBuf(buf, 0, 4)));
                            log.debug("receive port data buf: " + Arrays.toString(cloneBuf(buf, 4, 2)));
                        }
                        break;
                    case 2:
                    default:
                        break;
                    case 3:
                        this.fill(in, buf, 1);
                        int domainLength = buf[0] & 0xff;
                        this.fill(in, buf, domainLength + 2);
                        if (log.isDebugEnabled()) {
                            log.debug("receive domain data buf: " + Arrays.toString(cloneBuf(buf, 0, domainLength)));
                            log.debug("receive port data buf: " + Arrays.toString(cloneBuf(buf, domainLength, 2)));
                        }
                        break;
                    case 4:
                        this.fill(in, buf, 16 + 2);
                        if (log.isDebugEnabled()) {
                            log.debug("receive ipv6 data buf: " + Arrays.toString(cloneBuf(buf, 0, 16)));
                            log.debug("receive port data buf: " + Arrays.toString(cloneBuf(buf, 16, 2)));
                        }
                }
            }
        } catch (Exception e) {
            try {
                close();
            } catch (IOException ioException) {
                log.warn("problem close socket: " + ioException.getMessage(), ioException);
            }
            throw new ProxyException("Problem connect to destination by proxy: " + e.getMessage(), e);
        }
    }

    /**
     * 创建socket
     * @param host    socket对端地址
     * @param port    socket对端端口
     * @param timeout 打开socket的超时时间，使用join方式实现
     * @return 成功打开的socket对象
     * @throws IOException    输入输出异常
     * @throws ProxyException 代理服务异常
     */
    private Socket createSocket(String host, int port, long timeout) throws IOException, ProxyException {
        if (timeout <= 0) {
            return new Socket(host, port);
        }
        final Socket[] sockets = new Socket[1];
        final Exception[] exceptions = new Exception[1];
        Thread createSocketThread = new Thread(() -> {
            sockets[0] = null;
            try {
                sockets[0] = new Socket(host, port);
            } catch (Exception e) {
                exceptions[0] = e;
                if (sockets[0] != null && sockets[0].isConnected()) {
                    try {
                        sockets[0].close();
                    } catch (IOException ioException) {
                        log.warn("problem close socket: " + ioException.getMessage(), ioException);
                    }
                }
                sockets[0] = null;
            }
        });
        createSocketThread.setName("open-socket-" + host);
        createSocketThread.start();

        // wait to open socket
        try {
            createSocketThread.join(timeout);
        } catch (InterruptedException e) {
            log.warn("interrupted when join create socket thread: " + e.getMessage(), e);
        }

        if (sockets[0] != null && sockets[0].isConnected()) {
            return sockets[0];
        } else {
            String message = "socket is not established";
            createSocketThread.interrupt();
            if (exceptions[0] != null) {
                throw new ProxyException(message, exceptions[0]);
            }
            throw new ProxyException(message);
        }
    }

    /**
     * 从输入流内填充指定长度字节进入缓冲区
     * @param in  输入流
     * @param buf 缓冲区
     * @param len 指定长度
     * @throws IOException 输入输出异常
     */
    private void fill(InputStream in, byte[] buf, int len) throws IOException {
        int readNum = 0;
        for (int offset = 0; offset < len; offset += readNum) {
            readNum = in.read(buf, offset, len - offset);
            if (readNum <= 0) {
                throw new IOException("stream is closed");
            }
        }
    }

    /**
     * 克隆一个buf
     * @param src 原始buf
     * @param len 原始buf的有效长度
     * @return 新buf
     */
    private byte[] cloneBuf(byte[] src, int len) {
        return cloneBuf(src, 0, len);
    }

    /**
     * 克隆一个buf
     * @param src 原始buf
     * @param pos 原始buf的克隆起始位置
     * @param len 原始buf的有效长度
     * @return 新buf
     */
    private byte[] cloneBuf(byte[] src, int pos, int len) {
        if (src == null) {
            return null;
        }
        if (src.length < len) {
            len = src.length;
        }
        byte[] dest = new byte[len];
        System.arraycopy(src, pos, dest, 0, len);
        return dest;
    }

    /**
     * 关闭套接字
     * @throws IOException 输入输出异常
     */
    public void close() throws IOException {
        if (socket == null) {
            return;
        }
        socket.close();
    }
}
