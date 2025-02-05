package in.fortytwo42.adapter.controller;

import in.fortytwo42.adapter.enums.OnboardUserType;

public class OnboardUserFactory {

    private OnboardUserFactory() {
        super();
    }

    /**
     *
     * @param onboardType
     * @return
     */
    public static Onboarder buildOnboarder(OnboardUserType onboardType) {
        switch (onboardType) {
            case createAccount:
                return AccountOnboarder.getInstance();
            case createConsumer:
                return ConsumerOnboarder.getInstance();
            case onboardOnCrypto:
                return CryptoOnboarder.getInstance();
            case createUser:
                return UserCreatingOnboarder.getInstance();
            case addAttributes:
                return UserAttributesOnboarder.getInstance();
            case cryptoAttributesAndAttributeEditingOnboarder:
                return CryptoAttributesAndAttributeEditingOnboarder.getInstance();
            case onboardUser:
                return CamUserOnboarder.getInstance();
            case applicationAutoBinding:
                return ApplicationBindingOnboarder.getInstance();
            case addCredentialsToAccount:
                return AddCredentialsToAccountOnboarder.getInstance();
            default:
                return null;
        }
    }
}
