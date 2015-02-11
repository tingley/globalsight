/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
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

package com.globalsight.selenium.util.system;

import java.util.Hashtable;
import java.util.ResourceBundle;

import jodd.typeconverter.impl.BooleanConverter;
import jodd.util.StringUtil;

/**
 * @author Vincent
 *
 */
public class ConfigUtil
{
    private static ResourceBundle resourceBundle = null;
    
    public void ConfigUtil() {
        if (resourceBundle != null)
            return;
        
        resourceBundle = ResourceBundle
                .getBundle("com.globalsight.selenium.properties.Configuration");
        setupBaseConfiguration();
        setupCompanyConfiguration();
    }

    private void setupBaseConfiguration()
    {
        Configuration.GS_SERVER_HOST = getProperty("gsserver.host");
        Configuration.GS_SERVER_HTTPS = BooleanConverter.valueOf(getProperty("gsserver.https"));
        Configuration.GS_SERVER_URL = getProperty("gsserver.url");
        
        Configuration.SELENIUM_SERVER_HOST = getProperty("selenium.host");
        Configuration.SELENIUM_SERVER_PORT = getProperty("selenium.port");
        
        Configuration.DATABASE_HOST = getProperty("database.host");
        Configuration.DATABASE_PORT = getProperty("database.port");
        Configuration.DATABASE_SCHEMA = getProperty("database.schema");
        Configuration.DATABASE_EXTRA = getProperty("database.extra");
        Configuration.DATABASE_USERNAME = getProperty("database.username");
        Configuration.DATABASE_PASSWORD = getProperty("database.password");
        
        Configuration.BASE_FILE_INPUT_PATH = getProperty("file.input");
        Configuration.BASE_FILE_OUTPUT_PATH = getProperty("file.output");
        
        Configuration.BROWSER = getProperty("browser");
        
        Configuration.SHORT_WAIT = getProperty("wait.short");
        Configuration.MIDDLE_WAIT = getProperty("wait.middle");
        Configuration.LONG_WAIT = getProperty("wait.long");
        Configuration.BETWEEN_OPERATION_WAIT = getProperty("wait.betweenOperation");
    }
    
    private String getProperty(String p_key) {
        return resourceBundle.getString(p_key);
    }

    private void setupCompanyConfiguration()
    {
    }
    
}
