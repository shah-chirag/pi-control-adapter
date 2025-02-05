package in.fortytwo42.adapter.cam.dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {
    @Override
    public String toString() {
        return "UserResponseDto{" +
                "userKcId='" + userKcId + '\'' +
                ", username='" + username + '\'' +
                ", attributes=" + attributes +
                '}';
    }

    private String userKcId;
    private String username;
    private Map<String, List<String>> attributes;
}
