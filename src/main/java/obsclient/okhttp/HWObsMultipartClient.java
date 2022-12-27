package obsclient.okhttp;

import obsclient.okhttp.entity.CompleteMultipartUpload;
import obsclient.okhttp.entity.InitiatedMultipartUpload;
import obsclient.okhttp.entity.UploadedPart;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    public HWObsMultipartClient(obsclient.okhttp.ObsServer obsServer) {
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
        objectName = requireValidObjectKey(objectName);
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
        Request.Builder builder = new Request.Builder()
                .url(url);
        appendDateAndSignRequest(builder, "PUT", canonicalResource);
        try (InputStream is = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(is)) {
            if (offset > 0) {
                bis.skip(offset);
            }
            RequestBody requestBody = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return null;
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    Source source = Okio.source(bis);
                    sink.write(source, length);
                }
            };
            Request request = builder.put(requestBody).build();

            Response response = getHttpClient().newCall(request).execute();
            outputHeader(request, response);
            System.out.println(readResponse(response));
            if (response.code() != HttpStatus.SC_OK) {
                String responseBody = readResponse(response);
                throw new RuntimeException("Something error occurred when do multipart upload. \n"
                        + "Response Code: " + (response.code())
                        + "\n Response body: ".concat(responseBody));
            }
            return response.header("Etag");
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
        Request.Builder builder = new Request.Builder().url(url);
        appendDateAndSignRequest(builder, "POST", canonicalResource);

        String xml = object2Xml(completeMultipartUpload, completeMultipartUpload.getClass());
        RequestBody stringBody = RequestBody.create(null, xml);
        Request request = builder.post(stringBody).build();

        Response response = getHttpClient().newCall(request).execute();
        outputHeader(request, response);
        if (response.code() != HttpStatus.SC_OK) {
            String responseBody = readResponse(response);
            throw new RuntimeException("Something error occurred while merging multipart. \n"
                    + "Response Code: " + (response.code())
                    + "\n Response body: ".concat(responseBody));
        }

    }

    /**
     * 取消多段上传任务
     * @param objectName 对象名
     * @param uploadId   多段上传任务id
     * @throws IOException I/O 异常
     */
    public void cancelUpload(String objectName, String uploadId) throws IOException {
        objectName = requireValidObjectKey(objectName);
        if (uploadId == null || uploadId.isEmpty()) {
            throw new RuntimeException();
        }

        String url = generateUrl(objectName);
        String canonicalResource = generateCanonicalResource(objectName);
        url = appendUrlParam(url, "uploadId", uploadId);
        canonicalResource = appendUrlParam(canonicalResource, "uploadId", uploadId);

        Request.Builder builder = new Request.Builder().url(url);
        appendDateAndSignRequest(builder, "DELETE", canonicalResource);
        Request request = builder.delete().build();


        Response response = getHttpClient().newCall(request).execute();
        outputHeader(request, response);
        System.out.println(readResponse(response));

        if (response.code() != HttpStatus.SC_NO_CONTENT) {
            String responseBody = readResponse(response);
            throw new RuntimeException("Something error occurred while canceling multipart upload. \n"
                    + "Response Code: " + (response.code())
                    + "\n Response body: ".concat(responseBody));
        }
    }

    /**
     * 初始化多段上传任务
     * @param objectName 对象名
     * @return 多段上传任务号
     */
    public InitiatedMultipartUpload initMultipartUpload(String objectName) {
        objectName = requireValidObjectKey(objectName);
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
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(null, ""));
        String canonicalResource = generateCanonicalResource(objectName).concat("?uploads");
        appendDateAndSignRequest(builder, "POST", canonicalResource);

        Request request = builder.build();

        try {
            Response response = getHttpClient().newCall(request).execute();
            outputHeader(request, response);
            if (response.code() == HttpStatus.SC_OK) {
                String xml = readResponse(response);
                System.out.println(xml);
                InitiatedMultipartUpload initiatedMultipartUpload =
                        (InitiatedMultipartUpload) xml2Object(xml, InitiatedMultipartUpload.class);
                System.out.println(initiatedMultipartUpload);
                return initiatedMultipartUpload;
            } else {
                String xml = readResponse(response);
                throw new RuntimeException("Something error occurred when initialize multipart upload. \n"
                        + "Response Code: " + (response.code())
                        + "\n Response body: ".concat(xml));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to init multipart upload", e);
        }
    }
}
