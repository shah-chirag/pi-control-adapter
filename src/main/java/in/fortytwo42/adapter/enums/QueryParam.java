package in.fortytwo42.adapter.enums;


public enum QueryParam {

    CONSUMER_ID("consumerId", DataType.STRING),
    USERNAME("username", DataType.STRING),
    STATUS("status", DataType.STRING),
    SEARCH_QUERY("searchQuery", DataType.STRING),
    TRANSACTION_ID("transactionId", DataType.STRING),
    SIGN_TRANSACTION_ID("signTransactionId", DataType.STRING),
    PAGE("page", DataType.INTEGER),
	IAMSTATUS("iamStatus",DataType.STRING),
	_2FASTATUS("twoFactorStatus",DataType.STRING),
	USER_STATUS("userStatus",DataType.STRING),
	APPLICATION_TYPE("applicationType", DataType.STRING),
	APPLICATION_ID("applicationId", DataType.STRING),
	APPROVAL_STATUS("approvalStatus", DataType.STRING),
    ORDERBY("orderBy",DataType.STRING),
    SORTBY("sortBy",DataType.STRING),
    FROM_DATE("fromDate",DataType.LONG),
    TO_DATE("toDate",DataType.LONG),
    USER_TYPE("userType",DataType.STRING),
    MOBILE_NO("mobileNo",DataType.STRING),
    GROUP_FETCH_TYPE("groupFetchType",DataType.STRING),
    GROUP_STATUS("groupStatus",DataType.STRING),
    ACCESS_TYPE("accessType",DataType.STRING),
    GROUP_NAME("groupName",DataType.STRING),
    REQUEST_TYPE("requestType",DataType.STRING),
    ACTION_TYPE("actionType",DataType.STRING),
    APPLICATION_NAME("applicationName",DataType.STRING),
    SEARCH_TYPE("searchType",DataType.STRING),
    ATTRIBUTE_NAME("attributeName",DataType.STRING),
    ATTRIBUTE_VALUE("attributeValue",DataType.STRING), 
    USER_ID("userId",DataType.STRING),
    PAGE_SIZE("pageSize", DataType.INTEGER),
    USER_IDENTIFIER("userIdentifier",DataType.STRING),
    ACCOUNT_TYPE("accountType",DataType.STRING),
    EVIDENCE_ID("evidenceId",DataType.STRING),    
    LIMIT("limit", DataType.INTEGER),
    OFFSET("offset", DataType.INTEGER),
    ACCOUNT_ID("account_id",DataType.STRING),
    VERIFIER_TYPE("verifier_type",DataType.STRING),
    SRA_APPLICATION_TYPE("SRAApplicationType",DataType.STRING), 
    DEVICE_ID("deviceId", DataType.STRING),
    TOKEN_ID("tokenId", DataType.STRING), 
    GROUP_ID("groupId", DataType.LONG), 
    TYPE("type", DataType.STRING), 
    TUNNEL_HOST("tunnelHost", DataType.STRING),
    CLIENT_IP("clientIp", DataType.STRING),
    TUNNEL_PORT("tunnelPort", DataType.INTEGER),
    CONFIG_FILE("configFile", DataType.STRING),
    ACCOUNTID("accountId", DataType.STRING), 
    FILE_TYPE("fileType", DataType.STRING),
    USER_STATE("userState", DataType.STRING),
    EXPORT("export",DataType.STRING),
    CONFIG_TYPE("configType", DataType.STRING),
    OPERATION("operation",DataType.STRING ),
    USER_ROLE("userRole", DataType.STRING);


    private String key;
    private DataType dataType;

    private QueryParam(String key, DataType dataType) {
        this.key = key;
        this.dataType = dataType;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the dataType
     */
    public DataType getDataType() {
        return dataType;
    }

}
