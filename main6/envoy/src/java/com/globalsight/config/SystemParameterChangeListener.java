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
import com.globalsight.config.SystemParameterEntityException;

// java
import java.rmi.RemoteException;


/**
 * This interface defines a method needed by components that
 * want to know about any system parameter changes while the
 * system is up and running.
 * This parameter changes are made through the SystemParameterPersistenceManager.
 * Any component that defines this method can register themselves as a
 * listener to the SystemParameterPersistenceManager.
 */
public interface SystemParameterChangeListener
{
    /**
     * This method is called by the SystemParameterPersistenceManager when
     * the parameter name passed in has changed. The class that implements
     * this method can handle this event accordingly.
     *
     * @param p_systemParameterName  The system parameter name (from SystemConfigParamNames)
     * @param p_newSystemParameterValue  The new value.
     */
    void listen(String p_systemParameterName,
                String p_newSystemParameterValue)
        throws RemoteException, SystemParameterEntityException;
}

