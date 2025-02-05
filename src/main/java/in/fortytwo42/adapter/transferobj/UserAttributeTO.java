
package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.tos.transferobj.AttributeDataTO;

@JsonInclude(value = Include.NON_NULL)
public class UserAttributeTO {

    private String attributeName;

    private String attributeType;

    private List<AttributeDataTO> attributeData;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public List<AttributeDataTO> getAttributeData() {
        return attributeData;
    }

    public void setAttributeData(List<AttributeDataTO> attributeData) {
        this.attributeData = attributeData;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserAttributeTO other = (UserAttributeTO) obj;
        if (attributeName == null) {
            if (other.attributeName != null)
                return false;
        }
        else if (!attributeName.equals(other.attributeName))
            return false;
        return true;
    }

}
