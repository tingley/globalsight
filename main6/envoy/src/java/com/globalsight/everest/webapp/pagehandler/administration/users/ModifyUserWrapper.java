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

package com.globalsight.everest.webapp.pagehandler.administration.users;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.GeneralException;

/**
 * Helper class used by pagehandlers to contain code used to modify existing
 * users, assign them to the appropriate groups, and generate all their user
 * roles.
 */
public class ModifyUserWrapper extends CreateUserWrapper
{
    // used for logging errors
    private static final Logger c_logger = Logger
            .getLogger(ModifyUserWrapper.class);

    private UserFluxCalendar m_calendar = null;
    private Vector m_tmpRoles = new Vector();
    private ArrayList m_tmpDefaultRoles = new ArrayList();
    private HashMap<String, UserDefaultRole> m_tmpDefaultRolesHash = new HashMap<String, UserDefaultRole>();
    private Hashtable m_tmpSourceTargetMap = new Hashtable();

    /**
     * Default constructor.
     */
    public ModifyUserWrapper(UserManager p_userMgr, User p_userRequestingMod,
            User p_user) throws RemoteException, GeneralException,
            NamingException
    {
        super(p_userMgr, p_userRequestingMod);

        // Set the passed-in user object.
        m_user = p_user;

        // Need two sets of data in case user does a cancel. Cancel
        // only cancels the last set of changes. There may have been
        // many before a save. This user codes needs to be cleaned up
        // in a big way!
        Collection tmpRoles1 = m_userMgr.getUserRoles(m_user);

        if (tmpRoles1 != null)
        {
            m_roles = new Vector(tmpRoles1);
            m_tmpRoles = new Vector(tmpRoles1);
        }

        // Set up the default roles of current user
        ArrayList<UserDefaultRole> tmp = SetDefaultRoleUtil
                .getDefaultRolesByUser(m_user.getUserId());
        if (tmp != null)
        {
            m_defaultRoles = new ArrayList<UserDefaultRole>(tmp);
            m_tmpDefaultRoles = new ArrayList<UserDefaultRole>(tmp);
            HashMap<String, UserDefaultRole> hash = SetDefaultRoleUtil
                    .convert(tmp);
            m_defaultRolesHash = new HashMap<String, UserDefaultRole>(hash);
            m_tmpDefaultRolesHash = new HashMap<String, UserDefaultRole>(hash);
            ;
        }

        initSourceTargetMap(m_roles, m_sourceTargetMap);
        m_tmpSourceTargetMap = new Hashtable();
        initSourceTargetMap(m_tmpRoles, m_tmpSourceTargetMap);
    }

    /**
     * Called to commit the contents of the wrapper back to LDAP, using the
     * UserManager we've held onto.
     */
    public void commitWrapper(HttpSession p_session)
            throws EnvoyServletException
    {
        if (m_calendar != null)
        {
            // commit the calendar
            CalendarHelper.modifyUserCalendar(p_session, m_calendar);
        }

        commitWrapper();
    }

    /**
     * Called to commit the contents of the wrapper back to LDAP, using the
     * UserManager we've held onto. Calendar information is not updated.
     */
    public void commitWrapper() throws EnvoyServletException

    {
        try
        {
            m_userMgr.modifyUser(m_userPerformingAction, m_user, m_projects,
                    m_fieldSecurity, new ArrayList(m_roles));
            SetDefaultRoleUtil.saveDefaultRoles(m_user.getUserId(),
                    m_defaultRoles);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Initialize the source-target map from the Vector of roles.
     */
    public void initSourceTargetMap(Vector p_roles, Hashtable p_sourceTargetMap)
    {
        // Iterate through the list of roles.
        for (int i = 0; i < p_roles.size(); i++)
        {
            Role curRole = (Role) p_roles.get(i);
            // Get the source and target locale for each role.
            String sourceLocale = curRole.getSourceLocale();
            String targetLocale = curRole.getTargetLocale();

            // if (p_sourceTargetMap.containsKey(companyId)) {
            // Hashtable ht=(Hashtable)p_sourceTargetMap.get(companyId);
            // if(ht.containsKey(sourceLocale)){
            // ((Vector)ht.get(sourceLocale)).add(targetLocale);
            // }else{
            // Vector vTargets = new Vector();
            // vTargets.add(targetLocale);
            // ht.put(sourceLocale,vTargets);
            // }
            // } else {
            // Hashtable ht=new Hashtable();
            // Vector vTargets = new Vector();
            //
            // vTargets.add(targetLocale);
            // ht.put(sourceLocale,vTargets);
            //
            // p_sourceTargetMap.put(sourceLocale,ht);
            // }

            // Try to get the Vector of targets for this source from
            // the map.
            long companyId = curRole.getActivity().getCompanyId();
            Vector vTargets = (Vector) p_sourceTargetMap.get(sourceLocale + "="
                    + companyId);

            if (vTargets == null)
            {
                vTargets = new Vector();
                vTargets.addElement(targetLocale);
                p_sourceTargetMap.put(sourceLocale + "=" + companyId, vTargets);
            }
            else
            {
                // Otherwise, add the target if it doesn't already exist
                // in the Vector of targets.
                if (!(vTargets.contains(targetLocale)))
                {
                    vTargets.addElement(targetLocale);
                }
            }
            // Fenshid
            // if(p_sourceTargetMap instanceof HashtableWithCompanyId){
            // String companyId=curRole.getActivity().getCompanyId();
            // ((HashtableWithCompanyId)p_sourceTargetMap).setCompanyId(companyId);
            // }
        }
    }

    public void saveRoles()
    {
        m_roles = (Vector) m_tmpRoles.clone();
        m_sourceTargetMap = (Hashtable) m_tmpSourceTargetMap.clone();
    }

    public void cancelRoles()
    {
        m_tmpRoles = (Vector) m_roles.clone();
        m_tmpSourceTargetMap = (Hashtable) m_sourceTargetMap.clone();
    }

    /**
     * Generates a batch of user roles from a source/target locale pair, and a
     * Hashtable of activity-cost pairs.
     * 
     * @param p_sourceLocale
     *            <code>Locale.toString()</code> for the valid source locale.
     * @param p_targetLocale
     *            <code>Locale.toString()</code> for the valid target locale.
     * @param p_activityCostMap
     *            A Hashtable containing String names of activities as keys, and
     *            Float values of costs for those activities.
     */
    public void addUserRoles(String p_sourceLocale, String p_targetLocale,
            Hashtable p_activityCostMap) throws EnvoyServletException
    {
        addUserRoles(p_sourceLocale, p_targetLocale, p_activityCostMap,
                getCurCompanyId());
    }

    /**
     * Generates a batch of user roles from a source/target locale pair, and a
     * Hashtable of activity-cost pairs.
     * 
     * @param p_sourceLocale
     *            <code>Locale.toString()</code> for the valid source locale.
     * @param p_targetLocale
     *            <code>Locale.toString()</code> for the valid target locale.
     * @param p_activityCostMap
     *            A Hashtable containing String names of activities as keys, and
     *            Float values of costs for those activities.
     */
    public void addUserRoles(String p_sourceLocale, String p_targetLocale,
            Hashtable p_activityCostMap, String p_companyId)
            throws EnvoyServletException
    {
        try
        {
            Vector p_roles = getTmpRoles();
            Hashtable p_sourceTargetMap = getTmpSourceTargetMap();

            removeRoles(p_roles, p_sourceTargetMap, p_sourceLocale,
                    p_targetLocale, p_companyId);

            Enumeration eKeys = p_activityCostMap.keys();
            while (eKeys.hasMoreElements())
            {

                // Pull activity name/cost pair out of the hashtable.
                Activity curKey = (Activity) eKeys.nextElement();
                Vector params = (Vector) p_activityCostMap.get(curKey);
                String activity = (String) params.elementAt(0);
                UserRole userRole = m_userMgr.createUserRole();
                ((Role) userRole).setActivity(curKey);
                ((Role) userRole).setSourceLocale(p_sourceLocale);
                ((Role) userRole).setTargetLocale(p_targetLocale);
                userRole.setUser(m_user.getUserId());

                if (UserUtil.isJobCostingEnabled())
                {
                    long expense = Long.parseLong((String) params.elementAt(1));

                    Rate expenseRate;
                    expenseRate = (Rate) ServerProxy.getCostingEngine()
                            .getRate(expense);
                    if (expenseRate != null)
                    {
                        userRole.setRate((new Long(expenseRate.getId()))
                                .toString());
                    }
                    else
                    {
                        userRole.setRate("-1");
                    }
                    ((Role) userRole).addRate(expenseRate);
                }

                p_roles.addElement(userRole);
            }

            Vector vTargets = (Vector) p_sourceTargetMap.get(p_sourceLocale
                    + "=" + p_companyId);

            if (vTargets == null)
            {
                vTargets = new Vector();
                p_sourceTargetMap.put(p_sourceLocale + "=" + p_companyId,
                        vTargets);
            }

            if (!(vTargets.contains(p_targetLocale)))
            {
                vTargets.addElement(p_targetLocale);
            }
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    /**
     * Removes all roles for the specified source and target locales, and purges
     * the pairs form the source/target map, too.
     */
    public void removeRoles(Vector p_roles, Hashtable p_sourceTargetMap,
            String p_sourceLocale, String p_targetLocale, String p_companyId)
    {
        // Purge the pairs from the source/target map.
        Vector vTargets = (Vector) p_sourceTargetMap.get(p_sourceLocale + "="
                + p_companyId);
        if (vTargets != null)
        {
            for (int i = 0; i < vTargets.size(); i++)
            {
                String curTarget = (String) vTargets.get(i);
                if (curTarget.equalsIgnoreCase(p_targetLocale))
                {
                    vTargets.remove(curTarget);
                    break;
                }
            }

            // Now remove the roles from the Roles array we're maintaining.
            Enumeration eRoles = p_roles.elements();
            Role[] removeRole = new Role[p_roles.size()];
            int i = 0;
            while (eRoles.hasMoreElements())
            {
                Role curRole = (Role) eRoles.nextElement();
                if (curRole.getSourceLocale().equalsIgnoreCase(p_sourceLocale)
                        && curRole.getTargetLocale().equalsIgnoreCase(
                                p_targetLocale)
                        && String.valueOf(curRole.getActivity().getCompanyId())
                                .equals(p_companyId))
                {
                    if (curRole != null)
                    {
                        removeRole[i++] = curRole;
                    }
                }
            }
            for (i = 0; i < removeRole.length; i++)
            {
                p_roles.remove(removeRole[i]);
            }
        }
    }

    /**
     * Gets all the roles that have the specified source and target locale.
     */
    public Vector getRoles(String p_sourceLocale, String p_targetLocale)
    {
        Vector retVal = new Vector();

        for (int i = 0; i < m_tmpRoles.size(); i++)
        {
            Role curRole = (Role) m_tmpRoles.get(i);
            if (p_sourceLocale.equalsIgnoreCase(curRole.getSourceLocale())
                    && p_targetLocale.equalsIgnoreCase(curRole
                            .getTargetLocale()))
            {
                retVal.addElement(curRole);
            }
        }

        return retVal;
    }

    public Vector getTmpRoles()
    {
        return m_tmpRoles;
    }

    public Hashtable getTmpSourceTargetMap()
    {
        return m_tmpSourceTargetMap;
    }

    public void setCalendar(UserFluxCalendar p_calendar)
    {
        this.m_calendar = p_calendar;
    }

    public UserFluxCalendar getCalendar()
    {
        return m_calendar;
    }

    public void addDefaultRole(UserDefaultRole p_role)
    {
        if (p_role == null)
            return;
        UserDefaultRole oldRole = null;
        String key = p_role.getSourceLocaleId() + "_"
                + p_role.getTargetLocaleId();
        if (m_tmpDefaultRolesHash.containsKey(key))
        {
            modifyDefaultRole(p_role);
        }
        else
        {
            m_tmpDefaultRolesHash.put(key, p_role);
            m_tmpDefaultRoles.add(p_role);
        }
    }

    private void resetDefaultRole(UserDefaultRole p_role,
            UserDefaultRole p_newRole)
    {
        if (p_role == null)
            return;
        int i;
        for (i = 0; i < m_tmpDefaultRoles.size(); i++)
        {
            if (m_tmpDefaultRoles.get(i).equals(p_role))
                break;
        }
        m_tmpDefaultRoles.remove(i);
        m_tmpDefaultRoles.add(p_newRole);
    }

    public void removeDefaultRole(UserDefaultRole p_role)
    {
        if (p_role == null)
        {
            return;
        }
        UserDefaultRole role = null;
        String key = p_role.getSourceLocaleId() + "_"
                + p_role.getTargetLocaleId();
        m_tmpDefaultRolesHash.remove(key);
        m_tmpDefaultRoles.remove(p_role);
    }

    public void removeDefaultRole(String key)
    {
        UserDefaultRole role = null;
        if (m_tmpDefaultRolesHash.containsKey(key))
        {
            role = m_tmpDefaultRolesHash.get(key);
            m_tmpDefaultRoles.remove(role);
            m_tmpDefaultRolesHash.remove(key);
        }
    }

    public void modifyDefaultRole(UserDefaultRole p_role)
    {
        if (p_role == null)
            return;
        UserDefaultRole role = null;
        UserDefaultActivity ac = null;
        String key = generateKey(p_role);
        role = m_tmpDefaultRolesHash.get(key);
        // Set acs = p_role.getActivities();
        // for (Iterator it = acs.iterator(); it.hasNext();) {
        // ac = (UserDefaultActivity)it.next();
        // if (!role.getActivities().contains(ac))
        // role.getActivities().remove(ac);
        // else
        // role.getActivities().add(ac);
        // }
        role.setStatus(p_role.getStatus());
        role.setActivities(p_role.getActivities());
        m_tmpDefaultRolesHash.put(key, role);
        // resetDefaultRole(role, p_role);
    }

    private String generateKey(UserDefaultRole p_role)
    {
        if (p_role == null)
            return "";
        return p_role.getSourceLocaleId() + "_" + p_role.getTargetLocaleId();
    }

    public void saveDefaultRoles()
    {
        m_defaultRoles = (ArrayList) m_tmpDefaultRoles.clone();
        m_defaultRolesHash = (HashMap) m_tmpDefaultRolesHash.clone();
    }

    public void cancelDefaultRoles()
    {
        m_tmpDefaultRoles = (ArrayList) m_defaultRoles.clone();
        m_tmpDefaultRolesHash = (HashMap<String, UserDefaultRole>) m_defaultRolesHash
                .clone();
    }

    public ArrayList<UserDefaultRole> getTmpDefaultRoles()
    {
        ArrayList<UserDefaultRole> roles = new ArrayList<UserDefaultRole>();
        Set keys = m_tmpDefaultRolesHash.keySet();
        String key = "";
        for (Iterator it = keys.iterator(); it.hasNext();)
        {
            key = (String) it.next();
            roles.add((UserDefaultRole) m_tmpDefaultRolesHash.get(key));
        }
        return roles;
        // return m_tmpDefaultRoles;
    }

    public UserDefaultRole getDefaultRoleFromTmp(String id)
    {
        if (m_tmpDefaultRolesHash.containsKey(id))
            return m_tmpDefaultRolesHash.get(id);
        else
            return new UserDefaultRole();
    }

    public ArrayList<String> getTargetLocalesOfDefaultRole()
    {
        ArrayList<String> tls = new ArrayList<String>();
        if (m_tmpDefaultRolesHash == null)
            return tls;
        for (Iterator it = m_tmpDefaultRolesHash.keySet().iterator(); it
                .hasNext();)
        {
            tls.add((String) it.next());
        }
        return tls;
    }
}
