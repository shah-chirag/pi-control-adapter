
package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.adapter.enums.DeviceMetadata;

@JsonInclude(value = Include.NON_NULL)
public class DeviceDetailsTO {
    
    private DeviceMetadata key;

    private String value;

    public DeviceMetadata getKey() {
        return key;
    }

    public void setKey(DeviceMetadata key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
