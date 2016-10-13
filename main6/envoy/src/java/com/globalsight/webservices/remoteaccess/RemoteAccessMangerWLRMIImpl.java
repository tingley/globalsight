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

import com.globalsight.everest.util.system.RemoteServer;

public class RemoteAccessMangerWLRMIImpl 
    extends RemoteServer
    implements RemoteAccessManagerWLRemote
{
	RemoteAccessManager m_localReference;
	
    public RemoteAccessMangerWLRMIImpl() throws RemoteException
    {
        super(RemoteAccessManager.SERVICE_NAME);
        m_localReference = new RemoteAccessManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }
    
    public RemoteAccessHistory saveRemoteAccessHistory(RemoteAccessHistory p_rah) 
    	throws RemoteException 
    {
    	return m_localReference.saveRemoteAccessHistory(p_rah);
    }
    
    public Collection getRemoteAccessHistory(String p_accessToken, String p_APIName, String p_userName)
        throws RemoteException
    {
    	return m_localReference.getRemoteAccessHistory(p_accessToken, p_APIName, p_userName);
    }
}
