package in.fortytwo42.adapter.controller;

import in.fortytwo42.adapter.enums.AttributeUpdateType;

public class UpdateAttributeFactory {
    public UpdateAttributeFactory() {
    }

    public static AttributeUpdater buildAttributeUpdater(AttributeUpdateType updateType) {
        switch (updateType) {
            case updateFromadAdapter:
                return AttributeUpdaterFromAdapter.getInstance();
            case UpdateFromIds:
                return AttributeUpdaterFromIds.getInstance();
            case updateFromCam:
                return AttributeUpdaterFromCam.getInstance();
            default:
                return null;
        }
    }
}
