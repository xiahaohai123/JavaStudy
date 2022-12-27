package obsclient.apache;

import obsclient.apache.entity.CompleteMultipartUpload;
import obsclient.apache.entity.InitiatedMultipartUpload;
import obsclient.apache.entity.MultipartUploads;
import obsclient.apache.entity.UploadedPart;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 多段上传用OBS客户端
 * 专注于多段上传任务
 */
public class HWObsMultipartClient extends AbstractHWObsClient {

    /** 一段数据量大小 */
    private static final long DEFAULT_PART_SIZE = 256 << 10 << 10;

    /**
     * 构造器
     * @param obsServer 对象存储服务器数据
     */
    public HWObsMultipartClient(ObsServer obsServer) {
        super(obsServer);
    }

    /**
     * 分段上传文件
     * 当文件过大时必须使用多段上传法上传文件
     * 1. 确定文件分几段
     * 2. 初始化段上传任务
     * 3. 按顺序按段上传文件
     * 4. 合并段
     * 异常情况：取消段任务以释放空间
     * 特殊异常情况：取消段任务失败，另外考虑
     * @param file       文件对象
     * @param objectName 对象名，对应路径名
     */
    @Override
    public void upload(File file, String objectName) throws IOException {
        objectName = requireValidObjectKeyAndEncodeUrl(objectName);
        Objects.requireNonNull(file, "File can not be null");
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: ".concat(file.getAbsolutePath()));
        }
        long startTime = System.currentTimeMillis();
        long fileLength = file.length();
        int partQuantity = calculateNumberOfPart(fileLength, DEFAULT_PART_SIZE);
        InitiatedMultipartUpload initiatedMultipartUpload = doInitMultipartUpload(objectName);
        String uploadId = initiatedMultipartUpload.getUploadId();
        try {
            CompleteMultipartUpload completedMultipartUpload = new CompleteMultipartUpload();
            long offset = 0;
            for (int partId = 1; partId <= partQuantity; partId++) {
                long firstTime = System.currentTimeMillis();
                long realLength = partId < partQuantity
                        ? DEFAULT_PART_SIZE : fileLength - (partId - 1) * DEFAULT_PART_SIZE;
                String etag = uploadPart(file, offset, realLength, objectName, uploadId, partId);
                completedMultipartUpload.addUploadedPart(new UploadedPart(partId, etag));
                offset += realLength;
                long secondTime = System.currentTimeMillis();
                System.out.println("part id: " + partId + " use time: " + (secondTime - firstTime) + "ms");
            }
            mergePart(objectName, uploadId, completedMultipartUpload);
        } catch (IOException e) {
            e.printStackTrace();
            cancelUpload(objectName, uploadId);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("upload use: " + (endTime - startTime) + "ms");
    }

    /**
     * 上传段的文件接口
     * @param file       文件
     * @param offset     偏移量
     * @param length     要上传的数据长度
     * @param objectName 对象名
     * @param uploadId   上传任务ID
     * @param partId     段编号
     * @return etag，已上传的段标识
     * @throws IOException I/O异常
     */
    @SuppressWarnings("igonred")
    private String uploadPart(File file, long offset, long length, String objectName, String uploadId, int partId)
            throws IOException {
        String url = generateUrl(objectName);
        String canonicalResource = generateCanonicalResource(objectName);
        url = appendUrlParam(url, "partNumber", partId);
        url = appendUrlParam(url, "uploadId", uploadId);
        canonicalResource = appendUrlParam(canonicalResource, "partNumber", partId);
        canonicalResource = appendUrlParam(canonicalResource, "uploadId", uploadId);
        HttpPut request = new HttpPut(url);
        try (InputStream is = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(is)) {
            if (offset > 0) {
                bis.skip(offset);
            }
            InputStreamEntity entity = new InputStreamEntity(bis, length);
            request.setEntity(entity);

            String requestTime = getDate();
            appendDate(request, requestTime);
            String canonicalString = generateCanonicalString(request.getMethod(),
                    "", "", requestTime, "",
                    canonicalResource);
            signRequest(request, canonicalString);

            HttpResponse response = getHttpClient().execute(request);
            outputHeader(request, response);
            System.out.println(readResponse(response));
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                String responseBody = readResponse(response);
                throw new RuntimeException("Something error occurred when do multipart upload. \n"
                        + "Response Code: " + (response.getStatusLine().getStatusCode())
                        + "\n Response body: ".concat(responseBody));
            }
            return response.getLastHeader("Etag").getValue();
        }
    }


    /**
     * 计算分段数
     * 当源数据规模正好等于每段数据规模的整数倍时则直接整除返回
     * 当源数据规模非每段数据规模的整数倍则整除加1后返回
     * @param srcSize  源数据规模
     * @param partSize 每段数据规模
     * @return 分段数
     */
    private int calculateNumberOfPart(long srcSize, long partSize) {
        if (srcSize == 0) {
            return 1;
        }
        if (srcSize % partSize == 0) {
            return (int) (srcSize / partSize);
        } else {
            return (int) (srcSize / partSize) + 1;
        }
    }


    /**
     * 多段任务方式上传文件
     * 1. 初始化上传任务
     * 2. 段式上传
     * 3. 合并段
     * 如果其中任意一步骤失败则取消上传任务
     * @param inputStream 输入流
     * @param objectName  对象key
     */
    public void upload(InputStream inputStream, String objectName) {
        objectName = requireValidObjectKeyAndEncodeUrl(objectName);
        Objects.requireNonNull(inputStream);
        InitiatedMultipartUpload initiatedMultipartUpload = doInitMultipartUpload(objectName);
        String uploadId = initiatedMultipartUpload.getUploadId();
        try {
            CompleteMultipartUpload completeMultipartUpload = multipartUpload(inputStream, objectName, uploadId);
            mergePart(objectName, uploadId, completeMultipartUpload);
        } catch (Exception e) {
            try {
                cancelUpload(objectName, uploadId);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            throw new RuntimeException("Failed to upload", e);
        }
    }


    /**
     * 上传段
     * @param inputStream 输入流
     * @param objectName  对象名称
     * @param uploadId    上传任务ID
     * @param partId      段编号
     * @return ETag 上传段成功后返回的ETag值，是段内容的唯一标识
     * @throws IOException IO异常
     */
    private String uploadPart(InputStream inputStream, String objectName, String uploadId, int partId) throws IOException {
        String url = generateUrl(objectName);
        String canonicalResource = generateCanonicalResource(objectName);
        url = appendUrlParam(url, "partNumber", partId);
        canonicalResource = appendUrlParam(canonicalResource, "partNumber", partId);
        url = appendUrlParam(url, "uploadId", uploadId);
        canonicalResource = appendUrlParam(canonicalResource, "uploadId", uploadId);

        HttpPut request = new HttpPut(url);
        InputStreamEntity entity = new InputStreamEntity(inputStream, inputStream.available());
        request.setEntity(entity);

        appendDateAndSignRequest(request, canonicalResource);

        HttpResponse response = getHttpClient().execute(request);
        outputHeader(request, response);
        // TODO: 2021/8/30 异常处理
        return response.getLastHeader("Etag").getValue();
    }

    /**
     * 合并段
     * @param objectName              对象名
     * @param uploadId                上传任务ID
     * @param completeMultipartUpload 已上传段的列表
     * @throws IOException I/O异常
     */
    private void mergePart(String objectName, String uploadId, CompleteMultipartUpload completeMultipartUpload) throws IOException {
        String url = generateUrl(objectName);
        String canonicalResource = generateCanonicalResource(objectName);

        url = appendUrlParam(url, "uploadId", uploadId);
        canonicalResource = appendUrlParam(canonicalResource, "uploadId", uploadId);

        HttpPost request = new HttpPost(url);
        String xml = object2Xml(completeMultipartUpload, completeMultipartUpload.getClass());

        appendDateAndSignRequest(request, canonicalResource);

        try (InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            InputStreamEntity entity = new InputStreamEntity(inputStream);
            request.setEntity(entity);

            HttpResponse response = getHttpClient().execute(request);
            outputHeader(request, response);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                String responseBody = readResponse(response);
                throw new RuntimeException("Something error occurred while merging multipart. \n"
                        + "Response Code: " + (response.getStatusLine().getStatusCode())
                        + "\n Response body: ".concat(responseBody));
            }
        }
    }

    /**
     * 取消多段上传任务
     * @param objectName 对象名
     * @param uploadId   多段上传任务id
     * @throws IOException I/O 异常
     */
    public void cancelUpload(String objectName, String uploadId) throws IOException {
        objectName = requireValidObjectKeyAndEncodeUrl(objectName);
        if (uploadId == null || uploadId.isEmpty()) {
            throw new RuntimeException();
        }

        String url = generateUrl(objectName);
        String canonicalResource = generateCanonicalResource(objectName);
        url = appendUrlParam(url, "uploadId", uploadId);
        canonicalResource = appendUrlParam(canonicalResource, "uploadId", uploadId);

        HttpDelete request = new HttpDelete(url);

        appendDateAndSignRequest(request, canonicalResource);

        HttpResponse response = getHttpClient().execute(request);
        outputHeader(request, response);
        System.out.println(readResponse(response));

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
            String responseBody = readResponse(response);
            throw new RuntimeException("Something error occurred while canceling multipart upload. \n"
                    + "Response Code: " + (response.getStatusLine().getStatusCode())
                    + "\n Response body: ".concat(responseBody));
        }
    }


    /**
     * 列出所有在OBS上已初始化且未结束的多段上传任务
     * @throws IOException IO异常
     */
    public MultipartUploads listAllMultipartUploadTask() throws Exception {
        String url = generateUrl();
        url = appendUrlParam(url, "uploads");
        HttpRequestBase request = new HttpGet(url);

        String canonicalResource = generateCanonicalResource().concat("?uploads");
        appendDateAndSignRequest(request, canonicalResource);
        HttpResponse response = getHttpClient().execute(request);
        outputHeader(request, response);
        String xml = readResponse(response);
        System.out.println(xml);
        MultipartUploads uploads = (MultipartUploads) xml2Object(xml, MultipartUploads.class);
        System.out.println(uploads);
        return uploads;
    }

    /**
     * 初始化多段上传任务
     * @param objectName 对象名
     * @return 多段上传任务号
     */
    public InitiatedMultipartUpload initMultipartUpload(String objectName) {
        objectName = requireValidObjectKeyAndEncodeUrl(objectName);
        return doInitMultipartUpload(objectName);
    }

    /**
     * 执行初始化多段上传任务
     * @param objectName 对象名
     * @return 多段上传任务号
     */
    private InitiatedMultipartUpload doInitMultipartUpload(String objectName) {
        String url = generateUrl(objectName);
        url = appendUrlParam(url, "uploads");
        HttpPost request = new HttpPost(url);

        String canonicalResource = generateCanonicalResource(objectName).concat("?uploads");
        appendDateAndSignRequest(request, canonicalResource);

        try {
            HttpResponse response = getHttpClient().execute(request);
            outputHeader(request, response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String xml = readResponse(response);
                System.out.println(xml);
                InitiatedMultipartUpload initiatedMultipartUpload =
                        (InitiatedMultipartUpload) xml2Object(xml, InitiatedMultipartUpload.class);
                System.out.println(initiatedMultipartUpload);
                return initiatedMultipartUpload;
            } else {
                String xml = readResponse(response);
                throw new RuntimeException("Something error occurred when initialize multipart upload. \n"
                        + "Response Code: " + (response.getStatusLine().getStatusCode())
                        + "\n Response body: ".concat(xml));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to init multipart upload", e);
        }
    }

    /**
     * 多段上传实现
     * @param inputStream 数据源输入流
     * @param objectName  对象名
     * @param uploadId    上传任务ID
     * @return 上传完毕后，已上传的数据标识集合
     * @throws Exception 异常
     */
    private CompleteMultipartUpload multipartUpload(InputStream inputStream, String objectName, String uploadId) throws Exception {
        int partNumber = 0;
        CompleteMultipartUpload completedUploads = new CompleteMultipartUpload();

        byte[] buffer = new byte[300 << 10];
        int readLength;
        while ((readLength = inputStream.read(buffer)) != -1) {
            try (InputStream is = new ByteArrayInputStream(buffer, 0, readLength);
                 BufferedInputStream bis = new BufferedInputStream(is)) {
                partNumber++;
                String etag = uploadPart(bis, objectName, uploadId, partNumber);
                completedUploads.addUploadedPart(new UploadedPart(partNumber, etag));
            }
        }
        return completedUploads;
    }
}
