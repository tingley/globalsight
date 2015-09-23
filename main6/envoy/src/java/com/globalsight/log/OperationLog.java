package com.globalsight.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * A helper class for logging operation within GlobalSight.  The goal is to
 * allows us to see everything happening at any moment.
 */
public class OperationLog {
    // our normal log
    private static final Logger log = Logger.getLogger(OperationLog.class);
    // the operation log
    private static final Logger operationLog =
        Logger.getLogger(OperationLog.class.getName() + ".entry");

    public static final String EVENT_ADD = "add";
    public static final String EVENT_EDIT = "edit";
    public static final String EVENT_DELETE = "delete";
    public static final String EVENT_TM_IMPORT = "tmImport";
    public static final String EVENT_COMPLETE = "complete";

    public static final String COMPONET_WORKFLOW = "Workflow";
    public static final String COMPONET_TM = "TM";
    public static final String COMPONET_TM_PROFILE = "TM Profile";
    public static final String COMPONET_PROJECT = "Project";
    public static final String COMPONET_L10N_PROFILE = "Localization Profile";
    public static final String COMPONET_FILE_PROFILE = "File Profile";
    public static final String COMPONET_FILTER_CONFIGURATION = "Filter Configuration";
    public static final String COMPONET_SEGMENTATION_RULE = "Segmentation Rule";
    public static final String COMPONET_USERS = "Users";
    public static final String COMPONET_TASK = "Task";
    
    public static void log(String operationUser, String operationAction,
            String gsComponent, String componentName)
    {
        try
        {
			String operationDateStr = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss").format(new Date());
            if (operationDateStr != null && operationUser != null
                    && operationAction != null && gsComponent != null
                    && componentName != null)
            {
                operationLog.info("<" + operationDateStr + ">" + "<"
                        + operationUser + ">" + "<" + operationAction + ">"
                        + "<" + gsComponent + ":\"" + componentName + "\">");
            }
        }
        catch (Exception e)
        {
            log.error("Problem logging start event", e);
        }
    }
}
