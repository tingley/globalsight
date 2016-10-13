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
package com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.naming.NamingException;

import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.ContainerRoleImpl;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.util.comparator.ActivityComparator;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.util.comparator.LocalePairComparator;
import com.globalsight.persistence.dependencychecking.L10nProfileDependencyChecker;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class LocProfileHandlerHelper
{
    /**
     * Add a localization profile to the system.
     * <p>
     * 
     * @param p_l10nProfile
     *            The localization profile to add.
     * @exception EnvoyServletException. Failed
     *                to add the profile; the cause is indicated by the
     *                exception code.
     * @exception GeneralException. Miscellaneous
     *                exception; most likely occuring in the persistence
     *                component.
     */
    public static void addL10nProfile(L10nProfile p_l10nProfile)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getProjectHandler().addL10nProfile(p_l10nProfile);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Modify a localization profile to the system.
     * <p>
     * 
     * @param p_l10nProfile
     *            The localization profile to modify.
     * @exception EnvoyServletException. Failed
     *                to modify the profile; the cause is indicated by the
     *                exception code.
     * @exception GeneralException. Miscellaneous
     *                exception; most likely occuring in the persistence
     *                component.
     */
    public static void modifyL10nProfile(L10nProfile p_l10nProfile,
            Vector<WorkflowInfos> workflowInfos, long originalLocId)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getProjectHandler().modifyL10nProfile(p_l10nProfile,
                    workflowInfos, originalLocId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Remove a localization profile in the system.
     * <p>
     * 
     * @param p_l10nProfile
     *            The localization profile to remove.
     * @exception EnvoyServletException. Failed
     *                to remove the profile; the cause is indicated by the
     *                exception code.
     * @exception GeneralException. Miscellaneous
     *                exception; most likely occuring in the persistence
     *                component.
     */
    public static void removeL10nProfile(L10nProfile p_l10nProfile)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getProjectHandler().removeL10nProfile(p_l10nProfile);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    public static void duplicateL10nProfile(long lpId, ArrayList localePairs,
            String newName, ResourceBundle bundle) throws EnvoyServletException
    {
        try
        {
            String displayRoleName = bundle.getString("lb_all_qualified_users");
            ServerProxy.getProjectHandler().duplicateL10nProfile(lpId, newName,
                    localePairs, displayRoleName);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Check if any objects have dependencies on this L10nProfile. This should
     * be called BEFORE attempting to remove a Profile.
     * <p>
     * 
     * @param p_l10nProfile
     *            The profile to check for dependencies on
     * @exception EnvoyServletException
     *                Failed to look for dependencies for the profile. The cause
     *                is indicated by the exception message.
     */
    public static String checkForDependencies(BasicL10nProfile p_l10nProfile,
            ResourceBundle p_bundle) throws RemoteException, GeneralException
    {
        L10nProfileDependencyChecker depChecker = new L10nProfileDependencyChecker();
        Hashtable catDeps = depChecker.categorizeDependencies(p_l10nProfile);

        // now convert the hashtable into a Vector of Strings
        StringBuffer deps = new StringBuffer();
        if (catDeps.size() == 0)
            return null;

        deps.append("<span class=\"errorMsg\">");
        Object[] args =
        { p_bundle.getString("lb_loc_profile") };
        deps.append(MessageFormat.format(p_bundle.getString("msg_dependency"),
                args));

        for (Enumeration e = catDeps.keys(); e.hasMoreElements();)
        {
            String key = (String) e.nextElement();
            deps.append("<p>*** " + p_bundle.getString(key) + " ***<br>");
            Vector values = (Vector) catDeps.get(key);
            for (int i = 0; i < values.size(); i++)
            {
                deps.append((String) values.get(i));
                deps.append("<br>");
            }
        }
        deps.append("</span>");
        return deps.toString();
    }

    /**
     * Get a list of all existing activities in the system.
     * <p>
     * 
     * @exception EnvoyServletException
     *                Component related exception.
     * @exception GeneralException
     * @exception NamingException
     */
    public static Vector getAllActivities(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            ArrayList al = new ArrayList(ServerProxy.getJobHandler()
                    .getAllActivities());
            ActivityComparator comp = new ActivityComparator(
                    ActivityComparator.NAME, p_locale);
            SortUtil.sort(al, comp);
            return new Vector(al);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Get a list of all rates for a particular source locale.
     * 
     * @see CostingEngine.getRates(GlobalSightLocale) to see what the hashtable
     *      contains.
     *      <p>
     * @exception EnvoyServletException
     *                Component related exception.
     */
    public static Hashtable getRatesForLocale(GlobalSightLocale p_source)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine().getRates(p_source);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Get a list of all rates for a particular source/target locale.
     * 
     * @see CostingEngine.getRates(GlobalSightLocale, GlobalSightLocale) to see
     *      what the hashtable contains.
     *      <p>
     * @exception EnvoyServletException
     *                Component related exception.
     */
    public static Hashtable getRatesForLocale(GlobalSightLocale p_source,
            GlobalSightLocale p_target) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine().getRates(p_source, p_target);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /*
     * Get all the source locales from locale manager. <p>
     * 
     * @exception EnvoyServletException Component related exception.
     * 
     * @exception GeneralException
     */
    public static List getAllSourceLocales(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            ArrayList al = new ArrayList(ServerProxy.getLocaleManager()
                    .getAllSourceLocales());
            GlobalSightLocaleComparator comp = new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.DISPLAYNAME, p_locale);
            SortUtil.sort(al, comp);
            return al;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /*
     * Get all the target locales from locale manager. <p>
     * 
     * @exception EnvoyServletException Component related exception.
     * 
     * @exception GeneralException
     */
    public static List getAllTargetLocales(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            ArrayList al = new ArrayList(ServerProxy.getLocaleManager()
                    .getAllTargetLocales());
            GlobalSightLocaleComparator comp = new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.DISPLAYNAME, p_locale);
            SortUtil.sort(al, comp);
            return al;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Get a list of all locale pairs.
     */
    static List getAllLocalePairs(Locale p_locale) throws EnvoyServletException
    {
        try
        {
            ArrayList al = new ArrayList(ServerProxy.getLocaleManager()
                    .getSourceTargetLocalePairs());
            LocalePairComparator comp = new LocalePairComparator(p_locale);
            SortUtil.sort(al, comp);
            return al;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Returns all the localization profiles in the system.
     * <p>
     * 
     * @return Return all the localization profiles as a vector.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    public static Vector getAllL10nProfiles() throws EnvoyServletException
    {
        try
        {
            return vectorizedCollection(ServerProxy.getProjectHandler()
                    .getAllL10nProfiles());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Returns all the localization profiles (for GUI) in the system.
     * <p>
     * 
     * @return Return all the localization profiles (for GUI) as a vector.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    public static Vector getAllL10nProfilesForGUI()
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getAllL10nProfilesForGUI();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    public static Vector getAllL10nProfilesForGUI(String[] filterParams,
            Locale uiLocale) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getAllL10nProfilesForGUI(
                    filterParams, uiLocale);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Get the names (and primary keys) of all the projects. The key in the
     * hashtable is the primary key.
     * <p>
     * 
     * @return All the names and keys of all projects.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    public static Hashtable getAllProjectNames() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getAllProjectNames();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * 
     * @return All the project infos for a particular user id.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    public static List getAllProjectNamesForManagedUser(User p_user)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler()
                    .getProjectInfosManagedByUser(p_user,
                            Permission.GROUP_MODULE_GLOBALSIGHT);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Get all the available locales (GlobalSightLocale) supported by the
     * system.
     * <p>
     * 
     * @exception EnvoyServletException
     *                Component related exception.
     * @exception GeneralException
     * @exception NamingException
     */
    public static Vector getSupportedLocales() throws EnvoyServletException
    {
        try
        {
            return vectorizedCollection(ServerProxy.getLocaleManager()
                    .getAvailableLocales());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Get all the code sets from locale manager.
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Vector getAllCodeSets() throws EnvoyServletException
    {
        try
        {
            return vectorizedCollection(ServerProxy.getLocaleManager()
                    .getAllCodeSets());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Get a locale by id from locale manager.
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static GlobalSightLocale getLocaleById(long p_id)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getLocaleManager().getLocaleById(p_id);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Get a list of all the TMs in the system.
     * 
     * @return A list of all TMs as a hashtable (id, and name).
     * @exception EnvoyServletException
     *                Component related exception.
     */
    public static Hashtable getAllTM() throws EnvoyServletException
    {
        // should get the list of all TMs from linguistic component
        // TomyD -- this is a temp solution for testing purposes...
        Hashtable ht = new Hashtable();

        ht.put(new Long(11), "TM 1");
        ht.put(new Long(12), "TM 2");
        ht.put(new Long(13), "TM 3");
        ht.put(new Long(14), "TM 4");
        ht.put(new Long(15), "TM 5");

        return ht;
    }

    /**
     * Get a localization profile by its ID.
     * <p>
     * 
     * @param p_profileId
     *            The primary ID of the localization profile.
     * @return The localization profile.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    public static L10nProfile getL10nProfile(long p_profileId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getL10nProfile(p_profileId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Returns the Collection of all ContainerRole objects with the given
     * activity name, source locale, and target locale.
     * <p>
     * 
     * @param p_activity
     *            The results of the Activity.getActivityName() method.
     * @param p_sourceLocale
     *            The results of the Locale.toString() method, for the source
     *            Locale.
     * @param p_targetLocale
     *            The results of the Locale.toString() method, for the target
     *            Locale.
     * @return A Collection of role object.
     * @exception UserManagerException
     *                Component related exception.
     */
    public static Collection getContainerRoles(String p_activity,
            String p_sourceLocale, String p_targetLocale)
            throws EnvoyServletException
    {
        try
        {
            Collection roles = ServerProxy.getUserManager().getContainerRoles(
                    p_activity, p_sourceLocale, p_targetLocale);

            Object[] objects = roles == null ? null : roles.toArray();
            boolean isValid = false;
            // we only have ONE container role in the collection
            if (objects != null && objects.length > 0)
            {
                ContainerRoleImpl rl = (ContainerRoleImpl) objects[0];
                isValid = rl.isContainerRoleValid();
            }
            return isValid ? roles : null;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Get user matched the given uid
     * 
     * @param p_uid
     *            - The user id
     * @return a User object
     * @exception EnvoyServletException
     *                - Component related exception.
     */
    public static User getUser(String p_uid) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getUserManager().getUser(p_uid);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Returns the Collection of all UserRole objects with the given activity
     * name, source locale, and target locale.
     * <p>
     * 
     * @param p_activty
     *            The results of the Activity.getActivityName() method.
     * @param p_sourceLocale
     *            The results of the Locale.toString() method, for the source
     *            Locale.
     * @param p_targetLocale
     *            The results of the Locale.toString() method, for the target
     *            Locale.
     * @return Collection of all UserRole objects.
     * @exception EnvoyServletException
     *                Component related exception.
     */
    public static Collection getUserRoles(String p_activity,
            String p_sourceLocale, String p_targetLocale)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getUserManager().getUserRoles(p_activity,
                    p_sourceLocale, p_targetLocale);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    // get an array of l10nProfile names
    public static Object[] getL10nProfileNames() throws EnvoyServletException
    {
        // Call server
        Hashtable l10nprofiles = null;
        Object[] names = null;
        try
        {
            l10nprofiles = ServerProxy.getProjectHandler()
                    .getAllL10nProfileNames();
            // get the names from the hashtable (worst case is an empty
            // hashtable)
            Collection values = l10nprofiles.values();
            if (values != null)
            {
                names = values.toArray();
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_ALL_PROFILE_NAMES,
                    null, e);
        }

        return names;
    }

    private static Vector vectorizedCollection(Collection p_collection)
    {
        return new Vector(p_collection);
    }

    public static Vector getTargetLocales(GlobalSightLocale p_sourceLocale)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getLocaleManager().getTargetLocales(
                    p_sourceLocale);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge.getExceptionId(), ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    public static Vector getTargetLocalesByProject(
            GlobalSightLocale p_sourceLocale, String p_project)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getLocaleManager().getTargetLocalesByProject(
                    p_sourceLocale, p_project);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge.getExceptionId(), ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }
}
