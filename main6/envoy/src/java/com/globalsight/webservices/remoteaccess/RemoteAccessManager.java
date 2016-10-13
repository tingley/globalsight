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

/**
 * RemoteAccessManager is an interface used for handling web service access history processes.
 */
public interface RemoteAccessManager 
{
	public static final String SERVICE_NAME = "RemoteAccessManagerServer";
	
	/**
	 * Create a new remote access history
	 * @param p_rah
	 * @throws RemoteException
	 */
	public RemoteAccessHistory saveRemoteAccessHistory(RemoteAccessHistory p_rah) 
		throws RemoteException;
	
	/**
	 * Search remote access history by access Token and api name.
	 * @param p_accessToken
	 * @param p_APIName
	 * @param p_userName : can be null
	 * @return Collection of RemoteAccessHistory objects.
	 * @throws RemoteException
	 */
    public Collection getRemoteAccessHistory(
    		String p_accessToken, String p_APIName, String p_userName)
    	throws RemoteException;
}
