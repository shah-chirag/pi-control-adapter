package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import in.fortytwo42.tos.transferobj.AttributeDataTO;
import lombok.ToString;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@ToString
public class UserIciciStatusTO {

    private Long id;
    private List<AttributeDataTO> searchAttributes;
    private String status;
    private Integer statusCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<AttributeDataTO> getSearchAttributes() {
        return searchAttributes;
    }

    public void setSearchAttributes(List<AttributeDataTO> searchAttributes) {
        this.searchAttributes = searchAttributes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
}
