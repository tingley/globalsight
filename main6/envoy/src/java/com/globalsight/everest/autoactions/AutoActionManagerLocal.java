package com.globalsight.everest.autoactions;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.costing.CostingEngineLocal;
import com.globalsight.everest.costing.CostingException;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class AutoActionManagerLocal {
    private static final GlobalSightCategory c_logger = (GlobalSightCategory) GlobalSightCategory
    .getLogger(CostingEngineLocal.class.getName());
    
    public Collection getAllActions() {
        Collection actions = null;
        try
        {
            String hql = "from AutoAction a where a.companyID =:comanyID";
            String companyId = CompanyThreadLocal.getInstance().getValue();
            HashMap map = new HashMap<String, String>();
            map.put("comanyID", companyId);
            actions = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            c_logger.error("PersistenceException while retrieving all auto actons.",
                    pe);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_CURRENCIES, null,
                    pe);
        }

        return actions;
    }
    
    public AutoAction getAutoActionByName(String p_name) {
        AutoAction autoAction = null;
        try {
            String hql = "from AutoAction a where a.name = :name and a.companyID =:comanyID";
            String companyId = CompanyThreadLocal.getInstance().getValue();
            HashMap map = new HashMap<String, String>();
            map.put("name", p_name);
            map.put("comanyID", companyId);
            Collection actions = HibernateUtil.search(hql, map);
            Iterator i = actions.iterator();
            autoAction = i.hasNext() ? (AutoAction) i.next() : null;
        }
        catch (Exception pe) {
            c_logger.error("Persistence Exception when retrieving auto activities "
                + p_name, pe);
        }
        
        return autoAction;
    }
    
    public boolean isActionExist(String p_name) {
        return getAutoActionByName(p_name) != null;
    }
    
    public void createAction(AutoAction p_action) 
        throws RemoteException, AutoActionException{
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
            
            throw new AutoActionException(
                AutoActionException.MSG_FAILED_TO_CREATE_action, 
                new String[]{p_action.getName()}, e);
        }
    }
    
    public AutoAction getActionByID(long p_id) {
        AutoAction autoAction = null;
        
        try
        {
            String hql = "from AutoAction a where a.id = :id and a.companyID =:comanyID";
            String companyId = CompanyThreadLocal.getInstance().getValue();
            HashMap map = new HashMap<String, String>();
            map.put("id", p_id);
            map.put("comanyID", companyId);
            Collection servers = HibernateUtil.search(hql, map);
            Iterator i = servers.iterator();
            autoAction = i.hasNext() ? (AutoAction) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when retrieving auto activities "
                    + p_id, pe);
        }
        return autoAction;
    }
    
    public void updateAction(AutoAction p_action) 
        throws RemoteException, AutoActionException {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            AutoAction oldAction = getActionByID(p_action.getId());
            
            if (oldAction != null) {
                oldAction.setName(p_action.getName());
                oldAction.setEmail(p_action.getEmail());
                oldAction.setDescription(p_action.getDescription());
                oldAction.setCompanyID(p_action.getCompanyID());
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
    
    public void removeAction(long p_id) 
        throws RemoteException, AutoActionException {
        Session session = null;
        Transaction transaction = null;
        
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            AutoAction oldAction = getActionByID(p_id);    
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
     * get all acitivities by the given auto action id.
     */
    public Collection getActivitiesByActionID(String p_actionID) 
        throws RemoteException, AutoActionException {
        Collection activities = null;
        
        if(p_actionID != null) {
            try {
                String hql = "from Activity a where a.isActive = 'Y' "
                             + "and a.autoActionID = :id";
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
}
