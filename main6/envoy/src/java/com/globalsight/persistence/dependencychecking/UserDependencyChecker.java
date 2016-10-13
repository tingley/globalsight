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
package com.globalsight.persistence.dependencychecking;

// globalsight
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.l10nprofile.WorkflowTemplateInfoDescriptorModifier;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Checks for objects that may have dependencies on a particular User. The class
 * dependencies are hard-coded into this class.
 */
public class UserDependencyChecker extends DependencyChecker
{
    private static final Logger c_logger = Logger
            .getLogger(UserDependencyChecker.class);

    /**
     * Returns the dependencies in a Vector of Objects. The vector contains all
     * objects that are dependent on the UserImpl object being passed in.
     * 
     * @param p_object
     *            The object to test if there are dependencies on.
     * @return A vector of all objects that are dependent on p_object.
     * @exception A
     *                general exception that wraps the specific exception of why
     *                retrieving the dependencies failed.
     * @exception DependencyCheckException
     */
    protected Vector findDependencies(PersistentObject p_object)
            throws DependencyCheckException
    {
        if (p_object.getClass() != UserImpl.class)
        {
            String args[] =
            { this.getClass().getName(), p_object.getClass().getName() };
            throw new DependencyCheckException(
                    DependencyCheckException.MSG_INVALID_OBJECT, args, null);
        }

        UserImpl user = (UserImpl) p_object;

        Vector deps = findProjectDependencies(user);
        deps.addAll(findWorkflowDependencies(user));
        return deps;
    }

    /**
     * Return the Projects that are dependent on this User.
     * 
     * @param p_user
     * @return
     * @exception DependencyCheckException
     */
    private Vector findProjectDependencies(UserImpl p_user)
            throws DependencyCheckException
    {
        Vector projects = new Vector();
        try
        {
            // if the user can manage projects
            PermissionSet permSet = Permission.getPermissionManager()
                    .getPermissionSetForUser(p_user.getUserId());
            if (permSet.getPermissionFor(Permission.PROJECTS_MANAGE))
            {
                // Query to see if this project manager has any
                // projects assigned to them.
                String hql = "from ProjectImpl p where p.managerUserId = :uId";
                HashMap map = new HashMap();
                map.put("uId", p_user.getUserId());
                String currentId = CompanyThreadLocal.getInstance().getValue();
                if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
                {
                    hql += " and p.companyId = :companyId";
                    map.put("companyId", Long.parseLong(currentId));
                }
                projects = new Vector(HibernateUtil.search(hql, map));
            }
        }
        catch (Exception pe)
        {
            StringBuffer errorMessage = new StringBuffer(
                    "Failed to query for all projects dependent on Project Mgr user.");
            errorMessage.append(p_user.getUserName());

            c_logger.error(errorMessage.toString(), pe);

            String[] args =
            { p_user.getUserName() };
            throw new DependencyCheckException(
                    DependencyCheckException.FAILED_PROJECT_DEPENDENCIES_FOR_USER,
                    args, pe);
        }
        return projects;
    }

    /**
     * Return the Workflow Templates that are dependent on this Workflow Manager
     * User or Project Manager.
     * 
     * @param p_user
     * @return Vector of Workflow Templates
     * @exception DependencyCheckException
     */
    private Vector findWorkflowDependencies(UserImpl p_user)
            throws DependencyCheckException
    {
        Vector workflows = new Vector();
        try
        {
            PermissionSet permSet = Permission.getPermissionManager()
                    .getPermissionSetForUser(p_user.getUserId());
            Vector args = new Vector();
            args.add(p_user.getUserId());

            // first check if this person can manage projects
            if (permSet.getPermissionFor(Permission.PROJECTS_MANAGE) == true)
            {
                String sql = WorkflowTemplateInfoDescriptorModifier.TEMPLATE_BY_PROJECT_MANAGER_ID_SQL;
                Map map = new HashMap();
                map.put("projectManagerId", p_user.getUserId());

                workflows = new Vector(HibernateUtil.searchWithSql(sql, map,
                        WorkflowTemplateInfo.class));
            }
            else if (permSet
                    .getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS))
            {
                String sql = WorkflowTemplateInfoDescriptorModifier.TEMPLATE_BY_WF_MGR_ID_SQL;
                Map map = new HashMap();
                map.put("workflowManagerId", p_user.getUserId());

                workflows = new Vector(HibernateUtil.searchWithSql(sql, map,
                        WorkflowTemplateInfo.class));
            }
        }
        catch (Exception pe)
        {
            StringBuffer errorMessage = new StringBuffer(
                    "Failed to query for all workflows dependent on user.");
            errorMessage.append(p_user.getUserName());

            c_logger.error(errorMessage.toString(), pe);

            String[] args =
            { p_user.getUserName() };
            throw new DependencyCheckException(
                    DependencyCheckException.FAILED_WORKFLOW_DEPENDENCIES_FOR_USER,
                    args, pe);
        }

        return workflows;
    }
}
