package in.fortytwo42.adapter.enums;

public enum AddAttributeType {
    addAttributesOnCrypto("ADD_ATTRIBUTES_CRYPTO"),
    addAttributesOnIds("ADD_ATTRIBUTE_IDS"), addAttributesOnAdapter("ADD_ATTRIBUTES_ADAPTER"), addAttributesOnCAM("ADD_ATTRIBUTE_CAM"), applicationAutoBinding("APPLICATION_AUTO_BINDING"), onboardUserOnCAM("ONBOARD_USER_CAM"), setCredentials("SET_CREDENTIALS");
    String onboardType;

    AddAttributeType(String onboardType) {
        this.onboardType = onboardType;
    }

    public String getOnboardType() {
        return onboardType;
    }
}
