package in.fortytwo42.adapter.cam.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Credential {
    private Boolean temporary;
    private String type;
    private String value;
}
