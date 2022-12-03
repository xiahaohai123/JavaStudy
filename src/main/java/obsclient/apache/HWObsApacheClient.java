package obsclient.apache;

import com.example.huaweiOBSApiClient.apache.entity.Delete;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HWObsApacheClient extends AbstractHWObsClient {

    /** 一次性上传数据量的上限，如果超出该数据量，则委托到多段上传任务 */
    private static final long MAX_UPLOAD_SIZE = 1 << 10 << 10 << 10;
    /** 最大重传次数 */
    private static final int MAX_RETRY_TIME = 3;
    /** 委托对象 */
    private volatile AbstractHWObsClient entrustedClient;

    /**
     * 构造器
     * @param obsServer 对象存储服务器数据
     */
    public HWObsApacheClient(ObsServer obsServer) {
        super(obsServer);
    }

    @Override
    public void upload(File file, String objectName) throws IOException {
        Objects.requireNonNull(file, "File can not be null!");
        String encodedObjectName = requireValidObjectKeyAndEncodeUrl(objectName);
        System.out.println("encoded object key: ".concat(encodedObjectName));
        if (!file.exists()) {
            throw new IllegalArgumentException("File is not exist");
        }

        if (file.length() < MAX_UPLOAD_SIZE) {
            for (int i = 0; i < MAX_RETRY_TIME; i++) {
                try {
                    doUpload(file, encodedObjectName);
                    break;
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        } else {
            doEntrustedUpload(file, objectName);
        }
    }

    /**
     * 上传文件
     * @param inputStream 数据源输入流
     * @param size        数据量大小，单位:字节 byte
     * @param objectName  对象名
     */
    public void upload(InputStream inputStream, long size, String objectName) throws Exception {
        Objects.requireNonNull(inputStream);
        Objects.requireNonNull(objectName);
        requireValidSize(size);
        if (size < MAX_UPLOAD_SIZE) {
        } else {
            ((HWObsMultipartClient) getEntrustedClient()).upload(inputStream, objectName);
        }
    }

    private HttpRequestBase generateBaseUploadRequest(InputStream is, String objectName) {
        objectName = requireValidObjectKeyAndEncodeUrl(objectName);
        Objects.requireNonNull(is);

        HttpPut request = new HttpPut(generateUrl(objectName));
        String requestTime = getDate();
        appendDate(request, requestTime);
        InputStreamEntity entity = new InputStreamEntity(is);
        request.setEntity(entity);
        String canonicalResource = generateCanonicalResource(objectName);
        String canonicalString = generateCanonicalString(request.getMethod(),
                "", "", requestTime, "", canonicalResource);

        signRequest(request, canonicalString);
        return request;
    }

    /**
     * 上传文件
     * @param file       文件
     * @param objectName 对象key
     * @throws IOException 通信IO异常
     */
    public void doUpload(File file, String objectName) throws IOException {

        HttpPut request = new HttpPut(generateUrl(objectName));
        String requestTime = getDate();
        appendDate(request, requestTime);
        FileEntity fileEntity = new FileEntity(file);
        request.setEntity(fileEntity);

        String canonicalResource = generateCanonicalResource(objectName);
        String canonicalString = generateCanonicalString(request.getMethod(),
                "", "", requestTime, "", canonicalResource);

        System.out.println("canonicalResource: ".concat(canonicalResource));
        System.out.println("canonicalString: ".concat(canonicalString));

        signRequest(request, canonicalString);

        HttpResponse response = getHttpClient().execute(request);

        outputHeader(request, response);
        System.out.println(readResponse(response));
    }

    /**
     * 委托上传
     * 当上传某个文件超出本类能力范围时，委托另一个类进行上传。
     * 当前只有文件大小超出一定范围才会使用委托上传。
     * @param file       文件对象
     * @param objectName 对象名
     * @throws IOException I/O异常
     */
    private void doEntrustedUpload(File file, String objectName) throws IOException {
        getEntrustedClient().upload(file, objectName);
    }

    /**
     * 获取用来委托多段上传任务的客户端
     * @return 委托客户端
     */
    private AbstractHWObsClient getEntrustedClient() {
        if (entrustedClient == null) {
            synchronized (this) {
                if (entrustedClient == null) {
                    entrustedClient = new HWObsMultipartClient(getObsServer());
                }
            }
        }
        return entrustedClient;
    }

    /**
     * 批量删除文件
     * @param paths 文件路径列表
     */
    public void deleteFiles(List<String> paths) {
        List<List<String>> pathsList = splitPathList(paths);
        for (List<String> list : pathsList) {
            doDeleteFiles(list);
        }
    }

    protected List<List<String>> splitPathList(List<String> paths) {
        int size = paths.size();
        List<List<String>> pathsList = new ArrayList<>();
        int numberOfSlices = size / 1000;
        int index = 0;
        for (; index < numberOfSlices; index++) {
            pathsList.add(paths.subList(index * 1000, (index + 1) * 1000));
        }
        if (size % 1000 != 0) {
            pathsList.add(paths.subList(index * 1000, paths.size() - 1));
        }
        return pathsList;
    }

    /**
     * 批量删除文件
     * @param paths 文件路径列表 要求: size < 1000
     */
    private void doDeleteFiles(Collection<String> paths) {
        String url = generateUrl() + "?delete";
        HttpPost request = new HttpPost(url);

        String requestTime = getDate();
        appendDate(request, requestTime);

        List<Delete.DeleteObject> deleteObjects = paths.stream().map(Delete.DeleteObject::new)
                .collect(Collectors.toList());
        Delete delete = new Delete();
        delete.setDeleteObjects(deleteObjects);
        String xml = object2Xml(delete, delete.getClass());
        byte[] content = xml.getBytes(StandardCharsets.UTF_8);
        //request.setHeader("Content-Length", String.valueOf(content.length));
        request.addHeader("Content-SHA256", digestSha256Base64(content));
        request.addHeader("Content-Type", "text/plain");
        String canonicalResource = generateCanonicalResource() + "?delete";
        String canonicalString = generateCanonicalString(request.getMethod(),
                "", "text/plain", requestTime, "", canonicalResource);
        signRequest(request, canonicalString);

        try {
            StringEntity stringEntity = new StringEntity(xml);
            request.setEntity(stringEntity);

            HttpResponse response = getHttpClient().execute(request);
            outputHeader(request, response);
            String responseBody = readResponse(response);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Something error occurred while deleting files. \n"
                        + "Response Code: " + (response.getStatusLine().getStatusCode())
                        + "\n Response body: ".concat(responseBody));
            } else {
                System.out.println("Response body: ");
                System.out.println(responseBody);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String digestSha256Base64(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(bytes);
            byte[] encode = Base64.getEncoder().encode(digest);
            return new String(encode);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 下载文件
     * @param objectKey 对象key
     * @param localDir  本地存放路径，不带文件名
     * @throws RuntimeException 异常信息
     */
    public void downloadFile(String objectKey, String localDir) throws RuntimeException {
        Objects.requireNonNull(localDir, "localDir should not be null");
        File dir = computeDirIfAbsent(localDir);
        requireValidObjectKey(objectKey);
        doDownloadFile(objectKey, dir.getAbsolutePath());
    }

    /**
     * 执行下载任务
     * @param objectKey 对象key
     * @param dir       下载路径
     * @throws RuntimeException 异常信息
     */
    private void doDownloadFile(String objectKey, String dir) throws RuntimeException {
        String filename = getFilenameInObjectKey(objectKey);
        String downloadFilePath = dir + File.separator + filename;
        try (FileOutputStream fileOutputStream = new FileOutputStream(downloadFilePath);
             BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream)) {
            doDownload(objectKey, bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行下载
     * @param objectKey    对象key
     * @param outputStream 数据输出流
     * @throws IOException IO异常
     */
    private void doDownload(String objectKey, OutputStream outputStream) throws IOException {
        String encodedObjectName = urlEncodeObjectKey(objectKey);
        String url = generateUrl(encodedObjectName);
        HttpGet request = new HttpGet(url);
        String canonicalResource = generateCanonicalResource(encodedObjectName);
        appendDateAndSignRequest(request, canonicalResource);
        HttpResponse response = getHttpClient().execute(request);
        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new RuntimeException("Something error occurred while download file. \n"
                    + "Response Code: " + (response.getStatusLine().getStatusCode())
                    + "\n Response body: ".concat(readResponse(response)));
        } else {
            try (BufferedInputStream bis = new BufferedInputStream(entity.getContent())) {
                IOUtils.copyLarge(bis, outputStream);
            }
        }
    }

    /**
     * 从对象名内获取文件名
     * @param objectKey 对象key
     * @return 文件名
     */
    private String getFilenameInObjectKey(String objectKey) {
        int index = objectKey.lastIndexOf("/");
        return objectKey.substring(index + 1);
    }

    /**
     * 计算路径是否存在，若不存在则创建目录
     * @param localPath 路径
     * @return 文件
     * @throws RuntimeException 路径无法创建,路径已存在但不是目录
     */
    private File computeDirIfAbsent(String localPath) throws RuntimeException {
        File file = new File(localPath);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("create dir failed: " + file.getAbsolutePath());
            }
        }
        if (!file.isDirectory()) {
            throw new RuntimeException("the file already exists and is not a directory");
        }
        return file;
    }
}
