
package in.fortytwo42.adapter.transferobj;

import in.fortytwo42.enterprise.extension.tos.ContactTO;
import lombok.ToString;

@ToString
public class ContactWeTO {

    private ContactTO contactTO;

    private String state;

    private String comments;
    
    private String username;

    private String status;

    private Long id;

    public String getStatus() {
        return status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContactTO getContactTO() {
        return contactTO;
    }

    public void setContactTO(ContactTO contactTO) {
        this.contactTO = contactTO;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
