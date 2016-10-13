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

package com.globalsight.util;

// Core Java clases
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

/**
 * This class is used to load and store Java properties files from any
 * directory in conjunction with the manifest file.
 *
 * @version 1.0
 * @author Aswin Dinakar, adinakar@globalsight.com
 */

public class PropertiesFactory
    implements PropertiesDefaults
{
    //
    // Public Methods
    //

    /**
     * The name of the appropriate property file needs to be added to
     * the INTERNAL_PROPERTIES string. Need to insert the appropriate
     * precondition. Make sure the name of the properties file being
     * passed is a valid one. Throw an Exception if its not in the
     * dictionary of valid property files
     */
    public void setProperties(Properties p_properties, String p_property_file)
    {
        setInternalProperties(p_properties, p_property_file);
    }

    /**
     * Returns the a Properties object that is loaded them from the
     * specified file.  Need to insert the appropriate
     * precondition. Make sure the name of the properties file being
     * passed is a valid one.  Throw an Exception if its not in the
     * dictionary of valid property files
     *
     * @return java.util.Properties
     * @exception com.globalsight.util.GeneralException
     */
    public Properties getProperties(String p_property_file)
        throws GeneralException
    {
        Properties p;

        try
        {
            p = getInternalProperties(p_property_file);
        }
        catch (IOException ie)
        {
            throw new GeneralException(GeneralExceptionConstants.COMP_GENERAL,
                GeneralExceptionConstants.EX_PROPERTIES,
                "File " + p_property_file + " not found");
        }

        return p;
    }

    //
    // Private Methods
    //

    /**
     * Returns the Properties object, loading them from an internal
     * resource found in the jar achive or in the resource path
     * (classpath).  If no classloader is available it returns null.
     *
     * @return java.util.Properties
     * @exception java.lang.Exception
     */
    private Properties getInternalProperties(final String p_resource)
        throws IOException
    {
        Properties p1 = null;

        try
        {
            p1 = (Properties) AccessController.doPrivileged(
                new PrivilegedAction()
                    {
                        public Object run()
                        {
                            Properties p = loadPropertiesFile(p_resource);
                            return p;
                        }
                    });
        }
        catch (Throwable e)
        {
            System.err.println(e);
            throw new IOException();
        }

        return p1;
    }

    private Properties loadPropertiesFile(final String p_resource)
    {
        Properties p = new Properties();

        try
        {
            p.load(getClass().getResourceAsStream(p_resource));
        }
        catch (IOException ie)
        {
            ie.printStackTrace();
        }

        return p;
    }

    /**
     * Sets the properties storing them into an internal resource
     * found in the jar achive or in the resource path (classpath).
     * If no classloader is available it returns null.
     */
    private void setInternalProperties(final Properties p_properties,
        final String p_resource)
    {
        try
        {
            AccessController.doPrivileged(new PrivilegedAction()
                {
                    public Object run()
                    {
                        storePropertiesFile(p_properties, p_resource);
                        return null;
                    }
                });
        }
        catch (Throwable e)
        {
            System.err.println(e);
        }
    }

    private void storePropertiesFile(final Properties p_properties,
        final String p_resource)
    {
        try
        {
            URL url = getClass().getResource(p_resource);

            String file_path_name = new File(url.toURI().getPath()).getPath();
            FileOutputStream fileOutputStream =
                new FileOutputStream(file_path_name);

            p_properties.store(fileOutputStream, null);

            fileOutputStream.close();
        }
        catch (Exception fe)
        {
            fe.printStackTrace();
        }
    }
}
