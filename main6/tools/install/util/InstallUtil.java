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

package util;

import installer.InputOption;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class InstallUtil
{
    private static ResourceBundle RESOURCE_NO_UI = ResourceBundle
            .getBundle("data/installNoUI");
    private static ResourceBundle PAGE_PROPERTIES = ResourceBundle
            .getBundle("data/installOrderUI");
    private static ResourceBundle RESOURCE_UI = ResourceBundle
            .getBundle("data/installAmbassador");

    private final static String SETTINGS_FILE_NAME = "installValues.properties";

    private final static String INPUT_SELECT = "input_select";
    private final static String INPUT_FILE_PATH = "input_settings_path";

    private final static String ERROR_SELECT = "error_select";
    private final static String ERROR_FILE_PATH = "error_settings_path";
    private final static String ERROR_SEARCH_FILE_PATH = "error_search_settings_path";
    private final static String SELECT_SETTING_FILE = "select_settings_file";

    public final static String GENERATE_CONFGURATEION_FILE = "copy_configuration";
    public final static String MERGE_PROPERTIES = "merge_properties";
    public final static String CREATE_NT_SERVICE = "create_nt_service";
    public final static String CREATE_DATABASE = "create_database";

    public final static String EXIT_CHAR = "E";
    public final static String NEXT_CHAR = "N";
    public final static String PREVIOUS_CHAR = "P";
    public final static String MAIN_PAGE_CHAR = "W";
    public final static String INSTALL_CHAR = "I";

    public final static int EXIT = 0;
    public final static int MAIN_PAGE = -1;
    public final static int NEXT = -2;
    public final static int PREVIOUS = -3;
    public final static int INSTALL = -4;

    public final static Integer SERCER_SETTINGS = new Integer(1);
    public final static Integer APPLICATION_SETTINGS = new Integer(2);
    public final static Integer DATABASE = new Integer(3);
    public final static Integer LDAP = new Integer(4);
    public final static Integer EMAIL_SETTINGS = new Integer(5);
    public final static Integer OPTIONAL_PACKAGES = new Integer(6);
    public final static Integer SPELL_CHECK_SETTINGS = new Integer(7);
    public final static Integer OPTIONAL_CMS_SETTINGS = new Integer(8);
    public final static Integer INSTALL_OPTIONS = new Integer(9);

    public static List<List<InputOption>> PAGE_DETAIL;
    public static String[] PAGES;
    private static Properties installOptions;

    public static int getPropertiesSize()
    {
        return getAllPageOptions().size();
    }

    public static Properties getInstallOptions()
    {
        if (installOptions == null)
        {
            installOptions = new Properties();
            installOptions.put(GENERATE_CONFGURATEION_FILE, "true");
            installOptions.put(MERGE_PROPERTIES, "false");
            installOptions.put(CREATE_NT_SERVICE, "false");
            installOptions.put(CREATE_DATABASE, "false");
        }

        return installOptions;
    }

    private static List<InputOption> getInstallPageOptions()
    {
        List<InputOption> options = new ArrayList<InputOption>();
        options.add(new InputOption("copy_configuration", InputOption.BOOLEAN));
        options.add(new InputOption("merge_properties", InputOption.BOOLEAN));
        options.add(new InputOption("create_nt_service", InputOption.BOOLEAN));
        options.add(new InputOption("create_database", InputOption.BOOLEAN));

        return options;
    }

    public static List<String> getPages()
    {
        String[] keys = getPageKeys();
        List<String> pages = new ArrayList<String>();
        for (int i = 0; i < keys.length; i++)
        {
            pages.add(RESOURCE_UI.getString(keys[i] + ".title"));
        }

        pages.add(RESOURCE_UI.getString("options_screen.title"));

        return pages;
    }

    private static String[] getPageKeys()
    {
        if (PAGES == null)
        {
            PAGES = readResouse("screen_list");
        }

        return PAGES;
    }

    private static String[] readResouse(String key)
    {
        String value = PAGE_PROPERTIES.getString(key);
        return value.split(",");
    }

    private static List<InputOption> getPage(String pageName)
    {
        String[] properties = readResouse(pageName);
        List<InputOption> page = new ArrayList<InputOption>();
        for (int i = 0; i < properties.length; i++)
        {
            page.add(new InputOption(properties[i]));
        }

        return page;
    }

    private static List<List<InputOption>> getAllPageOptions()
    {
        if (PAGE_DETAIL == null)
        {
            PAGE_DETAIL = new ArrayList<List<InputOption>>();
            String[] pages = getPageKeys();
            for (int i = 0; i < pages.length; i++)
            {
                PAGE_DETAIL.add(getPage(pages[i]));
            }

            PAGE_DETAIL.add(getInstallPageOptions());
        }

        return PAGE_DETAIL;
    }

    public static List<InputOption> getPageOptions(int n)
    {
        return getAllPageOptions().get(n - 1);
    }

    private static String getString()
    {
        BufferedReader input = new BufferedReader(new InputStreamReader(
                System.in));
        String value = "";
        try
        {
            value = input.readLine();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
        return value.trim();
    }

    public static String getInput(InputOption option)
    {
        return getInput(option, "=");
    }

    public static String getInput(InputOption option, String Connector)
    {
        System.out.print(option.getDesplayValue() + " " + Connector + " ");
        String userInput = getString();

        if (userInput.length() == 0 || option.matches(userInput))
        {
            return option.getValue(userInput);
        }

        // input error
        String message = MessageFormat.format(RESOURCE_NO_UI
                .getString(ERROR_SELECT), option.getAcceptString());

        System.out.println(message);
        return getInput(option, Connector);
    }

    public static File getSettingsFile()
    {
        System.out.print(RESOURCE_NO_UI.getString(INPUT_FILE_PATH));
        String userInput = getString();
        userInput = userInput.replace('\\', '/');
        if (userInput.length() > 0)
        {
            File file = new File(userInput);

            if (file.exists())
            {
                List<File> files = getSettingsFile(file);
                if (files.size() == 0)
                {
                    System.out.println(RESOURCE_NO_UI
                            .getString(ERROR_SEARCH_FILE_PATH));
                }
                else
                {
                    if (files.size() > 1)
                    {
                        for (int i = 0; i < files.size(); i++)
                        {
                            File f = (File) files.get(i);
                            System.out.println("    " + (i + 1) + ". "
                                    + f.getAbsolutePath());
                        }

                        int n = -1;

                        while (true)
                        {
                            System.out.println(RESOURCE_NO_UI
                                    .getString(SELECT_SETTING_FILE));
                            String select = getString();
                            if (select.length() == 0)
                            {
                                return null;
                            }

                            try
                            {
                                n = Integer.parseInt(select);
                            }
                            catch (Exception e)
                            {

                            }

                            if (n > 0 && n < files.size() + 1)
                            {
                                return (File) files.get(n - 1);
                            }

                            String message = RESOURCE_NO_UI
                                    .getString(ERROR_SELECT);
                            StringBuffer arguments = new StringBuffer("1");
                            arguments.append(" ~ " + files.size());
                            message = MessageFormat.format(message, arguments
                                    .toString());
                            System.out.println(message);
                        }

                    }

                    return (File) files.get(0);
                }
            }

            System.out.println(RESOURCE_NO_UI.getString(ERROR_FILE_PATH));
            return getSettingsFile();
        }

        return null;
    }

    private static FileFilter SETTING_FILE_FILTER = new FileFilter()
    {

        public boolean accept(File pathname)
        {
            // TODO Auto-generated method stub
            if (pathname.isFile()
                    && pathname.getName().equals(SETTINGS_FILE_NAME)
                    || !pathname.isFile())
            {
                return true;
            }
            return false;
        }
    };

    private static List<File> getSettingsFile(File file)
    {
        List<File> files = new ArrayList<File>();
        if (file.isFile())
        {
            if (file.getName().equals(SETTINGS_FILE_NAME))
            {
                files.add(file);
            }
        }
        else
        {
            File[] fs = file.listFiles(SETTING_FILE_FILTER);
            for (int i = 0; i < fs.length; i++)
            {
                files.addAll(getSettingsFile(fs[i]));
            }
        }

        return files;
    }

    public static int getSelection(int max, List<Action> actions)
    {
        return getSelection(1, max, actions);
    }

    public static int getSelection(int min, int max, List<Action> actions)
    {
        int n = -1;
        List<String> actionKey = new ArrayList<String>();
        String userInput = null;
        while (n < min || n > max)
        {
            if (userInput != null)
            {
                String message = RESOURCE_NO_UI.getString(ERROR_SELECT);
                StringBuffer arguments = new StringBuffer();
                if (min == max)
                {
                    arguments.append(min);
                }
                else if (max > min)
                {
                    arguments.append(min);
                    arguments.append(" ~ " + max);
                }

                for (int i = 0; i < actionKey.size(); i++)
                {
                    if (arguments.length() > 0)
                    {
                        arguments.append(", ");
                    }
                    arguments.append(actionKey.get(i));
                }
                message = MessageFormat.format(message, arguments.toString());
                System.out.println(message);
            }

            System.out.print(RESOURCE_NO_UI.getString(INPUT_SELECT));
            userInput = getString();
            for (int i = 0; i < actions.size(); i++)
            {
                Action action = (Action) actions.get(i);
                if (userInput.equalsIgnoreCase(action.getKey()))
                {
                    return action.getValue();
                }
                if (actionKey.size() < actions.size())
                {
                    actionKey.add(action.getKey());
                }
            }

            try
            {
                n = Integer.parseInt(userInput);
            }
            catch (Exception e)
            {
                // do nothing
            }
        }

        return n;
    }
}
