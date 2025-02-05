
package in.fortytwo42.adapter.util.factory;

import java.io.InputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.FileDownloader;

public class CsvFactory {

    private static final String CSV_FACTORY_LOG = "<<<<< CsvFactory";

    private static Logger logger= LogManager.getLogger(CsvFactory.class);
    private static ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    /**
     * Instantiates a new csv factory.
     */
    private CsvFactory() {
        super();
    }

    public static String processCsv(String fileType, InputStream inputStream, String role,Long id, String fileName) {
        logger.log(Level.DEBUG, CSV_FACTORY_LOG + " processCsv : start");
        try {
            switch (fileType) {
                case Constant.CSV_TYPE_ADD_ATTRIBUTE:
                    return new AddAttributeCsv().processCSV(inputStream, role, null,id, fileName);
                case Constant.CSV_TYPE_EDIT_USER_STATUS:
                    return new EditUserStatusCsv().processCSV(inputStream, role, null,id, fileName);
                case Constant.CSV_TYPE_USER_APPLICATION_MAPPING:
                    return new UserApplicationMappingCsv().processCSV(inputStream, role, null,id, fileName);
                case Constant.FALLOUT_PROCESS:
                    return FallOutProcessCsv.getInstance().processCSV(inputStream, role, null,id, fileName);
                default:
                    throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_TYPE(), errorConstant.getERROR_MESSAGE_INVALID_TYPE());
            }
        }
        catch (AuthException e) {
            new FileDownloader().writeFile(fileName, e);
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        logger.log(Level.DEBUG, CSV_FACTORY_LOG + " processCsv : end");
        return fileName;
    }

    public static String processCsv(String fileType, InputStream inputStream, String role, String accountId,Long id, String fileName) {
        logger.log(Level.DEBUG, CSV_FACTORY_LOG + " processCsv : start");
        try {
            switch (fileType) {
                case Constant.CSV_TYPE_ONBOARD_USERS:
                    return new OnboardUsersCsv().processCSV(inputStream, role, accountId,id, fileName);
                case Constant.CSV_TYPE_ONBOARD_USER:
                    return new OnboardUserCsv().processCSV(inputStream, role, accountId,id, fileName);
                default:
                    throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_TYPE(), errorConstant.getERROR_MESSAGE_INVALID_TYPE());
            }
        }
        catch (AuthException e) {
            new FileDownloader().writeFile(fileName, e);
           logger.log(Level.ERROR, e.getMessage(), e);
        }
        logger.log(Level.DEBUG, CSV_FACTORY_LOG + " processCsvOnboardUsers : end");
        return fileName;
    }
//
//    public static String processCsvEditUsersStatus(String fileType, InputStream inputStream, String role, String fileName) {
//        logger.log(Level.DEBUG, CSV_FACTORY_LOG + " processCsvEditUsersStatus : start");
//        try {
//            switch (fileType) {
//                case Constant.CSV_TYPE_EDIT_USER_STATUS:
//                    return new EditUserStatusCsv().processCSV(inputStream, role,null, null,fileName);
//                default:
//                    throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_TYPE(), errorConstant.getERROR_MESSAGE_INVALID_TYPE());
//            }
//        }
//        catch (AuthException e) {
//            new FileDownloader().writeFile(fileName, e);
//            logger.log(Level.ERROR, e.getMessage(), e);
//        }
//        logger.log(Level.DEBUG, CSV_FACTORY_LOG + " processCsv : end");
//        logger.log(Level.DEBUG, CSV_FACTORY_LOG + " processCsvEditUsersStatus : end");
//        return fileName;
//    }
//
//    public static String processCsvUserApplicationMapping(String fileType, InputStream inputStream, String role, String fileName) {
//        logger.log(Level.DEBUG, CSV_FACTORY_LOG + " processCsvUserApplicationMapping : start");
//        try {
//            switch (fileType) {
//                case Constant.CSV_TYPE_USER_APPLICATION_MAPPING:
//                    return new UserApplicationMappingCsv().processCSV(inputStream, role,null,null, fileName);
//                default:
//                    throw new AuthException(null, errorConstant.getERROR_CODE_INVALID_TYPE(), errorConstant.getERROR_MESSAGE_INVALID_TYPE());
//            }
//        }
//        catch (AuthException e) {
//            new FileDownloader().writeFile(fileName, e);
//            logger.log(Level.ERROR, e.getMessage(), e);
//        }
//        logger.log(Level.DEBUG, CSV_FACTORY_LOG + " processCsvUserApplicationMapping : end");
//        return fileName;
//    }
}
