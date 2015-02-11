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


package com.globalsight.log;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.LogLog;

import java.util.Properties;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.util.StringTokenizer;



/**
 * Extends {@link PropertyConfigurator} to provide configuration from
 * an external file.  See {@link PropertyConfigurator}.  See <b>{@link
 * PropertyConfigurator#doConfigure(String, Hierarchy)}</b> for the
 * expected format.
 */
public class GlobalSightPropertyConfigurator
    extends PropertyConfigurator
{
    static private boolean c_configured = false;
    static private URL c_configurationFileUrl = null;

    /**
     * GlobalSightPropertyConfigurator constructor.
     */
    public GlobalSightPropertyConfigurator()
    {
        super();
    }

    //
    // PropertyConfigurator Methods
    //

    /**
     * See {@link PropertyConfigurator#doConfigure(String,
     * Hierarchy)}.
     * @param p_configFileName The name of the configuration file
     * where the configuration information is stored in key=value
     * format.
     * @param p_hierarchy The Hierarchy.
     */
    public void doConfigure(String p_configFileName, Hierarchy p_hierarchy)
    {
        // Ensure log4j configured, so don't loose messages.
        defaultConfigure();
        super.doConfigure(p_configFileName, p_hierarchy);
    }

    /**
     * Configure with this property file.  See <b>{@link
     * PropertyConfigurator#doConfigure(String, Hierarchy)}</b> for
     * the expected format.
     * @param p_configFilename The name of the property file in
     * key=value format.
     */
    static public void configure(String p_configFilename)
    {
        // Ensure log4j configured, so don't loose messages.
        defaultConfigure();
        PropertyConfigurator.configure(p_configFilename);
    }

    /**
     * Configure with this property file.  See <b>{@link
     * PropertyConfigurator#doConfigure(String, Hierarchy)}</b> for
     * the expected format.
     * @param p_configURL The URL of the property file.
     */
    public static void configure(java.net.URL p_configURL)
    {
        // Ensure log4j configured, so don't loose messages.
        defaultConfigure();
        PropertyConfigurator.configure(p_configURL);
    }

    /**
     * Read configuration options from <code>p_properties</code>.
     *
     * See <b>{@link PropertyConfigurator#doConfigure(String,
     * Hierarchy)}</b> for the expected format.
     * @param p_properties The Properties to configure with.
     */
    static public void configure(Properties p_properties)
    {
        // Ensure log4j configured, so don't loose messages.
        defaultConfigure();
        PropertyConfigurator.configure(p_properties);
    }

    /**
     * Like {@link PropertyConfigurator#configureAndWatch(String,
     * long)} except that the default delay as defined by {@link
     * FileWatchdog#DEFAULT_DELAY} is used.
     *
     * @param p_configFilename A file in key=value format.
     */
    static public void configureAndWatch(String p_configFilename)
    {
        // Ensure log4j configured, so don't loose messages.
        defaultConfigure();
        PropertyConfigurator.configureAndWatch(p_configFilename);
    }

    /**
     * Read the configuration file <code>p_configFilename</code> if it
     * exists. Moreover, a thread will be created that will periodically
     * check if <code>p_configFilename</code> has been created or
     * modified. The period is determined by the <code>p_delay</code>
     * argument. If a change or file creation is detected, then
     * <code>p_configFilename</code> is read to configure log4j.
     *
     * @param p_configFilename A file in key=value format.
     * @param p_delay The delay in milliseconds to wait between each check.
     */
    static public void configureAndWatch(String p_configFilename, long p_delay)
    {
        // Ensure log4j configured, so don't loose messages.
        defaultConfigure();
        PropertyConfigurator.configureAndWatch(p_configFilename, p_delay);
    }

    /**
     * Read configuration options from <code>p_properties</code>.
     * See {@link PropertyConfigurator#doConfigure(String, Hierarchy)}
     * for the expected format.
     * @param p_properties The Properties to configure with.
     * @param p_hierarchy The Hierarchy.
     */
    public void doConfigure(Properties p_properties, Hierarchy p_hierarchy)
    {
        // Ensure log4j configured, so don't loose messages.
        defaultConfigure();
        super.doConfigure(p_properties, p_hierarchy);
    }

    /**
     * Read configuration options from url <code>p_configURL</code>.
     * See {@link PropertyConfigurator#doConfigure(String, Hierarchy)}
     * for the expected format.
     * @param p_configURL The URL of the property file.
     * @param p_hierarchy The Hierarchy.
     */
    public void doConfigure(java.net.URL p_configURL, Hierarchy p_hierarchy)
    {
        // Ensure log4j configured, so don't loose messages.
        defaultConfigure();
        super.doConfigure(p_configURL, p_hierarchy);
    }

    //
    // BasicConfigurator Methods
    //

    /**
     * Add a {@link FileAppender} that uses {@link PatternLayout} using
     * the {@link PatternLayout#TTCC_CONVERSION_PATTERN} and prints to
     * <code>System.out</code> to the root category.
     */
    static public void configure()
    {
        // Ensure log4j configured, so don't loose messages.
        defaultConfigure();
    }

    /**
     * Reset the configuration to its default.  This removes all
     * appenders from all categories, sets the priority of all
     * non-root categories to <code>null</code>, their additivity flad
     * to <code>true</code> and sets the priority of the root category
     * to {@link Priority#DEBUG DEBUG}.  Moreover, message disabling
     * is set its default "off" value.  <p>This method should be used
     * sparingly and with care as it will block all logging until it
     * is completed.</p>
     */
    public static void resetConfiguration()
    {
        c_configured = false;
        // Ensure log4j configured, so don't loose messages.
        defaultConfigure();
    }

    //
    // Protected Support Methods
    //

    /**
     * Provide a default configuration from properties file
     * log4j.properties in CLASSPATH.  Use configure() if no file.
     */
    protected static void defaultConfigure()
    {
        if (c_configured)
        {
            return;
        }

        c_configured = true;
        String override = null;

        try
        {
            override = System.getProperty(
                LogManager.DEFAULT_INIT_OVERRIDE_KEY, override);
        }
        catch (SecurityException e)
        {
            LogLog.debug("Could not read system property \"" +
                LogManager.DEFAULT_INIT_OVERRIDE_KEY + "\".", e);
        }

        URL url = null;
        String resource = null;

        // if there is no default init override, them get the resource
        // specified by the user or the default config file.
        if (override == null || "false".equalsIgnoreCase(override))
        {
            resource = System.getProperty(LogManager.DEFAULT_CONFIGURATION_KEY,
                LogManager.DEFAULT_CONFIGURATION_FILE);

            try
            {
                url = new URL(resource);
            }
            catch (MalformedURLException ex)
            {
                // so, resource is not a URL:
                // attempt to get the resource in the most generic way:
                url = Logger.class.getResource(resource);
            }

            if (url == null)
            {
                // if that doesn't work, then try again in a slightly
                // different way
                ClassLoader loader = Logger.class.getClassLoader();
                if (loader != null)
                {
                    url = loader.getResource(resource);
                }
            }

            if (url == null)
            {
                String classpath = System.getProperties().getProperty(
                    "java.class.path");
                String resourcePath = whereIsFileInPath(
                    resource, classpath);

                if (resourcePath != null)
                {
                    try
                    {
                        url = new URL(resourcePath);
                    }
                    catch (MalformedURLException ex)
                    {
                        try
                        {
                            File file = new File(resourcePath);
                            url = file.toURL();
                        }
                        catch (MalformedURLException ex2)
                        {
                        }
                    }
                }
            }
        }

        // If we have a non-null url, then delegate the rest of the
        // configuration to the OptionConverter.selectAndConfigure
        // method.
        if (url != null)
        {
            LogLog.debug("Found resource: " + url.toString());

            OptionConverter.selectAndConfigure(url, 
                GlobalSightPropertyConfigurator.class.getName(),
                LogManager.getLoggerRepository());
        }
        else
        {
            LogLog.warn("Could not find resource: [" + resource + "].");

            /*NOT SUPPORTED IN 1.2.8 version*/
            // Provide a default configuration so don't loose messages
            //PropertyConfigurator.configure();
        }
    }


    /**
     * Find where a specified file appears in the path.
     * @param p_fileName file name to find.
     * @returns - first occurance of file in path.
     */
    private static String whereIsFileInPath(String p_fileName,
        String p_path)
    {
        StringTokenizer tokenizer =
            new StringTokenizer(p_path, File.pathSeparator);

        while (tokenizer.hasMoreElements())
        {
            String elementPath = tokenizer.nextToken();

            if (elementPath.endsWith(File.separator))
            {
                elementPath = elementPath.substring(0,
                    elementPath.length() - 1);
            }

            File element = new File(elementPath);

            if (element.isDirectory())
            {
                File file = new File(element, p_fileName);

                if (file.exists())
                {
                    return elementPath + File.separator + p_fileName;
                }
            }
        }

        return null;
    }
}
