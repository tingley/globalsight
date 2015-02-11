package com.globalsight.util;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.action.ExecAction;
import com.globalsight.util2.FileUtils;

public class UsefulTools
{
    private static Logger log = Logger.getLogger(UsefulTools.class.getName());

    private static final String ERROR_UNKNOWN = "Unknown error, please contact "
            + Constants.CORM_NAME;

    private static String ERROR_RUBY = "- /usr/bin/ruby";

    private static String ERROR_GEM = "- /usr/bin/gem";

    private static String ERROR_SAFARIWATIR = "- SafariWatir";

    private static String ERROR_FIREFOX = "Unable to find Firefox.app on this machine."
            + "\nPlease make sure: \ninstall Firefox.app into /Applications ";

    public static String EMPTY_MSG = "";

    public static String ERROR_NOSUPPORT_OS = "Do not support your OS.";

    public static String ERROR_SOCKET = "Unable to use this function. \nReason: "
            + "\n - Unable to connect to 'localhost 9997' "
            + "\n\nplease goto $FIREFOX_HOME to strart firefox by \n./firefox-bin -jssh";

    public static boolean isLinux()
    {
        return (getOsName().toLowerCase()).indexOf("linux") != -1;
    }

    public static boolean isWindowsOS()
    {
        return (getOsName().toLowerCase()).indexOf("window") != -1;
    }

    public static boolean isMacOS()
    {
        return (getOsName().toLowerCase()).indexOf("mac") != -1;
    }
    
    public static boolean isWindowsVista()
    {
        String osName = getOsName();
        return "Windows Vista".equalsIgnoreCase(osName)
                || "Windows 7".equalsIgnoreCase(osName)
                || "Windows 2008 Server".equalsIgnoreCase(osName)
                || "Windows 2008 Server R2".equalsIgnoreCase(osName);
    }

    public static String getOsName()
    {
        return System.getProperty("os.name");
    }

    public static String getSystemEncoding()
    {
        return System.getProperty("file.encoding");
    }

    public static String getUserHome()
    {
        return System.getProperty("user.home");
    }

    public static String checkDependence()
    {
        String result = EMPTY_MSG;
        if (isWindowsOS() || isLinux())
        {
            result = notCheck();
        }
        else if (isMacOS())
        {
            result = checkDependenceOnMac();
        }
        else
        {
            result = ERROR_NOSUPPORT_OS;
        }

        return result;
    }

    public static String checkFirefoxWithJSSHRunningOnMac()
    {
        String result = EMPTY_MSG;

        // check firefox
        String firefox_path = "/Applications/Firefox.app";
        File firefox = new File(firefox_path);
        if (!firefox.exists())
        {
            result = ERROR_FIREFOX;
        }
        else
        {
            // check jssh
            try
            {
                Socket socket = new Socket("localhost", 9997);
                socket.close();
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                result = ERROR_SOCKET;
            }
        }

        return result;
    }

    private static String notCheck()
    {
        String result = EMPTY_MSG;

        return result;
    }

    private static String checkDependenceOnMac()
    {
        String result = EMPTY_MSG;
        try
        {
            // check ruby
            String ruby_path = "/usr/bin/ruby";
            if (!(new File(ruby_path)).exists())
            {
                result += "\n" + ERROR_RUBY;
            }
            // check gem
            String gem_path = "/usr/bin/gem";
            if (!(new File(gem_path)).exists())
            {
                result += "\n" + ERROR_GEM;
            }
            // check safariwatir
            String gems_path = "/usr/lib/ruby/gems/1.8/gems";
            File gems = new File(gems_path);
            File[] files = gems.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].getName().toLowerCase().startsWith("safariwatir"))
                {
                    break;
                }
                if (i == files.length - 1)
                {
                    result += "\n" + ERROR_SAFARIWATIR;
                }
            }
            // check firefox
            String firefox_path = "/Applications/Firefox.app";
            File firefox = new File(firefox_path);
            if (!firefox.exists())
            {
                result += "\n- Firefox.app";
            }

            if (!"".equals(result))
            {
                result = "Some software missing, " + result + "\n"
                        + "some functions (view) can't be able to use. ";
            }
        }
        catch (RuntimeException e)
        {
            log.info(e);
            result += "\n" + ERROR_UNKNOWN;
        }

        return result;
    }

    public static void openBrowser(String webSite)
    {
        try
        {
            Runtime.getRuntime().exec("explorer " + webSite);
        }
        catch (Exception e)
        {
            log.warn("Can not go to site: " + webSite, e);
        }
    }

    public static void openFile(File p_file)
    {
        openFile(p_file.getAbsolutePath());
    }

    public static void openFile(String p_fileName)
    {
        ExecAction exe = new ExecAction();
        try
        {
            if (!(new File(p_fileName).exists()))
            {
                throw new Exception(p_fileName + " is not existes. ");
            }
            else if (isWindowsOS())
            {
                if (checkFileSuffix(p_fileName, "txt"))
                    exe.execute(new String[]
                    { "notepad", p_fileName });
                else
                    exe.execute(new String[]
                    { "cmd", "/c", "start", "Opening file...", p_fileName });
            }
            else if (isMacOS())
            {
                exe.execute(new String[]
                { "open", p_fileName });
            }
            else if (isLinux())
            {
                if (new File(p_fileName).isDirectory())
                {
                    exe.execute(new String[]
                    { "nautilus", p_fileName });
                }
                else if (p_fileName.startsWith("http")
                        || checkFileSuffix(p_fileName, "html"))
                {
                    exe.execute(new String[]
                    { "firefox", p_fileName });
                }
                else if (checkFileSuffix(p_fileName, "pdf"))
                {
                    exe.execute(new String[]
                    { "evince", p_fileName });
                }
                else
                {
                    exe.execute(new String[]
                    { "gedit", p_fileName });
                }
            }
        }
        catch (Exception e1)
        {
            log.warn("Can't open file " + p_fileName + " with " + e1);
        }
    }

    public static boolean checkFileSuffix(String p_fileName, String p_suf)
    {
        if (p_fileName.toLowerCase().endsWith(p_suf.toLowerCase()))
            return true;
        else
            return false;
    }

    public static String listToString(List l)
    {
        StringBuffer sb = new StringBuffer("[");
        for (Iterator iter = l.iterator(); iter.hasNext();)
        {
            Object o = iter.next();
            sb.append(o.toString()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        if (sb.length() != 0)
        {
            sb.append("]");
        }

        return sb.toString();
    }

    public static boolean isInTheList(Object p_obj, List p_list)
    {
        for (Iterator iter = p_list.iterator(); iter.hasNext();)
        {
            try
            {
                Object obj = iter.next();
                if (p_obj.equals(obj))
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                continue;
            }
        }

        return false;
    }
    
    /**
     * Get Resource Directory of configure files.
     * If the OS is vis
     * @param p_dir
     * @return
     */
    public static String getResourceDir(String p_dir)
    {
        if (UsefulTools.isWindowsVista())
        {
            return Constants.PROGRAM_ROOT_VISTA + "resource/";
        }

        return p_dir;
    }
    
    /**
     * Gets Root Directory of log file.
     * @param p_dir
     * @return
     */
    public static String getLogfile(String p_dir)
    {
        if (UsefulTools.isWindowsVista())
        {
            String logPath = Constants.PROGRAM_ROOT_VISTA + "log.txt";
            File logFile = FileUtils.createFile(logPath);
            if (!logFile.exists())
            {
                try
                {
                    String data = "Create log file by " + Constants.APP_VERSION;
                    data += " on " + Calendar.getInstance().getTime() + "\r\n";
                    FileUtils.write(logFile, data);
                }
                catch (IOException e)
                {
                }
            }
            return logPath;
        }

        return p_dir + "log.txt";
    }
}
