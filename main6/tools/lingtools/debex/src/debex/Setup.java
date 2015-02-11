//                              -*- Mode: Java -*-
//
// Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
//

package debex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import debex.data.ExtractorSettings;
import debex.data.FileTypes;
import debex.data.Encodings;
import debex.data.Locales;
import debex.helpers.Extractor;
import debex.helpers.database.UserSettingsDatabase;
import debex.helpers.threadpool.LiteThreadPool;

/**
 * The "Application" singleton that provides object factories and
 * global settings.
 */
public class Setup
{
    //
    // Constants
    //

    public static final boolean DEBUG = true;

    public static final String DIR_NAME_RESOURCES = "resources";

    public static final String FILE_NAME_IMAGEICON = "debex.gif";
    public static final String FILE_NAME_HELP = "Help.html";
    public static final String FILE_NAME_STYLESHEET = "diplomat.xsl";
    public static final String FILE_NAME_SETTINGS = "debex.properties";

    public static final String CURRENT_DIRECTORY = "current_directory";

    //
    // Members
    //

    private static LiteThreadPool s_liteThreadPool;

    private static ImageIcon s_icon;
    private static File s_helpFile;
    private static String s_stylesheet;

    private static File s_rootDir;
    private static File s_currentDir;

    private static Extractor s_extractor;
    private static ExtractorSettings s_extractorSettings;
    private static FileTypes s_fileTypes;
    private static Encodings s_encodings;
    private static Locales s_locales;

    private static UserSettingsDatabase s_userSettingsDb;

    //
    // Constructor
    //

    private Setup()
    {
    }


    public static void init1() throws Exception
    {
        //Root Dir
        String rootPath = new File(".").getAbsolutePath();
        rootPath = rootPath.substring(0, rootPath.length() - 1);
        s_rootDir = new File(rootPath, DIR_NAME_RESOURCES);

        //Image Icon
        String imageFile =
            new File(s_rootDir, FILE_NAME_IMAGEICON).getAbsolutePath();
        s_icon = new ImageIcon(imageFile);

        //Help File
        String help = new File(s_rootDir, FILE_NAME_HELP).getAbsolutePath();
        s_helpFile = new File(help);

        //Stylesheet
        s_stylesheet =
            new File(s_rootDir, FILE_NAME_STYLESHEET).getAbsolutePath();
    }

    public static void init2() throws Exception
    {
        initUserSettings();

        //LiteThreadPool
        s_liteThreadPool = new LiteThreadPool();

        //Extractor
        s_extractor = new Extractor();
        s_extractorSettings = new ExtractorSettings();

        s_fileTypes = new FileTypes();
        s_encodings = new Encodings();
        s_locales = new Locales();
    }

    public static void initUserSettings()
        throws IOException
    {
        String userSettingsFile =
            new File(s_rootDir, FILE_NAME_SETTINGS).getAbsolutePath();
        s_userSettingsDb = new UserSettingsDatabase();
        s_userSettingsDb.setFileName(userSettingsFile);

        try
        {
            s_userSettingsDb.load();
        }
        catch (Exception fnfe)
        {
            new File(userSettingsFile).createNewFile();

            s_userSettingsDb.setCurrentDirectory(
                s_rootDir.getAbsolutePath());

            try
            {
                s_userSettingsDb.store();
            }
            catch (Exception e)
            {
                if (DEBUG)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveUserSettings()
    {
        try
        {
            s_userSettingsDb.store();
        }
        catch (Exception e)
        {
            if (DEBUG)
            {
                e.printStackTrace();
            }
        }
    }

    public static void initLF()
    {
        try
        {
            String uiTheme = System.getProperty("ui");

            if (uiTheme == null)
            {
               setDefaultUI();
            }
            else
            {
                UIManager.setLookAndFeel(uiTheme);
            }
        }
        catch (Exception exception)
        {
            if (DEBUG)
            {
                exception.printStackTrace();
            }
        }
    }

    private static void setDefaultUI() throws Exception
    {
        String osName = System.getProperty("os.name");

        if (osName.startsWith("Mac OS") || osName.startsWith("Windows"))
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
    }

    public static LiteThreadPool getLiteThreadPool()
    {
        return s_liteThreadPool;
    }

    public static ImageIcon getIcon()
    {
        return s_icon;
    }

    public static File getHelpFile()
    {
        return s_helpFile;
    }

    public static String getStylesheet()
    {
        return s_stylesheet;
    }

    public static Extractor getExtractor()
    {
        return s_extractor;
    }

    public static ExtractorSettings getExtractorSettings()
    {
        return s_extractorSettings;
    }

    public static FileTypes getFileTypes()
    {
        return s_fileTypes;
    }

    public static Encodings getEncodings()
    {
        return s_encodings;
    }

    public static Locales getLocales()
    {
        return s_locales;
    }

    //
    // User Settings
    //

    public static File getCurrentDirectory()
    {
        return new File(s_userSettingsDb.getCurrentDirectory());
    }

    public static void setCurrentDirectory(File p_cwd)
    {
        s_userSettingsDb.setCurrentDirectory(p_cwd.getAbsolutePath());
    }
}
