
package in.fortytwo42.adapter.facade;

import in.fortytwo42.tos.enums.BulkUploadType;

public class BulkUploadTypeResolver {

    public static BaseBulkUpload getBulkUpload(String type) {
        if (type.equalsIgnoreCase(BulkUploadType.USER_ONBOARD.name())) {
            return new UserBulkUpload();
        }
        if (type.equalsIgnoreCase(BulkUploadType.ATTRIBUTE_UPDATE.name())) {
            return new BulkAttributeUpdate();
        }
        if (type.equalsIgnoreCase(BulkUploadType.APPLICATION_ONBOARD.name())) {
            return new ApplicationBulkOnboard();
        }
        if (type.equalsIgnoreCase(BulkUploadType.USER_STATUS_CHANGE.name())) {
            return new BulkEditUserStatus();
        }
        if (type.equalsIgnoreCase(BulkUploadType.USER_APPLICATION_MAPPING.name())) {
            return new BulkUserApplicationMapping();
        }
        if (type.equalsIgnoreCase(BulkUploadType.GATEWAY_SETTING.name())) {
            return new SRAGatewayBulkOnboard();
        }
        if (type.equalsIgnoreCase(BulkUploadType.FALLOUT_PROCESS.name())) {
            return new FallOutProcess();
        }
        return null;
    }
}
