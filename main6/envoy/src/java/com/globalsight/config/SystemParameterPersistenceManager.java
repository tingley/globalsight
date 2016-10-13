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
package com.globalsight.config;

// globalsight
import com.globalsight.config.SystemParameterChangeListener;

// java
import java.util.Collection;
import java.rmi.RemoteException;

/**
 * Persistence manager for data access of system parameter objects.
 */
public interface SystemParameterPersistenceManager
{
    /**
     * System Parameter Persistence Service Name
     */
    public static final String SERVICE_NAME =
        "SystemParameterPersistenceManager";

    /**
     * Update specified system parameter in data store.
     *
     * @param p_systemParam System Parameter object to modify
     * @throws RemoteException Application Server Error
     * @throws SystemParameterEntityException Error updating system
     * parameter in data store.
     * @return Modified system parameter object
     */
    public SystemParameter updateSystemParameter(SystemParameter p_systemParam)
        throws RemoteException, SystemParameterEntityException;
    
    public SystemParameter updateAdminSystemParameter(SystemParameter p_sysParm)
    throws RemoteException, SystemParameterEntityException;

    /**
     * Retrieve a specific system parameter object with passed id
     *
     * @param p_id Id of system parameter to retreive
     * @throws RemoteException Application Server Error
     * @throws SystemParameterEntityException Error retrieving a
     * specific system parameter.
     * @return  System parameter object with matching id
     */
    public SystemParameter getSystemParameter(long p_id)
        throws RemoteException, SystemParameterEntityException;

    /**
     * Retrieve a specific system parameter object with passed parameter
     * name.
     *
     * @param p_name system parameter to retreive
     * @throws RemoteException Application Server Error
     * @throws SystemParameterEntityException Error retrieving a
     * specific system parameter.
     * @return  System parameter object with matching name
     */
    public SystemParameter getSystemParameter(String p_name)
        throws RemoteException, SystemParameterEntityException;

    /**
     * Retrieve a specific system parameter object with passed parameter
     * name.
     *
     * @param p_name system parameter to retreive
     * @throws RemoteException Application Server Error
     * @throws SystemParameterEntityException Error retrieving a
     * specific system parameter.
     * @return  System parameter object with matching name
     */
    public SystemParameter getSystemParameter(String p_name, String p_companyId)
        throws RemoteException, SystemParameterEntityException;
    
//  TODO refactor the name
    /**
     * get SystemParameter, not company param, that for e.g. cxe.docsDir
     */
    public SystemParameter getAdminSystemParameter(String p_name)
    throws RemoteException, SystemParameterEntityException;

    /**
     * Return all system parameter objects from data store
     *
     * @throws RemoteException Application Server Error
     * @throws SystemParameterEntityException Error getting collection
     * of system parameters from data store.
     * @return Collection of all system parameters
     */
    public Collection getSystemParameters()
        throws RemoteException, SystemParameterEntityException;

    /**
     * Register a class as a listener of the specified parameter.
     * The parameter names can be found in SystemConfigParamNames.java
     * If the parameter is changed by the user then the listener is 
     * notified.
     *
     * @param The class that is listening for changes of a system parameter.
     * @param The name of the system parameter which if changed, the listener
     *        should be notified about.
     */
    public void registerListener(SystemParameterChangeListener p_listener,
                                 String p_systemParameterName);
    
    public void clearDirty();

    public void setDirty();
}

