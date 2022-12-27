package obsclient.apache;

import obsclient.HuaweiSignature;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.springframework.web.util.UriUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public abstract class AbstractHWObsClient {
    /** 默认超时时间，单位: ms */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 2000;
    /** 日历 */
    private final Calendar calendar = Calendar.getInstance();
    /** 对象存储服务器数据 */
    private final ObsServer obsServer;
    /** 日期格式化器 */
    private SimpleDateFormat sdf;
    /** http请求客户端 */
    private HttpClient httpClient;

    /**
     * 上传文件
     * @param file       文件对象
     * @param objectName 对象名，对应路径名
     */
    public abstract void upload(File file, String objectName) throws IOException;

    /**
     * 上传文件
     * @param path       文件路径
     * @param objectName 对象名，对应路径名
     */
    public void upload(String path, String objectName) throws IOException {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }
        upload(new File(path), objectName);
    }

    public void upload(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException();
        }
        upload(file, file.getName());
    }

    /**
     * 判断桶地址是否存在
     * @return 如果桶地址存在则返回true 否则返回false
     * @throws IOException 通信相关异常
     */
    public boolean isBucketExist() throws IOException {
        HttpRequestBase request = new HttpHead(generateUrl());
        appendDateAndSignRequest(request, generateCanonicalResource());
        HttpResponse response = getHttpClient().execute(request);

        System.out.println("request message");
        System.out.println(request);
        System.out.println(Arrays.toString(request.getAllHeaders()));

        System.out.println("response message");
        System.out.println(response);
        System.out.println(Arrays.toString(response.getAllHeaders()));


        int statusCode = response.getStatusLine().getStatusCode();
        switch (statusCode) {
            case 404:
                return false;
            case 403:
                if ("SignatureDoesNotMatch".equals(response.getLastHeader("x-obs-error-code").getValue())) {
                    System.out.println("my auth failed");
                }
                throw new IllegalArgumentException("auth failed");
            case 200:
                return true;
            default:
                throw new IllegalStateException("Unexpected value: " + statusCode);
        }
    }

    /**
     * 构造器
     * @param obsServer 对象存储服务器数据
     */
    protected AbstractHWObsClient(ObsServer obsServer) {
        this.obsServer = obsServer;
        initHttpClient();
        initSdf();
    }

    /**
     * 获取华为API要求格式的当前时间
     * @return 时间字符串
     */
    protected String getDate() {
        return sdf.format(calendar.getTime());
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * 添加时间信息
     * @param request 请求
     * @param date    时间信息
     */
    protected void appendDate(HttpRequestBase request, String date) {
        request.addHeader("Date", date);
    }

    /**
     * 添加日期参数并且对请求签名
     * @param request           请求
     * @param canonicalResource 访问资源字符串
     */
    protected void appendDateAndSignRequest(HttpRequestBase request, String canonicalResource) {
        String requestTime = getDate();
        appendDate(request, requestTime);

        String canonicalString = generateCanonicalString(request.getMethod(),
                "", "", requestTime, "",
                canonicalResource);

        signRequest(request, canonicalString);
    }

    /**
     * 为请求签名
     * @param request         请求
     * @param canonicalString 待签名字符串
     */
    protected void signRequest(HttpRequestBase request, String canonicalString) {
        String signature = HuaweiSignature.signWithHmacSha1(getSecurityKey(), canonicalString);
        request.addHeader("Authorization", "OBS " + getAccessKey() + ":" + signature);
    }

    /**
     * 生成待签名字符串
     * @param method            请求类型: GET/POST/PUT等等
     * @param contentMD5        MD5校验值
     * @param contentType       请求体类型
     * @param requestTime       请求时间
     * @param canonicalHeaders  标准化请求头
     * @param canonicalResource 请求资源
     * @return 待签名字符串
     */
    protected String generateCanonicalString(String method,
                                             String contentMD5,
                                             String contentType,
                                             String requestTime,
                                             String canonicalHeaders,
                                             String canonicalResource) {
        return method + "\n"
                + contentMD5 + "\n"
                + contentType + "\n"
                + requestTime + "\n"
                + canonicalHeaders + canonicalResource;
    }

    /**
     * 构建访问url
     * @return url String
     */
    protected String generateUrl() {
        return getProtocol()
                + "://"
                + getBucket()
                + "."
                + getEndpoint()
                + ":"
                + getPort();
    }

    /**
     * 构建访问url
     * @param object 对象
     * @return url String
     */
    protected String generateUrl(String object) {
        return generateUrl() + "/" + object;
    }

    /**
     * 生成访问资源字符串
     * @return 访问资源字符串
     */
    protected String generateCanonicalResource() {
        return "/".concat(getBucket()).concat("/");
    }

    /**
     * 生成访问资源字符串
     * @param object 访问资源
     * @return 访问资源字符串
     */
    protected String generateCanonicalResource(String object) {
        if (object == null || "".equals(object)) {
            return generateCanonicalResource();
        } else {
            return generateCanonicalResource().concat(object);
        }
    }

    /**
     * 添加url参数
     * @param url   url
     * @param param 参数
     * @return 添加了url参数的字符串
     */
    protected String appendUrlParam(String url, String param) {
        if (url.contains("?")) {
            return url.concat("&").concat(param);
        } else {
            return url.concat("?").concat(param);
        }
    }

    /**
     * 添加url参数
     * @param url        url
     * @param paramKey   参数key
     * @param paramValue 参数值
     * @return 添加了url参数的字符串
     */
    protected String appendUrlParam(String url, String paramKey, Object paramValue) {
        return appendUrlParam(url, paramKey).concat("=") + paramValue;
    }

    /**
     * 读取响应体消息
     * @param response 响应
     * @return 响应体
     * @throws IOException IO异常
     */
    protected String readResponse(HttpResponse response) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try (BufferedInputStream bis = new BufferedInputStream(entity.getContent())) {
                int inputChar;
                while ((inputChar = bis.read()) != -1) {
                    stringBuilder.append((char) inputChar);
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * xml数据转对象
     * 无视了命名空间
     * @param xml   xml数据
     * @param clazz 对象类型
     * @return 对应类型的对象
     * @throws Exception 异常
     */
    protected Object xml2Object(String xml, Class<?> clazz) throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(false);
        XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();
        //xml = xml.replace("xmlns=\"http://obs.myhwclouds.com/doc/2015-06-30/\"", "");
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        try (StringReader characterStream = new StringReader(xml)) {
            SAXSource source = new SAXSource(xmlReader, new InputSource(characterStream));
            return unmarshaller.unmarshal(source);
        }
    }

    /**
     * 对象转换成xml数据
     * @param object 对象
     * @param clazz  对象类型
     * @return xml数据
     */
    protected String object2Xml(Object object, Class<?> clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Marshaller marshaller = context.createMarshaller();
            //marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            try (StringWriter writer = new StringWriter()) {
                marshaller.marshal(object, writer);
                return writer.toString();
            }
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 要求合法对象key
     * 华为标准key可以使用任何utf-8字符，但长度在1-1024之间
     * 该方法会消除object key的前置文件分隔符
     * 由于对象key会用来构建url，所以会将空格转换成%20
     * @param objectKey 对象key
     * @throws IllegalArgumentException 对象key不符合要求
     */
    protected String requireValidObjectKeyAndEncodeUrl(String objectKey) {
        Objects.requireNonNull(objectKey);
        int length = objectKey.length();

        int index = 0;
        for (; index < length; index++) {
            if (objectKey.charAt(index) != '/') {
                break;
            }
        }
        objectKey = objectKey.substring(index);
        if (length == 0 || length > 1024) {
            throw new IllegalArgumentException("the length of key in obs must between 1 and 1024");
        }
        UriUtils.encode(objectKey, "UTF-8");
        objectKey = encodeUrl(objectKey, "UTF-8");
        //objectKey.replace("%2F", "/");
        return objectKey;
    }

    /**
     * 检查对象key是否合法
     * 不再剔除前置路径分隔符
     * @param objectKey 对象Key
     */
    protected void requireValidObjectKey(String objectKey) {
        Objects.requireNonNull(objectKey);
        int length = objectKey.length();
        if (length == 0 || length > 1024) {
            throw new IllegalArgumentException("the length of key in obs must between 1 and 1024");
        }
    }

    /**
     * 将对象key 以url编码
     * @param objectKey 对象key
     * @return 编码后的key
     */
    protected String urlEncodeObjectKey(String objectKey) {
        return UriUtils.encode(objectKey, "UTF-8");
    }

    /**
     * 要求合法的数据量大小
     * @param size 大小
     */
    protected void requireValidSize(long size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 将数据转换成base64编码
     * @param src 源数据
     * @return base64编码后的数据，字符串格式
     */
    protected String toBase64(byte[] src) {
        Base64.Encoder encoder = Base64.getEncoder();
        return new String(encoder.encode(src)).replaceAll("\\s", "");
    }

    /**
     * 计算md5哈希，直接计算完流剩余数据
     * @param is 数据源输入流
     * @return md5哈希值
     * @throws IOException              I/O异常
     * @throws NoSuchAlgorithmException 找不到算法异常
     */
    protected byte[] computeMd5Hash(InputStream is) throws IOException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            int bufferSize = 16384;
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = bis.read(buffer, 0, buffer.length)) != -1) {
                messageDigest.update(buffer, 0, bytesRead);
            }
            return messageDigest.digest();
        }
    }

    /**
     * 计算md5哈希，只计算划定范围
     * @param is     数据源输入流
     * @param offset 偏移量
     * @param length 要计算的数据量长度
     * @return md5哈希数组
     * @throws IOException              I/O异常
     * @throws NoSuchAlgorithmException 无摘要计算器异常
     */
    protected byte[] computeMd5Hash(InputStream is, long offset, long length)
            throws IOException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            if (offset > 0) {
                long skipByte = bis.skip(offset);
            }
            int bufferSize = 16384;
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            long readLen = 0;
            long bufLen = Math.min(bufferSize, length);
            while (readLen < length && (bytesRead = bis.read(buffer, 0, (int) bufLen)) != -1) {
                messageDigest.update(buffer, 0, bytesRead);
                readLen += bytesRead;
                bufLen = Math.min((length - readLen), length);
            }
            return messageDigest.digest();
        }
    }

    /** 初始化http客户端 */
    private void initHttpClient() {
        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT)
                        .setSocketTimeout(DEFAULT_CONNECTION_TIMEOUT)
                        .setConnectionRequestTimeout(DEFAULT_CONNECTION_TIMEOUT)
                        .build())
                .setConnectionTimeToLive(24, TimeUnit.HOURS)
                .build();
    }

    /** 初始化华为API格式的时间格式化器 */
    private void initSdf() {
        sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * 打印请求和响应头
     * @param request  请求
     * @param response 响应
     */
    protected void outputHeader(HttpRequestBase request, HttpResponse response) {
        System.out.println("request message");
        System.out.println(request);
        System.out.println(Arrays.toString(request.getAllHeaders()));

        System.out.println("response message");
        System.out.println(response);
        System.out.println(Arrays.toString(response.getAllHeaders()));
    }

    public ObsServer getObsServer() {
        return obsServer;
    }

    protected String getAccessKey() {
        return obsServer.getAccessKey();
    }

    protected String getSecurityKey() {
        return obsServer.getSecurityKey();
    }

    protected String getEndpoint() {
        return obsServer.getEndpoint();
    }

    protected String getBucket() {
        return obsServer.getBucket();
    }

    protected String getProtocol() {
        return obsServer.getProtocol();
    }

    protected int getPort() {
        return obsServer.getPort();
    }

    /**
     * 进行url编码
     * @param src      待编码字符串
     * @param encoding 编码
     * @return 编码后字符串
     */
    private String encodeUrl(String src, String encoding) {
        StringBuilder result = new StringBuilder();
        try {
            for (int i = 0; i < src.length(); i++) {
                char ch = src.charAt(i);
                if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '_'
                        || ch == '-' || ch == '~' || ch == '.') {
                    result.append(ch);
                } else if (ch == '/') {
                    result.append("%2F");
                } else {
                    result.append(URLEncoder.encode(Character.toString(ch), encoding));
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return result.toString();
    }
}
