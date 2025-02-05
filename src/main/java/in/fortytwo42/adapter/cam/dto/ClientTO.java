package in.fortytwo42.adapter.cam.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Client/ application creation request dto class **/

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ClientTO {

    private String clientId;
    private String secret;
    private String rootUrl;
    private String baseUrl;
    private String adminUrl;
    private List<String> redirectUris;
    private String clientName;
    private String clientDescription;
    //public or confidential
    private Boolean publicClient = false;
    private Boolean bearerOnly = false;
    //enable service account
    // To authenticate client using client credential and u can impersonate a client
    private Boolean serviceAccountEnabled = true;
    private Boolean authorizationEnabled;
    private Map<String, String> attributes = new HashMap<>();
    private List<String> webOrigins = new ArrayList<>();

    private Boolean isEnabled = true;
    private Boolean standardFlowEnabled; //should be true
    private Boolean implicitFlowEnabled;
    private Boolean fullScopeAllowed = false;
    private Boolean directAccessGrantEnabled;
}
