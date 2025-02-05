package in.fortytwo42.adapter.cam.dto;

import java.util.List;

import in.fortytwo42.tos.enums.AttributeAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class EditUserRequest {

    private String userKcId;
    private Boolean enabled;
    private List<CamAttribute> attributes;

    private AttributeAction attributeAction;
}
