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
package com.globalsight.everest.gsedition;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.costing.CostingEngineLocal;
import com.globalsight.everest.costing.CostingException;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class GSEditionManagerLocal
{
    private static final Logger c_logger = Logger
            .getLogger(CostingEngineLocal.class.getName());

    public Collection getAllGSEdition()
    {
        Collection actions = null;

        try
        {
            String hql = "from GSEdition a where a.companyID =:comanyID";
            String companyId = CompanyThreadLocal.getInstance().getValue();
            HashMap map = new HashMap<String, String>();
            map.put("comanyID", Long.parseLong(companyId));
            actions = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "PersistenceException while retrieving all auto actons.",
                    pe);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_CURRENCIES, null,
                    pe);
        }

        return actions;
    }

    public GSEdition getGSEditionActionByName(String p_name)
    {
        GSEdition gsEdition = null;
        try
        {
            String hql = "from GSEdition a where a.name = :name and a.companyID =:comanyID";
            String companyId = CompanyThreadLocal.getInstance().getValue();
            HashMap map = new HashMap<String, String>();
            map.put("name", p_name);
            map.put("comanyID", Long.parseLong(companyId));
            Collection editions = HibernateUtil.search(hql, map);
            Iterator i = editions.iterator();
            gsEdition = i.hasNext() ? (GSEdition) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "Persistence Exception when retrieving GS Edition activities "
                            + p_name, pe);
        }

        return gsEdition;
    }

    public boolean isActionExist(String p_name)
    {
        return getGSEditionActionByName(p_name) != null;
    }

    public void createAction(GSEdition p_action) throws RemoteException,
            GSEditionException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            session.save(p_action);
            transaction.commit();
        }
        catch (PersistenceException e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }

            throw new GSEditionException(
                    GSEditionException.MSG_FAILED_TO_CREATE_action,
                    new String[]
                    { p_action.getName() }, e);
        }
    }

    public GSEdition getGSEditionByID(long p_id)
    {
        GSEdition gsEdition = null;

        try
        {
            String hql = "from GSEdition a where a.id = :id";
            HashMap map = new HashMap<String, String>();
            map.put("id", p_id);
            Collection servers = HibernateUtil.search(hql, map);
            Iterator i = servers.iterator();
            gsEdition = i.hasNext() ? (GSEdition) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when retrieving GS Edition"
                    + p_id, pe);
        }
        return gsEdition;
    }

    public void updateGSEdition(GSEdition p_action) throws RemoteException,
            GSEditionException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            GSEdition oldAction = getGSEditionByID(p_action.getId());

            if (oldAction != null)
            {
                oldAction.setName(p_action.getName());
                oldAction.setHostName(p_action.getHostName());
                oldAction.setHostPort(p_action.getHostPort());
                oldAction.setUserName(p_action.getUserName());
                oldAction.setPassword(p_action.getPassword());
                oldAction.setCompanyID(p_action.getCompanyID());
                oldAction.setDescription(p_action.getDescription());
            }

            session.saveOrUpdate(oldAction);
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }
        }
    }

    public void removeAction(long p_id) throws RemoteException,
            GSEditionException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            GSEdition oldAction = getGSEditionByID(p_id);
            session.delete(oldAction);
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }
        }
    }
}
