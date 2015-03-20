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
package com.globalsight.everest.webapp.pagehandler.administration.projects;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.projecthandler.ProjectInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

public class ProjectHandlerHelper
{
    private static Logger logger = Logger.getLogger(ProjectHandlerHelper.class
            .getName());

    /**
     * Add a project to the system.
     * <p>
     * 
     * @param p_project
     *            The project to add.
     * @exception EnvoyServletException. Failed
     *                to add the profile; the cause is indicated by the
     *                exception code.
     */
    public static void addProject(Project p_project)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getProjectHandler().addProject(p_project);
        }
        catch (ProjectHandlerException phe)
        {
            throw new EnvoyServletException(phe);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
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
     * Create a project.
     * <p>
     * 
     * @return Return the project that is created.
     * @exception EnvoyServletException. Failed
     *                to create the project; the cause is indicated by the
     *                exception code.
     */
    public static Project createProject() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().createProject();
        }
        catch (ProjectHandlerException phe)
        {
            throw new EnvoyServletException(phe);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
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
     * Delete a project from the system.
     * <p>
     * 
     * @param p_project
     *            The project to be deleted.
     * @exception EnvoyServletException. Failed
     *                to delete the project; the cause is indicated by the
     *                exception code.
     */
    public static void deleteProject(Project p_project)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getProjectHandler().deleteProject(p_project);
        }
        catch (ProjectHandlerException phe)
        {
            throw new EnvoyServletException(phe);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
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
     * Returns all the projects in the system.
     * <p>
     * 
     * @return Return all the projects in the system.
     * @exception EnvoyServletException. Miscellaneous
     *                exception, most likely occuring in the persistence
     *                component.
     */
    public static Collection<Project> getAllProjects()
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getAllProjects();
        }
        catch (ProjectHandlerException phe)
        {
            throw new EnvoyServletException(phe);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
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
     * Returns all the projects (as ProjectInfo) in the system.
     * <p>
     * 
     * @return Return all the projects (as ProjectInfo) in the system.
     * @exception EnvoyServletException. Miscellaneous
     *                exception, most likely occuring in the persistence
     *                component.
     */
    public static Collection<ProjectInfo> getAllProjectsForGUI()
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getAllProjectInfosForGUI();
        }
        catch (ProjectHandlerException phe)
        {
            throw new EnvoyServletException(phe);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
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
     * Get user matched the given uid.
     * <p>
     * 
     * @param p_userId
     *            - The user id
     * @return The user associated with the user id.
     * @exception EnvoyServletException
     */
    public static User getUser(String p_userId) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getUserManager().getUser(p_userId);
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
     * Modify a project in the system.
     * <p>
     * 
     * @param p_project
     *            The project to be modified.
     * @exception EnvoyServletException. Componenet
     *                related exception.
     */
    public static void modifyProject(Project p_project, String p_modifierId)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getProjectHandler().modifyProject(p_project,
                    p_modifierId);
        }
        catch (ProjectHandlerException phe)
        {
            throw new EnvoyServletException(phe);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
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
     * Retrieve an existing project.
     * <p>
     * 
     * @return Return an existing project.
     * @exception EnvoyServletException. Failed
     *                to access the project; the cause is indicated by the
     *                exception code.
     */
    public static Project getProjectById(long p_id)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getProjectById(p_id);
        }
        catch (ProjectHandlerException phe)
        {
            throw new EnvoyServletException(phe);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
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

    public static ProjectImpl getProjectByNameAndCompanyId(String p_name,
            long p_companyId)
    {
        try
        {
            return (ProjectImpl) ServerProxy.getProjectHandler()
                    .getProjectByNameAndCompanyId(p_name, p_companyId);
        }
        catch (Exception e)
        {
            logger.error(
                    "getProjectByNameAndCompanyId Error, with input name '"
                            + p_name + "' and companyId '" + p_companyId + "' "
                            + ": ", e);
        }

        return null;
    }

    /**
     * 
     * Returns all the projects (as Project) in the system.
     * <p>
     * 
     * @return Return all the projects in the system.
     * @exception EnvoyServletException.
     * 
     */
    public static List<Project> getProjectByUser(String p_id)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getProjectsByUser((p_id));
        }
        catch (ProjectHandlerException phe)
        {
            throw new EnvoyServletException(phe);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
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
     * Retrieve possible users for a project that a particular pm owns
     * <p>
     * 
     * @return Return an list of users.
     */
    public static List<UserInfo> getPossibleUsersForProject(User pm)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getAllPossibleUserInfos(pm);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
        }
    }

    /**
     * Extract data from users page and set in project.
     */
    public static void extractUsers(Project project,
            HttpServletRequest request, SessionManager sessionMgr)
            throws EnvoyServletException
    {
        // Set users
        String toField = (String) request.getParameter("toField");
        // First, make sure default users are in the list
        ArrayList<UserInfo> defUsers = (ArrayList<UserInfo>) sessionMgr
                .getAttribute("defUsers");
        TreeSet<String> addedUsers = new TreeSet<String>();
        for (int i = 0; i < defUsers.size(); i++)
        {
            addedUsers.add(((UserInfo) defUsers.get(i)).getUserId());
        }

        if (toField != null && !toField.equals(""))
        {
            String[] userids = toField.split(",");
            for (int i = 0; i < userids.length; i++)
            {
                addedUsers.add(userids[i]);
            }
        }
        project.setUserIds(addedUsers);
    }

    /**
     * Set Project Data from request
     * 
     * @param p_updatePm
     *            Whether update the Project PM
     */
    public static void setData(ProjectImpl p_project,
            HttpServletRequest p_request, boolean p_updatePm)
            throws EnvoyServletException
    {
        p_project.setName((String) p_request.getParameter("nameField"));
        p_project.setTermbaseName((String) p_request.getParameter("tbField"));

        // Update Project PM
        if (p_updatePm)
        {
            String pmName = (String) p_request.getParameter("pmField");
            if (pmName != null)
            {
                p_project.setProjectManager(ProjectHandlerHelper
                        .getUser(pmName));
            }
        }

        String attributeSetId = p_request.getParameter("attributeSet");
        AttributeSet attSet = null;
        if (attributeSetId != null)
        {
            long attSetId = Long.valueOf(attributeSetId);
            if (attSetId > 0)
            {
                attSet = HibernateUtil.get(AttributeSet.class, attSetId);
            }
            p_project.setAttributeSet(attSet);
        }

        p_project.setDescription((String) p_request.getParameter("descField"));

        String qpName = (String) p_request.getParameter("qpField");
        if ("-1".equals(qpName))
        {
            p_project.setQuotePerson(null);
        }
        else if ("0".equals(qpName))
        {
            p_project.setQuotePerson("0");
        }
        else
        {
            p_project.setQuotePerson(ProjectHandlerHelper.getUser(qpName));
        }

        float pmcost = 0.00f;
        try
        {
            pmcost = Float.parseFloat(p_request.getParameter("pmcost").trim()) / 100;
        }
        catch (Exception e)
        {
        }
        p_project.setPMCost(pmcost);

        int poRequired = Project.NO_PO_REQUIRED;
        if (p_request.getParameter("poRequired") != null)
        {
            poRequired = Project.PO_REQUIRED;
        }
        p_project.setPoRequired(poRequired);

        // Auto Send Options
        p_project.setAutoAcceptTrans("on".equalsIgnoreCase(p_request
                .getParameter("translationAA")));
        p_project.setAutoSendTrans("on".equalsIgnoreCase(p_request
                .getParameter("translationAS")));
        p_project.setReviewOnlyAutoAccept("on".equalsIgnoreCase(p_request
                .getParameter("reviewOnlyAA")));
        p_project.setReviewOnlyAutoSend("on".equalsIgnoreCase(p_request
                .getParameter("reviewOnlyAS")));
        p_project.setAutoAcceptPMTask("on".equalsIgnoreCase(p_request
                .getParameter("autoAcceptPMTask")));
        p_project.setReviewReportIncludeCompactTags("on"
                .equalsIgnoreCase(p_request
                        .getParameter("reviewReportIncludeCompactTags")));

        p_project.setCheckUnTranslatedSegments("on".equalsIgnoreCase(p_request
                .getParameter("checkUnTransSeg")));

        p_project.setSaveTranslationsEditReport("on".equalsIgnoreCase(p_request
                .getParameter("saveTranslationsEditReport")));
        p_project.setSaveReviewersCommentsReport("on"
                .equalsIgnoreCase(p_request
                        .getParameter("saveReviewersCommentsReport")));
        p_project.setSaveOfflineFiles("on".equalsIgnoreCase(p_request
                .getParameter("saveOfflineFiles")));
        p_project.setAllowManualQAChecks("on".equalsIgnoreCase(p_request
                .getParameter("allowManualQAChecks")));
        p_project.setAutoAcceptQATask("on".equalsIgnoreCase(p_request
                .getParameter("autoAcceptQATask")));
        p_project.setAutoSendQAReport("on".equalsIgnoreCase(p_request
                .getParameter("autoSendQAReport")));

        try
        {
            long companyId = p_project.getCompanyId();
            if (companyId == -1)
            {
                companyId = Long.parseLong(CompanyThreadLocal.getInstance()
                        .getValue());
            }
            boolean enableDitaChecks = ServerProxy.getJobHandler()
                    .getCompanyById(companyId).getEnableDitaChecks();
            if (enableDitaChecks)
            {
                p_project.setManualRunDitaChecks("on"
                        .equalsIgnoreCase(p_request
                                .getParameter("manualRunDitaQAChecks")));
                p_project.setAutoAcceptDitaQaTask("on"
                        .equalsIgnoreCase(p_request
                                .getParameter("autoAcceptDitaQaTask")));
                p_project.setAutoSendDitaQaReport("on"
                        .equalsIgnoreCase(p_request
                                .getParameter("autoSendDitaQaReport")));
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }
}
