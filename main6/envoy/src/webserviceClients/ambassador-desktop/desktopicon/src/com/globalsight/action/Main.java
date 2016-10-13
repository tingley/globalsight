package com.globalsight.action;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;

import org.apache.log4j.Logger;

import com.globalsight.ui.DependenceMsgDialog;
import com.globalsight.ui.MainFrame;
import com.globalsight.ui.UserOptionsPanel;
import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.Constants;
import com.globalsight.util.UsefulTools;
import com.globalsight.util2.ConfigureHelperV2;

public class Main
{

    static Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args)
    {
        String status = null;
        int style = Constants.FAILURE;

        try
        {
            long time = 0;
            try
            {
                time = Integer.parseInt(ConfigureHelperV2.readRuncount());
            }
            catch (NumberFormatException e1)
            {
                // do nothing
            }
            if (time < Long.MAX_VALUE)
            {
                ConfigureHelperV2.addRuncount();
            }

            // start for debug
            File project_root = new File(
                    "D:\\Ambassador\\workspace\\transware-desktopicon");
            File user_dir = new File(System.getProperty("user.dir"));
            if (project_root.equals(user_dir)
                    && System.getProperty("user.name").toLowerCase()
                            .indexOf("quincy") != -1)
            {
                // args = new String[] { "D:\\Temp\\1.txt" };
            }
            // end for debug

            // log some information at first running
            if (time == 0 || log.isDebugEnabled())
            {
                String osName = UsefulTools.getOsName();
                log.info("Running on \"" + osName
                        + "\" with default encoding \""
                        + UsefulTools.getSystemEncoding() + "\"");
                log.info(Constants.APP_FULL_NAME + " is installed in "
                        + Constants.PROGRAM_ROOT);
                log.info("Resource directory is in "
                        + Constants.RESOURCE_DIRECTORY);
            }
            // check dependence
            if (ConfigureHelper.canCheckDependence())
            {
                String result = UsefulTools.checkDependence();
                if (!result.equals(UsefulTools.EMPTY_MSG))
                {
                    log.info(result);
                    int i = 19890401;
                    DependenceMsgDialog msgDialog = new DependenceMsgDialog(
                            null);
                    msgDialog.setMsg(result);
                    msgDialog.setFocusable(true);
                    Dimension screen = Toolkit.getDefaultToolkit()
                            .getScreenSize();
                    Point location = new Point(
                            (screen.height - msgDialog.getHeight()) / 2,
                            (screen.width - msgDialog.getWidth()) / 2);
                    msgDialog.setLocation(location);
                    msgDialog.show();
                }
                if (result.equals(UsefulTools.ERROR_NOSUPPORT_OS))
                {
                    System.exit(1);
                }
            }

            MainFrame mainFrame = new MainFrame(Constants.APP_FULL_NAME + " - "
                    + Constants.APP_VERSION);
            mainFrame.setVisible(true);

            int len = (args == null) ? 0 : args.length;
            if (len > 0)
            {
                File[] files = new File[len];
                for (int i = 0; i < len; i++)
                {
                    files[i] = new File(args[i]);
                }
                mainFrame.addFiles(files);
            }

            // First time to run this Application
            if (ConfigureHelperV2.readDefaultUser() == null)
            {
                status = Constants.MSG_WELCOME_FIRST;
                style = Constants.WELCOME;
                mainFrame.setStatus(status, style);
                mainFrame.getCloseTabbedPanel().add(
                        mainFrame.getCloseTabbedPanel().getTabCount(),
                        Constants.USER_CONFIGURE_TITLE, new UserOptionsPanel());
            }
            else
            {
                mainFrame.logon();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
    }
}
