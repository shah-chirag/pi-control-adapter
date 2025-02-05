package in.fortytwo42.adapter.util;

import java.util.HashMap;
import java.util.Map;

import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;

public class TokenStore {

    private final Map<String, Token> tokenMap = new HashMap<>();
    private final Map<String, IAMExtensionV2> iamExtensionV2Map = new HashMap<>();
    private final Map<String, Token> tokenV2Map = new HashMap<>();

    private TokenStore() {
        super();
    }

    private static final class InstanceHolder {
        private static final TokenStore INSTANCE = new TokenStore();

        private InstanceHolder() {
            super();
        }
    }

    public static TokenStore getInstance() {
        return InstanceHolder.INSTANCE;
    }

    
    public void addToken(String applicationId, Token token) {
        tokenMap.put(applicationId,token);
    }
    
    public Token getTokenV2(String applicationId) {
        return tokenV2Map.get(applicationId);
    }
 
    public void addTokenV2(String applicationId, Token token) {
        tokenV2Map.put(applicationId,token);
    }
    
    public IAMExtensionV2 getIAMExtensionV2(String enterpriseId) {
        return iamExtensionV2Map.get(enterpriseId);
    }
    
    public void addIAMExtensionV2(String enterpriseId, IAMExtensionV2 iamExtension) {
        iamExtensionV2Map.put(enterpriseId,iamExtension);
    }
}
