/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package galign;

import galign.helpers.database.UserSettingsDatabase;
import galign.helpers.threadpool.LiteThreadPool;

import galign.data.Encodings;
import galign.data.Locales;
import galign.data.Project;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

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

    public static final String FILE_NAME_IMAGEICON = "galign.gif";
    public static final String FILE_NAME_HELP = "help/Overview1.htm";
    public static final String FILE_NAME_STYLESHEET = "diplomat.xsl";
    public static final String FILE_NAME_SETTINGS = "galign.properties";

    public static final String CURRENT_DIRECTORY = "current_directory";

    //
    // Members
    //

    private static UserSettingsDatabase s_userSettingsDb;

    private static LiteThreadPool s_liteThreadPool;

    private static ImageIcon s_icon;
    private static File s_helpFile;
    private static String s_stylesheet;
    private static ResourceBundle s_bundle;
    private static ResourceBundle s_errorBundle;

    private static File s_rootDir;
    private static File s_currentDir;

    // Global Application Objects
    public static Project s_project;
    public static DefaultTreeModel s_projectTree;

    /*
    private static Encodings s_encodings;
    private static Locales s_locales;
    */

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

        //Resource Bundle
        Locale locale = new Locale("en", "US");
        s_bundle = ResourceBundle.getBundle("resources/labels", locale);
        s_errorBundle = ResourceBundle.getBundle("resources/errors", locale);
    }

    public static void init2() throws Exception
    {
        initUserSettings();

        //LiteThreadPool
        s_liteThreadPool = new LiteThreadPool();

        /*
        s_encodings = new Encodings();
        s_locales = new Locales();
        */
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

    public static String getString(String key)
    {
        return s_bundle.getString(key);
    }

    public static String getLabel(String key)
    {
        return s_bundle.getString(key) + ":";
    }

    public static String getError(String key)
    {
        return s_errorBundle.getString(key);
    }

    /*
    public static Encodings getEncodings()
    {
        return s_encodings;
    }

    public static Locales getLocales()
    {
        return s_locales;
    }
    */

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

    public static LinkedList getRecentlyUsed()
    {
        return s_userSettingsDb.getRecentlyUsed();
    }

    public static void updateRecentlyUsed(String p_filename)
    {
        s_userSettingsDb.updateRecentlyUsed(p_filename);
    }

}
