package in.fortytwo42.adapter.util.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.facade.FacadeFactory;
import in.fortytwo42.adapter.facade.UserFacadeIntf;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.AuditLogUtil;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.PermissionUtil;
import in.fortytwo42.integration.enums.ActionType;
import in.fortytwo42.integration.enums.IdType;
import in.fortytwo42.tos.transferobj.AttributeDataTO;
import in.fortytwo42.tos.transferobj.UserTO;

public class EditUserStatusCsv extends BaseCsv{

    private String EDIT_USER_STATUS_CSV_LOG = "<<<<< EditUsersStatusCsv";

    private UserFacadeIntf userFacade = FacadeFactory.getUserFacade();
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    private static Logger logger= LogManager.getLogger(EditUserStatusCsv.class);

    private static final class InstanceHolder {
        private static final EditUserStatusCsv INSTANCE = new EditUserStatusCsv();

        private InstanceHolder() {
            super();
        }
    }

    public static EditUserStatusCsv getInstance() {
        return EditUserStatusCsv.InstanceHolder.INSTANCE;
    }

    /**
     * Instantiates a new base csv.
     */
    protected EditUserStatusCsv() {
        super(PermissionUtil.EDIT_USER_STATUS);
    }

    @Override
    protected void parseCSVandUpdateData(String[] record, String accountId, Session session, String fileName) {
        logger.log(Level.DEBUG, EDIT_USER_STATUS_CSV_LOG + " parseCSVandUpdateData : start");

        String attributeName = record[0].trim();
        String attributeValue = record[1].trim();
        String twoFactorStatus = record[2].trim();
        String userStatus = record[3].trim();
        String iamUserStatus = record[4].trim();
        String userCredential = record[5].trim();

        logger.log(Level.DEBUG, "attributeName : "+attributeName);
        logger.log(Level.DEBUG, "attributeValue : "+attributeValue);
        logger.log(Level.DEBUG, "twoFactorStatus : "+twoFactorStatus);
        logger.log(Level.DEBUG, "userStatus : "+userStatus);
        logger.log(Level.DEBUG, "iamUserStatus : "+iamUserStatus);
        logger.log(Level.DEBUG, "userCredential : "+userCredential);

        String responseComments = null;
        String requestId = null;

        try{
            UserTO userTO = new UserTO();
            List<AttributeDataTO> searchAttributeList = new ArrayList<>();
            AttributeDataTO searchAttribute = new AttributeDataTO();
            searchAttribute.setAttributeName(attributeName);
            searchAttribute.setAttributeValue(attributeValue);
            searchAttributeList.add(searchAttribute);
            userTO.setSearchAttributes(searchAttributeList);
            userTO.setUserStatus(userStatus);
            userTO.setIamUserStatus(iamUserStatus);
            userTO.setTwoFactorStatus(twoFactorStatus);
            if(!userCredential.isEmpty()) {
                userTO.setUserCredential(userCredential);
            }
            userTO = userFacade.editUser(userTO);
            AuditLogUtil.sendAuditLog("User " + attributeValue + " edited successfully", "ENTERPRISE", ActionType.AUTHENTICATION, "", IdType.ACCOUNT, "", "", userTO.getAccountId(), null);
            //requestId = userTO.getId().toString();
        }
        catch (AuthException e){
            logger.log(Level.ERROR, e.getMessage());
            responseComments = e.getMessage();
        }
        catch (IllegalArgumentException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            responseComments = errorConstant.getERROR_MESSAGE_INVALID_VALUE();
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            responseComments = errorConstant.getERROR_MESSAGE_INVALID_DATA();
        }
        String status = null;
        if (responseComments == null) {
            status = Constant.SUCCESS_STATUS;
            responseComments = Constant.SUCCESS_COMMENT;
        }
        else {
            status = Constant.FAILURE_STATUS;
        }
        String updatedRecord[] = { attributeName, attributeValue, twoFactorStatus, userStatus, iamUserStatus ,userCredential, status, responseComments };
        writer.writeNext(updatedRecord);
        logger.log(Level.DEBUG, EDIT_USER_STATUS_CSV_LOG + " parseCSVandUpdateData : end");
    }

    @Override
    protected List<String> getHeaderList() {
        logger.log(Level.DEBUG, EDIT_USER_STATUS_CSV_LOG + " getHeaderList : start");
        List<String> headerList = new ArrayList<>();
        headerList.add(Constant.CSV_ATTRIBUTE_NAME);
        headerList.add(Constant.CSV_ATTRIBUTE_VALUE);
        headerList.add(Constant.CSV_TWO_FACTOR_STATUS);
        headerList.add(Constant.CSV_USER_STATUS);
        headerList.add(Constant.CSV_IAM_USER_STATUS);
        headerList.add(Constant.USER_CREDENTIAL_HEADER);
        logger.log(Level.DEBUG, EDIT_USER_STATUS_CSV_LOG + " getHeaderList : end");
        return headerList;
    }
}
