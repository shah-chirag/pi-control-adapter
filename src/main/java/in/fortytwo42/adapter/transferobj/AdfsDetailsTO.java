package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class AdfsDetailsTO {

    private String email;

    private String mobile;

    private Boolean isEmailDefault;

    private Boolean isMobileDefault;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Boolean getIsEmailDefault() { return isEmailDefault; }

    public void setIsEmailDefault(Boolean emailDefault) { isEmailDefault = emailDefault; }

    public Boolean getIsMobileDefault() { return isMobileDefault; }

    public void setIsMobileDefault(Boolean mobileDefault) { isMobileDefault = mobileDefault; }
}
