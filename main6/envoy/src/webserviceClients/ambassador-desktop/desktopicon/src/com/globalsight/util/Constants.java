package com.globalsight.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * Any useful content here. public static
 */
public class Constants
{
    static Logger log = Logger.getLogger(Constants.class.getName());

    // for this application information
    // old version number for the version check
    public static String APP_VERSION_OLD = "(3.1,8.5)";

    public static String APP_VERSION_STRING = "8.5";

    public static String APP_VERSION = "Version: " + APP_VERSION_STRING;

    public static String APP_NAME = "Desktop Icon";

    public static String CORM_NAME = "GlobalSight";

    public static String APP_FULL_NAME = CORM_NAME + " " + APP_NAME;

    public static String APP_RELEASE_DATE = "May, 2013";

    public static String APP_VERSION_DATE = "Build date: " + APP_RELEASE_DATE;

    public static String TRANSWARE_URL = "http://www.globalsight.com";

    public static String HELP_URL = "http://www.globalsight.com";

    public static String PROGRAM_ROOT = "";
    
    // The Root Directory of log/configure files in Windows Vista
    public static String PROGRAM_ROOT_VISTA = "";

    private static String ENVIRONMENT_SEPRATOR = ":";
    /*
     * for get program path "user.dir" is not enough
     */
    static
    {
        String PACKAGE_ROOT = "com/globalsight/action/";
        URL url = java.lang.ClassLoader.getSystemResource(PACKAGE_ROOT);
        // log.info(url);
        try
        {
            PROGRAM_ROOT = java.net.URLDecoder.decode(url.getPath(),
                    System.getProperty("file.encoding"));
        }
        catch (UnsupportedEncodingException e)
        {
            PROGRAM_ROOT = url.getPath();
        }
        // jar:file:/d:/root/
        // jar:file:/Applications/
        if (PROGRAM_ROOT.startsWith("jar:"))
        {
            PROGRAM_ROOT = PROGRAM_ROOT.substring(4);
        }
        if (PROGRAM_ROOT.startsWith("file:/"))
        {
            if (UsefulTools.isWindowsOS())
            {
                PROGRAM_ROOT = PROGRAM_ROOT.substring(6);
            }
            else if (UsefulTools.isMacOS())
            {
                PROGRAM_ROOT = PROGRAM_ROOT.substring(5);
            }
        }
        if (PROGRAM_ROOT.startsWith("/"))
        {
            if (UsefulTools.isWindowsOS())
            {
                PROGRAM_ROOT = PROGRAM_ROOT.substring(1);
            }
            else if (UsefulTools.isMacOS())
            {
            }
        }

        // log.info(PROGRAM_ROOT);
        File root = new File(PROGRAM_ROOT);
        while (!root.exists())
        {
            root = root.getParentFile();
        }

        // log.info(parent);

        PROGRAM_ROOT = root.getPath().replace('\\', '/') + "/";
        if (PROGRAM_ROOT.indexOf(PACKAGE_ROOT) != -1)
        {
            PROGRAM_ROOT = PROGRAM_ROOT.substring(0,
                    PROGRAM_ROOT.indexOf(PACKAGE_ROOT));
        }
        
        if (UsefulTools.isWindowsVista())
        {
            PROGRAM_ROOT_VISTA = UsefulTools.getUserHome() 
                    + "/AppData/Local/" + CORM_NAME + "/" + APP_FULL_NAME + "/";
        }

        if (UsefulTools.isWindowsOS())
        {
            ENVIRONMENT_SEPRATOR = ";";
        }
    }

    public static String RESOURCE_DIRECTORY = PROGRAM_ROOT + "resource/";

    // public static String RUBYLIB = PROGRAM_PACKAGE + "ruby/Watir";

    public static String GLOBALSIGHT_ICON = RESOURCE_DIRECTORY
            + "image/globalsight.gif";

    public static String AMB_ICON = RESOURCE_DIRECTORY
            + "image/GlobalSight_Icon.jpg";

    public static String CONFIGURE_XML = UsefulTools.getResourceDir(RESOURCE_DIRECTORY)
            + "configure/configure.xml";

    public static String CONFIGURE_XML_FORMER = UsefulTools.getResourceDir(RESOURCE_DIRECTORY)
            + "configure/configure.former.xml";

    public static String CONFIGURE_XML_PROXY = UsefulTools.getResourceDir(RESOURCE_DIRECTORY)
            + "configure/proxy.properties";

    public static String DI_PROPERTIES_FILE = UsefulTools.getResourceDir(RESOURCE_DIRECTORY)
            + "configure/di.properties";

    public static String CVS_CONFIGURE_FILE = UsefulTools.getResourceDir(RESOURCE_DIRECTORY)
            + "configure/cvs_configure.properties";

    // set server url, password, username in Ruby
    public static String RUBY_LIB_WATIR = "RUBYLIB=" + RESOURCE_DIRECTORY
            + "ruby" + ENVIRONMENT_SEPRATOR + RESOURCE_DIRECTORY + "ruby/Watir";

    public static String RUBY_LIB_FIREWATIR = "RUBYLIB=" + RESOURCE_DIRECTORY
            + "ruby" + ENVIRONMENT_SEPRATOR + RESOURCE_DIRECTORY
            + "ruby/firewatir";

    public static String RUBY_PARAMETER = RESOURCE_DIRECTORY
            + "ruby/Parameters.rb";

    public static String RUBY_INPROGRESS_IE = RESOURCE_DIRECTORY
            + "ruby/InProgress_IE.rb";

    public static String RUBY_PENDING_IE = RESOURCE_DIRECTORY
            + "ruby/Pending_IE.rb";

    public static String RUBY_REPORT_IE = RESOURCE_DIRECTORY
            + "ruby/Reports_IE.rb";

    public static String RUBY_INPROGRESS_FF = RESOURCE_DIRECTORY
            + "ruby/InProgress_FF.rb";

    public static String RUBY_PENDING_FF = RESOURCE_DIRECTORY
            + "ruby/Pending_FF.rb";

    public static String RUBY_REPORT_FF = RESOURCE_DIRECTORY
            + "ruby/Reports_FF.rb";

    public static String RUBY_INPROGRESS_SF = RESOURCE_DIRECTORY
            + "ruby/InProgress_SF.rb";

    public static String RUBY_PENDING_SF = RESOURCE_DIRECTORY
            + "ruby/Pending_SF.rb";

    public static String RUBY_REPORT_SF = RESOURCE_DIRECTORY
            + "ruby/Reports_SF.rb";

    public static String RUBY_GOTOURL_SF = RESOURCE_DIRECTORY
            + "ruby/GoToURL_SF.rb";

    public static String HELP_FILE = PROGRAM_ROOT
            + "GlobalSight_DesktopIcon_Userguide.pdf";

    public static String CVS_HELP_FILE = PROGRAM_ROOT
            + "GlobalSight_DesktopIcon_Userguide_Connect_to_CVS.pdf";

    public static String SITE_DI_WIKI = "http://www.globalsight.com/wiki/index.php/Using_the_Desktop_Icon";
    
    public static String LOG_FILE = UsefulTools.getLogfile(PROGRAM_ROOT);
    static
    {
        if (!(new File(LOG_FILE).exists()))
        {
            LOG_FILE = System.getProperty("user.dir") + File.separator
                    + "log.txt";
        }
    }

    // public useful String from properties file
    public static String MSG_WELCOME = "Welcome to " + APP_NAME;

    public static String MSG_WELCOME_FIRST = MSG_WELCOME
            + " | please logon through \"Configure -> User Options\"";

    public static String MSG_SUCCESS_LOGON = "Logon successfully, Welcome to "
            + Constants.APP_NAME;

    public static String MSG_ERROR_TIMEDOUT = "Connection timed out";

    public static String MSG_ERROR_CONNECTION = "Can not connect to GlobalSight";

    public static String MSG_ERROR_CONNECTION_SERVER_VERSION_HIGH = "The server you connected has been out of date for the desktop icon you are using. \n Please logon to a newer server which is synchronized with your desktop icon version.";

    public static String MSG_ERROR_CONNECTION_SERVER_VERSION_LOW = "Your desktop icon is out of date. \n Please upgrade according to the correct version.";

    public static String MSG_ERROR_GETFP = "Can not get file profiles from GlobalSight, please logon again";

    public static String MSG_ERROR_PERMISSION = "Can not logon GlobalSight with permission limited";

    public static String MSG_ERROR_CONFIGURE_USER = "An error occured when logining and saving ";

    public static String MSG_ERROR_USER = "The username or password may be incorrect, please configure";

    public static String MSG_ERROR_REMOTE = "There are something wrong with remote server GlobalSight, please get help from "
            + CORM_NAME;

    public static String MSG_ERROR_HOST_OR_PORT = "Please make sure the host name or port was correctly input";

    public static String ERROR_NO_FILE = "No file is added to this job, please press Add File(s) button or drag and drop file(s) into Desktop Icon";

    public static String ERROR_LOGON = "Invalid user name and password to logon, please configure";

    public static String ERROR_READUSER = "Can not read the user from configure file, please configure";

    public static String ERROR_FIELDS = "Fields colored are invalid";

    public static String ERROR_EMPTY_JOB_STRING = "Please input job name!";

    public static String ERROR_NOT_LOGON = "Please logon first, view \"Configure -> User Options\"";

    public static String ERROR_MAPPING_FP = "File(s) do not mapping file profile correctly";

    public static String ERROR_NO_OPTION_SELECTED = "Please select one option to create job: max file size or max job size";

    public static String ERROR_INVALID_FILE_NUM = "Invalid file number";

    public static String ERROR_INVALID_JOB_SIZE = "Invalid job size";

    public static String ERROR_JOB_NAME = "Invalid job name, only alphabetic, number(0-9), plus(+), hyphen(-),"
            + " underscore(_), periods(.), single quotation(') and spaces can be used";

    public static String ERROR_HOSTNAME = "The host name is not valid";

    public static String ERROR_HOSTPORT = "The host port is not valid";

    public static String ERROR_NOT_INTEGER = "The input is invalid, \nnot a positive number or too long";

    public static String SHOULD_BE_FILE = "Should be a file with extension";

    public static String EMPTY_FILE = "There is no content in this file!";

    public static String ADD_SAME_FILE = "This file is already added in the file list. Add it all the same?";

    public static String ERROR_NOT_SAME_MODULE_PATH = "These selected files do NOT have the same module path.";

    // for mainFrame

    public static String PLEASE_SELECT_FILE = "Select files on the left to get file profiles";

    public static String PLEASE_MAPPING = "File NOT mapped";

    public static String PLEASE_ADD_FILE = "Please add file(s) into this job";

    public static String NO_FP_MSG = "ERROR: No File Profile for this file format";

    // public static String SERVER_ACCOUNT_TITLE = "Server URL and Account";

    public static String CREATE_JOB_TITLE = "Create Job";

    public static String TM_IMPORT_TITLE = "TM Import";

    // public static String DIR_TITLE = "Directory For Download";

    // public static String TIMER_TITLE = "Minutes For Timer";

    public static String USER_CONFIGURE_TITLE = "User Options";

    public static String UPLOADING = "Uploading";

    public static String NOTICE_SIGN = "***** ";

    // status bar type
    public static int FAILURE = 0;

    public static int SUCCESS = 1;

    public static int WELCOME = 2;

    // CVS
    public static String CONNECT_TO_CVS = "Connect to CVS"; // menu

    public static String CONNECT_TO_CVS_PERM = "desktopicon.connect.cvs"; // permission

    // cvs authentication
    public static String CVS_PROTOCOL = "CVS_PROTOCOL";

    public static String CVS_USERNAME = "CVS_USERNAME";

    public static String CVS_PASSWORD = "CVS_PASSWORD";

    // cvs repositories
    public static String CVS_REPOSITORY = "CVS_REPOSITORY";

    public static String CVS_ADD_REPOSITORY = "CVS_ADD_REPOSITORY";

    public static String CVS_EDIT_REPOSITORY = "CVS_EDIT_REPOSITORY";

    // cvs modules
    public static String CVS_MODULE_NAME = "CVS_MODULE_NAME";

    public static String CVS_MODULE_PATH = "CVS_MODULE_PATH";

    public static String CVS_BRANCH_TAG_REVISION = "CVS_BRANCH_TAG_REVISION";

    public static String CVS_PROJECT = "CVS_PROJECT";

    public static String CVS_ADD_MODULE = "CVS_ADD_MODULE";

    public static String CVS_EDIT_MODULE = "CVS_EDIT_MODULE";

    // cvs operation status
    public static int CVS_OPERATION_NOT_BEGIN = -1;

    public static int CVS_OPERATION_DOING = 0;

    public static int CVS_OPERATION_FINISHED = 1;

    // JTree node type
    public static String NODE_TYPE_ROOT = "ROOT";

    public static String NODE_TYPE_REPOSITORY = "REPOSITORY";

    public static String NODE_TYPE_MODULE = "MODULE";

    public static String NODE_TYPE_PATH = "MODULE_PATH";

    public static String NODE_TYPE_FILE = "FILE";

    // CVS command
    public static String CVS_WORK_DIRECTORY = "WORK_DIRECTORY";

    public static String CVS_COMMAND = "COMMAND";

    // job create options
    public static String JOB_CREATE_OPTION_MAX_FILE_NUM = "MAX_FILE_NUM";

    public static String JOB_CREATE_OPTION_MAX_FILE_SIZE = "MAX_FILE_SIZE";

    public static String MSG_SET_REQUIRED_ATTRIBUTE = "Some of the required attributes have not been set.";

    public static String MSG_FAIL_GET_ATTRIBUTE = "Failed to get attributes.";

    public static String MSG_FAIL_GET_PROJECT = "Failed to get project.";

    public static String MSG_NEED_MAP_FILE_FIRST = "Please map files first.";

    public static String MSG_FILE_NAME_REPEAT = "File name can not be repeated.";

    public static String INSTALLCERT_RESTART = "Server certificate for https was installed. You need to restart desktopicon.";

    // TM Import
    public static String TM_CHECK_TMNAME = "Please select a TM Name!";
    public static String TM_CHECK_FILES_EMPTY = "Please add the file for TM Import!";
    public static String TM_CHECK_FILE_TYPE = "Please check the file type.\n\nThe incorrect file is:\n";
    public static String TM_IMPORT_SUSS = "Finished Importing the TM(Translation Memory).\nPlease Check the TM.";
    public static String TM_IMPORT_FAIL = "An error occured  when uploading the file. \n\nPlease check the file:\n";

}
