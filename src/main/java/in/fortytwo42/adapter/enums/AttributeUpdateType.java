package in.fortytwo42.adapter.enums;

public enum AttributeUpdateType {
    updateFromadAdapter("UPDATE_FROM_ADAPTER"), UpdateFromIds("UPDATE_FROM_IDS"), updateFromCam("UPDATE_FROM_CAM");

    final String attributeDeleteType;

    AttributeUpdateType(String attributeDeleteType) {
        this.attributeDeleteType = attributeDeleteType;
    }
}
