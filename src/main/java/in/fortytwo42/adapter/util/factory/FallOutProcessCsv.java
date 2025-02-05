
package in.fortytwo42.adapter.util.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.daos.dao.CsvFileDataDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.entities.bean.CsvFileData;
import in.fortytwo42.tos.enums.AttributeAction;
import in.fortytwo42.tos.enums.BulkUploadType;
import in.fortytwo42.tos.enums.Status;
import in.fortytwo42.tos.transferobj.FalloutTO;

public class FallOutProcessCsv extends BaseCsv {

    private static Logger logger= LogManager.getLogger(FallOutProcessCsv.class);
    private String FALLOUT_PROCESS_CSV_LOG = "<<<<< FallOutProcessCSV";
    private CsvFileDataDaoIntf csvFileDataDaoIntf = DaoFactory.getCsvFileDataDao();

    private FallOutProcessCsv() {
        super(PermissionUtil.FALLOUT_PROCESS);
    }

    private static final class InstanceHolder {
        private static final FallOutProcessCsv INSTANCE = new FallOutProcessCsv();

        private InstanceHolder() {
            super();
        }
    }

    public static FallOutProcessCsv getInstance() {
        return FallOutProcessCsv.InstanceHolder.INSTANCE;
    }

    @Override
    protected void parseCSVandUpdateData(String[] record, String accountId, Session session, String fileName) {
        String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        requestReferenceNumber = requestReferenceNumber != null ? requestReferenceNumber : UUID.randomUUID().toString();
        ThreadContext.put(Constant.REQUEST_REFERENCE, requestReferenceNumber);
        logger.log(Level.DEBUG, FALLOUT_PROCESS_CSV_LOG + " parseCSVandUpdateData : start");
        String dbts = record[0].trim();
        String bankId = record[1].trim();
        String orgId = record[2].trim();
        String userId = record[3].trim();
        String ft42UserId = record[4].trim();
        String oldMobileNo = record[5].trim();
        String newMobileNo = record[6].trim();
        String operation = record[7].trim().toUpperCase();
        String dehStatus = record[8].trim();
        String ft42Status = record[9].trim();
        String remarks = record[10].trim();
        String freeText1 = record[11].trim();
        String freeText2 = record[12].trim();
        String delFlag = record[13].trim();
        String rModId = record[14].trim();
        String rModTime = record[15].trim();
        String rCreId = record[16].trim();
        String rCreTime = record[17].trim();
        try {
            FalloutTO falloutRecord = new FalloutTO();
            falloutRecord.setDbTs(dbts);
            falloutRecord.setBankId(bankId);
            falloutRecord.setOrgId(orgId);
            falloutRecord.setUserid(userId);
            falloutRecord.setFt42UserId(ft42UserId);
            falloutRecord.setOldMobileNo(oldMobileNo);
            falloutRecord.setNewMobileNo(newMobileNo);
            falloutRecord.setOperation(AttributeAction.valueOf(operation));
            falloutRecord.setDehStatus(dehStatus);
            falloutRecord.setFt42Status(ft42Status);
            falloutRecord.setRemarks(remarks);
            falloutRecord.setFreeText1(freeText1);
            falloutRecord.setFreeText2(freeText2);
            falloutRecord.setDelFlg(delFlag);
            falloutRecord.setrModId(rModId);
            falloutRecord.setrModTime(rModTime);
            falloutRecord.setrCreId(rCreId);
            falloutRecord.setrCreTime(rCreTime);
            falloutRecord.setRequestReferenceNumber(requestReferenceNumber);
            CsvFileData csvFileData = new CsvFileData();
            csvFileData.setRecord(new Gson().toJson(falloutRecord));
            String currentFileName = fileName;
            csvFileData.setFileName(currentFileName);
            csvFileData.setFileType(BulkUploadType.FALLOUT_PROCESS);
            csvFileData.setStatus(Status.PENDING);
            csvFileDataDaoIntf.create(session, csvFileData);
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        finally {
            logger.log(Level.DEBUG, FALLOUT_PROCESS_CSV_LOG + " parseCSVandUpdateData : end");
        }
    }

    @Override
    protected List<String> getHeaderList() {
        logger.log(Level.DEBUG, FALLOUT_PROCESS_CSV_LOG + " getHeaderList : start");
        List<String> headerList = new ArrayList<>();
        headerList.add(Constant.CSV_DB_TS);
        headerList.add(Constant.CSV_BANK_ID);
        headerList.add(Constant.CSV_ORG_ID);
        headerList.add(Constant.CSV_FALLOUT_USER_ID);
        headerList.add(Constant.CSV_FT42_USER_ID);
        headerList.add(Constant.CSV_OLD_MOBILE_NO);
        headerList.add(Constant.CSV_NEW_MOBILE_NO);
        headerList.add(Constant.CSV_OPERATION);
        headerList.add(Constant.CSV_DEH_STATUS);
        headerList.add(Constant.CSV_FT42_STATUS);
        headerList.add(Constant.CSV_REMARKS);
        headerList.add(Constant.CSV_FREE_TEXT1);
        headerList.add(Constant.CSV_FREE_TEXT2);
        headerList.add(Constant.CSV_DEL_FLG);
        headerList.add(Constant.CSV_R_MOD_ID);
        headerList.add(Constant.CSV_R_MOD_TIME);
        headerList.add(Constant.CSV_R_CRE_ID);
        headerList.add(Constant.CSV_R_CRE_TIME);
        logger.log(Level.DEBUG, FALLOUT_PROCESS_CSV_LOG + " getHeaderList : end");
        return headerList;
    }
}
