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

// globalsight
import com.globalsight.cxe.entity.exportlocation.ExportLocation;
// java
import java.util.Collection;
import java.rmi.RemoteException;

/** A service interface for performing CRUD operations for ExportLocations **/
public interface ExportLocationPersistenceManager
{
    public static final String SERVICE_NAME = "ExportLocationPersistenceManager";
    
    /**
    ** Creates a new ExportLocation object in the data store
    ** @return the created object
    **/
    public ExportLocation createExportLocation(ExportLocation p_ExportLocation)
    throws ExportLocationEntityException, RemoteException;
    
    public ExportLocation createNewExportLocation(ExportLocation p_ExportLocation)
    throws ExportLocationEntityException, RemoteException;

    /**
    ** Reads the ExportLocation object from the datastore
    ** @return the ExportLocation
    **/
    public ExportLocation readExportLocation(long p_id)
    throws ExportLocationEntityException, RemoteException;

    /**
    ** Deletes an ExportLocation from the datastore
    **/
    public void deleteExportLocation(ExportLocation p_ExportLocation)
    throws ExportLocationEntityException, RemoteException;


    /**
    ** Update the ExportLocation object in the datastore
    ** @return the updated object
    **/
    public ExportLocation updateExportLocation(ExportLocation p_ExportLocation)
    throws ExportLocationEntityException, RemoteException;

    /**
    ** Get a list of all existing ExportLocation objects in the datastore
    ** @return a vector of the ExportLocation objects
    **/
    public Collection getAllExportLocations()
    throws ExportLocationEntityException, RemoteException;
    
    /**
     ** Get a list of all existing ExportLocation objects in the datastore
     ** @return a vector of the ExportLocation objects
     **/
     public Collection getAllExportLocations(String companyId)
     throws ExportLocationEntityException, RemoteException;

    /**
     * Returns the default export location. The ID of this location is
     * stored in the system_parameter table as a UI
     * modifiable parameter
     * 
     */
    public ExportLocation getDefaultExportLocation()
    throws ExportLocationEntityException, RemoteException;
    
    public ExportLocation getDefaultExportLocation(String companyId)
    throws ExportLocationEntityException, RemoteException;

    /**
     * Sets the default export location. The ID of this location is
     * stored in the system_parameter table as a UI
     * modifiable parameter
     * 
     * @param p_id   the ID of the export location that should be made the default
     * @exception ExportLocationEntityException
     * @exception RemoteException
     */
    public void setDefaultExportLocation(long p_id)
    throws ExportLocationEntityException, RemoteException;
}

