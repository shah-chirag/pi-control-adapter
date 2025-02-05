package in.fortytwo42.adapter.controller;

import in.fortytwo42.adapter.enums.AddAttributeType;

public class AddAttributeFactory {

    private AddAttributeFactory() {
        super();
    }

    /**
     *
     * @param onboardType
     * @return
     */
    public static Onboarder buildOnboarder(AddAttributeType onboardType) {
        switch (onboardType) {
            case addAttributesOnAdapter:
                return AddAttributesAdapterOnboarder.getInstance();
            case addAttributesOnCrypto:
                return CryptoAttributesAndAttributeEditingOnboarder.getInstance();
            case addAttributesOnIds:
                return AddAttributeAccountOnboarder.getInstance();
            case addAttributesOnCAM:
                return CamAttributesOnboarder.getInstance();
            case onboardUserOnCAM:
                return CamUserOnboarder.getInstance();
            case setCredentials:
                return SetCredentialsOnboarder.getInstance();
            case applicationAutoBinding:
                return ApplicationBindingOnboarder.getInstance();
            default:
                return null;
        }
    }
}
