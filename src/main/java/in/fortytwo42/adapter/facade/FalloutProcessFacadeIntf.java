
package in.fortytwo42.adapter.facade;

import java.io.InputStream;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.enterprise.extension.exceptions.IAMException;
import in.fortytwo42.tos.enums.BulkUploadType;
import in.fortytwo42.tos.transferobj.FalloutConfigTO;
import in.fortytwo42.tos.transferobj.FalloutSyncDataTo;
import in.fortytwo42.tos.transferobj.FalloutTO;

public interface FalloutProcessFacadeIntf {

    void processFallout(String startTime, String endTime);

    String processCSV(String filename, InputStream inputStream);

    void fallOutProcess(String fileName);

    void processFalloutRecord(Session session, FalloutTO falloutRecord) throws AuthException, IAMException;

    CSVUploadTO uploadFalloutProcessCSV(BulkUploadType fileType, InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException;

    String readSampleCsvFile(String fileName);

    void processFalloutRecord(Integer numberOfRecordsToBeProcessed);

    PaginatedTO<FalloutConfigTO> getFalloutconfigs(int pageNo);

    FalloutConfigTO editFalloutConfig(String role, String actor,Long id, boolean saveRequest,FalloutConfigTO falloutConfigTO) throws AuthException;

    void approveEditRequest(Session session, FalloutConfigTO falloutConfigTO, String role, String actor) throws AuthException;

    FalloutSyncDataTo updateFalloutSyncData(FalloutSyncDataTo falloutSyncDataTo) throws AuthException;

    FalloutSyncDataTo createUpdateSyncDataRequest( String role, String actor,Long id, FalloutSyncDataTo falloutSyncDataTo,
                                                   boolean saveRequest)
            throws AuthException;

    public PaginatedTO<FalloutSyncDataTo> getAllFalloutSyncData(int pageNo);

    public PaginatedTO<FalloutTO> getAllFalloutData(int page, int pageSize, String attributeName, String attributeValue , String operations ,String status,Long fromDate,Long toDate);

}
