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

package com.globalsight.util.j2ee.pramati;
import com.globalsight.util.j2ee.AppServerWrapper;
import com.globalsight.util.j2ee.AppServerWrapperFactory;
import javax.naming.NamingException;

/**
 * Implements the wrapper for Pramati
 */
public class PramatiWrapper extends AppServerWrapper
{
    /**
     * The Pramati app server dependent JNDI lookup string for 
     * a UserTransaction.
     */
    public static final String USER_TRANSACTION = 
        "UserTransaction";
    /**
     * Constructor
     */
    public PramatiWrapper()
    {
        super();
        System.out.println("Using Pramati as the J2EE Application Server");
    }

    /**
     * Returns the name of the J2EE application server.
     * 
     * @return String
     */
    public String getJ2EEServerName()
    {
        return AppServerWrapperFactory.PRAMATI;
    }

    /**
     * Get the JNDI lookup string for getting a UserTransaction object.
     * @return The Pramati application server string for the lookup.
     */
    public String getUserTransactionString()
    {
        return USER_TRANSACTION;
    }
}

