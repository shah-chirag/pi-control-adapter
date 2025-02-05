package in.fortytwo42.adapter.cam.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Client {

    private String id;
    private String clientId;
    private String clientKcId;
    private String clientSecret;
    private String clientName;
}
