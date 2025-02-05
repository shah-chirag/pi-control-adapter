
package in.fortytwo42.adapter.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.exception.AuthException;

public class FileDownloader {

    private static String FILE_DOWNLOADER = "<<<<< FileDownloader";

    private static Logger logger= LogManager.getLogger(FileDownloader.class);

    public String downloadCSVStatusFile(String fileName, String role) throws AuthException {
        logger.log(Level.DEBUG, FILE_DOWNLOADER + " downloadUserUpdateStatus : start");
        InputStream inputStream = null;
        try {
            String folderPath = Config.getInstance().getProperty(Constant.CSV_FOLDER_PATH);
            checkErrorFile(folderPath, fileName);
            String filePath = folderPath + fileName;
            File file = new File(filePath);
            inputStream = new FileInputStream(file);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String content = result.toString(StandardCharsets.UTF_8.name());
            file.delete();
            logger.log(Level.DEBUG, FILE_DOWNLOADER + " downloadUserUpdateStatus : end");
            return content;
        }
        catch (FileNotFoundException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        finally {
            closeInputStream(inputStream);
        }
        return null;
    }

    /**
     * Check error file.
     *
     * @param folderPath the folder path
     * @param fileName the file name
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws AuthException the auth exception
     */
    public void checkErrorFile(String folderPath, String fileName) throws IOException, AuthException {
        logger.log(Level.DEBUG, FILE_DOWNLOADER + " checkErrorFile : start");
        File file = new File(folderPath + fileName + "_ERROR.txt");
        if (file.exists()) {
            Scanner sc = new Scanner(file);
            String content = "";
            while (sc.hasNext()) {
                content += sc.nextLine();
            }
            String errorContents[] = content.split(",");
            file.delete();
            logger.log(Level.DEBUG, FILE_DOWNLOADER + " checkErrorFile : end");
            throw new AuthException(null, Long.parseLong(errorContents[0]), errorContents[1]);
        }
    }

    /**
     * Write file.
     *
     * @param fileName the file name
     * @param e the e
     */
    public void writeFile(String fileName, AuthException e) {
        logger.log(Level.DEBUG, FILE_DOWNLOADER + " writeFile : start");
        String folderPath = Config.getInstance().getProperty(Constant.CSV_FOLDER_PATH);
        FileWriter writer = null;
        try {
            String filePath = folderPath + fileName + "_ERROR.txt";
            writer = new FileWriter(filePath);
            writer.write(e.getErrorCode() + "," + e.getMessage());
            writer.close();
        }
        catch (IOException e1) {
            logger.log(Level.ERROR, e1.getMessage(), e1);
        }
        finally {
            closeFileWriter(writer);
        }
        logger.log(Level.DEBUG, FILE_DOWNLOADER + " writeFile : end");
    }

    private void closeInputStream(InputStream inputeStream) {
        if (inputeStream != null) {
            try {
                inputeStream.close();
            }
            catch (IOException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }
    }

    private void closeFileWriter(FileWriter fileWriter) {
        if (fileWriter != null) {
            try {
                fileWriter.close();
            }
            catch (IOException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }
    }

}
