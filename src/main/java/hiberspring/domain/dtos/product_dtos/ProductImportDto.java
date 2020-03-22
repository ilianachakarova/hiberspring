package hiberspring.domain.dtos.product_dtos;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;

@XmlRootElement(name = "product")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductImportDto {
    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "clients")
    private Integer clients;
    @XmlElement(name = "branch")
    private String branch;

    public ProductImportDto() {
    }

    @NotNull


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @NotNull
    public Integer getClients() {
        return clients;
    }

    public void setClients(Integer clients) {
        this.clients = clients;
    }
    @NotNull
    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
