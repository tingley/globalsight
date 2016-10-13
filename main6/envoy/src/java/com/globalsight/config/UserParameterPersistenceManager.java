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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

/**
 * Persistence manager for data access of user parameter objects.
 */
public interface UserParameterPersistenceManager
{
    /**
     * User Parameter Persistence Service Name
     */
    public static final String SERVICE_NAME = "UserParameterPersistenceManager";

    /**
     * Update specified user parameter in data store.
     * 
     * @param p_userParam
     *            User Parameter object to modify
     * @throws RemoteException
     *             Application Server Error
     * @throws UserParameterEntityException
     *             Error updating user parameter in data store.
     * @return Modified user parameter object
     */
    public UserParameter updateUserParameter(UserParameter p_userParam)
            throws RemoteException, UserParameterEntityException;

    /**
     * Retrieve a specific user parameter object with passed id
     * 
     * @param p_id
     *            Id of user parameter to retreive
     * @throws RemoteException
     *             Application Server Error
     * @throws UserParameterEntityException
     *             Error retrieving a specific user parameter.
     * @return User parameter object with matching id
     */
    public UserParameter getUserParameter(long p_id) throws RemoteException,
            UserParameterEntityException;

    /**
     * Retrieve a specific user parameter object with passed parameter name.
     * 
     * @param p_name
     *            user parameter to retreive
     * @throws RemoteException
     *             Application Server Error
     * @throws UserParameterEntityException
     *             Error retrieving a specific user parameter.
     * @return User parameter object with matching name
     */
    public UserParameter getUserParameter(String p_userId, String p_name)
            throws RemoteException, UserParameterEntityException;

    /**
     * Return all user parameter objects from data store for a specific user.
     * 
     * @throws RemoteException
     *             Application Server Error
     * @throws UserParameterEntityException
     *             Error getting collection of user parameters from data store.
     * @return Collection of all user parameters for the user
     */
    public Collection getUserParameters(String p_userId)
            throws RemoteException, UserParameterEntityException;

    /**
     * Return all user parameter objects from data store for a specific user in
     * a HashMap.
     * 
     * @throws RemoteException
     *             Application Server Error
     * @throws UserParameterEntityException
     *             Error getting map of user parameters from data store.
     * @return HashMap of all user parameters, indexed by parameter name
     */
    public HashMap getUserParameterMap(String p_userId) throws RemoteException,
            UserParameterEntityException;
}
