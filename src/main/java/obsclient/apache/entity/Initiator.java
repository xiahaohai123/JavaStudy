package obsclient.apache.entity;

import javax.xml.bind.annotation.XmlElement;

public class Initiator {
    /** 创建者的DomainId */
    String id;

    @XmlElement(name = "ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Initiator{"
                + "id='" + id + '\''
                + '}';
    }
}
