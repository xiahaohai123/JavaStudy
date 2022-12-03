package obsclient.apache.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Delete")
public class Delete {

    /** 待删除的对象列表 */
    private List<DeleteObject> deleteObjects = new ArrayList<>();

    @XmlElement(name = "Object")
    public List<DeleteObject> getDeleteObjects() {
        return deleteObjects;
    }

    public void setDeleteObjects(List<DeleteObject> deleteObjects) {
        this.deleteObjects = deleteObjects;
    }

    /** 待删除的对象 */
    public static class DeleteObject {
        /** 对象名 */
        private String key;

        /** 构造器 */
        public DeleteObject() {
        }

        /**
         * 构造器
         * @param key 对象名
         */
        public DeleteObject(String key) {
            this.key = key;
        }

        @XmlElement(name = "Key")
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
