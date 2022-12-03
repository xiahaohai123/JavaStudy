package obsclient;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class HuaweiOBSClient {
    /** 默认超时时间，单位: 分 */
    private static final long DEFAULT_CONNECTION_TIMEOUT = 1;

    /** AK */
    private final String accessKey;
    /** SK */
    private final String securityKey;
    /** 终端节点 */
    private final String endpoint;
    /** 桶地址 */
    private String bucket;
    /** 访问协议 */
    private String protocol = "https";
    /** 端口 */
    private int port = 443;
    /** 日历 */
    private final Calendar calendar = Calendar.getInstance();
    /** 日期格式化器 */
    private SimpleDateFormat sdf;
    /** http请求客户端 */
    private OkHttpClient okHttpClient;
    /** http请求客户端 */
    private HttpClient httpClient;

    public HuaweiOBSClient(String endpoint, String bucket, String accessKey, String securityKey) {
        this.endpoint = endpoint;
        setBucket(bucket);
        this.accessKey = accessKey;
        this.securityKey = securityKey;
        initHttpClient();
        initSdf();
    }

    /**
     * 目标 https://support.huaweicloud.com/api-obs/obs_04_0022.html
     * 参考 https://support.huaweicloud.com/api-obs/obs_04_0017.html
     */
    public void listObjectInBucket() {
        String requestTime = getDate();
        Request request = new Request.Builder()
                .url(generateHttpUrl())
                .header("Date", requestTime)
                .get()
                .build();

        String canonicalizedResource = generateCanonicalizedResource();
        String canonicalString = generateCanonicalString(request.method(),
                "", "", requestTime, "",
                canonicalizedResource);

        request = signRequest(request, canonicalString);

        try {
            Response response = okHttpClient.newCall(request).execute();

            System.out.println("request message");
            System.out.println(request);
            System.out.println(request.headers());

            System.out.println("response message");
            System.out.println(response);
            System.out.println(response.headers());

            try (BufferedReader bufferedReader = new BufferedReader(response.body().charStream())) {
                int inputChar;
                StringBuilder stringBuilder = new StringBuilder();
                while ((inputChar = bufferedReader.read()) != -1) {
                    stringBuilder.append((char) inputChar);
                    if (inputChar == '>') {
                        stringBuilder.append('\n');
                    }
                }
                System.out.println(stringBuilder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断桶地址是否存在
     * @return 如果桶地址存在则返回true 否则返回false
     * @throws IOException 通信相关异常
     */
    public boolean isBucketExist() throws IOException {
        String requestTime = getDate();
        Request request = new Request.Builder()
                .url(generateHttpUrl())
                .header("Date", requestTime)
                .head()
                .build();

        String canonicalizedResource = generateCanonicalizedResource();
        String canonicalString = generateCanonicalString(request.method(),
                "", "", requestTime, "",
                canonicalizedResource);

        request = signRequest(request, canonicalString);

        try (Response response = okHttpClient.newCall(request).execute()) {
            System.out.println("request message");
            System.out.println(request);
            System.out.println(request.headers());

            System.out.println("response message");
            System.out.println(response);
            System.out.println(response.headers());

            try (BufferedReader bufferedReader = new BufferedReader(response.body().charStream())) {
                int inputChar;
                StringBuilder stringBuilder = new StringBuilder();
                while ((inputChar = bufferedReader.read()) != -1) {
                    stringBuilder.append((char) inputChar);
                    if (inputChar == '>') {
                        stringBuilder.append('\n');
                    }
                }
                System.out.println(stringBuilder);
            }

            switch (response.code()) {
                case 404:
                    return false;
                case 403:
                    if ("SignatureDoesNotMatch".equals(response.header("x-obs-error-code"))) {
                        System.out.println("my auth failed");
                    }
                    throw new IllegalArgumentException("auth failed");
                case 200:
                    return true;
                default:
                    throw new IllegalStateException("Unexpected value: " + response.code());
            }
        }
    }

    /**
     * 判断桶地址是否存在
     * @return 如果桶地址存在则返回true 否则返回false
     * @throws IOException 通信相关异常
     */
    public boolean isBucketExistApache() throws IOException {
        String requestTime = getDate();
        HttpRequestBase request = new HttpHead(generateUrl());
        request.addHeader("Date", requestTime);

        String canonicalizedResource = generateCanonicalizedResource();
        String canonicalString = generateCanonicalString(request.getMethod(),
                "", "", requestTime, "",
                canonicalizedResource);

        signRequest(request, canonicalString);

        HttpResponse response = httpClient.execute(request);

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
     * 上传文件
     * @param path      本地文件路径
     * @param objectKey 对象key
     * @throws IOException 通信IO异常
     */
    public void uploadFile(String path, String objectKey) throws IOException {
        objectKey = requireValidObjectKey(objectKey);
        File srcFile = new File(path);
        if (!srcFile.exists()) {
            throw new FileNotFoundException("File not found");
        }
        if (srcFile.isDirectory()) {
            throw new IllegalArgumentException("Cannot upload directory");
        }

        String requestTime = getDate();
        RequestBody fileBody = RequestBody.create(null, srcFile);
        Request request = new Request.Builder()
                .url(generateHttpUrl(objectKey))
                .header("Date", requestTime)
                .put(fileBody)
                .build();

        String canonicalResource = generateCanonicalizedResource(objectKey);
        String canonicalString = generateCanonicalString(request.method(),
                "", "", requestTime, "", canonicalResource);

        request = signRequest(request, canonicalString);

        Response response = okHttpClient.newCall(request).execute();

        try (BufferedReader bufferedReader = new BufferedReader(response.body().charStream())) {
            int inputChar;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputChar = bufferedReader.read()) != -1) {
                stringBuilder.append((char) inputChar);
                if (inputChar == '>') {
                    stringBuilder.append('\n');
                }
            }
            System.out.println(stringBuilder);
        }

    }

    /**
     * 上传文件
     * @param inputStream 输入流
     * @param objectKey   对象key
     * @throws IOException 通信IO异常
     */
    public void uploadInputStream(InputStream inputStream, String objectKey) throws IOException {
        objectKey = requireValidObjectKey(objectKey);
        Objects.requireNonNull(inputStream);

        String requestTime = getDate();
        RequestBody fileBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return null;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = Okio.source(inputStream);
                sink.writeAll(source);
            }
        };
        Request request = new Request.Builder()
                .url(generateHttpUrl(objectKey))
                .header("Date", requestTime)
                .put(fileBody)
                .build();

        String canonicalResource = generateCanonicalizedResource(objectKey);
        String canonicalString = generateCanonicalString(request.method(),
                "", "", requestTime, "", canonicalResource);

        request = signRequest(request, canonicalString);

        Response response = okHttpClient.newCall(request).execute();

        try (BufferedReader bufferedReader = new BufferedReader(response.body().charStream())) {
            int inputChar;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputChar = bufferedReader.read()) != -1) {
                stringBuilder.append((char) inputChar);
                if (inputChar == '>') {
                    stringBuilder.append('\n');
                }
            }
            System.out.println(stringBuilder);
        }
    }

    /**
     * 上传文件
     * @param inputStream 输入流
     * @param objectKey   对象key
     * @throws IOException 通信IO异常
     */
    public void uploadInputStreamApache(InputStream inputStream, String objectKey) throws IOException {
        objectKey = requireValidObjectKey(objectKey);
        Objects.requireNonNull(inputStream);

        String requestTime = getDate();

        HttpPut request = new HttpPut(generateUrl(objectKey));
        appendDate(request, requestTime);
        InputStreamEntity entity = new InputStreamEntity(inputStream);
        request.setEntity(entity);


        String canonicalResource = generateCanonicalizedResource(objectKey);
        String canonicalString = generateCanonicalString(request.getMethod(),
                "", "", requestTime, "", canonicalResource);

        signRequest(request, canonicalString);

        HttpResponse response = httpClient.execute(request);

        System.out.println("request message");
        System.out.println(request);
        System.out.println(Arrays.toString(request.getAllHeaders()));

        System.out.println("response message");
        System.out.println(response);
        System.out.println(Arrays.toString(response.getAllHeaders()));
        HttpEntity entity1 = response.getEntity();
        if (entity1 != null) {
            try (BufferedInputStream bis = new BufferedInputStream(entity1.getContent())) {
                int inputChar;
                StringBuilder stringBuilder = new StringBuilder();
                while ((inputChar = bis.read()) != -1) {
                    stringBuilder.append((char) inputChar);
                    if (inputChar == '>') {
                        stringBuilder.append('\n');
                    }
                }
                System.out.println(stringBuilder);
            }
        }
    }

    /**
     * 初始化http客户端
     */
    private void initHttpClient() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MINUTES)
                .writeTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MINUTES)
                .readTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MINUTES)
                .build();

        httpClient = HttpClients.createDefault();
    }

    /**
     * 初始化华为API格式的时间格式化器
     */
    private void initSdf() {
        sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * 获取华为API要求格式的当前时间
     * @return 时间字符串
     */
    private String getDate() {
        return sdf.format(calendar.getTime());
    }

    /**
     * 添加时间信息
     * @param request 请求
     * @param date    时间信息
     * @return 带时间请求头请求
     */
    private HttpRequestBase appendDate(HttpRequestBase request, String date) {
        request.addHeader("Date", date);
        return request;
    }


    /**
     * 为请求签名
     * @param request         请求
     * @param canonicalString 待签名字符串
     * @return 带签名的请求
     */
    private Request signRequest(Request request, String canonicalString) {
        String signature = HuaweiSignature.signWithHmacSha1(securityKey, canonicalString);

        request = request.newBuilder()
                .header("Authorization", "OBS " + accessKey + ":" + signature)
                .build();
        return request;
    }

    /**
     * 为请求签名
     * @param request         请求
     * @param canonicalString 待签名字符串
     * @return 带签名的请求
     */
    private HttpRequestBase signRequest(HttpRequestBase request, String canonicalString) {
        String signature = HuaweiSignature.signWithHmacSha1(securityKey, canonicalString);
        request.addHeader("Authorization", "OBS " + accessKey + ":" + signature);
        return request;
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
    private String generateCanonicalString(String method,
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
     * 生成访问桶地址的url
     * @return url
     */
    private HttpUrl generateHttpUrl() {
        return HttpUrl.get(generateUrl());
    }

    /**
     * 生成访问确切对象的url
     * @param object 对象资源定位key
     * @return url
     */
    private HttpUrl generateHttpUrl(String object) {
        return HttpUrl.get(protocol + "://" + bucket + "." + endpoint + ":" + port
                + "/" + object);
    }

    /**
     * 构建访问url
     * @return url String
     */
    private String generateUrl() {
        return protocol + "://" + bucket + "." + endpoint + ":" + port;
    }

    /**
     * 构建访问url
     * @param object 对象
     * @return url String
     */
    private String generateUrl(String object) {
        return protocol + "://" + bucket + "." + endpoint + ":" + port + "/" + object;
    }

    /**
     * 生成访问资源字符串
     * @param object 访问资源
     * @return 访问资源字符串
     */
    private String generateCanonicalizedResource(String object) {
        if (object == null || "".equals(object)) {
            return generateCanonicalizedResource();
        } else {
            return "/".concat(bucket).concat("/").concat(object);
        }
    }

    /**
     * 生成访问资源字符串
     * @return 访问资源字符串
     */
    private String generateCanonicalizedResource() {
        return "/".concat(bucket).concat("/");
    }

    /**
     * 判断桶地址是否合法，以华为为标准
     * @param bucket 桶地址
     * @return 如果合法则返回true，其他情况返回false
     */
    private boolean isBucketNameValid(String bucket) {
        int length = bucket.length();
        if (length < 3 || length > 63) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char ch = bucket.charAt(i);
            if (!(ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9' || ch == '-' || ch == '.')) {
                return false;
            }
        }
        return true;
    }

    /**
     * 要求合法对象key
     * 华为标准key可以使用任何utf-8字符，但长度在1-1024之间
     * 该方法会消除object key的前置文件分隔符
     * @param objectKey 对象key
     * @throws IllegalArgumentException 对象key不符合要求
     */
    private String requireValidObjectKey(String objectKey) {
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
        return objectKey;
    }

    /**
     * 设置桶地址
     * @param bucket 桶地址
     */
    private void setBucket(String bucket) {
        if (isBucketNameValid(bucket)) {
            this.bucket = bucket;
        } else {
            throw new IllegalArgumentException("invalid parameter of bucket");
        }
    }
}
