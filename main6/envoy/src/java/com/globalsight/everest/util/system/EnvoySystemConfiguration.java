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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.globalsight.config.SystemParameter;
import com.globalsight.config.SystemParameterPersistenceManagerLocal;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.util.GeneralException;
import com.globalsight.util.PropertiesFactory;
import com.globalsight.util.system.ConfigException;

/**
 * This is a concrete class definition of SystemConfiguration class. It reads
 * the Java Properties file for boot strapping and then reads the database for
 * additional system parameters.
 */
/*
 * Fenshid when system starting up, it read the param from database(sql) when it
 * started, it read the param from SystemParameterPersistenceManagerLocal, which
 * has a more proper cache tech and allows dynamic changes of the params. that's
 * to say, it allows changing the params with out restart the server. 
 * 
 * @author shaucle
 */
class EnvoySystemConfiguration extends SystemConfiguration implements
        SystemConfigParamNames {
    private static final Logger CATEGORY = Logger
            .getLogger(EnvoySystemConfiguration.class);

    private static final String CONFIG_SELECT = "SELECT name, value, company_id FROM SYSTEM_PARAMETER";

    //Fenshid m_paramStore now only load properties from file(but not database)
    private static Properties m_paramFileStore; // Parameters store
//  Fenshid m_paramStore now only load properties from file(but not database)
    private static Map m_paramDBStore = new HashMap(); // Parameters store

    //Fenshid get system parameter dynamic
    //that's to say, when we needn't restart appServer when changing the system
    // parameter(database)
    private static SystemParameterPersistenceManagerLocal spManager = new SystemParameterPersistenceManagerLocal();

    /**
     * Default EnvoySystemConfiguration constructor. This is not public because
     * clients must use the getInstance() method in the base class to get an
     * instance of this class.
     * 
     * @param p_propertyFiles --
     *            array of properties files to use.
     */
    //  First read the boot-strap properties files. At minimum
    //  the boot-strap properties should at least have enough
    //  configuration setting to connect to database to load
    //  additional system parameters.
    EnvoySystemConfiguration(String[] p_propertiesFiles)
            throws GeneralException, ConfigException
    {
        super();
        try
        {
            loadFromFiles(p_propertiesFiles);
            // we need loadFromDatabase for the need in Initializing
            // SystemParameterPersistenceManagerLocal
            loadFromDatabase();

            defineDocRoot();
        }
        catch (GeneralException ge)
        {
            CATEGORY.error("Could not load configuration from property files.",
                    ge);
            throw (ge);
        }
    }
    
    /**
     * Empty constructor for debug usage only!
     */
    EnvoySystemConfiguration() {}

    /**
     * Get the specified parameter and return it as a String.
     * 
     * @param p_paramName
     *            Name of the parameter to get value for.
     * @return Value of the specified parameter.
     * @exception com.globalsight.util.system.ConfigException
     */
    public String getStringParameter(String p_paramName) throws ConfigException {
        //  Return the parameter from cached hash table

        return getStringParameterFromStore(p_paramName);
    }
    
    public String getStringParameter(String p_paramName, String p_companyId) throws ConfigException {
        String result = null;
        if (p_companyId != null) {
        	if (AmbassadorServer.isSystem4Accessible()) {
                SystemParameter sp = spManager.getSystemParameter(p_paramName, p_companyId);
                if (sp != null) {
                    result = sp.getValue();
                }
            }
        } else {
        	result = getStringParameterFromStore(p_paramName);
        }
        return result;
    }

    /**
     * read from file first ,then read database.
     *
     * 
     * Get the specified parameter from loaded store of property file name/value
     * pairs with initial database name/value pairs, and return it as a String.
     * 
     * @param p_paramName
     *            Name of the parameter to get value for.
     * @return Value of the specified parameter.
     * @exception com.globalsight.util.system.ConfigException
     */
    private String getStringParameterFromStore(String p_paramName)
            throws ConfigException {
        String param = m_paramFileStore.getProperty(p_paramName);
        if(param != null) {
            return param;
        }
        if (AmbassadorServer.isSystem4Accessible()) {
            //it may cause StackOverflowError if not init ok
            try {
                SystemParameter sp = spManager.getSystemParameter(p_paramName);
                if (sp != null) {
                    param = sp.getValue();
                }
            } catch (Exception e) {
                //ignore
                //often because sp has no companyId in the file
            }
        }
        
        if(param == null) {
            param = getStringParameterFromDBStore(p_paramName);
        }

        if (param == null) {
            CATEGORY.error("getStringParameterFromStore " + p_paramName
                    + " not found");
            throw new ConfigException(ConfigException.EX_PARAMNOTFOUND);
        }

        return param;
    }

    private String getStringParameterFromDBStore(String p_paramName) {
    	
    	String companyId = CompanyThreadLocal.getInstance().getValue();
    	
    	if (m_paramDBStore.get(companyId) == null 
    			|| !((HashMap) m_paramDBStore.get(companyId)).containsKey(p_paramName)) {
    		
    		return (String)((HashMap) m_paramDBStore.get(
    				CompanyWrapper.SUPER_COMPANY_ID)).get(p_paramName);
    	}
		return (String)((HashMap) m_paramDBStore.get(companyId)).get(p_paramName);
	}

	/**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString() {
        return getClass().getName() + " " + m_paramFileStore.toString();
    }

    private void loadFromFiles(String[] p_propertiesFiles) 
    {
        CATEGORY.info("Loading properties from " + p_propertiesFiles[0]);
        m_paramFileStore = (new PropertiesFactory())
                .getProperties(p_propertiesFiles[0]);
        //read from other properties files and append to the original set
        for (int i = 1; i < p_propertiesFiles.length; i++) {
            String propFile = p_propertiesFiles[i];
            CATEGORY.info("Loading properties from " + propFile);
            PropertiesFactory factory = new PropertiesFactory();
            Properties props = factory.getProperties(propFile);
            m_paramFileStore.putAll(props);
        }
    }

    /**
     * Load additional system parameter from database.
     * 
     * @exception com.globalsight.util.system.ConfigException
     */
    void loadFromDatabase() throws ConfigException {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            connection = ConnectionPool.getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery(CONFIG_SELECT);
            while (rs.next()) {
                String value = rs.getString("value");
                if (value == null) {
                    value = "";
                }
                String companyId = rs.getString("company_Id");
                String name = rs.getString("name");
                if (m_paramDBStore.containsKey(companyId)) {
                	((HashMap) m_paramDBStore.get(companyId)).put(name, value);
                } else {
                	Map m = new HashMap();
                    m.put(name, value);
                    m_paramDBStore.put(companyId, m);
                }
            }
        } catch (Exception e) {
            CATEGORY.error("Got error getting config.", e);
            throw new ConfigException(ConfigException.EX_INVALIDTYPE, e);
        } finally {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(stmt);
            ConnectionPool.silentReturnConnection(connection);
        }
    }

    /**
     * Determine the document root of the installed Web Server.
     * Add the setting to system parameter.
     *  
     */
    private void defineDocRoot() {
        String docRoot = null;
        docRoot = m_paramFileStore
                .getProperty(SystemConfigParamNames.WEB_SERVER_DOC_ROOT);

        if (docRoot == null) {
            docRoot = "";
            m_paramFileStore.put(SystemConfigParamNames.WEB_SERVER_DOC_ROOT,
                    docRoot);
        }
    }
}