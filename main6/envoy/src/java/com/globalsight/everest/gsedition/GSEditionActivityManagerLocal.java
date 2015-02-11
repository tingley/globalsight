package com.globalsight.everest.gsedition;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.autoactions.AutoActionException;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.costing.CostingEngineLocal;
import com.globalsight.everest.costing.CostingException;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class GSEditionActivityManagerLocal {
    private static final Logger c_logger = Logger
        .getLogger(CostingEngineLocal.class.getName());
    
    public GSEditionActivity getGSEditionActionByName(String p_name, GSEdition gsEdition) {
        GSEditionActivity gsEditionAc = null;
        try {
            String hql = "from GSEditionActivity a where a.name = :name and a.gsEdition=:ge";
            HashMap map = new HashMap<String, String>();
            map.put("name", p_name);
            map.put("ge", gsEdition);
            Collection editions = HibernateUtil.search(hql, map);
            Iterator i = editions.iterator();
            gsEditionAc = i.hasNext() ? (GSEditionActivity) i.next() : null;
        }
        catch (Exception pe) {
            c_logger.error("Persistence Exception when retrieving GS Edition activities "
                + p_name, pe);
        }
        
        return gsEditionAc;
    }
    
    public boolean isActionExist(String p_name, GSEdition gsEdition) {
        return getGSEditionActionByName(p_name, gsEdition) != null;
    }
    
    public void createAction(GSEditionActivity p_action) 
        throws RemoteException, GSEditionException{
        Session session = null;
        Transaction transaction = null;
        
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            session.save(p_action);
            transaction.commit();
        } catch (PersistenceException e) {
            try {
                transaction.rollback();
            } catch (Exception e2) {
            }
            
            throw new GSEditionException(
                    GSEditionException.MSG_FAILED_TO_CREATE_action, 
                new String[]{p_action.getName()}, e);
        }
    }
    
    public GSEditionActivity getGSEditionActivityByID(long p_id) {
        GSEditionActivity gsEdition = null;
        
        try {
            String hql = "from GSEditionActivity a where a.id = :id";
            HashMap map = new HashMap<String, String>();
            map.put("id", p_id);
            Collection servers = HibernateUtil.search(hql, map);
            Iterator i = servers.iterator();
            gsEdition = i.hasNext() ? (GSEditionActivity) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when retrieving GS Edition"
                    + p_id, pe);
        }
        return gsEdition;
    }
    
    public void updateGSEditionActivity(GSEditionActivity p_action) throws RemoteException, GSEditionException {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            GSEditionActivity oldAction = getGSEditionActivityByID(p_action.getId());
            
            if (oldAction != null) {
                oldAction.setName(p_action.getName());
                oldAction.setFileProfile(p_action.getFileProfile());
                oldAction.setFileProfileName(p_action.getFileProfileName());
                oldAction.setGsEdition(p_action.getGsEdition());
                oldAction.setDescription(p_action.getDescription());
            }
            
            session.saveOrUpdate(oldAction);
            transaction.commit();
        } catch (Exception e) {
            try {
                transaction.rollback();
            } catch (Exception e2) {
            }
        }
    }
    
    public void removeAction(long p_id) throws RemoteException, GSEditionException {
        Session session = null;
        Transaction transaction = null;
        
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            GSEditionActivity oldAction = getGSEditionActivityByID(p_id);    
            session.delete(oldAction);
            transaction.commit();
        } catch (Exception e) {
            try {
                transaction.rollback();
            } catch (Exception e2) {
            }
        }
    }
    
    /*
     * get all acitivities by the given GS Edition action id.
     */
    public Collection getActivitiesByActionID(String p_actionID) 
        throws RemoteException, AutoActionException {
        Collection activities = null;
        
        if(p_actionID != null) {
            try {
                String hql = "from Activity a where a.isActive = 'Y' "
                    + "and a.editionActionID = :id";
                 String companyId = CompanyThreadLocal.getInstance().getValue();
                 Map map = new HashMap();
                 map.put("id", p_actionID);
                 activities = HibernateUtil.search(hql, map);
            }
            catch (Exception pe)
            {
                c_logger.error("Persistence Exception when retrieving "
                    + "activities by auto action id " + p_actionID, pe);
            }
        }
        
        return activities;
    }
    
    public Collection getAllActions() {
        Collection actions = null;
        try
        {
            String hql = "from GSEditionActivity a where a.gsEdition.companyID =:comanyID";
            String companyId = CompanyThreadLocal.getInstance().getValue();
            HashMap map = new HashMap<String, String>();
            map.put("comanyID", companyId);
            actions = HibernateUtil.search(hql, map);
        }catch (Exception pe)
        {
            c_logger.error("PersistenceException while retrieving all GS Edition Actons.", pe);
        }

        return actions;
    }
}
