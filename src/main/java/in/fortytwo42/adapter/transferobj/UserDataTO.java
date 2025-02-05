
package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class UserDataTO {

    private String userIdentifier;

    private List<UserAttributeTO> attributes;

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public List<UserAttributeTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<UserAttributeTO> attributes) {
        this.attributes = attributes;
    }

}
