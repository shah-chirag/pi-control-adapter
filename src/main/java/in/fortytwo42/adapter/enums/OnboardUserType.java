package in.fortytwo42.adapter.enums;

public enum OnboardUserType {
    createAccount("CREATE_ACCOUNT"), createConsumer("CREATE_CONSUMER"), onboardOnCrypto("ONBOARD_CRYPTO"), addAttributesOnCrypto("ADD_ATTRIBUTES_CRYPTO"),
    editAttribute("EDIT_ATTRIBUTE_IDS"), createUser("CREATE_USER"), addAttributes("ADD_ATTRIBUTES_ADAPTER"), onboardUser("ONBOARD_USER_CAM"),
    applicationAutoBinding("APPLICATION_AUTO_BINDING"), cryptoAttributesAndAttributeEditingOnboarder("CRYPTO_ATTRIBUTES_AND_ATTRIBUTE_EDITING_ONBOARDER"), addCredentialsToAccount("ADD_CREDENTIALS_TO_ACCOUNT");

    String onboardType;

    OnboardUserType(String onboardType){
        this.onboardType = onboardType;
    }

    public String getOnboardType() {
        return onboardType;
    }
}
