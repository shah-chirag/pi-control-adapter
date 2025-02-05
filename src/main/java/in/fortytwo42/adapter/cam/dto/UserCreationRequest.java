package in.fortytwo42.adapter.cam.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserCreationRequest {

    private String username;
    private String email;
    private String mobile;
    private String password;
    private List<String> requiredActions;
    private List<CamAttribute> attributes = new ArrayList<>();
    private List<Credential> credentials;
}
