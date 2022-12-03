package obsclient.okhttp.entity;

import javax.xml.bind.annotation.XmlElement;
import java.util.Date;

public class Upload {
    /** 初始化Multipart Upload任务的Object名字 */
    private String key;

    /** Multipart Upload任务的ID */
    private String uploadId;

    /** Multipart Upload任务的创建者 */
    private Initiator initiator;

    /** 任务段的所有者 */
    private Owner owner;

    /** 表明待多段上传的对象存储类型 */
    private String storageClass;

    /** Multipart Upload任务的初始化时间 */
    private Date initiated;

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

    @XmlElement(name = "Initiator")
    public Initiator getInitiator() {
        return initiator;
    }

    public void setInitiator(Initiator initiator) {
        this.initiator = initiator;
    }

    @XmlElement(name = "Owner")
    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @XmlElement(name = "StorageClass")
    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    @XmlElement(name = "Initiated")
    public Date getInitiated() {
        return (Date) initiated.clone();
    }

    public void setInitiated(Date initiated) {
        this.initiated = (Date) initiated.clone();
    }

    @Override
    public String toString() {
        return "Upload{"
                + "key='" + key + '\''
                + ", uploadId='" + uploadId + '\''
                + ", initiator=" + initiator
                + ", owner=" + owner
                + ", storageClass='" + storageClass + '\''
                + ", initiated=" + initiated
                + '}';
    }
}
