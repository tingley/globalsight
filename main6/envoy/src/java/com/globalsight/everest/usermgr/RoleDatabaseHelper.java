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
package com.globalsight.everest.usermgr;

import java.util.List;

import com.globalsight.everest.foundation.ContainerRoleImpl;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * UserLdapHelper, a LDAP Helper class that helps convert data for LDAP
 * operations on the 'User' object.
 */
public class RoleDatabaseHelper
{
   public static ContainerRoleImpl getContainerRoleByName(String name)
   {
	   String hql = "from ContainerRoleImpl c where c.name = ?";
	   return (ContainerRoleImpl) HibernateUtil.getFirst(hql, name);
   }
   
   public static List<ContainerRoleImpl> getContainerRoleByUserId(String id)
   {
	   String hql = "select c from ContainerRoleImpl c join c.userIds u where u = ?";
	   return (List<ContainerRoleImpl>) HibernateUtil.search(hql, id);
   }
   
   public static List<ContainerRoleImpl> getContainerRoleByCompanyId(String id)
   {
	   String hql = "from ContainerRoleImpl c where c.activity.companyId = ?";
	   return (List<ContainerRoleImpl>) HibernateUtil.search(hql, Long.parseLong(id));
   }
   
   public static List<UserRoleImpl> getUserRoleByCompanyId(String id)
   {
	   String hql = "from UserRoleImpl c where c.activity.companyId = ?";
	   return (List<UserRoleImpl>) HibernateUtil.search(hql, Long.parseLong(id));
   }
   
   public static UserRoleImpl getUserRoleByName(String name)
   {
	   String hql = "from UserRoleImpl u where u.name = ?";
	   return (UserRoleImpl) HibernateUtil.getFirst(hql, name);
   }
   
   public static UserRoleImpl getUserRoleByUserId(String id)
   {
	   String hql = "from UserRoleImpl u where u.user = ?";
	   return (UserRoleImpl) HibernateUtil.getFirst(hql, id);
   }
   
   public static Role getRoleByName(String name)
   {
	   UserRoleImpl r = getUserRoleByName(name);
	   if (r != null)
		   return r;
	   
	   return getContainerRoleByName(name);
   }
   
   public static void removeUserFromContainerRole(ContainerRoleImpl cr, List<String> uIds)
   {
	   if (cr == null || uIds == null || uIds.size() == 0)
		   return;
	   
       List<String> ids = cr.getUserIds();
       for (String uId : uIds)
       {
       	 ids.remove(uId);
       }
       
       HibernateUtil.saveOrUpdate(cr);
   }
   
   public static void removeUserFromContainerRole(ContainerRoleImpl cr, String uId)
   {
	   if (cr == null || uId == null)
		   return;
	   
       List<String> ids = cr.getUserIds();
       ids.remove(uId);
       
       HibernateUtil.saveOrUpdate(cr);
   }
}
