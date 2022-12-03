package obsclient.apache.entity;

import javax.xml.bind.annotation.XmlElement;

public class Owner {
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
        return "Owner{"
                + "id='" + id + '\''
                + '}';
    }
}
