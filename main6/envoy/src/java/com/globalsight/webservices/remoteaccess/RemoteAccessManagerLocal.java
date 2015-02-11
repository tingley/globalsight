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
package com.globalsight.webservices.remoteaccess;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class RemoteAccessManagerLocal implements RemoteAccessManager 
{
    private static Logger c_category = Logger
    		.getLogger(RemoteAccessManagerLocal.class.getName());
    
	/**
	 * Create a new remote access history
	 * @param p_rah
	 * @throws RemoteException
	 */
	public RemoteAccessHistory saveRemoteAccessHistory(RemoteAccessHistory p_rah) 
	    throws RemoteException 
	{
        try
        {
        	HibernateUtil.save(p_rah);
        }
        catch (Exception e)
        {
        	String msg = "Failed to save/update RemoteAccessHistory";
            c_category.error(msg, e);
            throw new RemoteException(msg, e);
        }

        return p_rah;
	}
	
	/**
	 * Search remote access history by access Token and api name.
	 * @param p_sessionKey
	 * @param p_APIName
	 * @param p_userName
	 * @return Collection of RemoteAccessHistory objects.
	 * @throws RemoteException
	 */
    public Collection getRemoteAccessHistory(
    		String p_accessToken, String p_APIName, String p_userName)
    	throws RemoteException
    {
    	Collection histories = null;
    	
    	try {
            StringBuffer hql = new StringBuffer();
            hql.append("from RemoteAccessHistory r " +
            		"where r.accessToken = :accessToken ");
            if (p_APIName != null) {
            	hql.append("and lower(r.apiName) = :apiName ");
            }
            if (p_userName != null) {
            	hql.append("and r.userId = :userId");
            }
            
            Map map = new HashMap();
            map.put("accessToken", p_accessToken);
            if (p_APIName != null) {
                map.put("apiName", p_APIName.toLowerCase());            	
            }
            if (p_userName != null) {
                map.put("userId", p_userName);            	
            }
            
            histories = HibernateUtil.search(hql.toString(), map);
    	} catch (Exception e) {
        	String msg = "Failed to search RemoteAccessHistory";
            c_category.error(msg, e);
            //don't throw out exception
//            throw new RemoteException(msg, e);
    	}
    	
    	return histories;
    }
}
