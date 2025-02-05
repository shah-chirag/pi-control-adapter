package in.fortytwo42.adapter.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;

// TODO: Auto-generated Javadoc
/**
 * Utilty class for image processing.
 * @author neebal
 *
 */
public class FileUtil {
    
    /** The feddb connection util. */
    private static String FILE_UTIL_LOG = "<<<<< FileUtil";

	private static Logger logger= LogManager.getLogger(FileUtil.class);
    private static ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();
    
    private static final String SERVER_ID = "ServerId";

	/**
	 * Saves the image to provided destination file path.
	 * @param imageData Base64 encoded image string
	 * @param path Destination file path for image.
	 */
	public static void saveImageToPath(String imageData,String path) {
        logger.log(Level.DEBUG, FILE_UTIL_LOG + " saveImageToPath : start");
        byte[] decodedImage = Base64.getMimeDecoder().decode(imageData);
        File file = new File(path);
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            outputStream.write(decodedImage);
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }finally {
            logger.log(Level.DEBUG, FILE_UTIL_LOG + " saveImageToPath : end");
        }
	}
	
	/**
	 * Save image to path.
	 *
	 * @param decodedImage the decoded image
	 * @param path the path
	 */
	public static void saveImageToPath(byte[] decodedImage,String path) {
        logger.log(Level.DEBUG, FILE_UTIL_LOG + " saveImageToPath : start");
        File file = new File(path);
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            outputStream.write(decodedImage);
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }finally {
            logger.log(Level.DEBUG, FILE_UTIL_LOG + " saveImageToPath : end");
        }
    }
	
	/**
	 * Returns file extension for provided file meta data.
	 * @param metadata File metadata.
	 * @return File extension.
	 */
	public static String getExtension(String metadata) {
        logger.log(Level.DEBUG, FILE_UTIL_LOG + " getExtension : start");
		switch (metadata) {
		case "image/jpeg":
			return ".jpeg";
		case "image/png":
			return ".png";
		default:// should write cases for more file types
			return ".jpg";
		}
	}
	
	/**
	 * Reads the image from the provided path and returns its Base64 encoded string.
	 * @param path File path
	 * @return Base64 encoded image string.
	 */
	public static String getEncodedStringOfFile(String path) {
        logger.log(Level.DEBUG, FILE_UTIL_LOG + " getEncodedStringOfFile : start");
		String base64Image = "";
		File file = new File(path);
		try (FileInputStream imageFile = new FileInputStream(file)) {
			// Reading a Image file from file system
			byte imageData[] = new byte[(int) file.length()];
			imageFile.read(imageData);
			base64Image = Base64.getMimeEncoder().encodeToString(imageData);
		} catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
		} finally {
	        logger.log(Level.DEBUG, FILE_UTIL_LOG + " getEncodedStringOfFile : end");
		}
		return base64Image;
	}
	
	/**
	 * Reads the image from the provided path and returns byte[] of the file.
	 * @param path File path
	 * @return byte[] of the image.
	 */
	public static byte[] getBytesOfFile(String path) {
        logger.log(Level.DEBUG, FILE_UTIL_LOG + " getBytesOfFile : start");
		File file = new File(path);
		try (FileInputStream imageFile = new FileInputStream(file)) {
			byte imageData[] = new byte[(int) file.length()];
			imageFile.read(imageData);
			return imageData;
		} catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
		} finally {
	        logger.log(Level.DEBUG, FILE_UTIL_LOG + " getBytesOfFile : end");
		}
		return null;
	}
	
	/**
	 * Gets the byte array.
	 *
	 * @param inputStream the input stream
	 * @return the byte array
	 */
	public static byte [] getByteArray(InputStream inputStream) {
        logger.log(Level.DEBUG, FILE_UTIL_LOG + " getByteArray : start");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        try {
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        }
        catch (IOException e) {
            System.out.println("IOEXCeption");
            logger.log(Level.ERROR, e.getMessage(), e);
        }finally {
            logger.log(Level.DEBUG, FILE_UTIL_LOG + " getByteArray : end");
        }
        return buffer.toByteArray();
    }
	
	/**
	 * Gets the file extension.
	 *
	 * @param fileName the file name
	 * @return the file extension
	 */
	public static String getFileExtension(String fileName) {
        logger.log(Level.DEBUG, FILE_UTIL_LOG + " getFileExtension : start");
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            logger.log(Level.DEBUG, FILE_UTIL_LOG + " getFileExtension : end");
            return ""; // empty extension
        }
        logger.log(Level.DEBUG, FILE_UTIL_LOG + " getFileExtension : end");
        return fileName.substring(lastIndexOf + 1);
    }
	
	/**
	 * Encode file to base 64 binary.
	 *
	 * @param file the file
	 * @return the string
	 */
	public static String encodeFileToBase64Binary(File file) {
        logger.log(Level.DEBUG, FILE_UTIL_LOG + " encodeFileToBase64Binary : start");
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        }
        catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }finally {
            logger.log(Level.DEBUG, FILE_UTIL_LOG + " encodeFileToBase64Binary : end");
        }
    }
	
	/**
	 * 
	 * @return
	 */
    public static String getSampleUserOnboardCsv(String fileName) {
        logger.log(Level.DEBUG, FILE_UTIL_LOG + " getSampleUserOnboardCsv : start");
        String basePath = System.getenv().get(Constant.ENVIRONMENT_VARIABLE);
        String serverName = System.getProperty(SERVER_ID);
        String finalPath;
        String content = null;
        if (serverName != null && !serverName.isEmpty()) {
            finalPath = basePath + Constant.FILE_SPERATOR + serverName + Constant.FILE_SPERATOR + Constant.RAW_DATA + Constant.FILE_SPERATOR + fileName;
        }
        else {
            finalPath = basePath + Constant.FILE_SPERATOR + Constant.RAW_DATA + Constant.FILE_SPERATOR + fileName;
        }
        try (InputStream inputStream = new FileInputStream(finalPath)) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            content = result.toString(StandardCharsets.UTF_8.name());
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e);
        }
        finally {
            logger.log(Level.DEBUG, FILE_UTIL_LOG + " getSampleUserOnboardCsv : end");
        }
        return content;
    }
    
    public static String readFile(String filePath) throws AuthException {
        logger.log(Level.DEBUG, FILE_UTIL_LOG + " readFile : start");
        try (InputStream configInputStream = new FileInputStream(filePath)) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = configInputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String fileContent = result.toString(StandardCharsets.UTF_8.name());
            result.close();
            configInputStream.close();
            logger.log(Level.DEBUG, FILE_UTIL_LOG + " readFile : end");
            return fileContent;
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e);
            throw new AuthException(e, errorConstant.getERROR_CODE_FILE_READ_FAILED(), e.getMessage());
        }
    }
}
