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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.vignette.cms.client.beans.*;
import java.util.Properties;

/**
 * Represents a connection to Vignette
 */
public class VignetteConnection
{
    //Public members
    public CMS cms = null;
    
    //Private members
    private String m_host = "Lptp-demo2k-01";
    private String m_port = "30210";
    private String m_user = "admin";
    private String m_passwd = "admin";
    private String m_configFile = "";
    private String m_configID = "";
    private boolean m_auth = false;
    private String m_tempDir = "C:\temp";

    private Logger m_logger = null;

    private static final String PROP_FILE = "/properties/vignette.properties";

    /**
     * Constructs a VignetteConnection
     */
    public VignetteConnection()
    {
        m_logger = Logger.getLogger();
    }


    /**
     * Creates a connection to the CMS
     *first try secure connection then go 
     *with the default
     * 
     * @return true - connection is working
     * @exception Exception
     */
    public void connect() throws Exception
    {
        readVignetteProperties();
        cms = new CMS();
        CMSSecurity sec = new CMSSecurity();
        if (m_auth)
            sec.configure(m_configFile, m_configID);
        int vignettePort = Integer.parseInt(m_port);
        cms.connect(sec, m_host, vignettePort, m_user, m_passwd);
    }


    /**
     * Disconnects from Vignette
     * 
     * @exception Exception
     */
    public void disconnect() throws Exception
    {
        cms.disconnect();
    }


    /**
    * Gets the property file
    * @throws Exception
    * @return String -- propety file path name
    */
    private File getPropertyFile() throws Exception
    {
        URL url = VignetteConnection.class.getResource(PROP_FILE);
        if (url == null)
            throw new FileNotFoundException("Property file " + PROP_FILE + " not found");
        return new File(url.toURI().getPath());
    }


    /**
     * Reads connection information from vignette.properties
     */
    private void readVignetteProperties() throws Exception
    {
        VignetteProperties props = new VignetteProperties();
        m_host = props.host;
        m_port = props.port;
        m_user = props.user;
        m_passwd = props.password;
        m_configFile = props.configFile;
        m_configID = props.configID;
        m_tempDir =  props.tempDir;
        m_auth = props.auth;
    }
}

