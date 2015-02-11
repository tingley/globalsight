package util;

import jodd.util.StringUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

public class WriteLog
{
    private static Logger logger = Logger.getLogger(WriteLog.class);
    private static String currentPath;

    static {
        if (StringUtil.isBlank(currentPath))
            currentPath = FileUtil.getCurrentPath();
    }

    public static void info(String msg) {
        if (logger.isInfoEnabled())
            logger.info(msg);
    }

    public static void debug(String msg) {
        if (logger.isDebugEnabled())
            logger.debug(msg);
    }

    public static void info(String logOrReturnedXML, String msg) {
        PrintStream ps = null;
        if (WebServiceConstants.IS_RETURNED_XML.equals(logOrReturnedXML.toUpperCase())) {
            ps = getPrintStreamForLog(WebServiceConstants.RETURNED_FILE);
        } else {
            ps = getPrintStreamForLog(WebServiceConstants.LOG_FILE);
        }

        java.util.Date date = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(date.getTime());
        ps.println(sqlDate.toString() + " : " + (String) msg);

        ps.flush();
        ps.close();
    }

    //Used for writing log
    public static PrintStream getPrintStreamForLog(String p_filePathName) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(currentPath + "/" + p_filePathName, true);
        }
        catch (IOException ioE) {
            PrintStream ps = WriteLog.getPrintStreamForLog(WebServiceConstants.LOG_FILE);
            ioE.printStackTrace(ps);
        }
        PrintStream ps = new PrintStream(os);

        return ps;
    }

    //Used for getting parameter from properties file
    public static Properties getResourceFile() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(currentPath + "/" + WebServiceConstants.PROPERTIES_FILE));
        }
        catch (FileNotFoundException fnfe) {
            String msg = "getResourceFile() : file not found error.";
            info(WebServiceConstants.IS_LOG, msg);
        }
        catch (IOException ioe) {
            String msg = "getResourceFile() : io exception occurs.";
            info(WebServiceConstants.IS_LOG, msg);
        }
        return props;
    }
}
