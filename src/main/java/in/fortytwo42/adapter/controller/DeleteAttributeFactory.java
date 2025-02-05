package in.fortytwo42.adapter.controller;

import in.fortytwo42.adapter.enums.AttributeDeleteType;

public class DeleteAttributeFactory {
    public DeleteAttributeFactory() {
    }

    public static AttributeValidater buildAttributeDeleter(AttributeDeleteType deleterType) {
        switch (deleterType) {
            case deleteFromadapter:
                return AttributeDeleterFromAdapter.getInstance();
            case deleteFromIds:
                return AttributeDeleterFromIds.getInstance();
            case deleteFromCam:
                return CamAttributeDeleter.getInstance();
            default:
                return null;
        }
    }
}
