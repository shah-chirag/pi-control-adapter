package in.fortytwo42.adapter.facade;

import java.io.InputStream;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;

public class ApplicationBulkOnboard implements BaseBulkUpload {

    ApplicationFacadeIntf applicationFacade = FacadeFactory.getApplicationFacade();

    @Override
    public CSVUploadTO upload(InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException {
        return applicationFacade.uploadOnboardApplication(inputStream,role,username,id,fileName);
    }

    @Override
    public String getSampleCsv(String fileName) {
        return applicationFacade.readSampleCsvFile(fileName);
    }

    @Override
    public String downloadUpdateStatus(String fileName, String role) throws AuthException {
        return applicationFacade.downloadUpdateApplicationStatus(fileName,role);
    }
}
