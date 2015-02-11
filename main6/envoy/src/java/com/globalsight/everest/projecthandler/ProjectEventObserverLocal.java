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

package com.globalsight.everest.projecthandler;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * This class represents an observer of events that affect Project. The callers
 * notify the observer of an event that could affect the state of projects.
 * 
 * Note: event observers do not throw exceptions.
 */
public class ProjectEventObserverLocal implements ProjectEventObserver
{
    private static final Logger s_logger = Logger
            .getLogger(ProjectEventObserverLocal.class);

    /**
     * Notification that a termbase has changed its name. Update all projects
     * that refer to the old name so they refer to the new name.
     * 
     * @param p_oldName
     *            old name of the termbase
     * @param p_newName
     *            new name of the termbase
     */
    public void notifyTermbaseRenamed(String p_oldName, String p_newName)
            throws RemoteException
    {
        try
        {
            String hql = "from ProjectImpl p where p.termbase = :oldName";
            HashMap map = new HashMap();
            map.put("oldName", p_oldName);

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and p.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentId));
            }

            List projects = HibernateUtil.search(hql, map);
            if (projects != null)
            {
                for (int i = 0; i < projects.size(); i++)
                {
                    ProjectImpl project = (ProjectImpl) projects.get(i);
                    project.setTermbase(p_newName);
                }

                HibernateUtil.update(projects);
            }
        }
        catch (Throwable ex)
        {
            s_logger.error("Can't update projects", ex);
        }
    }

    /**
     * Notification that a termbase was deleted. Update all projects that used
     * that termbase.
     * 
     * @param p_name
     *            name of the deleted termbase.
     */
    public void notifyTermbaseDeleted(String p_name) throws RemoteException
    {
        notifyTermbaseRenamed(p_name, "");
    }
}
