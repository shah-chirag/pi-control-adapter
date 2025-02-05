package in.fortytwo42.adapter.facade;

import java.io.InputStream;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;

public interface BaseBulkUpload {

    CSVUploadTO upload(InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException;

    String getSampleCsv(String fileName);

    String downloadUpdateStatus(String fileName, String role) throws AuthException;

}
