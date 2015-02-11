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

package com.globalsight.everest.webapp.pagehandler.administration.config.remoteip;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * One persistent object containing the information about remote ip filter for
 * webservice.
 */
public class RemoteIp extends PersistentObject
{
    private static final long serialVersionUID = 7618550163409340723L;
    private String ip;
    private String description;

    private static final String WILDCARD = "*";

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = repairIp(ip);
    }

    /**
     * Repairs ip address expression. At first, the leading and trailing
     * whitespace will be omitted, then the string after wild card(*) will be
     * omitted too.
     * 
     * <p>
     * Will return <tt>null</tt> if the ip addres expression is <tt>null</tt>
     * 
     * @param ip
     *            The ip address expression to be repaired.
     * @return The repaired ip address expression.
     */
    public static String repairIp(String ip)
    {
        if (ip != null)
        {
            ip = ip.trim();
        }

        return ip;
    }

    public String getDescription()
    {
        return description == null ? "" : description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
