
package in.fortytwo42.adapter.transferobj;

import java.sql.Timestamp;

public class APILogTO {

    private Long id;

    private String requestReferenceNumber;

    private String remoteAddress;

    private String requestMethod;

    private String requestUrl;

    private String xQuery;

    private String requestData;

    private String requestHeaderData;

    private String responseHeaderData;

    private String responseData;

    private Timestamp requestReceivedOn;

    private Timestamp responseSendOn;

    private int responseCode;

    private String source;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getxQuery() {
        return xQuery;
    }

    public void setxQuery(String xQuery) {
        this.xQuery = xQuery;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getRequestHeaderData() {
        return requestHeaderData;
    }

    public void setRequestHeaderData(String requestHeaderData) {
        this.requestHeaderData = requestHeaderData;
    }

    public String getRequestReferenceNumber() {
        return requestReferenceNumber;
    }

    public void setRequestReferenceNumber(String requestReferenceNumber) {
        this.requestReferenceNumber = requestReferenceNumber;
    }

    public String getResponseHeaderData() {
        return responseHeaderData;
    }

    public void setResponseHeaderData(String responseHeaderData) {
        this.responseHeaderData = responseHeaderData;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public Timestamp getRequestReceivedOn() { return requestReceivedOn; }

    public void setRequestReceivedOn(Timestamp requestReceivedOn) { this.requestReceivedOn = requestReceivedOn; }

    public Timestamp getResponseSendOn() { return responseSendOn; }

    public void setResponseSendOn(Timestamp responseSendOn) { this.responseSendOn = responseSendOn; }

    public int getResponseCode() { return responseCode; }

    public void setResponseCode(int responseCode) { this.responseCode = responseCode; }

    public String getSource() { return source; }

    public void setSource(String source) { this.source = source; }
}
