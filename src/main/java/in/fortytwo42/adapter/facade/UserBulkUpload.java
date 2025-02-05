package in.fortytwo42.adapter.facade;

import java.io.InputStream;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.util.Constant;

public class UserBulkUpload implements BaseBulkUpload{

    private UserFacadeIntf userFacade = FacadeFactory.getUserFacade();

    @Override
    public CSVUploadTO upload(InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException {
        return userFacade.uploadOnboardUsers(Constant.CSV_TYPE_ONBOARD_USERS, inputStream, role, username,id, fileName);
    }

    @Override
    public String getSampleCsv(String fileName) {
        return userFacade.readSampleCsvFile(fileName);
    }

    @Override
    public String downloadUpdateStatus(String fileName, String role) throws AuthException {
        return userFacade.downloadUpdateUserStatus(fileName, role);
    }
}
