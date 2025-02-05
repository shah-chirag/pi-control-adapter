package in.fortytwo42.adapter.util.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.SRAGatewaySettingFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.CsvConstant;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.tos.transferobj.SRAGatewaySettingTO;

public class OnboardSRAGatewaySettingsCsv extends BaseCsv {

    private static final String ADD_SRA_GATEWAY_SETTINGS_CSV_LOG = "<<<<< AddSRAGatewaySettingsCsv";

    private static Logger logger= LogManager.getLogger(OnboardSRAGatewaySettingsCsv.class);
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private final SRAGatewaySettingFacadeIntf SRAGatewaySettingsFacade = FacadeFactory.getSRAGatewaySettingFacade();

    protected OnboardSRAGatewaySettingsCsv() {
        super(PermissionUtil.ONBORAD_ADD_ATTRIBUTE);
    }

    private static final class InstanceHolder {
        private static final OnboardSRAGatewaySettingsCsv INSTANCE = new OnboardSRAGatewaySettingsCsv();

        private InstanceHolder() {
            super();
        }
    }

    public static OnboardSRAGatewaySettingsCsv getInstance() {
        return OnboardSRAGatewaySettingsCsv.InstanceHolder.INSTANCE;
    }

    @Override
    protected void parseCSVandUpdateData(String[] record, String accountId, Session session, String fileName) {

        logger.log(Level.DEBUG, ADD_SRA_GATEWAY_SETTINGS_CSV_LOG + " parseCSVandUpdateData : start");

        String settingName = record[0].trim();
        String address = record[1].trim();
        String port = record[2].trim();
        String proxyPort = record[3].trim();
        String comments = record[4].trim();

        String csvErrorComments = null;
        try {
            SRAGatewaySettingTO sraGatewaySettingTO = new SRAGatewaySettingTO();
            sraGatewaySettingTO.setName(settingName);
            sraGatewaySettingTO.setAddress(address);
            sraGatewaySettingTO.setPort(Integer.parseInt(port));
            sraGatewaySettingTO.setClientProxyPort(Integer.parseInt(proxyPort));
            sraGatewaySettingTO.setComments(comments);
            SRAGatewaySettingsFacade.addSRAGatewaySetting(sraGatewaySettingTO, accountId,null, null, false);
        } catch (AuthException e) {
            csvErrorComments = e.getMessage();
        } catch (IllegalArgumentException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            csvErrorComments = errorConstant.getERROR_MESSAGE_INVALID_VALUE();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            csvErrorComments = errorConstant.getERROR_MESSAGE_INVALID_DATA();
        }

        String status = null;
        if (csvErrorComments == null) {
            status = Constant.SUCCESS_STATUS;
            csvErrorComments = Constant.SUCCESS_COMMENT;
        }
        else {
            status = Constant.FAILURE_STATUS;
        }

        //String requestReferenceNumber = ThreadContext.get(Constant.REQUEST_REFERENCE);
        String[] updatedRecord = {settingName,address,port,proxyPort,comments,status,csvErrorComments};
        writer.writeNext(updatedRecord);
        logger.log(Level.DEBUG, ADD_SRA_GATEWAY_SETTINGS_CSV_LOG + " parseCSVandUpdateData : end");

    }

    @Override
    protected List<String> getHeaderList() {
        logger.log(Level.DEBUG, ADD_SRA_GATEWAY_SETTINGS_CSV_LOG + " getHeaderList : start");
        List<String> headerList = new ArrayList<>();
        headerList.add(CsvConstant.CSV_SETTING_NAME);
        headerList.add(CsvConstant.CSV_ADDRESS);
        headerList.add(CsvConstant.CSV_PORT);
        headerList.add(CsvConstant.CSV_PROXY_PORT);
        headerList.add(CsvConstant.CSV_COMMENTS);
        logger.log(Level.DEBUG, ADD_SRA_GATEWAY_SETTINGS_CSV_LOG + " getHeaderList : end");
        return headerList;
    }
}
