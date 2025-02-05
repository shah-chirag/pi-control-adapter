package in.fortytwo42.adapter.facade;

import java.io.InputStream;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.util.Constant;

public class BulkAttributeUpdate implements BaseBulkUpload{

    private AttributeStoreFacadeIntf attributeFacade = FacadeFactory.getAttributeFacade();

    @Override
    public CSVUploadTO upload(InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException {
        return attributeFacade.uploadAttributes(Constant.CSV_TYPE_ADD_ATTRIBUTE, inputStream, role,id, fileName);
    }

    @Override
    public String getSampleCsv(String fileName) {
        return attributeFacade.readSampleAttributeUpdateCsvFile(fileName);
    }

    @Override
    public String downloadUpdateStatus(String fileName, String role) throws AuthException {
        return attributeFacade.downloadAttributeUpdateStatus(fileName, role);
    }
}
