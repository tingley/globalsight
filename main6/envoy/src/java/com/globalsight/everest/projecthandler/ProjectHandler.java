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
import java.util.Vector;

import org.dom4j.Document;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfo;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.WorkflowInfos;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.everest.workflowmanager.WorkflowStatePosts;
import com.globalsight.exporter.ExporterException;
import com.globalsight.exporter.IExportManager;
import com.globalsight.importer.IImportManager;
import com.globalsight.importer.ImporterException;
import com.globalsight.util.GeneralException;

/**
 * This interface provides access to the project operations in Envoy.
 */

public interface ProjectHandler
{
    public static final String SERVICE_NAME = "ProjectHandler";

    // ////////////////////////////////////////////////////////////////////////////
    // BEGIN: Localization Profiles ////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Add a localization profile to the system.
     * <p>
     *
     * @param p_l10nProfile
     *            The localization profile to add.
     * @return Return the unique id assigned to the new profile.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Failed to add the profile; the cause is indicated by the
     *                exception code.
     */
    long addL10nProfile(L10nProfile p_l10nProfile) throws RemoteException,
            ProjectHandlerException;

    /**
     * Duplicate the specified localization profile.
     * <p>
     *
     * @param p_profileId
     *            The primary ID of the localization profile.
     * @return The duplicate copy of the specified localization profile.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    void duplicateL10nProfile(long p_profileId, String p_newName,
            Collection p_targetLocales, String p_displayRoleName)
            throws RemoteException, ProjectHandlerException;

    /**
     * Modify an existing localization profile in the system.
     * <p>
     *
     * @param p_l10nProfile
     *            The modified localization profile.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Failed to modify the profile; the cause is indicated by
     *                the exception code.
     */
    void modifyL10nProfile(L10nProfile p_l10nProfile,
            Vector<WorkflowInfos> workflowInfos, long originalLocId)
            throws RemoteException, ProjectHandlerException;

    /**
     * Remove the localization profile from the system.
     * <p>
     *
     * @param p_l10nProfile
     *            The localization profile to delete.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Failed to remove the profile; the cause is indicated by
     *                the exception code.
     */
    void removeL10nProfile(L10nProfile p_l10nProfile) throws RemoteException,
            ProjectHandlerException;

    /**
     * Returns all the localization profiles in the system.
     * <p>
     *
     * @return Return all the localization profiles as a vector.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    Collection getAllL10nProfiles() throws RemoteException,
            ProjectHandlerException;
    
	Collection getL10ProfilesByProjectId(long projectId)
			throws RemoteException, ProjectHandlerException;

    Collection getAllL10nProfilesData() throws RemoteException,
            ProjectHandlerException;
    /**
     * Get the names (and primary keys) of all the localization profiles. The
     * key in the hashtable is the primary key.
     * <p>
     *
     * @return Return all the localization profiles names as a hashtable.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    Hashtable getAllL10nProfileNames() throws RemoteException,
            ProjectHandlerException;

    /**
     * Returns all the localization profiles for GUI in the system.
     * <p>
     *
     * @return Return all the localization profiles for GUI as a vector.
     * @exception RemoteException
     *                System or network related exception.
     * @exception GeneralException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    Vector getAllL10nProfilesForGUI() throws RemoteException,
            ProjectHandlerException;
    
    Vector getAllL10nProfilesForGUI(String[] filterParams, Locale uiLocale) throws RemoteException,
    ProjectHandlerException;

    /**
     * Get a localization profile by its ID.
     * <p>
     *
     * @param p_profileId
     *            The primary ID of the localization profile.
     * @return The localization profile.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    L10nProfile getL10nProfile(long p_profileId) throws RemoteException,
            ProjectHandlerException;

    // ////////////////////////////////////////////////////////////////////////////
    // END: Localization Profiles //////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////
    // BEGIN: Projects /////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Add a project to the system.
     * <p>
     *
     * @param p_project
     *            The project to be added.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Failed to add the project; the cause is indicated by the
     *                exception code.
     */
    void addProject(Project p_project) throws RemoteException,
            ProjectHandlerException;

    /**
     * Create a project.
     * <p>
     *
     * @return Return the project that is created.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Failed to create the project; the cause is indicated by
     *                the exception code.
     */
    Project createProject() throws RemoteException, ProjectHandlerException;

    /**
     * Delete a project from the system.
     * <p>
     *
     * @param p_project
     *            The project to be deleted.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Failed to delete the project; the cause is indicated by
     *                the exception code.
     */
    void deleteProject(Project p_project) throws RemoteException,
            ProjectHandlerException;

    /**
     * Returns all the projects in the system.
     * <p>
     *
     * @return Return all the projects in the system.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     */
    Collection<Project> getAllProjects() throws RemoteException,
            ProjectHandlerException;

    /**
     * Returns all the project infos for GUI in the system.
     * <p>
     *
     * @return Return all the projects for GUI as a List.
     * @exception RemoteException
     *                System or network related exception.
     * @exception GeneralException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    List<ProjectInfo> getAllProjectInfosForGUI() throws RemoteException,
            ProjectHandlerException;

    List<ProjectInfo> getAllProjectInfosForGUIbyCondition(String condition)
            throws RemoteException, ProjectHandlerException;

    /**
     * Get the names (and primary keys) of all the projects. The key in the
     * hashtable is the primary key.
     * <p>
     *
     * @return All the names and keys of all projects.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    Hashtable getAllProjectNames() throws RemoteException,
            ProjectHandlerException;

    /**
     * Get project by the project id.
     * <p>
     *
     * @param p_id
     *            The project id.
     * @return Return the specified project.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     */
    Project getProjectById(long p_id) throws RemoteException,
            ProjectHandlerException;

    /**
     * Get project by the project name.
     * <p>
     *
     * @param p_name
     *            The project to be found under this name.
     * @return Return the specified project.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     */
    Project getProjectByName(String p_name) throws RemoteException,
            ProjectHandlerException;

    /**
     * Get project by the project name and company id.
     *
     * @param p_name The project to be found under this name.
     * @param companyId The company id.
     *
     * @return Return the specified project.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     */
    public Project getProjectByNameAndCompanyId(String p_name, long companyId)
            throws RemoteException, ProjectHandlerException;

    /**
     * Return all the projects the user has permission to access.
     *
     * This includes permissions like being able to get all projects, or just
     * the projects they manage, or the projects they are part of too.
     */
    List<Project> getProjectsByUserPermission(User p_user)
            throws RemoteException, ProjectHandlerException;

    /**
     * Get the information for each project the user specified manages for the
     * module specified. The user must be an admin or project manager for the
     * GlobalSight module or admin or vendor management for the Vendor
     * Management module.
     *
     * @param p_user
     *            The user that manages projects.
     * @param p_module
     *            The module that the projects are needed for. (@see
     *            com.globalsight.everest.permission.Permission for the valid
     *            module types)
     *
     * @return All project infos that the specific user manages, or empty if
     *         none.
     */
    List getProjectInfosManagedByUser(User p_user, String p_module)
            throws RemoteException, ProjectHandlerException;

    /**
     * @see ProjectHandlerLocal.getProjectsByProjectManager(User).
     *
     * @param p_user
     * @return
     * @throws RemoteException
     * @throws ProjectHandlerException
     */
    Collection<Project> getProjectsByProjectManager(User p_user)
            throws RemoteException, ProjectHandlerException;

    /**
     * Get the projects that the specified user manages for the module
     * specified. The user must be an admin or project manager for the
     * GlobalSight module or admin or vendor management for the Vendor
     * Management module.
     *
     * @param p_user
     *            The user that manages projects.
     * @param p_module
     *            The module that the projects are needed for. (@see
     *            com.globalsight.everest.permission.Permission for the valid
     *            module types)
     *
     * @return All projects that the specific user manages, or empty if none.
     */
    List<Project> getProjectsManagedByUser(User p_user, String p_module)
            throws RemoteException, ProjectHandlerException;

    /**
     * Get the project info of the projects this user is part of.
     */
    List<Project> getProjectInfosByUser(String p_userId)
            throws RemoteException, ProjectHandlerException;

    /**
     * Get the projects this user is part of.
     */
    List<Project> getProjectsByUser(String p_userId) throws RemoteException,
            ProjectHandlerException;

    /**
     * Get the projects this vendor is part of.
     */
    List getProjectsByVendor(long p_vendorId) throws RemoteException,
            ProjectHandlerException;

    /**
     * Get projects by the workflow instance ids.
     *
     * @param p_workflowInstanceIds
     *            An array of workflow instance ids.
     * @return Return all the projects by the specified id.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     */
    Collection getProjectsByWorkflowInstanceId(long[] p_workflowInstanceIds)
            throws RemoteException, ProjectHandlerException;

    /**
     * Modify a project in the system.
     * <p>
     *
     * @param p_modifierId
     *            - The modifier's user id.
     * @param p_project
     *            The project to be modified.
     * @exception RemoteException
     *                System of network related exception.
     * @exception ProjectHandlerException
     */
    void modifyProject(Project p_project, String p_modifierId)
            throws RemoteException, ProjectHandlerException;

    /**
     * Add the user to the specified list of projects.
     */
    void addUserToProjects(String p_userId, List p_projects)
            throws RemoteException, ProjectHandlerException;

    /**
     * Remove the user from the specified list of projects.
     */
    void removeUserFromProjects(String p_userId, List p_projects)
            throws RemoteException, ProjectHandlerException;

    /**
     * Associate the user with the specfied projects. This involved removing the
     * user from any current projects that aren't in the list and adding them to
     * any new projects that are in the specified list.
     *
     * @param p_userId
     *            The id of the user.
     * @param p_projectIs
     *            The list of project ids.
     */
    void associateUserWithProjectsById(String p_userId, List p_projectIds)
            throws RemoteException, ProjectHandlerException;

    /**
     * Return the list of basic information about all users that are associated
     * with projects that the specified user manages.
     */
    List<UserInfo> getAllPossibleUserInfos(User p_manager)
            throws RemoteException, ProjectHandlerException;

    /**
     * Returns a list of all distinct usernames that are in the same projects
     * that the specific user is in.
     */
    List getUserIdsInSameProjects(User p_user) throws RemoteException,
            ProjectHandlerException;

    /**
     * Returns a hashmap of the projects that the listed users are in. The users
     * can be in multiple projects. The hashmap's key is the project itself and
     * the value is a list of the users that are in the project. The list of
     * users ONLY includes users from the List "p_users" that was passed in .
     */
    HashMap getProjectsWithUsers(List p_users) throws RemoteException,
            ProjectHandlerException;

    /**
     * Get the L10nProfiles associated with the projects.
     *
     * @param p_projects
     *            Collection of Projects.
     * @returns List of L10nProfiles associated with the Projects.
     */
    public Collection<L10nProfile> getL10nProfiles(
            Collection<Project> p_projects) throws RemoteException,
            ProjectHandlerException;

    /**
     * Allocates a helper object to import project-related data into the
     * database.
     */
    public IImportManager getProjectDataImportManager(User p_user,
            long p_projectId) throws RemoteException, ProjectHandlerException,
            ImporterException;

    /**
     * Allocates a helper object to export project-related data from the
     * database to files (CSV or XML).
     */
    public IExportManager getProjectDataExportManager(User p_user,
            long p_projectId) throws RemoteException, ProjectHandlerException,
            ExporterException;

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
    void createWorkflowTemplateInfo(WorkflowTemplateInfo p_workflowTemplateInfo)
            throws RemoteException, ProjectHandlerException;

    /**
     * Duplicate a workflow template info object based on the given new name
     * (contains the workflow template designed via the graphical workflow UI).
     * This is not an exact duplicate. The tasks are assigned to "All Qualified
     * Users" and the rates are cleared out. This is because the locales may not
     * be the same.
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
    WorkflowTemplateInfo duplicateWorkflowTemplate(String p_newName,
            long p_wfTemplateInfoId, LocalePair p_localePair,
            WorkflowTemplate p_iflowTemplate, String p_displayRoleName)
            throws RemoteException, ProjectHandlerException;

    /**
     * Duplicate a workflow template info object based on the given new name
     * (contains the workflow template designed via the graphical workflow UI).
     * This is an EXACT duplicate - locale and all.
     *
     * @param p_newName
     *            - The new name given to the duplicated workflow template info.
     * @param p_wfTemplateInfoId
     *            - The id of the workflow template info to be duplicated.
     * @param p_iflowTemplate
     *            - The iflow template to duplicate.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    WorkflowTemplateInfo duplicateWorkflowTemplate(String p_newName,
            long p_wfTemplateInfoId, WorkflowTemplate p_iflowTemplate)
            throws RemoteException, ProjectHandlerException;

    /**
     * Duplicate the workflow template to the various locale pairs specified.
     * These aren't exact duplicates - the tasks are assigned to "All Qualified
     * Users" and the rates are at NO RATE.
     */
    void duplicateWorkflowTemplates(long p_origWFTempInfoId, String p_newName,
            Project project, WorkflowTemplate p_origIflowTemplate,
            Collection p_localePairs, String p_displayRoleName)
            throws RemoteException, ProjectHandlerException;

    /**
     * Import the workflow template to the various locale pairs specified.
     *
     * @param projectId
     */
    void importWorkflowTemplates(Document doc, String p_newName,
            Collection p_localePairs, String p_displayRoleName, String projectId)
            throws RemoteException, ProjectHandlerException;

    /**
     * Find a list of workflow template info objects based on the specified
     * search parameters.
     *
     * @param p_searchParameters
     *            The search parameters entered by the user.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    Collection findWorkflowTemplates(
            WfTemplateSearchParameters p_searchParameters)
            throws RemoteException, ProjectHandlerException;

    /**
     * Find a list of fileprofile template info objects based on the specified
     * search parameters.
     *
     * @param p_searchParameters
     *            The search parameters entered by the user.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    Collection findFileProfileTemplates(
            FileProfileSearchParameters p_searchParameters)
            throws RemoteException, ProjectHandlerException;

    /**
     * Replace the workflow template in the L10nProfile with the one being
     * passed in. Replace the one with the same target locale. Persist the
     * change.
     *
     * @p_l10nProfileId The id of the L10nProfile to replace the template in.
     * @p_workflowTemplateInfoId The id of the new template that will replace
     *                           the old one with the same target locale.
     */
    L10nProfile replaceWorkflowTemplateInL10nProfile(long p_l10nProfileId,
            long p_workflowTemplateInfoId) throws RemoteException,
            ProjectHandlerException;

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
    WorkflowTemplateInfo getWorkflowTemplateInfoById(long p_wkTemplateInfoId)
            throws RemoteException, ProjectHandlerException;

    /**
     * Get a collection of all active workflow template infos.
     *
     * @return A collection of workflow template info objects.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    Collection<WorkflowTemplateInfo> getAllWorkflowTemplateInfos()
            throws RemoteException, ProjectHandlerException;

    /**
     * Get a collection of all active workflow template infos with a particular
     * source and target locale.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    Collection getAllWorkflowTemplateInfosByLocalePair(LocalePair p_localPair)
            throws RemoteException, ProjectHandlerException;

    /**
     * Get a collection of all active workflow template infos that are
     * associated with the source and target locale and within the project
     * specified.
     *
     * @return A collection of workflow template info objects.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    Collection getAllWorkflowTemplateInfosByParameters(long p_sourceLocaleId,
            long p_targetLocaleId, long p_projectId) throws RemoteException,
            ProjectHandlerException;

    /**
     * Get a collection of all active workflow template infos;
     *
     * @return A collection of workflow template info objects.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    Collection getAllWorkflowTemplateInfosByL10nProfileId(Job p_job)
            throws RemoteException, ProjectHandlerException;

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
    void modifyWorkflowTemplate(WorkflowTemplateInfo p_workflowTemplateInfo)
            throws RemoteException, ProjectHandlerException;

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
    void removeWorkflowTemplate(long p_wfTemplateInfoId)
            throws RemoteException, ProjectHandlerException;

    /**
     * Get projects by company id.
     * @param companyId
     * @return
     */
    public List<Project> getProjectsByCompanyId(long companyId);

    /**
     * Create the Translation Memory Profile
     *
     * @param TranslationMemoryProfile
     * @exception RemoteException
     * @exception ProjectHandlerException
     */
    void createTranslationMemoryProfile(TranslationMemoryProfile p_tmProfile)
            throws RemoteException, ProjectHandlerException;

    /**
     *
     * Modify the Translation Memory Profile
     *
     * @param The
     *            Translation Memory Profile
     * @exception RemoteException
     * @exception ProjectHandlerException
     */
    void modifyTranslationMemoryProfile(TranslationMemoryProfile p_tmProfile)
            throws RemoteException, ProjectHandlerException;

    /**
     * Get a translation memory profile object for the given id.
     *
     * @param p_tmProfileId
     *            - The id of the tm profile to be queried.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    TranslationMemoryProfile getTMProfileById(long p_tmProfileId,
            boolean p_editable) throws RemoteException, ProjectHandlerException;

    /**
     * Get All ProjecT TMs
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception.
     */
    Collection<ProjectTM> getAllProjectTMs() throws RemoteException,
            ProjectHandlerException;

    /**
     * Get All ProjecT TMs
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception.
     */
    Collection<ProjectTM> getAllProjectTMs(String cond) throws RemoteException,
            ProjectHandlerException;

    /**
     * Get All ProjecT TMs
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception.
     */
    Collection<ProjectTM> getAllProjectTMs(boolean isSuperAdmin) throws RemoteException,
            ProjectHandlerException;

    /**
     * Get All ProjecT TMs
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception.
     */
    Collection<ProjectTM> getAllProjectTMs(boolean isSuperAdmin, String cond) throws RemoteException,
            ProjectHandlerException;

    /**
     * Create a new Project TM
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception.
     */
    void createProjectTM(ProjectTM p_projectTM) throws RemoteException,
            ProjectHandlerException;

    /**
     * Modify a new Project TM
     *
     * @exception RemoteException
     *                System or network related exception
     * @exception ProjectHandlerException
     *                Component specific exception.
     */
    void modifyProjectTM(ProjectTM p_projectTM) throws RemoteException,
            ProjectHandlerException;

    /**
     * Get a ProjectTM by Id
     *
     * @param p_projectTmId
     * @param p_editable
     * @return
     * @exception RemoteException
     * @exception ProjectHandlerException
     */
    ProjectTM getProjectTMById(long p_projectTmId, boolean p_editable)
            throws RemoteException, ProjectHandlerException;
    
    ProjectTM getProjectTMByTm3id(long p_tm3id) throws RemoteException,
            ProjectHandlerException;

    /**
     * Get a ProjectTM by Name
     *
     * @param p_projectTmName
     * @param p_editable
     * @return
     * @exception RemoteException
     * @exception ProjectHandlerException
     */
    ProjectTM getProjectTMByName(String p_projectTmName, boolean p_editable)
            throws RemoteException, ProjectHandlerException;

    /**
     * Remove a ProjectTM
     *
     * @param p_tmId
     *            ProjectTM id
     * @exception RemoteException
     * @exception ProjectHandlerException
     */
    void removeProjectTm(long p_tmId) throws RemoteException,
            ProjectHandlerException;

    /**
     * Get All TM Profiles
     *
     * @exception RemoteException
     * @exception ProjectHandlerException
     */
    Collection getAllTMProfiles() throws RemoteException,
            ProjectHandlerException;

    /**
     * Remove the workflow templates that are associated with the source and
     * target locale specified. This is used when removing a locale pair.
     *
     * @param p_localPair
     *            The localPair includes source locale id, target locale id and
     *            company id in the workflow template is associated with.
     *
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public void removeWorkflowTemplatesByLocalePair(LocalePair p_localPair)
            throws RemoteException, ProjectHandlerException;

    // ////////////////////////////////////////////////////////////////////
    // End: Workflow Template Info
    // ////////////////////////////////////////////////////////////////////
    public L10nProfileWFTemplateInfo getL10nProfileWfTemplateInfo(
            long l10nProfileId, long workflowId);

    /*
     * public void saveL10nProfileWfTemplateInfo(L10nProfileWFTemplateInfo
     * l10nProfileWFTemplateInfo);
     *
     * void updateL10nProfileWfTemplateInfo( L10nProfileWFTemplateInfo
     * profileWFTemplateInfo);
     */
    boolean isPrimaryKeyExist(long lnprofileId, long workflowId);

    public void removeTmProfile(TranslationMemoryProfile tmprofile)
            throws RemoteException, ProjectHandlerException;

    public void setResourceBundle(ResourceBundle p_resourceBundle);

    public HashSet getFileProfilesByProject(Project project)
            throws RemoteException, ProjectHandlerException;

    public ArrayList<FileProfile> fileProfileListTerminology(Project project)
            throws RemoteException, ProjectHandlerException;

    public List<ProjectImpl> getProjectsByTermbaseDepended(String termbaseName, long companyId);
    
    public List<WorkflowStatePosts> getAllWorkflowStatePostProfie(String[] filterParams);
    
    public List<WorkflowStatePosts> getAllWorkflowStatePostInfos();
   
    public void createWfStatePostProfile(WorkflowStatePosts wfStatePost);
    
    public WorkflowStatePosts getWfStatePostProfile(long wfStatePostId);
    
    public void modifyWfStatePostProfile(WorkflowStatePosts wfstaPosts);
    
    public void removeWorkflowStatePost(WorkflowStatePosts wfstaPosts);
    
}
