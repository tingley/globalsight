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

package com.globalsight.everest.edit;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.SynchronizationManager;
import com.globalsight.everest.edit.SynchronizationStatus;

import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.everest.util.system.SystemShutdownException;


import java.util.*;
import java.rmi.RemoteException;

/**
 * <p>Implementation of the SynchronizationManager interface.
 *
 * This RMI object allows offline uploads and online editing to be
 * synchronized.</p>
 */
public class SynchronizationManagerLocal
    extends RemoteServer
    implements SynchronizationManager
{
    private static Logger s_logger =
        Logger.getLogger(
            SynchronizationManagerLocal.class);

    //
    // Private Members
    //

    /** Internal map holding page upload statuses. */
    static private HashMap m_pagestatus = new HashMap();
    
    static private HashSet m_pageTempFileName = new HashSet();

    //
    // Constructor
    //
    public SynchronizationManagerLocal()
        throws RemoteException
    {
        super(RemoteServer.getServiceName(SynchronizationManager.class));
    }

    //
    // RemoteServer method overwrites
    //

    /**
     * <p>Binds the remote server to the ServerRegistry.</p>
     *
     * @throws SystemStartupException when a NamingException or other
     * Exception occurs.
     */
    public void init()
        throws SystemStartupException
    {
        super.init();
    }

    /**
     * <p>Unbinds the remote server from the ServerRegistry.</p>
     *
     * @throws SystemShutdownException when a NamingException or other
     * Exception occurs.
     */
    public void destroy()
        throws SystemShutdownException
    {
        m_pagestatus.clear();
        m_pageTempFileName.clear();

        super.destroy();
    }

    //
    // Interface Methods
    //

    public void uploadStarted(Long p_pageId)
        throws RemoteException
    {
        setStatus(p_pageId, UPLOAD_STARTED);
    }

    /**
     * Marks if a particular target page has been fully uploaded and
     * the database update is finished. This allows the online editor
     * to notify the user to reload the editor.
     */
    public void uploadFinished(Long p_pageId)
        throws RemoteException
    {
        setStatus(p_pageId, UPLOAD_FINISHED);
    }

    public void gxmlUpdateStarted(Long p_pageId)
        throws RemoteException
    {
        setStatus(p_pageId, GXMLUPDATE_STARTED);
    }

    public void gxmlUpdateFinished(Long p_pageId)
        throws RemoteException
    {
        setStatus(p_pageId, GXMLUPDATE_FINISHED);
    }

    /**
     * Marks if a particular target page has been fully uploaded and
     * the database update is finished. This allows the online editor
     * to notify the user to reload the editor.
     */
    public SynchronizationStatus getStatus(Long p_pageId)
        throws RemoteException
    {
        synchronized (m_pagestatus)
        {
            SynchronizationStatus status =
                (SynchronizationStatus)m_pagestatus.get(p_pageId);

            if (status != null)
            {
                return new SynchronizationStatus(status);
            }

            return null;
        }
    }
    
    public boolean checkTempFileName(String p_tempFileName)
    {
    	if(m_pageTempFileName.contains(p_tempFileName))
    	{
    		return false;
    	}
    	else
    	{
    		return true;
    	}
    }

    //
    // Private Methods
    //

    private void setStatus(Long p_pageId, String p_status)
    {
        if (p_pageId == null)
        {
            return;
        }

        synchronized (m_pagestatus)
        {
            SynchronizationStatus status =
                (SynchronizationStatus)m_pagestatus.get(p_pageId);

            if (status != null)
            {
                status.setStatus(p_status);
                status.setTimestamp(System.currentTimeMillis());
            }
            else
            {
                status = new SynchronizationStatus();

                status.setPageId(p_pageId);
                status.setStatus(p_status);
                status.setTimestamp(System.currentTimeMillis());

                m_pagestatus.put(p_pageId, status);
            }

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Page " + p_pageId + ": " + p_status);
            }
        }
    }
    
    public void setTempFileName(String p_tempFileName)
    {
    	m_pageTempFileName.add(p_tempFileName);
    }
}
