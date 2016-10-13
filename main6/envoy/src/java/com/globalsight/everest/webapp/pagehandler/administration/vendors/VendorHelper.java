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
package com.globalsight.everest.webapp.pagehandler.administration.vendors;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.customform.CustomField;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.VendorSecureFields;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.comparator.UserComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.vendormanagement.Rating;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.vendormanagement.VendorException;
import com.globalsight.everest.vendormanagement.VendorRole;
import com.globalsight.everest.vendormanagement.VendorSearchParameters;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * A bunch of helper methods for operating on Vendors.
 */

public class VendorHelper
{

    /**
     * Get all vendors.
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static List getVendors(User p_userQuerying)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getVendorManagement().getVendors(p_userQuerying);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Search for vendors with certain criteria
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static List searchVendors(User user, SessionManager sessionMgr)
            throws EnvoyServletException
    {
        VendorSearchParameters sp = (VendorSearchParameters) sessionMgr
                .getAttribute("vendorSearch");
        try
        {
            // Do search
            if (sp != null)
            {
                return ServerProxy.getVendorManagement().findVendors(user, sp);
            }
            else
            {
                return ServerProxy.getVendorManagement().getVendors(user);
            }

        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Search for vendors with certain criteria
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static List searchVendors(HttpServletRequest request, User user,
            SessionManager sessionMgr) throws EnvoyServletException
    {
        try
        {
            boolean setAtLeastOneCriteria = false;

            // adding search criteria
            VendorSearchParameters sp = new VendorSearchParameters();

            // set parameters

            // name
            String buf = (String) request.getParameter("nameField");
            if (buf.trim().length() != 0)
            {
                setAtLeastOneCriteria = true;
                sp.setVendorName(buf);
                sp.setVendorNameType(request.getParameter("nameTypeField"));
                sp.setVendorKey(request.getParameter("nameOptions"));
            }

            // company name
            buf = (String) request.getParameter("company");
            if (buf.trim().length() != 0)
            {
                setAtLeastOneCriteria = true;
                sp.setCompanyName(buf);
                sp.setCompanyNameKey(request.getParameter("companyOptions"));
            }

            // source locale
            buf = (String) request.getParameter("srcLocale");
            if (!buf.equals("-1"))
            {
                setAtLeastOneCriteria = true;
                sp.setSourceLocale(ServerProxy.getLocaleManager()
                        .getLocaleById(Long.parseLong(buf)));
            }

            // target locale
            buf = (String) request.getParameter("targLocale");
            if (!buf.equals("-1"))
            {
                setAtLeastOneCriteria = true;
                sp.setTargetLocale(ServerProxy.getLocaleManager()
                        .getLocaleById(Long.parseLong(buf)));
            }

            // rate/cost
            buf = (String) request.getParameter("cost");
            if (buf.trim().length() != 0)
            {
                setAtLeastOneCriteria = true;
                sp.setRateValue(Float.parseFloat(buf));
                buf = (String) request.getParameter("costingRate");
                sp.setRateType(new Integer(buf));
                sp.setRateCondition(request.getParameter("costingOptions"));
            }

            // activity
            buf = (String) request.getParameter("activities");
            if (!buf.equals("-1"))
            {
                setAtLeastOneCriteria = true;
                sp.setActivityId(Long.parseLong(buf));
            }

            // custom form keywords
            buf = (String) request.getParameter("keywords");
            if (buf != null && buf.trim().length() != 0)
            {
                setAtLeastOneCriteria = true;
                sp.setCustomPageKeyword(buf);
            }

            // Do search
            if (setAtLeastOneCriteria)
            {
                if (request.getParameter("caseSensitive") != null)
                    sp.isCaseSensitive(true);

                // save the search params in the session for when the user
                // does an action from the results page and the action should
                // take them back to the results page
                sessionMgr.setAttribute("vendorSearch", sp);

                return ServerProxy.getVendorManagement().findVendors(user, sp);
            }
            else
            {
                return ServerProxy.getVendorManagement().getVendors(user);
            }

        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get vendor by id
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Vendor getVendor(long id) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getVendorManagement().getVendorById(id);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get vendor by custom id
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Vendor getVendorByCustomId(User p_userQuerying, String id)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getVendorManagement().getVendorByCustomId(
                    p_userQuerying, id);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get Rating by id
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Rating getRating(long id, User user)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getVendorManagement().getRatingById(user, id);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a list of field level securities for the specified vendor infos.
     */
    public static List getSecurities(List p_vendorInfos, User p_user)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getSecurityManager().getFieldSecurities(p_user,
                    p_vendorInfos, true);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get the field level security hashtable
     */
    public static FieldSecurity getSecurity(Object vendor, User user,
            boolean checkProject) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getSecurityManager().getFieldSecurity(user,
                    vendor, checkProject);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get the tasks for rating (completed tasks that are assigned to vendors).
     */
    public static List getTasksForRating(long wfId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getTaskManager().getTasksForRating(wfId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get the all activities.
     */
    public static Collection getAllActivities() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getJobHandler().getAllActivities();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Remove vendor by id
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static void removeVendor(User user, long id) throws RemoteException,
            VendorException, GeneralException
    {
        ServerProxy.getVendorManagement().removeVendor(user, id);
    }

    /**
     * Remove rating from vendor
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static void removeRating(User user, Vendor vendor, long id)
            throws RemoteException, VendorException, GeneralException
    {
        ServerProxy.getVendorManagement().removeRating(user, vendor, id);
    }

    /**
     * Get status values
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static String[] getStatusValues() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getVendorManagement().getVendorStatusList();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get all the distinct company names that vendors are associated with.
     */
    public static String[] getCompanyNames() throws EnvoyServletException
    {
        try
        {
            Collection cns = ServerProxy.getVendorManagement()
                    .getCompanyNames();
            String[] cnArray = new String[cns.size()];
            cnArray = (String[]) cns.toArray(cnArray);
            return cnArray;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get possible users
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Vector getPossibleUsers(Locale uiLocale)
            throws EnvoyServletException
    {
        try
        {
            Vector usernames = ServerProxy.getUserManager()
                    .getVendorlessUsers();
            SortUtil.sort(usernames, new UserComparator(
                    UserComparator.DISPLAYNAME, uiLocale));
            return usernames;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get all aliases
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static ArrayList getAllAliases() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getVendorManagement().getPseudonyms();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get all custom vendor id's
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static ArrayList getAllCustomVendorIds()
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getVendorManagement().getCustomVendorIds();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get rates.
     * <p>
     * 
     * /** Get rates.
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Collection getCostingRates() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine().getRates();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get the rates for a given activity, src locale and targ locale
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Collection getRatesForActivity(Activity activity,
            GlobalSightLocale sourceLocale, GlobalSightLocale targetLocale)

    throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine().getRates(activity,
                    sourceLocale, targetLocale);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Given a list of locale pairs, remove them from the list of all locale
     * pairs. Then create a hashtable where the key is the source locale, and
     * the value is all possible target locales.
     * 
     */
    public static Hashtable getRemainingLocales(List currentLocales)
            throws EnvoyServletException
    {
        Hashtable remaining = new Hashtable();
        Vector sourceLocales = UserHandlerHelper.getAllSourceLocales();
        for (int i = 0; i < sourceLocales.size(); i++)
        {
            GlobalSightLocale curLocale = (GlobalSightLocale) sourceLocales
                    .elementAt(i);
            Vector validTargets = UserHandlerHelper.getTargetLocales(curLocale);
            remaining.put(curLocale, validTargets);
        }

        // Now that we have a hashtable of all valid source locales and
        // their target locales, removes the ones that already exist for
        // this vendor.
        for (int i = 0; i < currentLocales.size(); i++)
        {
            LocalePair pair = (LocalePair) currentLocales.get(i);
            GlobalSightLocale target = pair.getTarget();
            Vector targets = (Vector) remaining.get(pair.getSource());
            if (targets != null && targets.contains(target))
            {
                targets.remove(target);
                if (targets.size() == 0)
                {
                    // no valid targets left so remove the entry in the hash
                    remaining.remove(pair.getSource());
                }
            }
        }

        return remaining;
    }

    /**
     * Return a hashtable where the key is an activity and the value is a list
     * of the activity rates
     */
    public static HashMap getActivities(GlobalSightLocale sourceLocale,
            GlobalSightLocale targetLocale, Locale uiLocale)
            throws EnvoyServletException
    {
        boolean isCostingEnabled = false;
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            isCostingEnabled = sc
                    .getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
        }
        catch (Exception e)
        {
        }
        try
        {
            HashMap activityHash = new HashMap();
            Vector activities = UserHandlerHelper.getAllActivities(uiLocale);
            for (int i = 0; i < activities.size(); i++)
            {
                Activity activity = (Activity) activities.get(i);
                Collection activityRates = null;
                if (isCostingEnabled)
                {
                    activityRates = ServerProxy.getCostingEngine().getRates(
                            activity, sourceLocale, targetLocale);
                }
                activityHash.put(activity, activityRates);
            }
            return activityHash;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Given a vendor, src local, & targ locale, return a hashtable of it's
     * activities where the key is an activity and the value is it's rate.
     */
    public static HashMap getSelectedActivities(Vendor vendor,
            GlobalSightLocale srcLocale, GlobalSightLocale targLocale)
            throws EnvoyServletException
    {
        HashMap selectedActivities = new HashMap();

        boolean isCostingEnabled = false;
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            isCostingEnabled = sc
                    .getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
        }
        catch (Exception e)
        {
        }
        if (isCostingEnabled == false)
            return selectedActivities;

        Set roles = vendor.getRoles();
        for (Iterator r = roles.iterator(); r.hasNext();)
        {
            VendorRole role = (VendorRole) r.next();
            LocalePair lp = role.getLocalePair();
            if (lp.getSource().equals(srcLocale)
                    && lp.getTarget().equals(targLocale))
            {
                long id = -1;
                if (role.getRate() != null)
                {
                    id = role.getRate().getId();
                }
                selectedActivities.put(role.getActivity().getName(), new Long(
                        id));
            }
        }
        return selectedActivities;
    }

    /**
     * Save the request parameters from the Basic Info page
     */
    public static Rating saveRating(Rating rating, HttpServletRequest request,
            String userId, Task task) throws EnvoyServletException
    {
        String rate = (String) request.getParameter("ratingField");
        String comment = (String) request.getParameter("comment");
        if (rating == null)
        {
            rating = new Rating(Integer.parseInt(rate), userId, comment, task);
        }
        else
        {
            // if there is a task being used - must refresh it
            // and mark it and the rating as editable/clones.
            // otherwise the change will be made on the one copy in
            // the main cache. need to make the change out of the
            // cache and the commit will merge the copies.
            if (task != null)
            {
                try
                {
                    // refresh it and clone it
                    task = ServerProxy.getTaskManager().getTask(task.getId(),
                            true);
                    List ratings = task.getRatings();
                    // refresh the rating
                    rating = (Rating) ratings.get(ratings.indexOf(rating));
                }
                catch (Exception e)
                {
                    throw new EnvoyServletException(e);
                }
            }
            rating.updateRating(Integer.parseInt(rate), comment, userId);
        }
        return rating;
    }

    /**
     * Save the request parameters from the Basic Info page
     */
    public static void saveBasicInfo(Vendor vendor, HttpServletRequest request)
            throws EnvoyServletException
    {
        String buf = (String) request.getParameter("usernameSelect");
        if (buf != null && !buf.equals(""))
        {
            User user = UserHandlerHelper.getUser(buf);
            vendor.setUser(user);
        }
        buf = (String) request.getParameter("userName");
        if (buf != null && !buf.equals(""))
        {
            vendor.setUserId(buf);
            String password = (String) request.getParameter("password");
            vendor.setPassword(password);
        }

        buf = (String) request.getParameter("customVendorId");
        if (buf != null && !buf.equals(""))
        {
            vendor.setCustomVendorId(buf);
        }

        if ("0".equals(request.getParameter("accessAllowed")))
            vendor.useInAmbassador(false);
        else
            vendor.useInAmbassador(true);

        buf = (String) request.getParameter("status");
        if (buf != null)
            vendor.setStatus(buf);
        buf = (String) request.getParameter("firstName");
        if (buf != null)
            vendor.setFirstName(buf);
        buf = (String) request.getParameter("lastName");
        if (buf != null)
            vendor.setLastName(buf);
        buf = (String) request.getParameter("userTitle");
        if (buf != null)
            vendor.setTitle(buf);
        if (request.getParameter("company").equals("false"))
        {
            vendor.setCompanyName((String) request.getParameter("companies"));
        }
        else
        {
            vendor.setCompanyName((String) request.getParameter("companyName"));
        }
        if ("0".equals(request.getParameter("vendorType")))
            vendor.isInternalVendor(false);
        else
            vendor.isInternalVendor(true);
        buf = (String) request.getParameter("notes");
        if (buf != null)
            vendor.setNotes(buf);
        buf = (String) request.getParameter("countries");
        if (buf != null)
            vendor.setNationalities(buf);
        buf = (String) request.getParameter("dateOfBirth");
        if (buf != null)
            vendor.setDateOfBirth(buf);
        buf = (String) request.getParameter("alias");
        if (buf != null)
            vendor.setPseudonym(buf);

    }

    /**
     * Save the request parameters from the Contact Info page
     */
    public static void saveContactInfo(Vendor vendor, HttpServletRequest request)
    {
        String buf = null;
        buf = (String) request.getParameter("address");
        if (buf != null)
            vendor.setAddress(buf);
        buf = (String) request.getParameter("country");
        if (buf != null)
            vendor.setCountry(buf);
        buf = (String) request.getParameter("homePhone");
        if (buf != null)
            vendor.setPhoneNumber(User.PhoneType.HOME, buf);
        buf = (String) request.getParameter("workPhone");
        if (buf != null)
            vendor.setPhoneNumber(User.PhoneType.OFFICE, buf);
        buf = (String) request.getParameter("cellPhone");
        if (buf != null)
            vendor.setPhoneNumber(User.PhoneType.CELL, buf);
        buf = (String) request.getParameter("fax");
        if (buf != null)
            vendor.setPhoneNumber(User.PhoneType.FAX, buf);
        buf = (String) request.getParameter("email");
        if (buf != null)
            vendor.setEmail(buf);
        buf = (String) request.getParameter("uiLocale");
        if (buf != null)
            vendor.setDefaultUILocale(buf);
    }

    /**
     * Save the request parameters from the Custom Fields page
     */
    public static void saveCustom(Vendor vendor, HttpServletRequest request,
            SessionManager sessionMgr)
    {
        FieldSecurity fs = (FieldSecurity) sessionMgr
                .getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);
        if (fs != null)
        {
            String access = fs.get(VendorSecureFields.CUSTOM_FIELDS);
            if ("hidden".equals(access) || "locked".equals(access))
            {
                return;
            }
        }
        Hashtable fields = vendor.getCustomFields();
        if (fields == null)
        {
            fields = new Hashtable();
        }
        ArrayList list = CustomPageHelper.getCustomFieldNames();
        for (int i = 0; i < list.size(); i++)
        {
            String key = (String) list.get(i);
            String value = (String) request.getParameter(key);
            if (value == null)
            {
                fields.remove(key);
            }
            else
            {
                // if already in the list
                CustomField cf = (CustomField) fields.get(key);
                if (cf != null)
                {
                    cf.setValue(value);
                }
                else
                {
                    cf = new CustomField(key, value);
                }
                fields.put(key, cf);

            }
        }
        vendor.setCustomFields(fields);
    }

    /**
     * Save the request parameters from the Security page
     */
    public static void saveSecurity(FieldSecurity fs, HttpServletRequest request)
            throws EnvoyServletException
    {
        try
        {
            fs.put(VendorSecureFields.FIRST_NAME,
                    request.getParameter("firstName"));
            fs.put(VendorSecureFields.LAST_NAME,
                    request.getParameter("lastName"));
            fs.put(VendorSecureFields.TITLE, request.getParameter("title"));
            fs.put(VendorSecureFields.IS_INTERNAL,
                    request.getParameter("isInternal"));
            fs.put(VendorSecureFields.COMPANY,
                    request.getParameter("companyName"));
            fs.put(VendorSecureFields.NOTES, request.getParameter("notes"));
            fs.put(VendorSecureFields.AMBASSADOR_ACCESS,
                    request.getParameter("useInAmbassador"));
            fs.put(VendorSecureFields.STATUS, request.getParameter("status"));
            fs.put(VendorSecureFields.USERNAME,
                    request.getParameter("userName"));
            fs.put(VendorSecureFields.CUSTOM_ID,
                    request.getParameter("customVendorId"));
            fs.put(VendorSecureFields.PASSWORD,
                    request.getParameter("password"));
            fs.put(VendorSecureFields.CITIZENSHIP,
                    request.getParameter("countries"));
            fs.put(VendorSecureFields.DOB, request.getParameter("dateOfBirth"));
            fs.put(VendorSecureFields.ROLES, request.getParameter("roles"));
            fs.put(VendorSecureFields.ADDRESS, request.getParameter("address"));
            fs.put(VendorSecureFields.COUNTRY, request.getParameter("country"));
            fs.put(VendorSecureFields.HOME_PHONE,
                    request.getParameter("homePhone"));
            fs.put(VendorSecureFields.WORK_PHONE,
                    request.getParameter("workPhone"));
            fs.put(VendorSecureFields.CELL_PHONE,
                    request.getParameter("cellPhone"));
            fs.put(VendorSecureFields.FAX, request.getParameter("fax"));
            fs.put(VendorSecureFields.EMAIL, request.getParameter("email"));
            fs.put(VendorSecureFields.EMAIL_LANGUAGE,
                    request.getParameter("emailLanguage"));
            fs.put(VendorSecureFields.RESUME, request.getParameter("resume"));
            fs.put(VendorSecureFields.PROJECTS,
                    request.getParameter("projects"));
            fs.put(VendorSecureFields.CUSTOM_FIELDS,
                    request.getParameter("customForm"));

        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Save the permission data.
     */
    public static void savePermissions(Vendor vendor,
            HttpServletRequest request, SessionManager sessionMgr)
            throws EnvoyServletException
    {
        String toField = (String) request.getParameter("toField");
        ArrayList userPerms = new ArrayList();
        try
        {
            if (toField != null && !toField.equals(""))
            {
                String[] perm = toField.split(",");
                for (int i = 0; i < perm.length; i++)
                {
                    userPerms.add(Permission.getPermissionManager()
                            .readPermissionGroup(Long.parseLong(perm[i])));
                }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        sessionMgr.setAttribute("userPerms", userPerms);
    }

    /**
     * Create a vendor.
     */
    public static void createVendor(Vendor vendor, User user, FieldSecurity fs)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getVendorManagement().addVendor(user, vendor, fs);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Persist the vendor.
     */
    public static void saveVendor(Vendor vendor, User user, FieldSecurity fs)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getVendorManagement().modifyVendor(user, vendor, fs);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Add a rating to a vendor.
     */
    public static void addRating(Vendor vendor, Rating rating, User user)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getVendorManagement().addRating(user, vendor, rating);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Update a vendor rating.
     */
    public static void updateRating(Vendor vendor, Rating rating, User user)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getVendorManagement()
                    .updateRating(user, vendor, rating);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Save the request parameters from the new role page
     */
    public static void newRole(Vendor vendor, HttpServletRequest request,
            SessionManager sessionMgr, Locale uiLocale)
            throws EnvoyServletException
    {
        // Get the source and target locales
        String src = request.getParameter("srcLocales");
        String targ = request.getParameter("targLocales");
        if (src.equals("-1") || targ.equals("-1"))
        {
            // the user isn't adding a role (it's not required)
            return;
        }
        LocalePair lp = null;
        try
        {
            GlobalSightLocale sourceLocale = ServerProxy.getLocaleManager()
                    .getLocaleById(Long.parseLong(src));
            GlobalSightLocale targetLocale = ServerProxy.getLocaleManager()
                    .getLocaleById(Long.parseLong(targ));
            lp = ServerProxy.getLocaleManager().getLocalePairBySourceTargetIds(
                    sourceLocale.getId(), targetLocale.getId());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        // Get all activities and loop thru to get set ones
        Vector activities = UserHandlerHelper.getAllActivities(uiLocale);
        for (int i = 0; i < activities.size(); i++)
        {
            Activity activity = (Activity) activities.get(i);
            String activityName = (String) request.getParameter(activity
                    .getActivityName());
            Rate rate = null;
            if (activityName != null)
            {
                rate = getRate(activity, request);
                VendorRole newRole = new VendorRole(activity, lp, rate);
                vendor.addRole(newRole);
            }

        }
    }

    /**
     * Save the request parameters from the edit role page
     */
    public static void modifyRole(Vendor vendor, HttpServletRequest request,
            SessionManager sessionMgr, Locale uiLocale)
            throws EnvoyServletException
    {
        LocalePair lp = (LocalePair) sessionMgr.getAttribute("localePair");
        GlobalSightLocale srcLocale = (GlobalSightLocale) sessionMgr
                .getAttribute("sourceLocale");
        GlobalSightLocale targLocale = (GlobalSightLocale) sessionMgr
                .getAttribute("targetLocale");
        // Get all possible activities
        Vector activities = UserHandlerHelper.getAllActivities(uiLocale);
        for (int i = 0; i < activities.size(); i++)
        {
            Activity activity = (Activity) activities.get(i);
            String activityName = (String) request.getParameter(activity
                    .getActivityName());
            if (activityName != null)
            {
                Rate rate = getRate(activity, request);
                VendorRole role = activityInRole(activity.getName(), lp, vendor);
                if (role != null)
                {
                    // update the role
                    role.setRate(rate);
                }
                else
                {
                    // add new role
                    VendorRole newRole = new VendorRole(activity, lp, rate);
                    vendor.addRole(newRole);
                }
            }
            else
            {
                VendorRole role = activityInRole(activity.getName(), lp, vendor);
                if (role != null)
                {
                    // remove role
                    vendor.removeRole(role);
                }
            }
        }
    }

    /**
     * Save the request parameters from the CV/Resume page
     */
    public static void saveCV(Vendor vendor, HttpServletRequest request)
            throws EnvoyServletException
    {
        // Create a new file upload handler
        DiskFileUpload upload = new DiskFileUpload();

        String radioValue = null;
        String resumeText = null;
        boolean doUpload = false;
        byte[] data = null;
        String filename = null;
        // Parse the request
        try
        {
            List /* FileItem */items = upload.parseRequest(request);
            // Process the uploaded items
            Iterator iter = items.iterator();
            while (iter.hasNext())
            {
                FileItem item = (FileItem) iter.next();
                if (item.isFormField())
                {
                    String name = item.getFieldName();
                    String value = EditUtil.utf8ToUnicode(item.getString());
                    if (name.equals("radioBtn"))
                    {
                        radioValue = value;
                    }
                    else if (name.equals("resumeText"))
                    {
                        resumeText = value;
                    }
                }
                else
                {
                    filename = item.getName();
                    if (filename == null || filename.equals(""))
                    {
                        // user hit done button but didn't modify the page
                        continue;
                    }
                    else
                    {
                        doUpload = true;
                        data = item.get();
                    }
                }
            }
            if (radioValue != null)
            {
                if (radioValue.equals("doc") && doUpload)
                {
                    vendor.setResume(filename, data);
                    try
                    {
                        ServerProxy.getVendorManagement()
                                .saveResumeFile(vendor);
                    }
                    catch (Exception e)
                    {
                        throw new EnvoyServletException(e);
                    }
                }
                else if (radioValue.equals("text"))
                {
                    vendor.setResume(resumeText);
                }
            }
        }
        catch (FileUploadException fe)
        {
            throw new EnvoyServletException(fe);
        }
    }

    /**
     * Save the request parameters from the projects page
     */
    public static void saveProjects(Vendor vendor, HttpServletRequest request)
            throws EnvoyServletException
    {
        String toField = (String) request.getParameter("toField");
        ArrayList projects = new ArrayList();
        if (toField != null)
        {
            if (toField.equals(""))
            {
                vendor.setProjects(null);
            }
            else
            {
                try
                {
                    String[] projIds = toField.split(",");
                    for (int i = 0; i < projIds.length; i++)
                    {
                        Project proj = ServerProxy.getProjectHandler()
                                .getProjectById(Long.parseLong(projIds[i]));
                        projects.add(proj);
                    }
                    vendor.setProjects(projects);
                }
                catch (Exception ge)
                {
                    throw new EnvoyServletException(ge);
                }
            }
        }

        PermissionSet permSet = (PermissionSet) request.getSession(false)
                .getAttribute(WebAppConstants.PERMISSIONS);

        if (permSet.getPermissionFor(Permission.USERS_PROJECT_MEMBERSHIP) == true)
        {
            if (request.getParameter("allProjects") != null)
            {
                vendor.isInAllProjects(true);
            }
            else
            {
                vendor.isInAllProjects(false);
            }
        }
    }

    public static List getProjectsManagedByUser(User user)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getProjectsManagedByUser(
                    user, Permission.GROUP_MODULE_VENDOR_MANAGER);
        }
        catch (Exception ge)
        {
            throw new EnvoyServletException(ge);
        }
    }

    private static VendorRole activityInRole(String activityName,
            LocalePair lp, Vendor vendor)
    {
        Set roles = vendor.getRoles();
        for (Iterator r = roles.iterator(); r.hasNext();)
        {
            VendorRole role = (VendorRole) r.next();
            LocalePair existingLP = role.getLocalePair();
            if (existingLP.getId() == lp.getId()
                    && activityName
                            .equals(role.getActivity().getActivityName()))
            {
                return role;
            }
        }
        return null;
    }

    private static Rate getRate(Activity activity, HttpServletRequest request)
            throws EnvoyServletException
    {
        boolean isCostingEnabled = false;
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            isCostingEnabled = sc
                    .getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
        }
        catch (Exception e)
        {
        }
        Rate rate = null;
        if (isCostingEnabled)
        {
            // Get the rate
            String rateId = (String) request.getParameter(activity
                    .getActivityName() + "_expense");
            if (!rateId.equals("-1"))
            {
                try
                {
                    rate = (Rate) ServerProxy.getCostingEngine().getRate(
                            Long.parseLong(rateId));
                }
                catch (Exception e)
                {
                    throw new EnvoyServletException(e);
                }
            }
        }
        return rate;
    }
}
