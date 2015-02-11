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

package com.globalsight.webservices.coti.util;

import java.util.HashMap;

/**
 * COTI session object to save datas
 * @author Wayzou
 *
 */
public class COTISession
{
    public static String UserName = "UserName";
    public static String SessionId = "SessionId";
    public static String CompanyName = "CompanyName";

    private HashMap<String, Object> datas = new HashMap<String, Object>();

    public COTISession()
    {
        if (datas == null)
        {
            datas = new HashMap<String, Object>();
        }
    }

    public String getUserName()
    {
        return (String) get(UserName);
    }

    public String getSessionId()
    {
        return (String) get(SessionId);
    }

    public String getCompanyName()
    {
        return (String) get(CompanyName);
    }

    public void setUserName(String v)
    {
        put(UserName, v);
    }

    public void setSessionId(String v)
    {
        put(SessionId, v);
    }

    public void setCompanyName(String v)
    {
        put(CompanyName, v);
    }

    public Object get(String key)
    {
        return datas.get(key);
    }

    public Object remove(String key)
    {
        return datas.remove(key);
    }

    public Object put(String key, Object value)
    {
        if (datas.containsKey(key))
        {
            return null;
        }

        return datas.put(key, value);
    }

    public void clear()
    {
        datas.clear();
    }
}
