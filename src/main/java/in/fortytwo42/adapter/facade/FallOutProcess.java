package in.fortytwo42.adapter.facade;

import java.io.InputStream;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.tos.enums.BulkUploadType;

public class FallOutProcess implements BaseBulkUpload {
    private FalloutProcessFacadeIntf falloutProcessFacadeIntf = FacadeFactory.getFalloutFacade();
    
    @Override
    public CSVUploadTO upload(InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException {
        return falloutProcessFacadeIntf.uploadFalloutProcessCSV(BulkUploadType.FALLOUT_PROCESS, inputStream, role,username,id, fileName);
    }

    @Override
    public String getSampleCsv(String fileName) {
        return falloutProcessFacadeIntf.readSampleCsvFile(fileName);
    }

    @Override
    public String downloadUpdateStatus(String fileName, String role) throws AuthException {
        return null;
    }
}
