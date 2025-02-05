
package in.fortytwo42.adapter.util.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.util.SessionFactoryUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseCsv.
 */
public abstract class BaseCsv {

    /** The add attribute csv log. */
    private String ADD_ATTRIBUTE_CSV_LOG = "<<<<< AddAttributeCsv";

    private static Logger logger= LogManager.getLogger(BaseCsv.class);
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();

    /** The writer. */
    protected CSVWriter writer = null;

    /** The reader. */
    protected CSVReader reader = null;

    /** The max length. */
    private int maxLength;

    /** The permission. */
    private String permission;

    /**
     * Instantiates a new base csv.
     *
     * @param permission the permission
     */
    protected BaseCsv(String permission) {
        this.permission = permission;
    }

    /**
     * Process CSV.
     *
     * @param inputStream the input stream
     * @param role the role
     * @param fileName the file name
     * @return the string
     * @throws AuthException the auth exception
     */
    public String processCSV(InputStream inputStream, String role, String accountId,Long id, String fileName) throws AuthException {
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " processCSV : start");
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            reader = new CSVReader(bufferedReader);
            StringWriter stringWriter = new StringWriter();
            writer = new CSVWriter(stringWriter, ',');
            String[] record = null;
            boolean isHeaders = true;
            int recordCount = 0;
            List<String> headerList = getHeaderList();
            logger.log(Level.DEBUG, "headerSize "+headerList.size());
            logger.log(Level.DEBUG, "FileName "+fileName);

            this.maxLength = headerList.size();
            Session session = SessionFactoryUtil.getInstance().getSession();
            try {
                while ((record = reader.readNext()) != null) {
                    logger.log(Level.DEBUG, "inside while loop "+isHeaders);
                    if (isHeaders) {
                        validateHeaders(record, headerList);
                        writer.writeNext(getResponseHeaders(headerList));
                        isHeaders = false;
                        continue;
                    }
                    logger.log(Level.DEBUG, "Record length "+record.length);
                    logger.log(Level.DEBUG, "inside while loop "+Arrays.toString(record));

                    if (record.length < maxLength) {
                        skipEmptyRecord(record);
                    }
                    else {
                        logger.log(Level.DEBUG, "***************** Processing Start");
                        String [] recordAndMakerId = new String[record.length+1];
                        for(int i=0;i<recordAndMakerId.length-1;i++){
                            recordAndMakerId[i] = record[i];
                        }
                        recordAndMakerId[record.length] = id+"";
                        parseCSVandUpdateData(recordAndMakerId, accountId, session, fileName);
                        logger.log(Level.DEBUG, "***************** Processing end");
                        recordCount++;
                    }
                }
                SessionFactoryUtil.getInstance().closeSession(session);
            }
            catch (AuthException e) {
                session.getTransaction().rollback();
                logger.log(Level.ERROR, e.getMessage(), e);
                throw new AuthException(e, e.getErrorCode(), e.getMessage());
            }
            finally {
                if (session.isOpen()) {
                    session.close();
                }
            }
            logger.log(Level.INFO, "recordCount  : "+recordCount);
            if (recordCount == 0) {
                throw new AuthException(null, errorConstant.getERROR_CODE_PARSING_CSV(), errorConstant.getERROR_MESSAGE_EMPTY_CSV());
            } 
            String folderPath = Config.getInstance().getProperty(Constant.CSV_FOLDER_PATH);
            logger.log(Level.INFO, "folderPath : "+folderPath);
            createFolderIfNotPresent(folderPath);
            FileWriter fw = new FileWriter(folderPath+ fileName);
            fw.write(stringWriter.toString());
            logger.log(Level.INFO, "SUCESSSSSSSSSSSSSS ");
            fw.close();
            stringWriter.close();

            return stringWriter.toString();
        }
        catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(null, errorConstant.getERROR_CODE_PARSING_CSV(), errorConstant.getERROR_MESSAGE_PARSING_CSV());
        }catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(null, errorConstant.getERROR_CODE_PARSING_CSV(), errorConstant.getERROR_MESSAGE_PARSING_CSV());
        }
        finally {
            closeReader(reader);
            closeWriter(writer);
            logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " processCSV : end");
        }
    }

    /**
     * Validate headers.
     *
     * @param record the record
     * @param headerList the header list
     * @throws AuthException the auth exception
     */
    private void validateHeaders(String[] record, List<String> headerList) throws AuthException {
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " validateHeaders : start" +record.length + " maxlength "+maxLength);
        if (record.length < maxLength) {
            throw new AuthException(null, errorConstant.getERROR_CODE_PARSING_CSV(), errorConstant.getERROR_MESSAGE_PARSING_CSV());
        }
        for (int i = 0; i < maxLength; i++) {
            String header = record[i].trim();
            logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " validateHeaders : header" +header + " headerList"+headerList.get(i));
            if (!header.equalsIgnoreCase(headerList.get(i))) {
                if (i == 0) {
                    header = removeChar(header);
                    logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + "2 validateHeaders : header" +header + " headerList"+headerList.get(i));
                    if (!header.equalsIgnoreCase(headerList.get(i))) {
                        throw new AuthException(null, errorConstant.getERROR_CODE_PARSING_CSV(), errorConstant.getERROR_MESSAGE_PARSING_CSV());
                    }
                    else {
                        continue;
                    }
                }
                throw new AuthException(null, errorConstant.getERROR_CODE_PARSING_CSV(), errorConstant.getERROR_MESSAGE_PARSING_CSV());
            }
        }
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " validateHeaders : end");
    }

    /**
     * Gets the response headers.
     *
     * @param headerList the header list
     * @return the response headers
     */
    private String[] getResponseHeaders(List<String> headerList) {
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " getResponseHeaders : start");
        headerList.add(Constant.STATUS);
        headerList.add(Constant.COMMENTS);
        String[] updatedHeader = new String[headerList.size()];
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " getResponseHeaders : end");
        return headerList.toArray(updatedHeader);
    }

    /**
     * Parses the CS vand update data.
     *
     * @param record
     *         the record
     * @param session
     * @param fileName
     */
    protected abstract void parseCSVandUpdateData(String[] record, String accountId, Session session, String fileName);

    /**
     * Gets the header list.
     *
     * @return the header list
     */
    protected abstract List<String> getHeaderList();

    /**
     * Removes the char.
     *
     * @param userName the user name
     * @return the string
     */
    protected String removeChar(String userName) {
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " removeChar : start");
        char[] chr = userName.toCharArray();
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < chr.length; i++) {
            if (65279 != (int) chr[i]) {
                str.append(chr[i]);
            }
        }
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " removeChar : end");
        return str.toString();
    }

    /**
     * Skip empty record.
     *
     * @param record the record
     * @throws AuthException the auth exception
     */
    protected void skipEmptyRecord(String[] record) throws AuthException {
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " skipEmptyRecord : start");
        for (int i = 0; i < record.length; i++) {
            String value = record[i];
            for (char c : value.toCharArray()) {
                if (c != ' ') {
                    throw new AuthException(null, errorConstant.getERROR_CODE_PARSING_CSV(), errorConstant.getERROR_MESSAGE_PARSING_CSV());
                }
            }
        }
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " skipEmptyRecord : end");
    }

    /**
     * Close writer.
     *
     * @param writer the writer
     */
    private void closeWriter(CSVWriter writer) {
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " closeWriter : start");
        try {
            if (writer != null) {
                writer.close();
            }
        }
        catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }finally {
            logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " closeWriter : end");
        }
    }

    /**
     * Close reader.
     *
     * @param reader the reader
     */
    private void closeReader(CSVReader reader) {
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " closeReader : start");
        try {
            if (reader != null) {
                reader.close();
            }
        }
        catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }finally {
            logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " closeReader : end");
        }
    }

    /**
     * Creates the folder if not present.
     *
     * @param folderPath the folder path
     */
    private void createFolderIfNotPresent(String folderPath) {
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " createFolderIfNotPresent : start");
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            logger.log(Level.INFO, "Not Exists");
//            folder.mkdir();
            createFolderPathRecursively(folder);
        }
        logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " createFolderIfNotPresent : end");
    }

    /**
     * Creates the folder path given.
     *
     * @param folder the folder
     */
    private void createFolderPathRecursively(File folder) {
        if (folder.mkdir()) {
            logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " created folder : " + folder.getPath());
        } else {
            createFolderPathRecursively(folder.getParentFile());
            folder.mkdir();
            logger.log(Level.DEBUG, ADD_ATTRIBUTE_CSV_LOG + " created folder : " + folder.getPath());
        }
    }

}
