package obsclient.okhttp;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class HWObsClient extends AbstractHWObsClient {

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
    public HWObsClient(ObsServer obsServer) {
        super(obsServer);
    }

    @Override
    public void upload(File file, String objectName) throws IOException {
        Objects.requireNonNull(file, "File can not be null!");
        String encodedObjectName = requireValidObjectKey(objectName);
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
     * @param file       文件
     * @param objectName 对象key
     * @throws IOException 通信IO异常
     */
    public void doUpload(File file, String objectName) throws IOException {
        RequestBody requestBody = RequestBody.create(null, file);
        Request.Builder builder = new Request.Builder()
                .url(generateUrl(objectName))
                .put(requestBody);

        String requestTime = getDate();
        appendDate(builder, requestTime);

        String canonicalResource = generateCanonicalResource(objectName);
        String canonicalString = generateCanonicalString("PUT",
                "", "", requestTime, "", canonicalResource);

        System.out.println("canonicalResource: ".concat(canonicalResource));
        System.out.println("canonicalString: ".concat(canonicalString));

        signRequest(builder, canonicalString);

        Request request = builder.build();
        Response response = getHttpClient().newCall(request).execute();

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
}
