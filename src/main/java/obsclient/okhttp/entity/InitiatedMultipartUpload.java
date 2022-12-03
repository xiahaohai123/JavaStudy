package obsclient.okhttp.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "InitiateMultipartUploadResult")
public class InitiatedMultipartUpload {

    /** 桶地址 */
    private String bucket;
    /** 对象key */
    private String key;
    /** 上传任务id */
    private String uploadId;

    @XmlElement(name = "Bucket")
    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    @XmlElement(name = "Key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @XmlElement(name = "UploadId")
    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    @Override
    public String toString() {
        return "InitiateMultipartUploadResult{"
                + "bucket='" + bucket + '\''
                + ", key='" + key + '\''
                + ", uploadId='" + uploadId + '\''
                + '}';
    }
}
