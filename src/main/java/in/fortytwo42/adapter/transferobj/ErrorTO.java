
package in.fortytwo42.adapter.transferobj;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * @author ChiragShah
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long errorCode;

    private String humanizedMessage;

    private String developerMessage;

    private String href;

    /**
     * Default
     */
    public ErrorTO() {
        super();
    }

    /**
     * parameterized 
     * @param errorCode
     * @param humanizedMessage
     * @param developerMessage
     */
    public ErrorTO(Long errorCode, String humanizedMessage, String developerMessage) {
        super();
        this.errorCode = errorCode;
        this.humanizedMessage = humanizedMessage;
        this.developerMessage = developerMessage;
    }

    public Long getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Long errorCode) {
        this.errorCode = errorCode;
    }

    public String getHumanizedMessage() {
        return humanizedMessage;
    }

    public void setHumanizedMessage(String humanizedMessage) {
        this.humanizedMessage = humanizedMessage;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public String toString() {
        return "ErrorTO [" +
                (errorCode != null ? ("errorCode=" + errorCode) : "") +
                (humanizedMessage != null ? (", humanizedMessage='" + humanizedMessage + "'") : "") +
                (developerMessage != null ? (", developerMessage='" + developerMessage + "'") : "") +
                (href != null ? (", href='" + href + "'") : "") +
                "]";
    }
}
