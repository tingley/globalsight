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

import com.globalsight.util.GeneralException;
import com.globalsight.util.system.ConfigException;

// Core Java classes
import java.util.StringTokenizer;

/**
 * This is the control unit of the Envoy system.  It extends the
 * abstract base class SystemControlTemplate and implement the methods
 * that are used by methods in the base class to provide Envoy
 * specific information and behavior.
 */
public class EnvoySystemControl
    extends SystemControlTemplate
{


    EnvoySystemControl()
    {
    }

    //
    // Methods used by methods in the base class
    //

    /**
     * Get a list of the names of the server object classes.
     * This method is to be implemented by the subclasses.
     */
    protected String[] getServerClasses()
        throws ConfigException
    {
        String[] classes = null;
        SystemConfiguration config = null;

        try
        {
            config = SystemConfiguration.getInstance();
        }
        catch (GeneralException ge)
        {
            throw new ConfigException(ge.getExceptionId(), ge);
        }

        String classString = config.getStringParameter(
            SystemConfigParamNames.SERVER_CLASSES);

        StringTokenizer tokenizer = new StringTokenizer(
            classString,
            SystemConfiguration.DEFAULT_DELIMITER);

        int cnt = tokenizer.countTokens();
        if (cnt > 0)
        {
            classes = new String[cnt];

            for (int i = 0; i < cnt; i++)
            {
                classes[i] = tokenizer.nextToken();
            }
        }

        return classes;
    }
}
