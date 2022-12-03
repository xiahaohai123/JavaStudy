package obsclient.apache.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 多段上传任务清单
 */
@XmlRootElement(name = "ListMultipartUploadsResult")
public class MultipartUploads {

    /** 初始化任务所在的桶名 */
    private String bucket;
    /** 列举时的起始对象位置 */
    private String keyMarker;
    /** 列举时的起始UploadId位置 */
    private String uploadIdMarker;
    /** 请求中带的Delimiter */
    private String delimiter;
    /** 请求中带的Prefix */
    private String prefix;
    /** 如果本次没有返回全部结果，响应请求中将包含NextKeyMarker字段，用于标明接下来请求的KeyMarker值。 */
    private String nextKeyMarker;
    /** 如果本次没有返回全部结果，响应请求中将包含NextUploadMarker字段，用于标明接下来请求的UploadMarker值。 */
    private String nextUploadIdMarker;
    /** 最大多段上传任务数目 */
    private Integer maxUploads;
    /** 表明是否本次返回的Multipart Upload结果列表被截断。“true”表示本次没有返回全部结果；“false”表示本次已经返回了全部结果。 */
    private Boolean isTruncated;
    /** 多段任务列表 */
    private Collection<Upload> uploads;

    /** 构造器 */
    public MultipartUploads() {
        uploads = new ArrayList<>();
    }

    @XmlElement(name = "Bucket")
    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    @XmlElement(name = "KeyMarker")
    public String getKeyMarker() {
        return keyMarker;
    }

    public void setKeyMarker(String keyMarker) {
        this.keyMarker = keyMarker;
    }

    @XmlElement(name = "UploadIdMarker")
    public String getUploadIdMarker() {
        return uploadIdMarker;
    }

    public void setUploadIdMarker(String uploadIdMarker) {
        this.uploadIdMarker = uploadIdMarker;
    }

    @XmlElement(name = "Delimiter")
    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @XmlElement(name = "Prefix")
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @XmlElement(name = "NextKeyMarker")
    public String getNextKeyMarker() {
        return nextKeyMarker;
    }

    public void setNextKeyMarker(String nextKeyMarker) {
        this.nextKeyMarker = nextKeyMarker;
    }

    @XmlElement(name = "NextUploadIdMarker")
    public String getNextUploadIdMarker() {
        return nextUploadIdMarker;
    }

    public void setNextUploadIdMarker(String nextUploadIdMarker) {
        this.nextUploadIdMarker = nextUploadIdMarker;
    }

    @XmlElement(name = "MaxUploads")
    public Integer getMaxUploads() {
        return maxUploads;
    }

    public void setMaxUploads(Integer maxUploads) {
        this.maxUploads = maxUploads;
    }

    @XmlElement(name = "Truncated")
    public Boolean getTruncated() {
        return isTruncated;
    }

    public void setTruncated(Boolean truncated) {
        isTruncated = truncated;
    }

    @XmlElement(name = "Upload")
    public Collection<Upload> getUploads() {
        return uploads;
    }

    public void setUploads(Collection<Upload> uploads) {
        this.uploads = uploads;
    }

    @Override
    public String toString() {
        return "MultipartUploads{"
                + "Bucket='" + bucket + '\''
                + ", keyMarker='" + keyMarker + '\''
                + ", UploadIdMarker='" + uploadIdMarker + '\''
                + ", delimiter='" + delimiter + '\''
                + ", prefix='" + prefix + '\''
                + ", NextKeyMarker='" + nextKeyMarker + '\''
                + ", NextUploadIdMarker='" + nextUploadIdMarker + '\''
                + ", MaxUploads=" + maxUploads
                + ", IsTruncated=" + isTruncated
                + ", uploads=" + uploads
                + '}';
    }
}
