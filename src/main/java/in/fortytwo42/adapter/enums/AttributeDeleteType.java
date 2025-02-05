package in.fortytwo42.adapter.enums;

public enum AttributeDeleteType {

   deleteFromadapter("DELETE_FROM_ADAPTER"),deleteFromIds("DELETE_FROM_IDS"),deleteFromCam("DELETE_FROM_CAM") ;

    String attributeDeleteType;
    AttributeDeleteType(String attributeDeleteType){
        this.attributeDeleteType=attributeDeleteType;
    }
}
