package obsclient.okhttp.entity;

import javax.xml.bind.annotation.XmlElement;

/**
 * 已上传的段
 */
public class UploadedPart {
    /** 段编号 */
    private int partId;

    /** 上传段成功后返回的ETag值，是段内容的唯一标识 */
    private String etag;

    /** 构造器 */
    public UploadedPart() {
    }

    /**
     * 构造器
     * @param partNumber 段编号
     * @param etag       唯一标识
     */
    public UploadedPart(int partNumber, String etag) {
        this.partId = partNumber;
        this.etag = etag;
    }

    @XmlElement(name = "PartNumber")
    public int getPartId() {
        return partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
    }

    @XmlElement(name = "ETag")
    public String getEtag() {
        return etag;
    }

    public void setEtag(String eTag) {
        this.etag = eTag;
    }
}
