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

package com.globalsight.vignette;

import com.globalsight.diplomat.util.Logger;
import com.globalsight.util.GeneralException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.net.URL;
import java.io.IOException;
import java.util.Properties;

/**
 * Handles getting the Vignette properties
 */
public class VignetteProperties
{
    private static VignetteProperties s_instance = null;
    private static Integer s_instanceToken = new Integer(1);

    //public members
    public String host;
    public String port;
    public String user;
    public String password;
    public String uiURL;
    public String configFile;
    public String configID;
    public boolean auth = false;
    public String tempDir;

    //private members
    private static final String PROP_FILE = "/properties/vignette.properties";

    /**
     * Constructs a VignetteProperties object
     */
    public VignetteProperties() throws Exception
    {
        readVignetteProperties();
    }


    /**
     * Gets a singleton VignetteProperties object.
     * This will throw an exception if the singleton
     * cannot be created.
     * 
     * @return 
     * @exception Exception
     */
    public static VignetteProperties getInstance() throws Exception
    {
	synchronized (s_instanceToken)
	{
	    if (s_instance == null)
		s_instance = new VignetteProperties();
	}
	
	return s_instance;
    }

    /**
    * Gets the property file
    * @throws Exception
    * @return String -- propety file path name
    */
    private File getPropertyFile() throws Exception
    {
        URL url = VignetteProperties.class.getResource(PROP_FILE);
        if (url == null)
            throw new FileNotFoundException("Property file " + PROP_FILE + " not found");
        return new File(url.toURI().getPath());
    }

    /**
     * Reads information from vignette.properties
     */
    private void readVignetteProperties() throws Exception
    {
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(getPropertyFile());
        props.load(fis);
        fis.close();
        host = props.getProperty("host");  //"Lptp-demo2k-01"
        port = props.getProperty("port"); //"30210"
        user = props.getProperty("user"); //admin
        password = props.getProperty("password"); //admin
        uiURL = props.getProperty("uiURL");
        configFile = props.getProperty("configFile");
        configID = props.getProperty("configID");
        tempDir =  props.getProperty("tempDir"); //temp network mapped dir
        Boolean authorization = new Boolean(props.getProperty("auth"));
        auth = authorization.booleanValue();
    }
}

