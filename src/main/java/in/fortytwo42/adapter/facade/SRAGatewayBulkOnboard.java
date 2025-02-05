package in.fortytwo42.adapter.facade;

import java.io.InputStream;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;

public class SRAGatewayBulkOnboard implements BaseBulkUpload {

    SRAGatewaySettingFacadeIntf SRAGatewaySettingFacade = FacadeFactory.getSRAGatewaySettingFacade();
    @Override
    public CSVUploadTO upload(InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException {
        return SRAGatewaySettingFacade.uploadSRAGatewaySettings(inputStream, role, username,id, fileName);
    }

    @Override
    public String getSampleCsv(String fileName) { return SRAGatewaySettingFacade.readSampleCsvFile(fileName); }

    @Override
    public String downloadUpdateStatus(String fileName, String role) throws AuthException {
        return SRAGatewaySettingFacade.downloadUpdateSRAGatewaySettingStatus(fileName,role);
    }
}
