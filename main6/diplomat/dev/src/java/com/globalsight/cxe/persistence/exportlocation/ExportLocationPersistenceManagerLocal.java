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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.globalsight.config.SystemParameter;
import com.globalsight.cxe.entity.exportlocation.ExportLocation;
import com.globalsight.cxe.entity.exportlocation.ExportLocationImpl;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Implements the service interface for performing CRUD operations for
 * ExportLocations *
 */
public class ExportLocationPersistenceManagerLocal implements
        ExportLocationPersistenceManager
{
    // private Collection expLocations;
    // private boolean dirty;
    /**
     * Default Constructor TODO: remove throws clause
     */
    public ExportLocationPersistenceManagerLocal()
            throws ExportLocationEntityException, RemoteException
    {
        super();
    }

    /**
     * Creates a new ExportLocation object in the data store
     * 
     * @return the newly created object
     */
    public ExportLocation createExportLocation(ExportLocation p_ExportLocation)
            throws ExportLocationEntityException, RemoteException
    {
        try
        {
            p_ExportLocation.setCompanyId(Long.parseLong(CompanyWrapper
                    .getCurrentCompanyId()));
            HibernateUtil.save((ExportLocationImpl) p_ExportLocation);
            return readExportLocation(p_ExportLocation.getId());
        }
        catch (Exception e)
        {
            throw new ExportLocationEntityException(e);
        }
    }

    public ExportLocation createNewExportLocation(
            ExportLocation p_ExportLocation)
            throws ExportLocationEntityException, RemoteException
    {
        try
        {
            HibernateUtil.save((ExportLocationImpl) p_ExportLocation);
            return readExportLocation(p_ExportLocation.getId());
        }
        catch (Exception e)
        {
            throw new ExportLocationEntityException(e);
        }
    }

    /**
     * Reads the ExportLocation object from the datastore
     * 
     * @return ExportLocation with the given id
     */
    public ExportLocation readExportLocation(long p_id)
            throws ExportLocationEntityException, RemoteException
    {
        try
        {
            return (ExportLocationImpl) HibernateUtil.get(
                    ExportLocationImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new ExportLocationEntityException(e);
        }
    }

    /**
     * Deletes an XML Rule File from the datastore
     */
    public void deleteExportLocation(ExportLocation p_ExportLocation)
            throws ExportLocationEntityException, RemoteException
    {
        try
        {
            HibernateUtil.delete((ExportLocationImpl) p_ExportLocation);
        }
        catch (Exception e)
        {
            throw new ExportLocationEntityException(e);
        }
    }

    /**
     * Update the ExportLocation object in the datastore
     * 
     * @return the updated ExportLocation
     */
    public ExportLocation updateExportLocation(ExportLocation p_ExportLocation)
            throws ExportLocationEntityException, RemoteException
    {
        try
        {
            HibernateUtil.update((ExportLocationImpl) p_ExportLocation);
        }
        catch (Exception e)
        {
            throw new ExportLocationEntityException(e);
        }
        return p_ExportLocation;
    }

    /**
     * Get a list of all existing ExportLocation objects in the datastore; make
     * them editable.
     * 
     * @return a vector of the ExportLocation objects
     */
    public Collection getAllExportLocations()
            throws ExportLocationEntityException, RemoteException
    {
        try
        {
            String hql = "from ExportLocationImpl e order by e.name";
            Collection c = HibernateUtil.search(hql);
            Iterator it = c.iterator();
            Collection result = new ArrayList();
            String companyId = CompanyWrapper.getCurrentCompanyId();
            while (it.hasNext())
            {
                ExportLocation el = (ExportLocation) it.next();
                if (String.valueOf(el.getCompanyId()).equals(companyId))
                {
                    result.add(el);
                }
            }
            return result;
        }
        catch (Exception e)
        {
            throw new ExportLocationEntityException(e);
        }
    }

    /**
     * Get a list of all existing ExportLocation objects in the datastore; make
     * them editable.
     * 
     * @return a vector of the ExportLocation objects
     */
    public Collection getAllExportLocations(String companyId)
            throws ExportLocationEntityException, RemoteException
    {
        try
        {
            String hql = "from ExportLocationImpl e order by e.name";
            Collection c = HibernateUtil.search(hql);
            Iterator it = c.iterator();
            Collection result = new ArrayList();
            while (it.hasNext())
            {
                ExportLocation el = (ExportLocation) it.next();
                if (String.valueOf(el.getCompanyId()).equals(companyId))
                {
                    result.add(el);
                }
            }
            return result;
        }
        catch (Exception e)
        {
            throw new ExportLocationEntityException(e);
        }
    }

    /*
     * public Collection getAllExportLocations1() throws
     * ExportLocationEntityException, RemoteException { try { return
     * getAllEditableObjects(ExportLocationQueryNames.ALL_EXPORT_LOCATIONS); }
     * catch (Exception e) { throw new ExportLocationEntityException(e); } }
     */

    /**
     * Returns the default export location. The ID of this location is stored in
     * the system_parameter table as a UI modifiable parameter
     * 
     */
    public ExportLocation getDefaultExportLocation()
            throws ExportLocationEntityException, RemoteException
    {
        long id;
        try
        {
            SystemParameter sp;
            sp = ServerProxy.getSystemParameterPersistenceManager()
                    .getSystemParameter(
                            SystemConfigParamNames.DEFAULT_EXPORT_LOCATION);
            id = Long.parseLong(sp.getValue());
        }
        catch (Exception e)
        {
            throw new ExportLocationEntityException(e);
        }

        return this.readExportLocation(id);
    }

    /**
     * Returns the default export location. The ID of this location is stored in
     * the system_parameter table as a UI modifiable parameter
     * 
     */
    public ExportLocation getDefaultExportLocation(String companyId)
            throws ExportLocationEntityException, RemoteException
    {
        long id;
        try
        {
            SystemParameter sp;
            sp = ServerProxy.getSystemParameterPersistenceManager()
                    .getSystemParameter(
                            SystemConfigParamNames.DEFAULT_EXPORT_LOCATION,
                            companyId);
            id = Long.parseLong(sp.getValue());
        }
        catch (Exception e)
        {
            throw new ExportLocationEntityException(e);
        }

        return this.readExportLocation(id);
    }

    /**
     * Sets the default export location. The ID of this location is stored in
     * the system_parameter table as a UI modifiable parameter
     * 
     * @param p_id
     *            the ID of the export location that should be made the default
     * @exception ExportLocationEntityException
     * @exception RemoteException
     */
    public void setDefaultExportLocation(long p_id)
            throws ExportLocationEntityException, RemoteException
    {
        try
        {
            SystemParameter sp;
            sp = ServerProxy.getSystemParameterPersistenceManager()
                    .getSystemParameter(
                            SystemConfigParamNames.DEFAULT_EXPORT_LOCATION);
            sp.setValue(Long.toString(p_id));
            ServerProxy.getSystemParameterPersistenceManager()
                    .updateSystemParameter(sp);
        }
        catch (Exception e)
        {
            throw new ExportLocationEntityException(e);
        }
    }

}
