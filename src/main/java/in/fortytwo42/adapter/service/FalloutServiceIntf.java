package in.fortytwo42.adapter.service;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.tos.enums.Status;
import in.fortytwo42.tos.transferobj.CsvFileDataTO;
import in.fortytwo42.tos.transferobj.FalloutSyncDataTo;
import in.fortytwo42.tos.transferobj.FalloutTO;
import org.hibernate.Session;

import java.util.List;

public interface FalloutServiceIntf {

    FalloutTO getFallout(Session session);

    Status updateFallout(Session session, Long id, Status status, String message, Long processingDuration);

    CsvFileDataTO getCsvData(Session session, String csvFileName);

    Status updateCsvDataStatusAndMessage(Session session, Long id, Status status, String message);

    FalloutSyncDataTo updateFalloutSyncData(FalloutSyncDataTo falloutSyncDataTo) throws AuthException;

    List<FalloutSyncDataTo> getFalloutSyncData();

    List<FalloutTO> getAllFalloutDataList(int page, int pageSize, String attributeName, String attributeValue, String operations ,String status,Long fromDate,Long toDate) throws AttributeNotFoundException;

    Long getAllFalloutDataCount(String attributeName, String attributeValue, String operations ,String status,Long fromDate,Long toDate) throws AttributeNotFoundException;
}
