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
package com.globalsight.config;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.webapp.pagehandler.tasks.DownloadOfflineFilesConfigHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Implementation of User parameter Persistence Manager.
 * 
 * Calling getUserParameters() automatically creates missing parameters.
 */
public class UserParameterPersistenceManagerLocal implements
        UserParameterPersistenceManager, UserParamNames
{
    private static final Logger CATEGORY = Logger
            .getLogger(UserParameterPersistenceManagerLocal.class);

    //
    // UserParameterPersistenceManager interface methods
    //

    /**
     * Retrieve a specific user parameter object with passed id
     * 
     * @param p_id
     *            Id of user parameter to retreive
     * @throws RemoteException
     *             Application Server Error
     * @throws UserParameterEntityException
     *             Error retrieving a specific user parameter.
     * @return User parameter object with matching id
     */
    public UserParameter getUserParameter(long p_id) throws RemoteException,
            UserParameterEntityException
    {
        try
        {
            return (UserParameterImpl) HibernateUtil.get(
                    UserParameterImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new UserParameterEntityException(e);
        }
    }

    /**
     * Retrieve a specific user parameter object with passed parameter name (as
     * editable object).
     * 
     * @param p_name
     *            user parameter to retreive
     * @throws RemoteException
     *             Application Server Error
     * @throws UserParameterEntityException
     *             Error retrieving a specific user parameter.
     * @return User parameter object with matching name
     */
    public UserParameter getUserParameter(String p_userId, String p_name)
            throws RemoteException, UserParameterEntityException
    {
        String hql = "from UserParameterImpl u where u.userId = :USER_ID "
                + " and u.name = :NAME ";

        Session session = HibernateUtil.getSession();

        Query query = session.createQuery(hql);
        query.setString("USER_ID", p_userId);
        query.setString("NAME", p_name);

        try
        {
            List result = query.list();

            if (result == null || result.size() == 0)
            {
                return null;
            }

            return (UserParameter) result.get(0);
        }
        catch (Exception e)
        {
            throw new UserParameterEntityException(e);
        }
        finally
        {
            // session.close();
        }
    }

    /**
     * Return all user parameter objects from data store for a specific user (as
     * read-write objects).
     * 
     * @throws RemoteException
     *             Application Server Error
     * @throws UserParameterEntityException
     *             Error getting collection of user parameters from data store.
     * @return Collection of all user parameters
     */
    public Collection getUserParameters(String p_userId)
            throws RemoteException, UserParameterEntityException
    {
        String hql = "from UserParameterImpl u where u.userId = :USER_ID";

        Session session = HibernateUtil.getSession();

        Query query = session.createQuery(hql);
        query.setString("USER_ID", p_userId);

        try
        {
            List result = query.list();

            // Some parameters may be missing, auto-create them.
            if (createUserParameters(p_userId, result, session))
            {
                // reload from database
                result = query.list();
            }

            return result;
        }
        catch (PersistenceException ex)
        {
            throw new UserParameterEntityException(ex);
        }
        finally
        {
            // session.close();
        }
    }

    /**
     * Return all user parameter objects from data store for a specific user in
     * a HashMap (read-write objects).
     * 
     * @throws RemoteException
     *             Application Server Error
     * @throws UserParameterEntityException
     *             Error getting map of user parameters from data store.
     * @return HashMap of all user parameters, indexed by parameter name
     */
    public HashMap getUserParameterMap(String p_userId) throws RemoteException,
            UserParameterEntityException
    {
        HashMap result = new HashMap();

        Collection params = getUserParameters(p_userId);

        for (Iterator it = params.iterator(); it.hasNext();)
        {
            UserParameter param = (UserParameter) it.next();
            result.put(param.getName(), param);
        }

        return result;
    }

    /**
     * Update specified user parameter in data store.
     * 
     * @param p_param
     *            User Parameter object to modify
     * @throws RemoteException
     *             Application Server Error
     * @throws UserParameterEntityException
     *             Error updating user parameter in data store.
     * @return Modified user parameter object
     */
    public UserParameter updateUserParameter(UserParameter p_param)
            throws RemoteException, UserParameterEntityException
    {
        try
        {
            HibernateUtil.saveOrUpdate(p_param);
            notifyObservers(p_param);
        }
        catch (Exception e)
        {
            throw new UserParameterEntityException("cannot update object "
                    + p_param.toString(), e);
        }

        return p_param;
    }

    //
    // PRIVATE SUPPORT METHODS
    //

    /**
     * When a UserParameter is updated, notify observers.
     * 
     * At this time we don't implement the Observer pattern.
     * 
     * @param p_userParameter
     *            the UserParameter that was updated.
     * @throws UserParameterEntityException
     */
    private void notifyObservers(UserParameter p_userParameter)
            throws UserParameterEntityException
    {
        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("notifyObservers: " + p_userParameter);
            }

            /*
             * if (p_userParameter.getName().equals(
             * UserConfigParamNames.USER_LOGGING_PRIORITY)) { }
             * 
             * if (p_userParameter.getName().equals(
             * UserConfigParamNames.TOPLINK_LOGGING)) { String value =
             * p_userParameter.getValue().toUpperCase().trim(); if
             * (value.equalsIgnoreCase("true")) { ; } else { ; } }
             */
        }
        catch (Exception e)
        {
            throw new UserParameterEntityException(p_userParameter.toString(),
                    e);
        }
    }

    /**
     * Creates missing UserParameter objects for a given user based on a
     * collection of existing parameters.
     * 
     * @return true if parameters were created and need to be reloaded from the
     *         database, else false.
     */
    private boolean createUserParameters(String p_userId,
            Collection p_existing, Session session) throws RemoteException,
            UserParameterEntityException
    {
        try
        {
            boolean retval = false;
            Transaction tx = session.beginTransaction();

            Collection params = createMissingParameters(p_userId, p_existing);

            if (params.size() > 0)
            {
                for (Iterator it = params.iterator(); it.hasNext();)
                {
                    UserParameter param = (UserParameter) it.next();
                    session.save(param);
                    retval = true;
                }
            }

            tx.commit();
            return retval;
        }
        catch (PersistenceException e)
        {
            CATEGORY.debug("cannot create user parameters", e);
            throw new UserParameterEntityException(e);
        }
    }

    /**
     * Creates a list of user parameters with default values. Takes into account
     * an (optional) list of existing parameters.
     * @throws RemoteException 
     * @throws UserParameterEntityException 
     */
    private ArrayList createMissingParameters(String p_userId,
            Collection p_existing) throws UserParameterEntityException, RemoteException
    {
        // TODO: Defaults should be read from a property file that can
        // be adjusted per installation.

        ArrayList result = new ArrayList();

        UserParameter param;

        if (!haveParam(p_existing, NOTIFICATION_ENABLED))
        {
            param = new UserParameterImpl(p_userId, NOTIFICATION_ENABLED,
                    NOTIFICATION_ENABLED_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, TM_MATCHING_THRESHOLD))
        {
            param = new UserParameterImpl(p_userId, TM_MATCHING_THRESHOLD,
                    TM_MATCHING_THRESHOLD_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, TB_MATCHING_THRESHOLD))
        {
            param = new UserParameterImpl(p_userId, TB_MATCHING_THRESHOLD,
                    TB_MATCHING_THRESHOLD_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, EDITOR_AUTO_SAVE_SEGMENT))
        {
            param = new UserParameterImpl(p_userId, EDITOR_AUTO_SAVE_SEGMENT,
                    EDITOR_AUTO_SAVE_SEGMENT_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT))
        {
            param = new UserParameterImpl(p_userId, EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT,
            		EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT_DEFAULT);
            result.add(param);
        }
        
        if (!haveParam(p_existing, EDITOR_AUTO_UNLOCK))
        {
            param = new UserParameterImpl(p_userId, EDITOR_AUTO_UNLOCK,
                    EDITOR_AUTO_UNLOCK_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, EDITOR_AUTO_SYNC))
        {
            param = new UserParameterImpl(p_userId, EDITOR_AUTO_SYNC,
                    EDITOR_AUTO_SYNC_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, EDITOR_AUTO_ADJUST_WHITESPACE))
        {
            param = new UserParameterImpl(p_userId,
                    EDITOR_AUTO_ADJUST_WHITESPACE,
                    EDITOR_AUTO_ADJUST_WHITESPACE_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, EDITOR_LAYOUT))
        {
            param = new UserParameterImpl(p_userId, EDITOR_LAYOUT,
                    EDITOR_LAYOUT_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, EDITOR_VIEWMODE))
        {
            param = new UserParameterImpl(p_userId, EDITOR_VIEWMODE,
                    EDITOR_VIEWMODE_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, EDITOR_PTAGMODE))
        {
            param = new UserParameterImpl(p_userId, EDITOR_PTAGMODE,
                    EDITOR_PTAGMODE_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, EDITOR_PTAGHILITE))
        {
            param = new UserParameterImpl(p_userId, EDITOR_PTAGHILITE,
                    EDITOR_PTAGHILITE_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, EDITOR_SHOW_MT))
        {
            param = new UserParameterImpl(p_userId, EDITOR_SHOW_MT,
                    EDITOR_SHOW_MT_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, EDITOR_ITERATE_SUBS))
        {
            param = new UserParameterImpl(p_userId, EDITOR_ITERATE_SUBS,
                    EDITOR_ITERATE_SUBS_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, HYPERLINK_COLOR_OVERRIDE))
        {
            param = new UserParameterImpl(p_userId, HYPERLINK_COLOR_OVERRIDE,
                    HYPERLINK_COLOR_OVERRIDE_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, HYPERLINK_COLOR))
        {
            param = new UserParameterImpl(p_userId, HYPERLINK_COLOR,
                    HYPERLINK_COLOR_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, ACTIVE_HYPERLINK_COLOR))
        {
            param = new UserParameterImpl(p_userId, ACTIVE_HYPERLINK_COLOR,
                    ACTIVE_HYPERLINK_COLOR_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, VISITED_HYPERLINK_COLOR))
        {
            param = new UserParameterImpl(p_userId, VISITED_HYPERLINK_COLOR,
                    VISITED_HYPERLINK_COLOR_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, PREVIEW_100MATCH_COLOR))
        {
            param = new UserParameterImpl(p_userId, PREVIEW_100MATCH_COLOR,
                    PREVIEW_100MATCH_COLOR_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, PREVIEW_ICEMATCH_COLOR))
        {
            param = new UserParameterImpl(p_userId, PREVIEW_ICEMATCH_COLOR,
                    PREVIEW_ICEMATCH_COLOR_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, PREVIEW_NONMATCH_COLOR))
        {
            param = new UserParameterImpl(p_userId, PREVIEW_NONMATCH_COLOR,
                    PREVIEW_NONMATCH_COLOR_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, EDITOR_SEGMENTS_MAX_NUM))
        {
            param = new UserParameterImpl(p_userId, EDITOR_SEGMENTS_MAX_NUM,
                    EDITOR_SEGMENTS_MAX_NUM_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, PAGENAME_DISPLAY))
        {
            param = new UserParameterImpl(p_userId, PAGENAME_DISPLAY,
                    PAGENAME_DISPLAY_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, EDITOR_SELECTION))
        {
            param = new UserParameterImpl(p_userId, EDITOR_SELECTION,
                    EDITOR_SELECTION_DEFAULT);
            result.add(param);
        }

        // sets closeAllComment option
        if (!haveParam(p_existing, EDITOR_SHOW_CLOSEALLCOMMENT))
        {
            param = new UserParameterImpl(p_userId,
                    EDITOR_SHOW_CLOSEALLCOMMENT,
                    EDITOR_SHOW_CLOSEALLCOMMENT_DEFAULT);
            result.add(param);
        }

        // sets OverDue PM unavailable default
        if (!haveParam(p_existing, NOTIFY_OVERDUE_PM))
        {
            param = new UserParameterImpl(p_userId, NOTIFY_OVERDUE_PM,
                    NOTIFY_OVERDUE_PM_DEFAULT);
            result.add(param);
        }
        // sets OverDue User unavailable default
        if (!haveParam(p_existing, NOTIFY_OVERDUE_USER))
        {
            param = new UserParameterImpl(p_userId, NOTIFY_OVERDUE_USER,
                    NOTIFY_OVERDUE_USER_DEFAULT);
            result.add(param);
        }
        // sets the following notification options disabled default
        if (!haveParam(p_existing, NOTIFY_DELAYED_REIMPORT))
        {
            param = new UserParameterImpl(p_userId, NOTIFY_DELAYED_REIMPORT,
                    NOTIFICATION_DISABLED_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, NOTIFY_WORKFLOW_DISCARD))
        {
            param = new UserParameterImpl(p_userId, NOTIFY_WORKFLOW_DISCARD,
                    NOTIFICATION_DISABLED_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, NOTIFY_EXPORT_FOR_UPDATE))
        {
            param = new UserParameterImpl(p_userId, NOTIFY_EXPORT_FOR_UPDATE,
                    NOTIFICATION_DISABLED_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, NOTIFY_EXPORT_COMPLETION))
        {
            param = new UserParameterImpl(p_userId, NOTIFY_EXPORT_COMPLETION,
                    NOTIFICATION_DISABLED_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, NOTIFY_SUCCESSFUL_UPLOAD))
        {
            param = new UserParameterImpl(p_userId, NOTIFY_SUCCESSFUL_UPLOAD,
                    NOTIFICATION_DISABLED_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, NOTIFY_SAVING_SEGMENTS_FAILURE))
        {
            param = new UserParameterImpl(p_userId,
                    NOTIFY_SAVING_SEGMENTS_FAILURE,
                    NOTIFICATION_DISABLED_DEFAULT);
            result.add(param);
        }

        if (!haveParam(p_existing, NOTIFY_QUOTE_PERSON))
        {
            param = new UserParameterImpl(p_userId, NOTIFY_QUOTE_PERSON,
                    NOTIFICATION_DISABLED_DEFAULT);
            result.add(param);
        }

        for (int i = 0; i < DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS
                .size(); i++)
        {
            String downloadOption = DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS
                    .get(i);
            String downloadOptionDefault = DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS_DEFAULT
                    .get(i);
            if (!haveParam(p_existing, downloadOption))
            {
                param = new UserParameterImpl(p_userId, downloadOption,
                        downloadOptionDefault);
                result.add(param);
            }
            // rtfTrados is removed, and we should use rtfTradosOptimized instead
            else if (UserParamNames.DOWNLOAD_OPTION_FORMAT.equals(downloadOption))
            {
                param = getParam(p_existing, downloadOption);
                if (param!= null && "rtfTrados".equals(param.getValue()))
                {
                    param.setValue("rtfTradosOptimized");
                    result.add(param);
                }
                if (param!= null && "text".equalsIgnoreCase(param.getValue()))
                {
                    param.setValue(UserParamNames.DOWNLOAD_OPTION_FORMAT_DEFAULT);
                    result.add(param);
                }
            }
        }

        createNotificationParameters(p_userId, p_existing, result);

        return result;
    }

    /*
     * Create the parameters used for notification purposes
     */
    private void createNotificationParameters(String p_userId,
            Collection p_existing, ArrayList result)
    {
        PermissionSet perms = new PermissionSet();
        try
        {
            perms = Permission.getPermissionManager().getPermissionSetForUser(
                    p_userId);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to read permissions for user " + p_userId, e);
        }

        if (perms.getPermissionFor(Permission.ACCOUNT_NOTIFICATION_SYSTEM))
        {
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_INITIAL_IMPORT_FAILURE);
        }

        if (perms.getPermissionFor(Permission.ACCOUNT_NOTIFICATION_WFMGMT))

        {
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_JOB_DISCARD_FAILURE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_READY_TO_DISPATCH);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_DISPATCH_FAILURE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_IMPORT_FAILURE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_IMPORT_CORRECTION);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_DELAYED_REIMPORT_FAILURE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_TASK_ACCEPTANCE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_TASK_COMPLETION);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_WFL_COMPLETION);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_JOB_COMPLETION);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_TASK_REJECTION);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_PM_CHANGE_IN_PROJECT);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_NO_AVAILABLE_RESOURCE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_EXPORT_SOURCE_FAILURE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_ESTIMATED_EXCEEDS_PLANNED_DATE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_ADD_WORKFLOW_TO_JOB_FAILURE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_STF_CREATION_FAILURE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_SCHEDULING_FAILURE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_ACTIVITY_DEADLINE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_BATCH_ALIGNMENT_SUCCESS);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_BATCH_ALIGNMENT_FAILURE);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_ALIGNMENT_UPLOAD_SUCCESS);
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_ALIGNMENT_UPLOAD_FAILURE);

        }

        if (perms.getPermissionFor(Permission.ACCOUNT_NOTIFICATION_GENERAL))
        {
            createNotificationParameter(p_userId, p_existing, result,
                    NOTIFY_NEWLY_ASSIGNED_TASK);
        }
    }
    
    private static UserParameter getParam(Collection p_params, String p_name)
    {
        if (p_params == null)
        {
            return null;
        }

        for (Iterator it = p_params.iterator(); it.hasNext();)
        {
            UserParameter param = (UserParameter) it.next();

            if (param.getName().equals(p_name))
            {
                return param;
            }
        }

        return null;
    }

    private static boolean haveParam(Collection p_params, String p_name)
    {
        if (p_params == null)
        {
            return false;
        }

        for (Iterator it = p_params.iterator(); it.hasNext();)
        {
            UserParameter param = (UserParameter) it.next();

            if (param.getName().equals(p_name))
            {
                return true;
            }
        }

        return false;
    }

    /*
     * Possibly create the notification parameter.
     */
    private void createNotificationParameter(String p_userId,
            Collection p_existing, ArrayList p_result, String p_parameterName)
    {
        if (!haveParam(p_existing, p_parameterName))
        {
            UserParameter param = new UserParameterImpl(p_userId,
                    p_parameterName, NOTIFICATION_ENABLED_DEFAULT);
            p_result.add(param);
        }
    }
}
