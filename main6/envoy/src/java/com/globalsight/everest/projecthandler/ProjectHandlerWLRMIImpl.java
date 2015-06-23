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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import org.dom4j.Document;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfo;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.WorkflowInfos;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.everest.workflowmanager.WorkflowStatePosts;
import com.globalsight.exporter.ExporterException;
import com.globalsight.exporter.IExportManager;
import com.globalsight.importer.IImportManager;
import com.globalsight.importer.ImporterException;

/**
 * This class is responsible for managing projects and localization profiles
 * which are assigned to projects.
 */
public class ProjectHandlerWLRMIImpl extends RemoteServer implements
        ProjectHandlerWLRemote
{
    ProjectHandler m_localReference;

    public ProjectHandlerWLRMIImpl() throws RemoteException,
            ProjectHandlerException
    {
        super(ProjectHandler.SERVICE_NAME);
        m_localReference = new ProjectHandlerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // BEGIN: Localization Profiles ////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////
    public long addL10nProfile(L10nProfile p_l10nProfile)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.addL10nProfile(p_l10nProfile);
    }

    public void duplicateL10nProfile(long p_profileId, String p_newName,
            Collection p_localePairs, String p_displayRoleName)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.duplicateL10nProfile(p_profileId, p_newName,
                p_localePairs, p_displayRoleName);

    }

    public void modifyL10nProfile(L10nProfile p_l10nProfile,
            Vector<WorkflowInfos> workflowInfos, long originalId)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.modifyL10nProfile(p_l10nProfile, workflowInfos,
                originalId);
    }

    public Collection getAllL10nProfiles() throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getAllL10nProfiles();
    }

    public Collection getAllL10nProfilesData() throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getAllL10nProfilesData();
    }

    public Hashtable getAllL10nProfileNames() throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getAllL10nProfileNames();
    }

    public L10nProfile getL10nProfile(long p_profileId) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getL10nProfile(p_profileId);
    }

    public void removeL10nProfile(L10nProfile p_l10nProfile)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.removeL10nProfile(p_l10nProfile);
    }

    public Vector getAllL10nProfilesForGUI() throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getAllL10nProfilesForGUI();
    }
    
    public Vector getAllL10nProfilesForGUI(String[] filterParams, Locale uiLocale) throws RemoteException,
    ProjectHandlerException
    {
        return m_localReference.getAllL10nProfilesForGUI(filterParams, uiLocale);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // END: Localization Profiles //////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////
    // BEGIN: Projects /////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////
    public void addProject(Project p_project) throws RemoteException,
            ProjectHandlerException
    {
        m_localReference.addProject(p_project);
    }

    public Project createProject() throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.createProject();
    }

    public void deleteProject(Project p_project) throws RemoteException,
            ProjectHandlerException
    {
        m_localReference.deleteProject(p_project);
    }

    public Collection<Project> getAllProjects() throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getAllProjects();
    }

    public Hashtable getAllProjectNames() throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getAllProjectNames();
    }

    public Project getProjectById(long p_id) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getProjectById(p_id);
    }

    public Project getProjectByName(String p_name) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getProjectByName(p_name);
    }

    public List<Project> getProjectsByUserPermission(User p_user)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getProjectsByUserPermission(p_user);
    }

    public List getProjectInfosManagedByUser(User p_user, String p_module)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getProjectInfosManagedByUser(p_user, p_module);
    }

    public List getProjectsManagedByUser(User p_user, String p_module)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getProjectsManagedByUser(p_user, p_module);
    }

    public Collection getProjectsByProjectManager(User p_user)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getProjectsByProjectManager(p_user);
    }

    public List getProjectInfosByUser(String p_userId) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getProjectInfosByUser(p_userId);
    }

    public List getProjectsByUser(String p_userId) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getProjectsByUser(p_userId);
    }

    public List getProjectsByVendor(long p_vendorId) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getProjectsByVendor(p_vendorId);
    }

    public Collection getProjectsByWorkflowInstanceId(
            long[] p_workflowInstanceIds) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference
                .getProjectsByWorkflowInstanceId(p_workflowInstanceIds);
    }

    public void modifyProject(Project p_project, String p_modifierId)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.modifyProject(p_project, p_modifierId);
    }

    /**
     * @see ProjectHandler.addUserToProjects(String, List)
     */
    public void addUserToProjects(String p_userId, List p_projects)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.addUserToProjects(p_userId, p_projects);
    }

    /**
     * @see ProjectHandler.removeUserFromProjects(String, List)
     */
    public void removeUserFromProjects(String p_userId, List p_projects)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.removeUserFromProjects(p_userId, p_projects);
    }

    /**
     * @see ProjectHandler.associateUserWithProjectsById(String, List)
     */
    public void associateUserWithProjectsById(String p_userId, List p_projectIds)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.associateUserWithProjectsById(p_userId, p_projectIds);
    }

    /**
     * @see ProjectHandler.getAllPossibleUserInfos(User)
     */
    public List<UserInfo> getAllPossibleUserInfos(User p_manager)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getAllPossibleUserInfos(p_manager);
    }

    /**
     * @see ProjectHandler.getUserIdsInSameProjects(User)
     */
    public List getUserIdsInSameProjects(User p_user) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getUserIdsInSameProjects(p_user);
    }

    /**
     * @see ProjectHandler.getProjectsWithUsers(List)
     */
    public HashMap getProjectsWithUsers(List p_users) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getProjectsWithUsers(p_users);
    }

    /**
     * Get the L10nProfiles associated with the projects.
     *
     * @param p_projects
     *            Collection of Projects.
     * @returns List of L10nProfiles associated with the Projects.
     */
    public Collection<L10nProfile> getL10nProfiles(
            Collection<Project> p_projects) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getL10nProfiles(p_projects);
    }

    public List<ProjectInfo> getAllProjectInfosForGUI() throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getAllProjectInfosForGUI();
    }

    public List<ProjectInfo> getAllProjectInfosForGUIbyCondition(
            String condition) throws RemoteException, ProjectHandlerException
	{
		return m_localReference.getAllProjectInfosForGUIbyCondition(condition);
	}

    public IImportManager getProjectDataImportManager(User p_user,
            long p_projectId) throws RemoteException, ProjectHandlerException,
            ImporterException
    {
        return m_localReference
                .getProjectDataImportManager(p_user, p_projectId);
    }

    public IExportManager getProjectDataExportManager(User p_user,
            long p_projectId) throws RemoteException, ProjectHandlerException,
            ExporterException
    {
        return m_localReference
                .getProjectDataExportManager(p_user, p_projectId);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // END: Projects ///////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Workflow Template Info
    // ////////////////////////////////////////////////////////////////////
    /**
     * Create a workflow template info object (contains the workflow template
     * designed via the graphical workflow UI).
     *
     * @param p_workflowTemplateInfo
     *            - The workflow template info to be created.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public void createWorkflowTemplateInfo(
            WorkflowTemplateInfo p_workflowTemplateInfo)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.createWorkflowTemplateInfo(p_workflowTemplateInfo);
    }

    public void duplicateWorkflowTemplates(long p_wfTemplateInfoId,
            String p_newName, Project project,
            WorkflowTemplate p_iflowTemplate, Collection p_localePairs,
            String p_displayRoleName) throws RemoteException,
            ProjectHandlerException
    {
        m_localReference.duplicateWorkflowTemplates(p_wfTemplateInfoId,
                p_newName, project, p_iflowTemplate, p_localePairs,
                p_displayRoleName);
    }

    /**
     * Duplicate a workflow template info object based on the given new name
     * (contains the workflow template designed via the graphical workflow UI).
     *
     * @param p_newName
     *            - The new name given to the duplicated workflow template info.
     * @param p_wfTemplateInfo
     *            - The workflow template info to be duplicated.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public WorkflowTemplateInfo duplicateWorkflowTemplate(String p_newName,
            long p_wfTemplateInfoId, LocalePair p_localePair,
            WorkflowTemplate p_iflowTemplate, String p_displayRoleName)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.duplicateWorkflowTemplate(p_newName,
                p_wfTemplateInfoId, p_localePair, p_iflowTemplate,
                p_displayRoleName);
    }

    /**
     * @see ProjectHandler.duplicateWorkflowTemplate(String, long,
     *      WorkflowTemplate)
     */
    public WorkflowTemplateInfo duplicateWorkflowTemplate(String p_newName,
            long p_wfTemplateInfoId, WorkflowTemplate p_iflowTemplate)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.duplicateWorkflowTemplate(p_newName,
                p_wfTemplateInfoId, p_iflowTemplate);
    }

    /**
     * Import a workflow template info object based on the given new name
     *
     * @param p_newName
     *            - The new name given to the workflow template info.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public void importWorkflowTemplates(Document doc, String p_newName,
            Collection p_localePairs, String p_displayRoleName, String projectId)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.importWorkflowTemplates(doc, p_newName, p_localePairs,
                p_displayRoleName, projectId);
    }

    /**
     * @see ProjectHandler.findWorkflowTemplates(WfTemplateSearchParameters)
     */
    public Collection findWorkflowTemplates(
            WfTemplateSearchParameters p_searchParameters)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.findWorkflowTemplates(p_searchParameters);
    }

    /**
     * @see ProjectHandler.replaceWorkflowTemplateInL10nProfile(long, long)
     */
    public L10nProfile replaceWorkflowTemplateInL10nProfile(
            long p_l10nProfileId, long p_workflowTemplateInfoId)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.replaceWorkflowTemplateInL10nProfile(
                p_l10nProfileId, p_workflowTemplateInfoId);
    }

    /**
     * Get a workflow template info object for the given id.
     *
     * @param p_wfTemplateInfoId
     *            - The id of the worklfow to be queried.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public WorkflowTemplateInfo getWorkflowTemplateInfoById(
            long p_wfTemplateInfoId) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getWorkflowTemplateInfoById(p_wfTemplateInfoId);
    }

    /**
     * Get a list of all active workflow template infos.
     *
     * @return A list of workflow template info objects.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public Collection<WorkflowTemplateInfo> getAllWorkflowTemplateInfos()
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getAllWorkflowTemplateInfos();
    }

    /**
     * @see ProjectHandler.getAllWorkflowTemplateInfosByLocalePair(long, long)
     */
    public Collection getAllWorkflowTemplateInfosByLocalePair(
            LocalePair p_localPair) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference
                .getAllWorkflowTemplateInfosByLocalePair(p_localPair);
    }

    /**
     * @see ProjectHandler.getAllWorkflowTemplateInfosByLocalePair(long, long)
     */
    public Collection getAllWorkflowTemplateInfosByL10nProfileId(Job p_job)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference
                .getAllWorkflowTemplateInfosByL10nProfileId(p_job);
    }

    /**
     * Modify a workflow template info object (contains the workflow template
     * designed via the graphical workflow UI).
     *
     * @param p_workflowTemplateInfo
     *            - The workflow template info to be updated.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public void modifyWorkflowTemplate(
            WorkflowTemplateInfo p_workflowTemplateInfo)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.modifyWorkflowTemplate(p_workflowTemplateInfo);
    }

    /**
     * Remove the workflow template info object based on the given id. The
     * remove process will check for dependencies first. If there are no
     * dependencies, it'll remove the workflow template info. Otherwise, an
     * exception will be thrown indicating the dependencies.
     *
     * @param p_wfTemplateInfoId
     *            - The id of the workflow template info to be removed.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public void removeWorkflowTemplate(long p_wfTemplateInfoId)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.removeWorkflowTemplate(p_wfTemplateInfoId);
    }

    /*
     * @see ProjectHandler.removeWorkflowTemplatesByLocalePair(LocalePair
     * p_localPair)
     */
    public void removeWorkflowTemplatesByLocalePair(LocalePair p_localPair)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.removeWorkflowTemplatesByLocalePair(p_localPair);
    }

    public Collection getAllWorkflowTemplateInfosByParameters(
            long p_sourceLocaleId, long p_targetLocaleId, long p_projectId)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getAllWorkflowTemplateInfosByParameters(
                p_sourceLocaleId, p_targetLocaleId, p_projectId);
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Workflow Template Info
    // ////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////
    // Beginning: Translation Memory Profile
    // ////////////////////////////////////////////////////////////////////
    public void createTranslationMemoryProfile(
            TranslationMemoryProfile p_tmProfile) throws RemoteException,
            ProjectHandlerException
    {
        m_localReference.createTranslationMemoryProfile(p_tmProfile);
    }

    public Collection getAllProjectTMs() throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getAllProjectTMs();
    }

    public Collection getAllTMProfiles() throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getAllTMProfiles();
    }

    public void modifyTranslationMemoryProfile(
            TranslationMemoryProfile p_tmProfile) throws RemoteException,
            ProjectHandlerException
    {
        m_localReference.modifyTranslationMemoryProfile(p_tmProfile);
    }

    public TranslationMemoryProfile getTMProfileById(long p_tmProfileId,
            boolean p_editable) throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getTMProfileById(p_tmProfileId, p_editable);
    }

    public void createProjectTM(ProjectTM p_projectTM) throws RemoteException,
            ProjectHandlerException
    {
        m_localReference.createProjectTM(p_projectTM);
    }

    public void modifyProjectTM(ProjectTM p_projectTM) throws RemoteException,
            ProjectHandlerException
    {
        m_localReference.modifyProjectTM(p_projectTM);
    }

    public ProjectTM getProjectTMById(long p_projectTMId, boolean p_editable)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getProjectTMById(p_projectTMId, p_editable);
    }

    public ProjectTM getProjectTMByName(String p_projectTmName,
            boolean p_editable) throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getProjectTMByName(p_projectTmName, p_editable);
    }
    
    public ProjectTM getProjectTMByTm3id(long p_tm3id) throws RemoteException,
            ProjectHandlerException
    {
        return m_localReference.getProjectTMByTm3id(p_tm3id);
    }

    public void removeProjectTm(long p_tmId) throws RemoteException,
            ProjectHandlerException
    {
        m_localReference.removeProjectTm(p_tmId);
    }

    public Collection findFileProfileTemplates(
            FileProfileSearchParameters p_searchParameters)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.findFileProfileTemplates(p_searchParameters);
    }

    public L10nProfileWFTemplateInfo getL10nProfileWfTemplateInfo(
            long l10nProfileId, long workflowId)
    {

        return m_localReference.getL10nProfileWfTemplateInfo(l10nProfileId,
                workflowId);
    }

    /*
     * public void saveL10nProfileWfTemplateInfo( L10nProfileWFTemplateInfo
     * l10nProfileWFTemplateInfo) {
     * m_localReference.saveL10nProfileWfTemplateInfo
     * (l10nProfileWFTemplateInfo); }
     *
     * @Override public void updateL10nProfileWfTemplateInfo(
     * L10nProfileWFTemplateInfo profileWFTemplateInfo) {
     * m_localReference.updateL10nProfileWfTemplateInfo(profileWFTemplateInfo);
     * }
     */

    @Override
    public boolean isPrimaryKeyExist(long lnprofileId, long workflowId)
    {

        return m_localReference.isPrimaryKeyExist(lnprofileId, workflowId);
    }

    public void removeTmProfile(TranslationMemoryProfile tmprofile)
            throws RemoteException, ProjectHandlerException
    {
        m_localReference.removeTmProfile(tmprofile);
    }

    public void setResourceBundle(ResourceBundle p_resourceBundle)
    {
        m_localReference.setResourceBundle(p_resourceBundle);
    }

    public HashSet getFileProfilesByProject(Project project)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getFileProfilesByProject(project);
    }

    public ArrayList<FileProfile> fileProfileListTerminology(Project project)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.fileProfileListTerminology(project);
    }

    public List<ProjectImpl> getProjectsByTermbaseDepended(String terbaseName, long companyId)
    {
        return m_localReference.getProjectsByTermbaseDepended(terbaseName,companyId);
    }

    @Override
    public Project getProjectByNameAndCompanyId(String p_name, long companyId)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getProjectByNameAndCompanyId(p_name, companyId);
    }

    @Override
    public List<Project> getProjectsByCompanyId(long companyId)
    {
        return m_localReference.getProjectsByCompanyId(companyId);
    }

    @Override
    public Collection<ProjectTM> getAllProjectTMs(boolean isSuperAdmin)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getAllProjectTMs(isSuperAdmin);
    }

    @Override
    public Collection<ProjectTM> getAllProjectTMs(String cond)
            throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getAllProjectTMs(cond);
    }

    @Override
    public Collection<ProjectTM> getAllProjectTMs(boolean isSuperAdmin,
            String cond) throws RemoteException, ProjectHandlerException
    {
        return m_localReference.getAllProjectTMs(isSuperAdmin, cond);
    }

	@Override
	public Collection getL10ProfilesByProjectId(long projectId)
			throws RemoteException, ProjectHandlerException
	{
		return m_localReference.getL10ProfilesByProjectId(projectId);
	}

    @Override
    public List<WorkflowStatePosts> getAllWorkflowStatePostProfie(String[] filterParams)
    {
        return m_localReference.getAllWorkflowStatePostProfie(filterParams);
    }

    @Override
    public List<WorkflowStatePosts> getAllWorkflowStatePostInfos()
    {
        return m_localReference.getAllWorkflowStatePostInfos();
    }

    @Override
    public void createWfStatePostProfile(WorkflowStatePosts wfStatePost)
    {
        m_localReference.createWfStatePostProfile(wfStatePost);
    }

    @Override
    public WorkflowStatePosts getWfStatePostProfile(long wfStatePostId)
    {
        return m_localReference.getWfStatePostProfile(wfStatePostId);
    }

    @Override
    public void modifyWfStatePostProfile(WorkflowStatePosts wfstaPosts)
    {
        m_localReference.modifyWfStatePostProfile(wfstaPosts);
    }

    @Override
    public void removeWorkflowStatePost(WorkflowStatePosts wfstaPosts)
    {
        m_localReference.removeWorkflowStatePost(wfstaPosts);
    }
}
