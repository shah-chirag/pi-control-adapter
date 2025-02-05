
package in.fortytwo42.adapter.transferobj;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import in.fortytwo42.tos.transferobj.AttributeDataTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
@ToString
public class CryptoTokenTO {

    private List<AttributeTO> searchAttributes;
    private AttributeDataTO attributeData;
    private String cryptoToken;
    private String status;
}
