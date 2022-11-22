package rabbitmq;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class SSLContextUtil {

    private static final String KEY_CLIENT = "/etc/xxx/client.key";
    private static final String CERT_CLIENT = "/etc/xxx/client.crt";
    private static final String CERT_CA = "/etc/xxx/ca.crt";

    /** 禁止构造 */
    private SSLContextUtil() {
    }

    public static SSLContext getSSLContext() throws Exception {
        KeyPair clientKeyPair;
        try (PEMParser parser = new PEMParser(new FileReader(KEY_CLIENT))) {
            clientKeyPair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) parser.readObject());
        }
        X509Certificate clientCert;
        try (PEMParser parser = new PEMParser(new FileReader(CERT_CLIENT))) {
            clientCert = new JcaX509CertificateConverter().getCertificate((X509CertificateHolder) parser.readObject());
        }
        X509Certificate caCert;
        try (PEMParser parser = new PEMParser(new FileReader(CERT_CA))) {
            caCert = new JcaX509CertificateConverter().getCertificate((X509CertificateHolder) parser.readObject());
        }
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null);
        ks.setCertificateEntry("ca", caCert);
        ks.setKeyEntry("client", clientKeyPair.getPrivate(), "changeit".toCharArray(), new Certificate[]{clientCert});

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(ks, "changeit".toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }
}
