package httpclient;

import com.rabbitmq.client.TrustEverythingTrustManager;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class HttpClient4Demo {
    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException {
        createHttpClient(10000, true);
    }

    private static HttpClient createHttpClient(int timeout, boolean isHttp) throws KeyManagementException,
            NoSuchAlgorithmException {
        if (isHttp) {
            return HttpClients.createDefault();
        }
        SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, new TrustManager[]{new TrustEverythingTrustManager()}, null);
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sc,
                NoopHostnameVerifier.INSTANCE);
        return HttpClients.custom()
                .disableCookieManagement()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .setMaxConnPerRoute(1000)
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectionRequestTimeout(timeout)
                                .setConnectTimeout(timeout)
                                .setSocketTimeout(timeout).build())
                .build();
    }
}
