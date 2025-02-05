package in.fortytwo42.adapter.transferobj;


import java.util.List;

/** Wrapper class for List<AttributeMetadataTO> */
public class AttributeMetadataWTO {
    private List<AttributeMetadataTO> attributeMetadataTOs;
    public AttributeMetadataWTO(List<AttributeMetadataTO> attributeMetadataTOs){
        this.attributeMetadataTOs = attributeMetadataTOs;
    }

    public List<AttributeMetadataTO> getAttributeMetadataTOs() {
        return attributeMetadataTOs;
    }

    public void setAttributeMetadataTOs(List<AttributeMetadataTO> attributeMetadataTOs) {
        this.attributeMetadataTOs = attributeMetadataTOs;
    }

    @Override
    public String toString() {
        return "AttributeMetadataWTO{" +
                "attributeMetadataTOs=" + attributeMetadataTOs +
                '}';
    }
}
