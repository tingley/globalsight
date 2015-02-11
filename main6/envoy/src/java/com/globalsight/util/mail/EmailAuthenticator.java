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
package com.globalsight.util.mail;

// Java
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import org.apache.log4j.Logger;
//GlobalSight
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.util.system.SystemConfigParamNames;


/**
 * This is an email authentication class
 * The class Authenticator represents an object that knows how 
 * to obtain authentication for a network connection. We
 * obtain the user for information from the database. 
 *
 * Applications use this class by creating a subclass, 
 * and registering an instance of that subclass with the session 
 * when it is created. When authentication is required, the system 
 * will invoke a method on the subclass (like getPasswordAuthentication). 
 * and form an appropriate message for the user. 
 */


public class EmailAuthenticator extends Authenticator 
{
    private static final Logger c_category =
        Logger.getLogger(
            EmailAuthenticator.class.getName());

    /**
     * Public Constructor.
     */
    public EmailAuthenticator()
    {
        super();        
    }

    
    /**
     * Called when password authentication is needed.  This is an override 
     * method of Authenticator interface.
     * @return The PasswordAuthentication collected from the user, or null 
     * if none is provided.
     */
    public PasswordAuthentication getPasswordAuthentication() 
    {
        String accountUsername = ""; 
        String accountPassword = "";
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            accountUsername = sc.getStringParameter(SystemConfigParamNames.ACCOUNT_USERNAME);            
            accountPassword = sc.getStringParameter(SystemConfigParamNames.ACCOUNT_PASSWORD);            
        }
        catch (Exception ce)
        {
            c_category.error("Error getting in email authentication parameters" + ce);
        }        
        
        return new PasswordAuthentication(accountUsername, accountPassword);
    }
}
