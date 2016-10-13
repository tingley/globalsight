package com.globalsight.everest.webapp.pagehandler.administration.users;

import java.util.*;

import org.apache.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.jobhandler.JobHandlerWLRemote;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class SetDefaultRoleUtil
{
	private static Logger c_logger = 
		Logger.getLogger(SetDefaultRoleUtil.class.getName());

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ArrayList<UserDefaultRole> getDefaultRolesByUser(
            String p_userId)
    {
        ArrayList<UserDefaultRole> roles = new ArrayList<UserDefaultRole>();
        if (p_userId == null || p_userId.trim().equals(""))
            return roles;

        try
        {
            String hql = "from UserDefaultRole a where a.userId=:userId";
            HashMap map = new HashMap();
            map.put("userId", p_userId);
            roles = new ArrayList(HibernateUtil.search(hql, map));
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }
        return roles;
    }
	
    public static UserDefaultRole getDefaultRole(long id)
    {
        return HibernateUtil.get(UserDefaultRole.class, id);
    }
	
    public static HashMap<String, UserDefaultRole> convert(
            ArrayList<UserDefaultRole> p_roles)
    {
        HashMap<String, UserDefaultRole> result = new HashMap<String, UserDefaultRole>();
        if (p_roles == null || p_roles.size() == 0)
            return result;
        UserDefaultRole role = null;
        for (int i = 0; i < p_roles.size(); i++)
        {
            role = p_roles.get(i);
            result.put(
                    role.getSourceLocaleId() + "_" + role.getTargetLocaleId(),
                    role);
        }
        return result;
    }
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean isExist(UserDefaultRole p_role)
    {
        if (p_role == null)
            return false;
        try
        {
            String hql = "from UserDefaultRole a where a.sourceLocaleId=:sId and a.targetLocaleId=:tId and a.userId=:userId";
            HashMap map = new HashMap();
            map.put("sId", p_role.getSourceLocaleId());
            map.put("tId", p_role.getTargetLocaleId());
            map.put("userId", p_role.getUserId());

            Collection result = HibernateUtil.search(hql, map);
            if (result != null && result.size() > 0)
                return true;
            else
                return false;
        }
        catch (Exception e)
        {
            return false;
        }
    }
	
    @SuppressWarnings("rawtypes")
    public static void saveDefaultRoles(String p_userId,
            ArrayList<UserDefaultRole> p_roles)
    {
        UserDefaultRole role = null;
        if (p_roles == null)
            return;
        try
        {
            Session session = HibernateUtil.getSession();
            Transaction transaction = session.beginTransaction();
            ArrayList<UserDefaultRole> oldRoles = getDefaultRolesByUser(p_userId);
            for (int i = 0; i < oldRoles.size(); i++)
            {
                // Remove the removed roles
                role = oldRoles.get(i);
                if (contained(p_roles, role) == null)
                {
                    HibernateUtil.delete(role);
                }
            }
            // Add or modify existed default role
            UserDefaultRole curRole = null;
            for (int i = 0; i < p_roles.size(); i++)
            {
                role = p_roles.get(i);
                curRole = contained(oldRoles, role);

                if (curRole == null)
                {
                    // New default role
                    session.save(role);
                }
                else
                {
                    if (role.getStatus().equals(UserDefaultRole.EDIT))
                    {
                        // Set oldAcs = curRole.getActivities();
                        removeActivity(curRole);
                        UserDefaultActivity uda = null;
                        for (Iterator it = role.getActivities().iterator(); it
                                .hasNext();)
                        {
                            uda = (UserDefaultActivity) it.next();
                            if (!curRole.getActivities().contains(uda))
                            {
                                uda.setDefaultRole(curRole);
                                curRole.getActivities().add(uda);
                            }
                        }
                        // curRole.setActivities(role.getActivities());
                        session.update(curRole);
                    }
                }
            }
            transaction.commit();
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }
    }
	
    private static UserDefaultRole contained(ArrayList<UserDefaultRole> p_list,
            UserDefaultRole p_role)
    {
        if (p_list == null || p_list.size() == 0)
            return null;
        if (p_role == null)
            return null;
        UserDefaultRole role = null;
        for (int i = 0; i < p_list.size(); i++)
        {
            role = p_list.get(i);
            if (role.getSourceLocaleId() == p_role.getSourceLocaleId()
                    && role.getTargetLocaleId() == p_role.getTargetLocaleId()
                    && role.getUserId().equals(p_role.getUserId()))
                return role;
        }
        return null;
    }
	
    public static void removeActivity(UserDefaultRole p_role)
    {
        try
        {
            UserDefaultActivity uda = null;
            for (Iterator it = p_role.getActivities().iterator(); it.hasNext();)
            {
                uda = (UserDefaultActivity) it.next();
                uda.setDefaultRole(null);
                HibernateUtil.delete(uda);
                it.remove();
            }
            // HibernateUtil.saveOrUpdate(p_role);
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static void setUserDefaultRoleToProject(Project p_project) {
		if (p_project == null)
			return;
		try {
			UserManager userManager = ServerProxy.getUserManager();
			LocaleManager localeManager = ServerProxy.getLocaleManager();
			JobHandlerWLRemote jobHandler = ServerProxy.getJobHandler();
			
			User user = null;
			String userId = "";
			HashMap<String, UserDefaultRole> defaultRoles = null;
			ArrayList<Role> roles = null;
			Role role = null;
			String key = "";
			LocalePair lp = null;
			UserDefaultRole defaultRole = null;
			Set defaultActivities = null;
			UserDefaultActivity defAct = null;
			Hashtable<String, ArrayList<Activity>> existActivities = null;
			Activity activity = null;
			ArrayList<Activity> acs = null;
			
			Vector pairs = localeManager.getSourceTargetLocalePairs();

			//Get users who are in current project
			Set users = p_project.getUserIds();
			for (Iterator it = users.iterator(); it.hasNext();) {
				userId = (String)it.next();
				user = userManager.getUser(userId);
				if (user == null)
				    continue;
				if (jobHandler.getCompany(user.getCompanyName()) == null)
				    continue;
				if (jobHandler.getCompany(user.getCompanyName()).getId() > 1)
					continue;
				//Get default roles of current user
				defaultRoles = convert(getDefaultRolesByUser(userId));
				
				existActivities = new Hashtable<String, ArrayList<Activity>>();
				//To judge if the locale pair which are in default roles has the same locale pair in current company
				for (int j=0;j<pairs.size();j++) {
					lp = (LocalePair) pairs.get(j);
					key = lp.getSource().getId() + "_" + lp.getTarget().getId();
					if (defaultRoles.containsKey(key)) {
						//default role include locale pair with them in current company
						defaultRole = defaultRoles.get(key);
						defaultActivities = defaultRole.getActivities();
						acs = new ArrayList<Activity>();
						for (Iterator ita = defaultActivities.iterator(); ita.hasNext();) {
							//Verify activity
							defAct = (UserDefaultActivity)ita.next();
							activity = jobHandler.getActivityByDisplayName(defAct.getActivityName());
							if (activity != null) {
								//There is activity according with the same activity name defined in default role
								acs.add(activity);
							}
						}
						existActivities.put(key, acs);
					}
				}
				
				ArrayList<Activity> acs1 = null;
				UserRole ctRole = null;
				String[] locales = null;
				String sourceLocale, targetLocale;

				if (existActivities.size() > 0) {
					//Exist
					Collection userRoles = userManager.getUserRoles(user);
					if (userRoles == null) {
						//Current super user don't have any roles in current company
						for (Iterator keys = existActivities.keySet().iterator(); keys.hasNext();) {
							key = (String)keys.next();
							locales = key.split("_");
							acs1 = existActivities.get(key);
							for (int k = 0; k < acs1.size(); k++) {
								ctRole = userManager.createUserRole();
								((Role)ctRole).setActivity(acs1.get(k));
								((Role)ctRole).setSourceLocale(localeManager.getLocaleById(Long.parseLong(locales[0])).toString());
								((Role)ctRole).setTargetLocale(localeManager.getLocaleById(Long.parseLong(locales[1])).toString());
								ctRole.setUser(userId);
	
								Role cRole2 = userManager.getContainerRole(((Role)ctRole).getActivity(),
										((Role)ctRole).getSourceLocale(), ((Role)ctRole)
		                                        .getTargetLocale());
								String[] uids = new String[]{userId};
		                        if (cRole2 != null)
		                        {
		                            if (user.isActive())
		                            {
		                                userManager.addUsersToRole(uids, cRole2.getName());
		                                ((Role)ctRole).setState(User.State.ACTIVE);
		                            }
		                            userManager.addRole((Role)ctRole);
		                            userManager.addUsersToRole(uids, ((Role)ctRole).getName());
		                        }
							}
						}
					} else {
						//Get roles of current user
						roles = new ArrayList<Role>(userRoles);
						
						for (Iterator keys = existActivities.keySet().iterator(); keys.hasNext();) {
							key = (String)keys.next();
							locales = key.split("_");
							sourceLocale = localeManager.getLocaleById(Long.parseLong(locales[0])).toString();
							targetLocale = localeManager.getLocaleById(Long.parseLong(locales[1])).toString();
							acs1 = existActivities.get(key);
							
							//Remove existing user roles
                            for (int k = 0; k < roles.size(); k++)
                            {
                                role = (Role) roles.get(k);
                                if (role.getSourceLocale().equals(sourceLocale)
                                        && role.getTargetLocale().equals(
                                                targetLocale))
                                {
                                    acs1.remove(role.getActivity());
                                }
                            }
							
							removeProceedRole(roles, sourceLocale, targetLocale);
							
							for (int j = 0; j < acs1.size(); j++) {
								ctRole = userManager.createUserRole();
								((Role)ctRole).setActivity(acs1.get(j));
								((Role)ctRole).setSourceLocale(sourceLocale);
								((Role)ctRole).setTargetLocale(targetLocale);
								ctRole.setUser(userId);

								Role cRole2 = userManager.getContainerRole(((Role)ctRole).getActivity(),
										((Role)ctRole).getSourceLocale(), ((Role)ctRole)
		                                        .getTargetLocale());
								String[] uids = new String[]{userId};
		                        if (cRole2 != null)
		                        {
		                            if (user.isActive())
		                            {
		                                userManager.addUsersToRole(uids, cRole2.getName());
		                                ((Role)ctRole).setState(User.State.ACTIVE);
		                            }
		                            userManager.addRole((Role)ctRole);
		                            userManager.addUsersToRole(uids, ((Role)ctRole).getName());
		                        }
	
						        //userManager.addRole((Role)ctRole);
						        //userManager.addUsersToRole(new String[]{userId}, ((Role)ctRole).getName());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			c_logger.error(e.getMessage(), e);
		}
	}
	
    private static void removeProceedRole(ArrayList<Role> p_roles,
            String p_srcLocale, String p_tarLocale)
    {
        if (p_roles == null || p_roles.size() == 0)
            return;

        Role role = null;
        for (int i = 0; i < p_roles.size(); i++)
        {
            role = (Role) p_roles.get(i);
            if (role.getSourceLocale().equals(p_srcLocale)
                    && role.getTargetLocale().equals(p_tarLocale))
                p_roles.remove(role);
        }
    }
	
    public static void removeDefaultRoles(String p_userId)
    {
        ArrayList<UserDefaultRole> roles = null;
        UserDefaultRole role = null;
        try
        {
            roles = getDefaultRolesByUser(p_userId);
            if (roles == null)
                return;
            for (int i = 0; i < roles.size(); i++)
            {
                role = roles.get(i);
                HibernateUtil.delete(role);
            }
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }
    }
}
