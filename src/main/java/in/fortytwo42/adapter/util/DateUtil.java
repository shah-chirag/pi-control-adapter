package in.fortytwo42.adapter.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class DateUtil.
 */
public class DateUtil {
    
    /** The database health checker. */
    private static String DATE_UTIL_LOG = "<<<<< DateUtil";

	private static Logger logger= LogManager.getLogger(DateUtil.class);
    
    /**
     * Gets the formatted date.
     *
     * @param dateValue the date value
     * @return the formatted date
     */
    public static String getFormattedDate(Long dateValue) {
        logger.log(Level.DEBUG, DATE_UTIL_LOG + " getFormattedDate : start");
        if(dateValue != null) {
            logger.log(Level.DEBUG, DATE_UTIL_LOG + " getFormattedDate : end");
            return  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateValue));
        }
        logger.log(Level.DEBUG, DATE_UTIL_LOG + " getFormattedDate : end");
        return null;
    }
    
    /**
     * Gets the timestamp.
     *
     * @param formattedDate the formatted date
     * @return the timestamp
     */
    public static Timestamp getTimestamp(String formattedDate) {
        logger.log(Level.DEBUG, DATE_UTIL_LOG + " getTimestamp : start");
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = dateFormat.parse(formattedDate);
			return new Timestamp(date.getTime());
		} catch (ParseException e) {
			return null;
		}finally {
	        logger.log(Level.DEBUG, DATE_UTIL_LOG + " getTimestamp : end");
		}
    }

    public static Long getEpochTimeFromStringDateLong(String dateString) {

        logger.log(Level.DEBUG, DATE_UTIL_LOG + " getEpochTimeFromStringDateLong : start");
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
            Date date = dateFormat.parse(dateString);
            return date.getTime();
        } catch (ParseException e) {
            return null;
        }
        finally {
            logger.log(Level.DEBUG, DATE_UTIL_LOG + " getTimestamp : end");
        }
    }
}
