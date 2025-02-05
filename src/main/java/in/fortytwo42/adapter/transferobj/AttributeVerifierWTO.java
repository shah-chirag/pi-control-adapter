package in.fortytwo42.adapter.transferobj;


/** Wrapper class for PaginatedTO<in.fortytwo42.enterprise.extension.tos.ThirdPartyVerifierTO> */
public class AttributeVerifierWTO {
    private PaginatedTO<in.fortytwo42.enterprise.extension.tos.ThirdPartyVerifierTO> attributeVerifierTOs;
    public AttributeVerifierWTO(PaginatedTO<in.fortytwo42.enterprise.extension.tos.ThirdPartyVerifierTO> attributeVerifierTOs){
        this.attributeVerifierTOs = attributeVerifierTOs;
    }

    public PaginatedTO<in.fortytwo42.enterprise.extension.tos.ThirdPartyVerifierTO> getAttributeVerifierTOs() {
        return attributeVerifierTOs;
    }

    public void setAttributeVerifierTOs(PaginatedTO<in.fortytwo42.enterprise.extension.tos.ThirdPartyVerifierTO> attributeVerifierTOs) {
        this.attributeVerifierTOs = attributeVerifierTOs;
    }
}
