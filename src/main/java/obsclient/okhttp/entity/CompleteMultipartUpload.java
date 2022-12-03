package obsclient.okhttp.entity;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "CompleteMultipartUpload")
public class CompleteMultipartUpload {

    /** 已上传的段集合 */
    private final List<UploadedPart> uploadedPartList;

    /** 构造器 */
    public CompleteMultipartUpload() {
        uploadedPartList = new ArrayList<>();
    }

    @XmlElement(name = "Part")
    public List<UploadedPart> getUploadedPartList() {
        return uploadedPartList;
    }

    /**
     * 添加已上传的段
     * @param uploadedPart 已上传的段
     */
    public void addUploadedPart(UploadedPart uploadedPart) {
        uploadedPartList.add(uploadedPart);
    }
}
