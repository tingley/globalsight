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

package com.globalsight.everest.util.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import com.globalsight.util.system.ConfigException;

/**
 * A SystemConfiguration object that holds information from property files only.
 * It will also check the last modified time of the property file and if it has
 * changed, re-read the set of properties from that file.
 */
public class DynamicPropertiesSystemConfiguration extends SystemConfiguration
{
    private File m_propertyFile;

    private Properties m_properties = null;

    private long m_lastModTime = -1;
    
    private static String LIB_DIR = "/lib/classes";
    
    DynamicPropertiesSystemConfiguration(String p_propertyFileName)
            throws ConfigException
    {
        super();
        m_propertyFile = new File(getPropertyFilePath(p_propertyFileName));
        loadProperties();
    }
    
    /**
     * Constructor for unit test only!
     */
    DynamicPropertiesSystemConfiguration() {}

    /**
     * Get the specified parameter and return it as a String.
     * 
     * @param p_paramName
     *            Name of the parameter to get value for.
     * @return Value of the specified parameter.
     * @exception com.globalsight.util.system.ConfigException
     */
    public String getStringParameter(String p_paramName) throws ConfigException
    {
        checkProperties();
        String s = (String) m_properties.get(p_paramName);
        return s;
    }

    /**
     * For parameters from file system, no parameter has company id.
     */
    public String getStringParameter(String p_paramName, String p_companyId)
            throws ConfigException
    {
        return null;
    }

    /**
     * Gets the Property file path name as a System Resource
     * 
     * @param propertyFile
     *            basename of the property file
     * @throws ConfigException
     * @return String -- propety file path name
     */
    private static String getPropertyFilePath(String p_propertyFile)
            throws ConfigException
    {
        URL url = DynamicPropertiesSystemConfiguration.class
                .getResource(p_propertyFile);

        if (url == null)
        {
            p_propertyFile = LIB_DIR + p_propertyFile;
            url = DynamicPropertiesSystemConfiguration.class
                    .getResource(p_propertyFile);
        }
        
        if (url == null)
        {
            throw new ConfigException(
                    ConfigException.EX_MISSING_RESOURCE_EXCEPTION,
                    new FileNotFoundException("Property file " + p_propertyFile
                            + " not found"));
        }
        String path = null;
        try
        {
            path = url.toURI().getPath();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        
        return path;
    }

    /**
     * Checks to see if the property file has changed and if it has, re-reads
     * the property file
     * 
     * @exception ConfigException
     */
    private void checkProperties() throws ConfigException
    {
        if (m_propertyFile.lastModified() > m_lastModTime
                || m_properties == null)
        {
            loadProperties();
        }
    }

    /**
     * Loads the properties from the named file
     * 
     * @exception ConfigException
     */
    private synchronized void loadProperties() throws ConfigException
    {
        // check again to prevent accidentally loading twice
        // now that we're actually in a synchronized block
        if (m_propertyFile.lastModified() > m_lastModTime)
        {
            try
            {
                FileInputStream fis = new FileInputStream(m_propertyFile);
                m_properties = new Properties();
                m_properties.load(fis);
                fis.close();
                m_lastModTime = m_propertyFile.lastModified();
            }
            catch (Exception e)
            {
                throw new ConfigException(ConfigException.EX_PROPERTIES, e);
            }
        }
    }

    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        return getClass().getName() + " " + m_properties.toString();
    }

    /**
     * @return the properties in file For issue "Properties files separation by
     *         Company"
     * @throws ConfigException
     */
    public Properties getProperties() throws ConfigException
    {
        checkProperties();
        return m_properties;
    }

    public String getPlaceHolders()
    {
        StringBuffer result =  new StringBuffer();
        if (m_propertyFile.lastModified() >= m_lastModTime)
        {
            BufferedReader br = null;
            try
            {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(m_propertyFile)));
                String s = null;
                boolean isExtracted = false;
                
                while(( s = br.readLine()) != null){
                    if(isExtracted)
                    {
                        if(!s.trim().startsWith("#")){
                            result.append(s);
                            result.append("\n");
                            continue;
                        }else{
                            isExtracted = false;
                        }
                    }
                    if(!s.trim().startsWith("#"))
                    {
                        if(s.indexOf("wordcounter_count_placeholders") != -1){
                            isExtracted = true;
                            String re = s.substring(s.indexOf("wordcounter_count_placeholders") + "wordcounter_count_placeholders=".length());
                            result.append(re);
                            result.append("\n");
                        }
                    }else{
                        isExtracted = false;
                    }
                }
            }
            catch (Exception e)
            {
                throw new ConfigException(ConfigException.EX_PROPERTIES, e);
            }
            finally
            {
                if(br != null)
                {
                    try
                    {
                        br.close();
                    }
                    catch (IOException e)
                    {
                        throw new ConfigException(ConfigException.EX_PROPERTIES, e);
                    }
                }
            }
        }
        return result.toString();
    }
}
