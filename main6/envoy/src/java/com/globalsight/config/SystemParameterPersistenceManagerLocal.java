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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import org.apache.log4j.Level;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.system.SystemConfigParamNames;

import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Implementation of System parameter Persistence Manager.
 * 
 * TODO: This class (and its related persistence classes) must be moved into its
 * own package, and made a subclass of PersistenceManagerLocal.
 */
public class SystemParameterPersistenceManagerLocal implements
        SystemParameterPersistenceManager
{
    private static final Logger CATEGORY = Logger
            .getLogger(SystemParameterPersistenceManagerLocal.class.getName());

    private static boolean dirty = true;

    // private static Collection systemParams = null;

    private static Map systemParamMap = new HashMap();

    private Hashtable m_listeners = new Hashtable(); // the key is the system

    private static final long SUPER_COMPANY_ID = Long
            .parseLong(CompanyWrapper.SUPER_COMPANY_ID);

    private static SystemParameterPersistenceManagerLocal instance = new SystemParameterPersistenceManagerLocal();

    /**
     * Creates new SystemParameterPersistenceManagerLocal
     */
    public SystemParameterPersistenceManagerLocal()
    {
        // initSystemParamters();
    }

    public static SystemParameterPersistenceManagerLocal getInstance()
    {
        return instance;
    }

    /**
     * TODO this method seems no use.
     */
    public SystemParameter getSystemParameter(long p_id)
    {
        /*
         * initSystemParamters(); Iterator it = systemParams.iterator(); while
         * (it.hasNext()) { SystemParameter sp = (SystemParameter) it.next(); if
         * (sp.getId() == p_id) { return sp; } } return null;
         */
        throw new RuntimeException("not supported yet");
    }

    /**
     * 
     * Retrieve a specific system parameter object with passed id
     * 
     * @param p_id
     *            Id of system parameter to retreive
     * @throws RemoteException
     *             Application Server Error
     * @throws SystemParameterEntityException
     *             Error retrieving a specific system parameter.
     * @return System parameter object with matching id
     */
    protected SystemParameter getSystemParameterFromDB(long p_id)
            throws RemoteException, SystemParameterEntityException
    {
        try
        {
            return (SystemParameterImpl) HibernateUtil.get(
                    SystemParameterImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new SystemParameterEntityException(e);
        }
    }

    /**
     * get SystemParameter, if it has a companyId , return the company
     * Parameter, else return the system default parameter Retrieve a specific
     * system parameter object with passed parameter name.
     * 
     * @param p_name
     *            system parameter to retreive
     * @throws RemoteException
     *             Application Server Error
     * @throws SystemParameterEntityException
     *             Error retrieving a specific system parameter.
     * @return System parameter object with matching name
     */
    public SystemParameter getSystemParameter(String p_name)
    {
        // check if reloading from database is needed
        initSystemParamters();
        String companyId = CompanyWrapper.getCurrentCompanyId();
        SystemParameter result = this.getSystemParameter(p_name, companyId);
        if (result == null)
        {
            result = this.getAdminSystemParameter(p_name);
        }
        return result;
    }

    public SystemParameter getSystemParameter(String p_name, String p_companyId)
    {
        // check if reloading from database is needed
        initSystemParamters();
        HashMap companyParams = (HashMap) systemParamMap.get(p_companyId);
        SystemParameter result = null;
        if (companyParams != null)
        {
            result = (SystemParameter) companyParams.get(p_name);
        }

        return result;
    }

    /**
     * get SystemParameter, not company param, that for e.g. cxe.docsDir
     */
    public SystemParameter getAdminSystemParameter(String p_name)
    {
        initSystemParamters();
        return this.getSystemParameter(p_name, "" + SUPER_COMPANY_ID);
    }

    /**
     * TODO seems no reference to this method.
     * 
     * @throws RemoteException
     *             Application Server Error
     * @throws SystemParameterEntityException
     *             Error getting collection of system parameters from data
     *             store.
     * @return Collection of all system parameters
     * @deprecated the old method
     */

    public Collection getSystemParameters() throws RemoteException,
            SystemParameterEntityException
    {
        /*
         * initSystemParamters(); return systemParams;
         */
        throw new RuntimeException("not supported yet");

    }

    /**
     * TODO it's my poor comment ,who fix me. 1 if it is company param
     * (getCompanyId()==currentCompanyId() and getId() exit in system param) ,
     * we update it. 2 if it is system param , we should create a new param with
     * getName() and currentCompanyId()
     * 
     * 
     * Update specified system parameter in data store.
     * 
     * @param p_sysParm
     *            System Parameter object to modify
     * @throws RemoteException
     *             Application Server Error
     * @throws SystemParameterEntityException
     *             Error updating system parameter in data store.
     * @return Modified system parameter object
     */

    public SystemParameter updateSystemParameter(SystemParameter p_sysParm)
            throws RemoteException, SystemParameterEntityException
    {
        SystemParameterImpl result = (SystemParameterImpl) getSystemParameterFromDB(p_sysParm
                .getId());
        result.setValue(p_sysParm.getValue());
        try
        {
            HibernateUtil.update((SystemParameterImpl) result);
        }
        catch (Exception e)
        {            
            throw new SystemParameterEntityException(e);
        }

        setDirty();
        return result;
    }

    /**
     * 
     * Update specified admin system parameter in data store.
     * 
     * @param p_sysParm
     *            System Parameter object to modify
     * @return Modified system parameter object
     */

    public SystemParameter updateAdminSystemParameter(SystemParameter p_sysParm)
            throws RemoteException, SystemParameterEntityException
    {
        SystemParameterImpl result = null;
        // TODO i think we should check first
        if (p_sysParm.getCompanyId() == SUPER_COMPANY_ID)
        {
            // we update it
            result = (SystemParameterImpl) getSystemParameterFromDB(p_sysParm
                    .getId());
            result.setValue(p_sysParm.getValue());
            try
            {
                HibernateUtil.update((SystemParameterImpl) result);
            }
            catch (Exception e)
            {
                throw new SystemParameterEntityException(e);
            }
            setDirty();
            notifyListeners(p_sysParm);
        }
        return result;
    }

    /**
     * @see SystemParameterPersistenceManager.registerListener(
     *      SystemParamterChangeListener, String)
     */
    public void registerListener(SystemParameterChangeListener p_listener,
            String p_systemParameterName)
    {
        ArrayList listeners = null;
        if (m_listeners.containsKey(p_systemParameterName))
        {
            listeners = (ArrayList) m_listeners.get(p_systemParameterName);
        }
        else
        {
            listeners = new ArrayList(1);
        }
        listeners.add(p_listener);
        m_listeners.put(p_systemParameterName, listeners);
    }

    // package methods

    /**
     * Notify listeners that system parameters have new values now.
     * 
     * @exception SystemParameterEntityException
     */
    void notifyListeners() throws SystemParameterEntityException,
            RemoteException
    {
        Iterator it = getSystemParameters().iterator();
        while (it.hasNext())
        {
            SystemParameter systemParameter = (SystemParameter) it.next();
            notifyListeners(systemParameter);
        }
    }

    /**
     * When a SystemParameter is updated, notify listeners.
     * 
     * @param p_systemParameter
     *            the SystemParameter that was updated.
     * @throws SystemParameterEntityException
     */
    private void notifyListeners(SystemParameter p_systemParameter)
            throws SystemParameterEntityException
    {
        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("system parameter "
                        + p_systemParameter.getName() + "="
                        + p_systemParameter.getValue());
            }

            // notify the listners of this parameter
            if (m_listeners.containsKey(p_systemParameter.getName()))
            {
                ArrayList listeners = (ArrayList) m_listeners
                        .get(p_systemParameter.getName());
                for (int i = 0; i < listeners.size(); i++)
                {
                    SystemParameterChangeListener listener = (SystemParameterChangeListener) listeners
                            .get(i);
                    listener.listen(p_systemParameter.getName(),
                            p_systemParameter.getValue());
                }
            }
        }
        catch (Exception e)
        {
            throw new SystemParameterEntityException(p_systemParameter
                    .toString()
                    + " " + e.toString(), e);
        }
    }

    private void initSystemParamters()
    {
        if (dirty)
        {
            loadSystemParamters();
        }
    }

    private void loadSystemParamters()
    {
        String hql = "from SystemParameterImpl";
        Collection systemParams = null;
        try
        {
            systemParams = HibernateUtil.search(hql, null);
        }
        catch (Exception e)
        {           
            CATEGORY.error("Load system parameter failed ");
        }
        resetSystemParamMap(systemParams);
        clearDirty();
    }

    private void resetSystemParamMap(Collection systemParams)
    {
        systemParamMap.clear();
        Iterator it = systemParams.iterator();
        while (it.hasNext())
        {
            SystemParameter sp = (SystemParameter) it.next();
            String cid = "" + sp.getCompanyId();
            if (systemParamMap.containsKey(cid))
            {
                ((HashMap) systemParamMap.get(cid)).put(sp.getName(), sp);
            }
            else
            {
                HashMap hm = new HashMap();
                hm.put(sp.getName(), sp);
                systemParamMap.put(cid, hm);
            }
        }
    }

    public void clearDirty()
    {
        dirty = false;
    }

    public void setDirty()
    {
        dirty = true;
    }

}
