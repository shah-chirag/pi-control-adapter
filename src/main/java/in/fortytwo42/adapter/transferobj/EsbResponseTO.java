package in.fortytwo42.adapter.transferobj;
/**
 * 
 * @author ChiragShah
 *
 */
public class EsbResponseTO {

    String version;
    String statusCode;
    String subStatusCode;
    String statusDesc;
    String IaminboundId;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getSubStatusCode() {
        return subStatusCode;
    }

    public void setSubStatusCode(String subStatusCode) {
        this.subStatusCode = subStatusCode;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getIaminboundId() {
        return IaminboundId;
    }

    public void setIaminboundId(String iaminboundId) {
        IaminboundId = iaminboundId;
    }

}
