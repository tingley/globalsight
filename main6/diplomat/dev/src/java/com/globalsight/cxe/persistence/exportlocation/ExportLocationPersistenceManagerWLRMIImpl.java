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

package com.globalsight.cxe.persistence.exportlocation;

import java.rmi.RemoteException;
import java.util.Collection;

import com.globalsight.cxe.entity.exportlocation.ExportLocation;
import com.globalsight.everest.util.system.RemoteServer;


public class ExportLocationPersistenceManagerWLRMIImpl 
    extends RemoteServer
    implements ExportLocationPersistenceManagerWLRemote
{
    ExportLocationPersistenceManager m_localReference;

    public ExportLocationPersistenceManagerWLRMIImpl() 
        throws java.rmi.RemoteException, ExportLocationEntityException
    {
        super(ExportLocationPersistenceManager.SERVICE_NAME);
        m_localReference = new ExportLocationPersistenceManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public  com.globalsight.cxe.entity.exportlocation.ExportLocation createExportLocation(com.globalsight.cxe.entity.exportlocation.ExportLocation param1) throws com.globalsight.cxe.persistence.exportlocation.ExportLocationEntityException,java.rmi.RemoteException
    {
        return m_localReference.createExportLocation(param1);
    }

    public  com.globalsight.cxe.entity.exportlocation.ExportLocation readExportLocation(long param1) throws com.globalsight.cxe.persistence.exportlocation.ExportLocationEntityException,java.rmi.RemoteException
    {
        return m_localReference.readExportLocation(param1);
    }

    public  void deleteExportLocation(com.globalsight.cxe.entity.exportlocation.ExportLocation param1) throws com.globalsight.cxe.persistence.exportlocation.ExportLocationEntityException,java.rmi.RemoteException
    {
        m_localReference.deleteExportLocation(param1);
    }

    public  com.globalsight.cxe.entity.exportlocation.ExportLocation updateExportLocation(com.globalsight.cxe.entity.exportlocation.ExportLocation param1) throws com.globalsight.cxe.persistence.exportlocation.ExportLocationEntityException,java.rmi.RemoteException
    {
        return m_localReference.updateExportLocation(param1);
    }

    public  java.util.Collection getAllExportLocations() throws com.globalsight.cxe.persistence.exportlocation.ExportLocationEntityException,java.rmi.RemoteException
    {
        return m_localReference.getAllExportLocations();
    }

    public  com.globalsight.cxe.entity.exportlocation.ExportLocation getDefaultExportLocation() throws com.globalsight.cxe.persistence.exportlocation.ExportLocationEntityException,java.rmi.RemoteException
    {
        return m_localReference.getDefaultExportLocation();
    }

    public void setDefaultExportLocation(long p_id) throws com.globalsight.cxe.persistence.exportlocation.ExportLocationEntityException,java.rmi.RemoteException
    {
        m_localReference.setDefaultExportLocation(p_id);
    }

    public ExportLocation createNewExportLocation(ExportLocation p_ExportLocation) throws ExportLocationEntityException, RemoteException {
        return m_localReference.createNewExportLocation(p_ExportLocation);
    }

	public Collection getAllExportLocations(String companyId) throws ExportLocationEntityException, RemoteException {
		return m_localReference.getAllExportLocations(companyId);
	}

	@Override
	public ExportLocation getDefaultExportLocation(String companyId)
			throws ExportLocationEntityException, RemoteException {
		return m_localReference.getDefaultExportLocation(companyId);
	}
}

