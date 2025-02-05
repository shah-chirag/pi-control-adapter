package in.fortytwo42.adapter.transferobj;

import com.fasterxml.jackson.annotation.JsonInclude;

import in.fortytwo42.adapter.enums.Component;
import in.fortytwo42.adapter.enums.ResponseStatus;
import lombok.ToString;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@ToString
public class CacheTO {
    private Component cacheComponent;
    private ResponseStatus status;

    public Component getCacheComponent() {
        return cacheComponent;
    }

    public void setCacheComponent(Component cacheComponent) {
        this.cacheComponent = cacheComponent;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }
}
