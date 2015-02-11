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
package com.globalsight.everest.webapp.pagehandler.administration.users;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.UserSecureFields;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.AppletDirectory;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.usermgr.UserLdapHelper;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.util.comparator.CompanyComparator;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.util.comparator.RateComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * Class containing static utility methods available to user pagehandlers.
 */
public class UserUtil
{
    static private final Logger logger = Logger.getLogger(UserUtil.class);

    static final String COST_ATTR = "Cost";

    static final String NAME_TYPE_ATTR = "selectName";
    static final String MATCH_TYPE_ATTR = "selectMatchType";
    static final String NAME_TEXT_ATTR = "textName";
    static final String PERM_GROUP_ATTR = "selectPermGroup";
    static final String SOURCE_LOCALE_ATTR = "selectSourceLocale";
    static final String TARGET_LOCALE_ATTR = "selectTargetLocale";
    static final String SUPER_COMPANY = "supperCompany";
    static final String ALL_COMPANY_MAP = "allCompanyMap";
    static final String UPDATE_COMPANY_MAP_FLAG = "updateCompnayMapFlag";
    static final String IS_YES = "yes";

    static final String USERNAME_ATTR = "userName";
    static final String SSOUSERNAME_ATTR = "ssoUserName";
    static final String FIRSTNAME_ATTR = "firstName";
    static final String LASTNAME_ATTR = "lastName";
    static final String PASSWORD_ATTR = "password";
    static final String ADDRESS_ATTR = "address";
    static final String TITLE_ATTR = "title";
    static final String WSSEPASSWORD_ATTR = "wssePassword";
    static final String COMPANYNAME_ATTR = "companyName";
    static final String EMAIL_ATTR = "email";
    static final String CC_EMAIL_ATTR = "ccEmail";
    static final String BCC_EMAIL_ATTR = "bccEmail";
    static final String HOMEPHONE_ATTR = "homePhone";
    static final String WORKPHONE_ATTR = "workPhone";
    static final String CELLPHONE_ATTR = "cellPhone";
    static final String FAX_ATTR = "fax";
    static final String VENDOR_GROUP_ATTR = "vendorGroups";
    static final String UILOCALE_ATTR = "uiLocale";
    static final int REVENUE = 0;
    static final int EXPENSE = 1;

    private static final String SQL_INSERT_USER_ID_USER_NAME = "insert into USER_ID_USER_NAME values(?, ?)";

    private static final String SQL_QUERY_USER_ID_USER_NAME_USER_ID = "select USER_ID from USER_ID_USER_NAME where USER_NAME=?";

    private static final String SQL_QUERY_USER_ID_USER_NAME_USER_NAME = "select USER_NAME from USER_ID_USER_NAME where USER_ID=?";

    private static final String SQL_REMOVE_USER_ID_USER_NAME = "delete from USER_ID_USER_NAME where USER_ID=?";

    private static final String SQL_UPDATE_USER_ID_USER_NAME = "update USER_ID_USER_NAME set USER_NAME=? where USER_ID=?";

    private static Map<String, String> userIdNameMap = new HashMap<String, String>();

    private static Map<String, String> userNameIdMap = new HashMap<String, String>();

    /**
     * Factory method returns a new CreateUserWrapper object.
     */
    static CreateUserWrapper createCreateUserWrapper(User p_userRequestingAdd)
            throws EnvoyServletException
    {
        UserManager userMgr = getUserManager();
        return new CreateUserWrapper(userMgr, p_userRequestingAdd);
    }

    /**
     * Extract the source locale String from the HttpServletRequest.
     */
    static String extractSourceLocale(HttpServletRequest p_theRequest)
    {
        return p_theRequest.getParameter(SOURCE_LOCALE_ATTR);
    }

    /**
     * Extract the target locale String from the HttpServletRequest.
     */
    static String extractTargetLocale(HttpServletRequest p_theRequest)
    {
        return p_theRequest.getParameter(TARGET_LOCALE_ATTR);
    }

    static String[] extractTargetLocales(HttpServletRequest p_theRequest)
    {
        return p_theRequest.getParameterValues(TARGET_LOCALE_ATTR);
    }

    /**
     * Save the request parameters from the Security page
     */
    public static void extractSecurity(FieldSecurity fs,
            HttpServletRequest request) throws EnvoyServletException
    {
        try
        {
            fs.put(UserSecureFields.ACCESS_GROUPS,
                    request.getParameter("accessGroups"));
            fs.put(UserSecureFields.ADDRESS, request.getParameter("address"));
            fs.put(UserSecureFields.CELL_PHONE,
                    request.getParameter("cellPhone"));
            fs.put(UserSecureFields.COMPANY, request.getParameter("company"));
            fs.put(UserSecureFields.COUNTRY, request.getParameter("country"));
            fs.put(UserSecureFields.EMAIL_ADDRESS,
                    request.getParameter("email"));
            fs.put(UserSecureFields.CC_EMAIL_ADDRESS,
                    request.getParameter("ccEmail"));
            fs.put(UserSecureFields.BCC_EMAIL_ADDRESS,
                    request.getParameter("bccEmail"));
            fs.put(UserSecureFields.EMAIL_LANGUAGE,
                    request.getParameter("emailLanguage"));
            fs.put(UserSecureFields.FAX, request.getParameter("fax"));
            fs.put(UserSecureFields.FIRST_NAME,
                    request.getParameter("firstName"));
            fs.put(UserSecureFields.HOME_PHONE,
                    request.getParameter("homePhone"));
            fs.put(UserSecureFields.LAST_NAME, request.getParameter("lastName"));
            fs.put(UserSecureFields.PASSWORD, request.getParameter("password"));
            fs.put(UserSecureFields.PROJECTS, request.getParameter("projects"));
            fs.put(UserSecureFields.ROLES, request.getParameter("roles"));
            fs.put(UserSecureFields.STATUS, request.getParameter("status"));
            fs.put(UserSecureFields.TITLE, request.getParameter("title"));
            fs.put(UserSecureFields.WORK_PHONE,
                    request.getParameter("workPhone"));
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Extracts base user data from the HttpServletRequest and stores it in the
     * passed CreateUserWrapper.
     */
    static void extractUserData(HttpServletRequest p_request,
            CreateUserWrapper p_wrapper)
    {
        extractUserData(p_request, p_wrapper, true);
    }

    /**
     * Extracts base user data from the HttpServletRequest and stores it in the
     * passed CreateUserWrapper.
     */
    static void extractUserData(HttpServletRequest p_request,
            CreateUserWrapper p_wrapper, boolean extractUid)
    {
        if (p_request == null)
            return;
        if (p_wrapper == null)
            return;

        // Get all the straight strings.
        String userName = p_request.getParameter(USERNAME_ATTR).trim();
        String ssoUN = p_request.getParameter(SSOUSERNAME_ATTR);
        String ssoUserName = ssoUN == null ? null : EditUtil
                .utf8ToUnicode(ssoUN.trim());
        String firstName = EditUtil.utf8ToUnicode(p_request
                .getParameter(FIRSTNAME_ATTR));
        String lastName = EditUtil.utf8ToUnicode(p_request
                .getParameter(LASTNAME_ATTR));
        String password = EditUtil.utf8ToUnicode(p_request
                .getParameter(PASSWORD_ATTR));
        String title = EditUtil.utf8ToUnicode(p_request
                .getParameter(TITLE_ATTR));
        String wssePassword = EditUtil.utf8ToUnicode(p_request
                .getParameter(WSSEPASSWORD_ATTR));
        String companyName = EditUtil.utf8ToUnicode(p_request
                .getParameter(COMPANYNAME_ATTR));

        if (p_request.getParameter("company").equals("false"))
        {
            companyName = EditUtil.utf8ToUnicode(p_request
                    .getParameter("companies"));
        }

        // When modify user
        if (isBlank(companyName) && !isBlank(p_wrapper.getCompanyName()))
        {
            companyName = p_wrapper.getCompanyName();
        }

        // When company admin user creates new users
        if (isBlank(companyName))
        {
            try
            {
                companyName = ServerProxy
                        .getJobHandler()
                        .getCompanyById(
                                Long.parseLong(CompanyThreadLocal.getInstance()
                                        .getValue())).getCompanyName();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        // String group = p_request.getParameter(GROUP_ATTR);
        String uiLocale = p_request.getParameter(UILOCALE_ATTR);

        p_wrapper.setUserName(userName);

        if (ssoUserName != null)
        {
            p_wrapper.setSsoUserId(ssoUserName);
        }

        if (firstName != null)
        {
            p_wrapper.setFirstName(firstName);
        }

        if (lastName != null)
        {
            p_wrapper.setLastName(lastName);
        }

        if (password != null && !password.equals("***************************"))
        {
            if (password.equals(""))
            {
                p_wrapper.setPassword(null);
            }
            else
            {
                p_wrapper.setPassword(password);
            }
        }

        if (title != null)
        {
            p_wrapper.setTitle(title);
        }
        
        if (wssePassword != null)
        {
            p_wrapper.setWssePassword(wssePassword);
        }

        if (companyName != null)
        {
            p_wrapper.setCompanyName(companyName);
        }

        if (uiLocale != null)
        {
            p_wrapper.setDefaultUILocale(uiLocale);
        }
    }

    /**
     * Extracts contact info data from the HttpServletRequest and stores it in
     * the passed CreateUserWrapper.
     */
    static void extractContactInfoData(HttpServletRequest p_request,
            CreateUserWrapper p_wrapper)
    {
        if (p_request == null)
            return;
        if (p_wrapper == null)
            return;

        // Get all the straight strings.
        String address = EditUtil.utf8ToUnicode(p_request
                .getParameter(ADDRESS_ATTR));
        String email = EditUtil.utf8ToUnicode(p_request
                .getParameter(EMAIL_ATTR));
        String ccEmail = EditUtil.utf8ToUnicode(p_request
                .getParameter(CC_EMAIL_ATTR));
        String bccEmail = EditUtil.utf8ToUnicode(p_request
                .getParameter(BCC_EMAIL_ATTR));
        String homePhone = EditUtil.utf8ToUnicode(p_request
                .getParameter(HOMEPHONE_ATTR));
        String workPhone = EditUtil.utf8ToUnicode(p_request
                .getParameter(WORKPHONE_ATTR));
        String cellPhone = EditUtil.utf8ToUnicode(p_request
                .getParameter(CELLPHONE_ATTR));
        String fax = EditUtil.utf8ToUnicode(p_request.getParameter(FAX_ATTR));
        String uiLocale = p_request.getParameter(UILOCALE_ATTR);

        if (address != null)
        {
            p_wrapper.setAddress(address);
        }
        else
        {
            p_wrapper.setAddress("");
        }

        if (email != null)
        {
            p_wrapper.setEmail(email);
        }
        else
        {
            p_wrapper.setEmail("");
        }

        if (ccEmail != null)
        {
            p_wrapper.setCCEmail(ccEmail);
        }
        else
        {
            p_wrapper.setCCEmail("");
        }

        if (bccEmail != null)
        {
            p_wrapper.setBCCEmail(bccEmail);
        }
        else
        {
            p_wrapper.setBCCEmail("");
        }

        if (homePhone != null)
        {
            p_wrapper.setHomePhoneNumber(homePhone);
        }
        else
        {
            p_wrapper.setHomePhoneNumber("");
        }

        if (workPhone != null)
        {
            p_wrapper.setOfficePhoneNumber(workPhone);
        }
        else
        {
            p_wrapper.setOfficePhoneNumber("");
        }

        if (cellPhone != null)
        {
            p_wrapper.setCellPhoneNumber(cellPhone);
        }
        else
        {
            p_wrapper.setCellPhoneNumber("");
        }

        if (fax != null)
        {
            p_wrapper.setFaxPhoneNumber(fax);
        }
        else
        {
            p_wrapper.setFaxPhoneNumber("");
        }

        if (uiLocale != null)
        {
            p_wrapper.setDefaultUILocale(uiLocale);
        }
        else
        {
            p_wrapper.setDefaultUILocale("");
        }
    }

    /**
     * Extracts contact info data from the HttpServletRequest and stores it in
     * the passed CreateUserWrapper.
     */
    public static void extractPermissionData(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        if (p_request == null)
            return;

        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String toField = (String) p_request.getParameter("toField");
        ArrayList<PermissionGroup> userPerms = new ArrayList<PermissionGroup>();
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
     * Extracts calendar data from the HttpServletRequest and stores it in the
     * passed CreateUserWrapper.
     */
    public static UserFluxCalendar extractCalendarData(
            HttpServletRequest p_request, String userId)
            throws EnvoyServletException
    {
        return extractCalendarData(p_request, userId, false);
    }

    /**
     * Extracts calendar data from the HttpServletRequest and stores it in the
     * passed CreateUserWrapper.
     */
    public static UserFluxCalendar extractCalendarData(
            HttpServletRequest p_request, String userId, boolean fromBase)
            throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        UserFluxCalendar cal = (UserFluxCalendar) sessionMgr
                .getAttribute(CalendarConstants.CALENDAR);
        if (cal == null)
        {
            // Create calendar.
            String parentId = (String) p_request
                    .getParameter(CalendarConstants.BASE_CAL_FIELD);
            String activityBuffer = "0"; // TomyD -- just a placeholder
            String tz = (String) p_request
                    .getParameter(CalendarConstants.TZ_FIELD);
            cal = new UserFluxCalendar(Long.parseLong(parentId), userId,
                    Integer.parseInt(activityBuffer), tz);
            // Save the parent id and timezone. Needed in case user does
            // a cancel out of reserved times. That removes all calendar
            // updates - basically create a new cal
            sessionMgr.setAttribute(CalendarConstants.BASE_CAL_ID, parentId);
            sessionMgr.setAttribute(CalendarConstants.TIME_ZONE, tz);
        }
        if (fromBase)
        {
            String parentId = (String) p_request
                    .getParameter(CalendarConstants.BASE_CAL_FIELD);
            CalendarHelper.updateUserCalFieldsFromBase(parentId, cal);
        }
        else
        {
            CalendarHelper.updateUserCalFields(p_request, cal);
        }
        sessionMgr.setAttribute(CalendarConstants.CALENDAR, cal);
        return cal;
    }

    /**
     * Extracts project data from the HttpServletRequest and stores it in the
     * passed CreateUserWrapper.
     */
    static void extractProjectData(HttpServletRequest p_request,
            CreateUserWrapper p_wrapper)
    {
        if (p_request == null)
            return;
        if (p_wrapper == null)
            return;

        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.setAttribute("ProjectPageVisited", "true");

        String toField = (String) p_request.getParameter("toField");
        ArrayList<Long> projectIds = new ArrayList<Long>();
        if (toField != null && !toField.equals(""))
        {
            String[] projIds = toField.split(",");
            for (int i = 0; i < projIds.length; i++)
            {
                projectIds.add(Long.decode(projIds[i]));
            }
        }

        p_wrapper.setProjects(projectIds);
        PermissionSet permSet = (PermissionSet) p_request.getSession(false)
                .getAttribute(WebAppConstants.PERMISSIONS);

        if (permSet.getPermissionFor(Permission.USERS_PROJECT_MEMBERSHIP))
        {
            if (p_request.getParameter("allProjects") != null)
            {
                p_wrapper.setIsInAllProjects(true);
            }
            else
            {
                p_wrapper.setIsInAllProjects(false);
            }
        }
    }

    /**
     * Prepare data for the Role page
     */
    static void prepareRolesPage(HttpSession session,
            HttpServletRequest p_theRequest, CreateUserWrapper wrapper,
            String sourceLocale, String targetLocale, boolean addAnotherFlag)
            throws EnvoyServletException
    {
        boolean isCostingEnabled = false;
        boolean isRevenueEnabled = false;

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            isCostingEnabled = sc
                    .getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
            isRevenueEnabled = sc
                    .getBooleanParameter(SystemConfigParamNames.REVENUE_ENABLED);
        }
        catch (Exception e)
        {
            // Problem getting system parameter.
        }

        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        StringBuffer sourceLocaleBuf = new StringBuffer();
        StringBuffer jsBuf = new StringBuffer();
        StringBuffer paddingBuf = new StringBuffer();
        StringBuffer activityBuf = new StringBuffer();
        StringBuffer addAnother = new StringBuffer();

        // get the UI locale
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        ResourceBundle bundle = PageHandler.getBundle(session);
        Hashtable sourceTargetMap = wrapper.getSourceTargetMap();
        String selectedCompanyId = getSelectedCompanyId(p_theRequest, wrapper);
        wrapper.setCurCompanyId(selectedCompanyId);

        StringBuffer allRoleCompanyNamesBuf = null;

        // update Company Map here
        if ((sessionMgr.getAttribute(UPDATE_COMPANY_MAP_FLAG) != null)
                && (sessionMgr.getAttribute(UPDATE_COMPANY_MAP_FLAG)
                        .equals(IS_YES)))
        {
            sessionMgr.removeElement(UPDATE_COMPANY_MAP_FLAG);
            HashMap companyMap = getAllCompanyMap(sessionMgr);
            companyMap.remove(selectedCompanyId);
            if (companyMap.keySet().iterator().hasNext())
            {
                selectedCompanyId = (String) companyMap.keySet().iterator()
                        .next();
            }
            sessionMgr.setAttribute(ALL_COMPANY_MAP, companyMap);
            sourceLocale = null;
            targetLocale = null;
        }
        // create company select HTML
        // if is supper company, perpare the Company data map and list;
        allRoleCompanyNamesBuf = createCompanyHTML(sessionMgr, wrapper,
                selectedCompanyId);

        // Select change, no need to find the activiy for the locale.
        if (p_theRequest.getAttribute("selectSource") != null)
        {
            sourceLocale = null;
            targetLocale = null;
        }

        UserUtil.createLocaleHTML(selectedCompanyId, sourceLocaleBuf, jsBuf,
                paddingBuf, bundle, sourceTargetMap, uiLocale, addAnother,
                sourceLocale, targetLocale, addAnotherFlag);
        // have add all the roles for the company. so remove this company from
        // the array.
        // and update the companyMap, allCompanyNames
        // addAnother is a flag to enable the add buttom.
        // when action setRate do update.

        // first add locale, set the flag to update companyMap.
        /*
         * if ( isSuperCompany(sessionMgr, wrapper) &&
         * ("false").equals(addAnother.toString()) &&
         * (getAllCompanyMap(sessionMgr) != null)) { HashMap companyMap =
         * getAllCompanyMap(sessionMgr); if (companyMap.size() > 1) { addAnother
         * = new StringBuffer("true"); if (addAnotherFlag == false) {
         * sessionMgr.setAttribute(UPDATE_COMPANY_MAP_FLAG, IS_YES); } } else {
         * if(addAnotherFlag == false) {
         * sessionMgr.removeElement(ALL_COMPANY_MAP); }
         * 
         * } }
         */

        sessionMgr.setAttribute(UserConstants.ADD_ANOTHER, addAnother);
        UserUtil.createActivitySelectionHTML(selectedCompanyId, activityBuf,
                uiLocale, sourceLocale, targetLocale);
        p_theRequest.setAttribute("allRoleCompanyNames",
                allRoleCompanyNamesBuf.toString());
        p_theRequest.setAttribute("activities", activityBuf.toString());
        p_theRequest.setAttribute("allSourceLocales",
                sourceLocaleBuf.toString());
        p_theRequest.setAttribute("jsArrays", jsBuf.toString());
        p_theRequest.setAttribute("optionPadding", paddingBuf.toString());
        p_theRequest.setAttribute(SystemConfigParamNames.COSTING_ENABLED,
                new Boolean(isCostingEnabled));
        p_theRequest.setAttribute(SystemConfigParamNames.REVENUE_ENABLED,
                new Boolean(isRevenueEnabled));
    }

    /**
     * Generate the activity-cost map hashtable from the HttpServletRequest.
     * 
     * @return A Hashtable containing String names of activities as keys, and
     *         Float costs as values.
     */
    static Hashtable<Activity, Vector<String>> generateActivityCostMap(
            HttpServletRequest p_theRequest) throws EnvoyServletException
    {
        Hashtable<Activity, Vector<String>> retVal = new Hashtable<Activity, Vector<String>>();
        Locale uiLocale = (Locale) p_theRequest.getSession().getAttribute(
                WebAppConstants.UILOCALE);

        Vector<Activity> vActivities = UserHandlerHelper
                .getAllActivities(uiLocale);
        Enumeration<Activity> eActivities = vActivities.elements();
        while (eActivities.hasMoreElements())
        {
            Activity curActivity = (Activity) eActivities.nextElement();
            Vector<String> params = new Vector<String>();
            String activityName = curActivity.getActivityName();
            String activityCostString = curActivity.getActivityName()
                    + COST_ATTR;

            String costStr = p_theRequest.getParameter(activityCostString);
            params.addElement((String) p_theRequest
                    .getParameter(activityCostString));
            params.addElement((String) p_theRequest.getParameter(activityName
                    + "_expense"));
            if (costStr != null && (!(costStr.equalsIgnoreCase(""))))
            {
                // If result from the cost field isn't null or the empty
                // string, then try to add this pair to the hashtable.
                retVal.put(curActivity, params);
            }
        }

        return retVal;
    }

    /**
     * Generates an HTML option String based on the LocaleWrapper passed and the
     * UI locale it should be displayed in.
     */
    static String genLocaleOptionString(GlobalSightLocale p_localeWrapper,
            Locale p_uiLocale)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("<option value=\"");
        buf.append(p_localeWrapper.toString());
        buf.append("\" selected>");
        buf.append(p_localeWrapper.getDisplayName(p_uiLocale));
        buf.append("</option>\n");

        return buf.toString();
    }

    /**
     * Builds a Vector of User objects based on search parameters passed to the
     * method. Converts the search parameters built up on the web page into
     * LDAP-enabled methods, and passes them on to the UserManager.
     * 
     * @param p_searchParams
     *            String-based search parameters used to construct the actual
     *            UserManager query.
     * @return A Vector of User objects complying with the search parameters.
     *         Returns an empty Vector if the search parameters are null, or if
     *         an exception is raised by the calls to UserManager.
     */
    @SuppressWarnings("unchecked")
    static Vector getUsersForSearchParams(UserSearchParams p_searchParams)
            throws EnvoyServletException
    {
        Vector retVal = new Vector();

        // Get the user manager, for queries.
        UserManager userMgr = getUserManager();
        if (p_searchParams != null && p_searchParams.isValid())
        {
            try
            {
                retVal = userMgr.getUsers(p_searchParams, null);

                // filter out the users not in the specified perm group
                String permGroupName = p_searchParams.getPermissionGroupParam();
                if (p_searchParams.getPermissionGroupParam() != null)
                {
                    if (permGroupName != null && !permGroupName.equals(""))
                    {
                        // only filter if they selected a real perm group
                        ArrayList pgUsers = new ArrayList(Permission
                                .getPermissionManager()
                                .getAllUsersForPermissionGroup(permGroupName));
                        Iterator iter = retVal.iterator();
                        while (iter.hasNext())
                        {
                            User u = (User) iter.next();
                            if (pgUsers.contains(u.getUserId()) == false)
                                iter.remove();
                        }
                    }
                }
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
        else
        {
            retVal = UserHandlerHelper.getUsers();
        }

        return retVal;
    }

    /**
     * Pulls all the attributes off the HttpServletRequest for the purposes of
     * generating search parameters.
     * 
     * @param p_theRequest
     *            The HttpServletRequest we're wanting to pull attributes off
     * @return A valid UserSearchParams object based on the attributes found on
     *         the HttpServletRequest.
     */
    static UserSearchParams generateSearchParams(HttpServletRequest p_theRequest)
    {
        Integer selectName = new Integer(-1);
        Integer selectMatchType = new Integer(-1);

        if (p_theRequest.getParameter(NAME_TYPE_ATTR) != null)
        {
            selectName = new Integer(p_theRequest.getParameter(NAME_TYPE_ATTR));
        }

        if (p_theRequest.getParameter(MATCH_TYPE_ATTR) != null)
        {
            selectMatchType = new Integer(
                    p_theRequest.getParameter(MATCH_TYPE_ATTR));
        }

        String textName = p_theRequest.getParameter(NAME_TEXT_ATTR);
        String selectSourceLocale = p_theRequest
                .getParameter(SOURCE_LOCALE_ATTR);
        String selectTargetLocale = p_theRequest
                .getParameter(TARGET_LOCALE_ATTR);
        String selectPermGroup = p_theRequest.getParameter(PERM_GROUP_ATTR);

        UserSearchParams params = new UserSearchParams();
        params.setNameFilter(selectMatchType.intValue());
        params.setNameType(selectName.intValue());
        params.setNameParam(textName);

        String noneStr = "none";

        if (selectSourceLocale != null
                && !(selectSourceLocale.equalsIgnoreCase(noneStr)))
        {
            params.setSourceLocaleParam(selectSourceLocale);
        }

        if (selectTargetLocale != null
                && !(selectTargetLocale.equalsIgnoreCase(noneStr)))
        {
            params.setTargetLocaleParam(selectTargetLocale);
        }

        if (selectPermGroup != null)
        {
            params.setPermissionGroupParam(selectPermGroup);
        }

        return params;
    }

    /**
     * Removes all user-activity related values from the session manager.
     */
    public static void removeUserAttrsFromSessionMgr(SessionManager p_mgr)
    {
        p_mgr.removeElement(UserConstants.MODIFY_USER_WRAPPER);
        p_mgr.removeElement(UserConstants.CREATE_USER_WRAPPER);

        p_mgr.removeElement(Modify2Handler.MOD_ROLES);
        p_mgr.removeElement(Modify2Handler.SOURCE_LOCALE);
        p_mgr.removeElement(Modify2Handler.TARGET_LOCALE);
    }

    /**
     * Builds up three StringBuffers with the relevant HTML and JavaScript for
     * enabling dynamic repopulation of target locale dropdown based on
     * selection in source locale dropdown.
     * 
     * @param p_sourceLocaleBuffer
     *            The StringBuffer to be filled with source locale options.
     * @param p_targetLocaleBuffer
     *            The StringBuffer to be filled with target locale JavaScript.
     * @param p_paddingBuffer
     *            The StringBuffer to be filled with blank options to ensure
     *            proper working of the script in the JSP.
     * @param p_bundle
     * @param p_sourceTargetMap
     *            Map that holds a source locale as a key and a vector or target
     *            locales associated with each source. (GlobalSightLocales)
     * @param p_uiLocale
     *            The locale the UI is being displayed in for this user.
     */
    static void createLocaleHTML(String p_companyId,
            StringBuffer p_sourceLocaleBuffer,
            StringBuffer p_targetLocaleBuffer, StringBuffer p_paddingBuffer,
            ResourceBundle p_bundle, Hashtable p_sourceTargetMap,
            final Locale p_uiLocale, StringBuffer p_addAnother,
            String p_sourceLocale, String p_targetLocale, boolean addAnotherFlag)
            throws EnvoyServletException
    {

        // Get the user manager, for queries.
        // LocaleManager localeMgr = ServerProxy.getLocaleManager();
        // ResourceBundle bundle =
        // SystemResourceBundle.getInstance().getResourceBundle(ResourceBundleConstants.LOCALE_RESOURCE_NAME,
        // Locale.getDefault());
        int curLargestTargetList = 0;
        GlobalSightLocale sourceSelected = getLocale(p_sourceLocale);
        GlobalSightLocale targetSelected = getLocale(p_targetLocale);

        Vector vSourceLocales = UserHandlerHelper
                .getAllSourceLocalesByCompanyId(p_companyId);
        ArrayList<GlobalSightLocale> sourceLocales = new ArrayList<GlobalSightLocale>();
        for (int i = 0; i < vSourceLocales.size(); i++)
        {
            GlobalSightLocale curLocale = (GlobalSightLocale) vSourceLocales
                    .elementAt(i);
            sourceLocales.add(curLocale);
        }
        SortUtil.sort(sourceLocales,
                new GlobalSightLocaleComparator(Locale.getDefault()));
        int sourceSize = 0;
        int targetSize = 0;
        int sourceId = 0;
        for (int i = 0; i < sourceLocales.size(); i++)
        {
            GlobalSightLocale curLocale = sourceLocales.get(i);

            StringBuffer targetTextBuf = new StringBuffer();
            StringBuffer targetValueBuf = new StringBuffer();

            Vector<GlobalSightLocale> vValidTargets = UserHandlerHelper
                    .getTargetLocalesByCompanyId(curLocale, p_companyId);

            SortUtil.sort(vValidTargets, new Comparator<GlobalSightLocale>()
            {
                public int compare(GlobalSightLocale locale1,
                        GlobalSightLocale locale2)
                {
                    return locale1.getDisplayName(p_uiLocale).compareTo(
                            locale2.getDisplayName(p_uiLocale));
                }
            });

            // Trim out the used target locales for the current source
            // locales.
            Vector vUsedTargets = (Vector) p_sourceTargetMap.get(curLocale
                    .toString() + "=" + p_companyId);
            if (vUsedTargets != null)
            {
                for (Iterator iValidTargets = vValidTargets.iterator(); iValidTargets
                        .hasNext();)
                {
                    GlobalSightLocale curElement = (GlobalSightLocale) iValidTargets
                            .next();
                    if (vUsedTargets.contains(curElement.toString()))
                    {
                        iValidTargets.remove();
                    }
                }
            }
            targetSize = vValidTargets.size();
            if (targetSize > 0)
            {
                sourceSize++;
                p_sourceLocaleBuffer.append("<option ");
                if (curLocale.equals(sourceSelected) && !addAnotherFlag)
                {
                    p_sourceLocaleBuffer.append("SELECTED ");
                }
                p_sourceLocaleBuffer.append(" value=\"");
                p_sourceLocaleBuffer.append(curLocale.toString());
                p_sourceLocaleBuffer.append("\">");
                p_sourceLocaleBuffer.append(curLocale
                        .getDisplayName(p_uiLocale));
                p_sourceLocaleBuffer.append("</option>\n");
                targetTextBuf.append("var targetArrayText");
                targetTextBuf.append(sourceId + 1);
                targetTextBuf.append(" = new Array(\"");
                targetTextBuf.append(p_bundle.getString("lb_choose"));

                targetValueBuf.append("var targetArrayValue");
                targetValueBuf.append(sourceId + 1);
                targetValueBuf.append(" = new Array(\"-1");
                for (int j = 0; j < vValidTargets.size(); j++)
                {
                    GlobalSightLocale curValidTarget = (GlobalSightLocale) vValidTargets
                            .elementAt(j);

                    targetTextBuf.append("\",");
                    targetTextBuf.append("\"");
                    targetTextBuf.append(curValidTarget
                            .getDisplayName(p_uiLocale));

                    targetValueBuf.append("\",");
                    targetValueBuf.append("\"");
                    targetValueBuf.append(curValidTarget.toString());
                }
                // We're at the end. Close the arrays.
                targetTextBuf.append("\");");
                targetValueBuf.append("\");");
                p_targetLocaleBuffer.append(targetTextBuf.toString());
                p_targetLocaleBuffer.append("\n");
                p_targetLocaleBuffer.append(targetValueBuf.toString());
                p_targetLocaleBuffer.append("\n\n");
                sourceId++;
            }

            // Get the size of the target list, if it's larger than the
            // current max. We need this number to generate the options
            // padding.

            if (curLargestTargetList < vValidTargets.size())
            {
                curLargestTargetList = vValidTargets.size();
            }

        }
        if ((sourceSize == 1) && (targetSize <= 1))
        {
            p_addAnother.append("false");
        }
        else
        {
            p_addAnother.append("true");
        }
        if (targetSelected == null || addAnotherFlag)
        {
            p_paddingBuffer.append("<option value=\"-1\">&lt;--");
            p_paddingBuffer.append(p_bundle.getString("lb_sel_source_first"));
            p_paddingBuffer.append("</option>");
        }
        else
        {
            String targetStr = targetSelected.getDisplayName(p_uiLocale);
            // if(targetStr != null && targetStr.trim()!=""){
            p_paddingBuffer.append("<option value=\"" + p_targetLocale + "\"");
            p_paddingBuffer.append(" SELECTED>");
            p_paddingBuffer.append(targetStr);
            p_paddingBuffer.append("</option>");
        }
        for (int k = 0; k < curLargestTargetList; k++)
        {
            p_paddingBuffer.append("<option value=\"\"></option>\n");
        }
    }

    /**
     * Generate the activity/cost HTML table for the activities currently in the
     * system.
     */
    static void createActivityCostHTML(StringBuffer p_leftActivities,
            StringBuffer p_rightActivities, Locale p_locale)
            throws EnvoyServletException
    {
        Vector vActivities = UserHandlerHelper.getAllActivities(p_locale);
        Vector vLeftActivities = null;
        Vector vRightActivities = null;

        // Split activities into two Vectors, so that the table's even (if there
        // are an odd number of activities, then the left column will have get
        // the extra).
        if (vActivities.size() < 1)
        {
            // No activities.
            vLeftActivities = new Vector();
            vRightActivities = new Vector();
        }
        else if (vActivities.size() == 1)
        {
            // One activity.
            vLeftActivities = new Vector(vActivities);
            vRightActivities = new Vector();
        }
        else if (vActivities.size() % 2 > 0)
        {
            // Odd number of activities, greater than 1.
            vLeftActivities = new Vector(vActivities.subList(0,
                    (vActivities.size() / 2) + 1));

            vRightActivities = new Vector(vActivities.subList(
                    ((vActivities.size() / 2) + 1), (vActivities.size())));
        }
        else
        {
            // Even number of activities, greater than one.
            vLeftActivities = new Vector(vActivities.subList(0,
                    vActivities.size() / 2));

            vRightActivities = new Vector(vActivities.subList(
                    (vActivities.size() / 2), (vActivities.size())));
        }

        genTableHTMLForActivityList(vLeftActivities, p_leftActivities);
        genTableHTMLForActivityList(vRightActivities, p_rightActivities);
    }

    /**
     * Generate the activity/cost HTML table for the activities currently in the
     * system.
     */
    public static void createActivitySelectionHTML(String p_companyId,
            StringBuffer p_activities, Locale p_locale, String p_source,
            String p_target) throws EnvoyServletException
    {
        Vector vActivities = UserHandlerHelper.getAllActivities(p_companyId,
                p_locale);
        genSelectHTMLForActivityList(vActivities, p_activities, p_locale,
                p_source, p_target);
    }

    /**
     * Generate the activity/cost HTML table for the activities currently in the
     * system.
     */
    public static void createActivitySelectionHTML(StringBuffer p_activities,
            Locale p_locale, String p_source, String p_target)
            throws EnvoyServletException
    {
        Vector vActivities = UserHandlerHelper.getAllActivities(p_locale);
        genSelectHTMLForActivityList(vActivities, p_activities, p_locale,
                p_source, p_target);
    }

    /**
     * Private helper method for generating HTML based on activity list.
     */
    private static void genSelectHTMLForActivityList(Vector p_activityList,
            StringBuffer p_buffer, Locale p_locale, String p_source,
            String p_target) throws EnvoyServletException
    {
        Enumeration eActivities = p_activityList.elements();
        int count = 0;
        String bgColor = "#EEEEEE";

        while (eActivities.hasMoreElements())
        {
            StringBuffer expenses = new StringBuffer();
            Activity curActivity = (Activity) eActivities.nextElement();

            if (count % 2 == 0)
            {
                bgColor = "White";
            }
            p_buffer.append("<TR CLASS=standardText BGCOLOR=\"" + bgColor
                    + "\">\n");
            p_buffer.append("<TD NOWRAP VALIGN=TOP>");

            p_buffer.append("<INPUT TYPE=\"CHECKBOX\" NAME=\"");
            p_buffer.append(curActivity.getActivityName() + COST_ATTR);
            p_buffer.append("\" VALUE=\"true\">");
            p_buffer.append(curActivity.getDisplayName());

            if (isJobCostingEnabled())
            {
                p_buffer.append("</TD>\n");
                p_buffer.append("<TD NOWRAP VALIGN=TOP>");
                createRateSelectionHTML(expenses, p_locale, p_source, p_target,
                        curActivity, EXPENSE, -1);
                p_buffer.append(expenses);
                p_buffer.append("</TD>\n");
                p_buffer.append("</TR>\n");
            }
            count++;
        }
    }

    /**
     * Private helper method for generating HTML based on activity list.
     */
    private static void genTableHTMLForActivityList(Vector p_activityList,
            StringBuffer p_buffer)
    {
        ResourceBundle bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(
                        ResourceBundleConstants.LOCALE_RESOURCE_NAME,
                        Locale.getDefault());
        Enumeration eActivities = p_activityList.elements();
        while (eActivities.hasMoreElements())
        {
            Activity curActivity = (Activity) eActivities.nextElement();

            p_buffer.append("<TR CLASS=standardText>\n");
            p_buffer.append("<TD NOWRAP VALIGN=TOP>");

            p_buffer.append("<INPUT TYPE=\"CHECKBOX\" NAME=\"");
            p_buffer.append(curActivity.getActivityName() + COST_ATTR);
            p_buffer.append("\" VALUE=\"true\">");
            p_buffer.append(curActivity.getDisplayName());

            p_buffer.append("</TD>\n");
            p_buffer.append("</TR>\n");
        }
    }

    /**
     * Generate the activity/cost HTML table for the activities currently in the
     * system, with cost data for the roles passed in as an argument.
     */
    static void createActivityHTMLForRoles(StringBuffer p_activities,
            Vector p_roles, Locale p_locale, String p_source, String p_target)
            throws EnvoyServletException
    {

        Vector vActivities = UserHandlerHelper.getAllActivities(p_locale);
        genHTMLForActivityList(vActivities, p_roles, p_activities, p_source,
                p_target, p_locale);
    }

    /**
     * Generate the activity/cost HTML table for the activities currently in the
     * system, with cost data for the roles passed in as an argument.
     * 
     * @param selectedCompanyId
     * @param activityBuf
     * @param roles
     * @param uiLocale
     * @param sourceLocale
     * @param targetLocale
     */
    public static void createActivityHTMLForRoles(String selectedCompanyId,
            StringBuffer p_activities, Vector p_roles, Locale p_locale,
            String p_source, String p_target) throws EnvoyServletException
    {
        Vector vActivities = UserHandlerHelper.getAllActivities(
                selectedCompanyId, p_locale);
        genHTMLForActivityList(vActivities, p_roles, p_activities, p_source,
                p_target, p_locale);

    }

    /**
     * Private helper method for generating HTML based on activity list and
     * roles.
     */
    private static void genHTMLForActivityList(Vector p_activityList,
            Vector p_roles, StringBuffer p_buffer, String p_source,
            String p_target, Locale p_locale) throws EnvoyServletException
    {

        ResourceBundle bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(
                        ResourceBundleConstants.LOCALE_RESOURCE_NAME,
                        Locale.getDefault());
        Enumeration eActivities = p_activityList.elements();
        while (eActivities.hasMoreElements())
        {
            StringBuffer expenses = new StringBuffer();
            Activity curActivity = (Activity) eActivities.nextElement();
            String curActivityName = curActivity.getActivityName();
            boolean isReview = curActivity.isType(Activity.TYPE_REVIEW);

            if (isReview)
            {
                boolean isEditable = curActivity.getIsEditable();
                isReview = isReview && !isEditable;
            }

            p_buffer.append("<TR CLASS=standardText>\n");
            p_buffer.append("<TD NOWRAP VALIGN=TOP>");
            p_buffer.append("<INPUT TYPE=\"CHECKBOX\" NAME=\"");
            p_buffer.append(curActivity.getActivityName() + COST_ATTR);
            p_buffer.append("\" VALUE=\"true\" ");
            p_buffer.append(" isReviewOnly = \"" + isReview + "\"");
            long rateId = -1;

            Enumeration eRoles = p_roles.elements();
            while (eRoles.hasMoreElements())
            {
                Role curRole = (Role) eRoles.nextElement();

                if (curActivityName.equals(curRole.getActivity()
                        .getActivityName()))
                {
                    Rate rate = null;
                    p_buffer.append("CHECKED");
                    String sRate = ((UserRole) curRole).getRate();
                    Vector rates = new Vector(((Role) curRole).getRates());
                    // if(rate == null || rate.equals(""))
                    if (rates == null || rates.size() < 1)
                    {
                        if ((sRate == null || sRate.equals("")))
                        {
                            rateId = -1;
                        }
                        else
                        {
                            Long rateLong = new Long(sRate);
                            rateId = rateLong.longValue();
                        }
                    }
                    else
                    {
                        // Assuming that there is just one rate associated with
                        // user
                        // get the first one
                        rate = (Rate) rates.elementAt(0);
                        rateId = rate.getId();
                    }
                    break;
                }
            }

            p_buffer.append(">");
            p_buffer.append(curActivity.getDisplayName());
            if (isJobCostingEnabled())
            {
                p_buffer.append("</TD>\n");
                p_buffer.append("<TD NOWRAP VALIGN=TOP>");
                createRateSelectionHTML(expenses, p_locale, p_source, p_target,
                        curActivity, EXPENSE, rateId);
                p_buffer.append(expenses);
                p_buffer.append("</TD>\n");
                p_buffer.append("</TR>\n");
            }
        }
    }

    /**
     * Generate the activity/cost HTML table for the activities currently in the
     * system, with cost data for the roles passed in as an argument.
     */
    static void createActivityCostHTMLForRoles(StringBuffer p_leftActivities,
            StringBuffer p_rightActivities, Vector p_roles, Locale p_locale)
            throws EnvoyServletException
    {
        Vector vActivities = UserHandlerHelper.getAllActivities(p_locale);
        Vector vLeftActivities = null;
        Vector vRightActivities = null;

        // Split activities into two Vectors, so that the table's even (if there
        // are an odd number of activities, then the left column will have get
        // the extra).
        // Split activities into two Vectors, so that the table's even (if there
        // are an odd number of activities, then the left column will have get
        // the extra).
        if (vActivities.size() < 1)
        {
            // No activities.
            vLeftActivities = new Vector();
            vRightActivities = new Vector();
        }
        else if (vActivities.size() == 1)
        {
            // One activity.
            vLeftActivities = new Vector(vActivities);
            vRightActivities = new Vector();

        }
        else if (vActivities.size() % 2 > 0)
        {
            // Odd number of activities, greater than 1.
            vLeftActivities = new Vector(vActivities.subList(0,
                    (vActivities.size() / 2) + 1));

            vRightActivities = new Vector(vActivities.subList(
                    ((vActivities.size() / 2) + 1), (vActivities.size())));
        }
        else
        {
            // Even number of activities, greater than one.
            vLeftActivities = new Vector(vActivities.subList(0,
                    vActivities.size() / 2));

            vRightActivities = new Vector(vActivities.subList(
                    (vActivities.size() / 2), (vActivities.size())));
        }

        genTableHTMLForActivityList(vLeftActivities, p_roles, p_leftActivities);
        genTableHTMLForActivityList(vRightActivities, p_roles,
                p_rightActivities);
    }

    /**
     * Private helper method for generating HTML based on activity list and
     * roles.
     */
    private static void genTableHTMLForActivityList(Vector p_activityList,
            Vector p_roles, StringBuffer p_buffer)
    {
        ResourceBundle bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(
                        ResourceBundleConstants.LOCALE_RESOURCE_NAME,
                        Locale.getDefault());

        Enumeration eActivities = p_activityList.elements();
        while (eActivities.hasMoreElements())
        {
            Activity curActivity = (Activity) eActivities.nextElement();
            String curActivityName = curActivity.getActivityName();

            p_buffer.append("<TR CLASS=standardText>\n");
            p_buffer.append("<TD NOWRAP VALIGN=TOP>");

            p_buffer.append("<INPUT TYPE=\"CHECKBOX\" NAME=\"");
            p_buffer.append(curActivity.getActivityName() + COST_ATTR);
            p_buffer.append("\" VALUE=\"true\" ");

            Enumeration eRoles = p_roles.elements();
            while (eRoles.hasMoreElements())
            {
                Role curRole = (Role) eRoles.nextElement();

                if (curActivityName.equals(curRole.getActivity()
                        .getActivityName()))
                {
                    p_buffer.append("CHECKED");
                }
            }

            p_buffer.append(">");
            p_buffer.append(curActivity.getDisplayName());

            p_buffer.append("</TD>\n");
            p_buffer.append("</TR>\n");
        }
    }

    private static UserManager getUserManager() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getUserManager();
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge.getExceptionId(), ge);
        }
    }

    private static String getUserCompanyId(CreateUserWrapper wrapper)
            throws EnvoyServletException
    {
        String companyId = "-1";
        try
        {
            companyId = CompanyWrapper.getCompanyIdByName(wrapper
                    .getCompanyName());
            return companyId;
        }
        catch (PersistenceException e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private static boolean isSuperCompany(SessionManager sessionMgr,
            CreateUserWrapper wrapper) throws EnvoyServletException
    {
        if (sessionMgr.getAttribute(SUPER_COMPANY) != null
                && sessionMgr.getAttribute(SUPER_COMPANY).equals(IS_YES))
        {
            return true;
            // for multi company issue
            // companyId == 1 means that the company is super company, that is
            // "Welocalize".
            // so this user should be able to access all companies in
            // GlobalSight.
        }
        else if (Long.parseLong(getUserCompanyId(wrapper)) == 1)
        {
            sessionMgr.setAttribute(SUPER_COMPANY, IS_YES);
            return true;
        }
        else
        {
            return false;
        }

    }

    /**
     * Create All Company Map, key-companyId, value-companyName,
     * 
     * @param sessionMgr
     * @return
     */
    private static HashMap<String, String> getAllCompanyMap(
            SessionManager sessionMgr) throws EnvoyServletException
    {
        try
        {
            HashMap<String, String> companyMap = new HashMap<String, String>();

            if (sessionMgr.getAttribute(ALL_COMPANY_MAP) == null)
            {
                String[] allCompanis = CompanyWrapper.getAllCompanyNames();
                for (int i = 0; i < allCompanis.length; i++)
                {
                    String curCompanyId = CompanyWrapper
                            .getCompanyIdByName(allCompanis[i]);
                    companyMap.put(curCompanyId, allCompanis[i]);
                }
                sessionMgr.setAttribute(ALL_COMPANY_MAP, companyMap);
            }
            else
            {
                companyMap = (HashMap<String, String>) sessionMgr
                        .getAttribute(ALL_COMPANY_MAP);
            }

            return companyMap;
        }
        catch (PersistenceException e)
        {
            throw new EnvoyServletException(e);
        }
    }

    public static String getSelectedCompanyId(HttpServletRequest p_request,
            CreateUserWrapper wrapper) throws EnvoyServletException
    {
        String selectedCompanyId = (String) p_request
                .getParameter(WebAppConstants.SELECTED_COMPANY_ID);
        if (selectedCompanyId == null)
        {
            selectedCompanyId = getUserCompanyId(wrapper);
        }
        return selectedCompanyId;
    }

    public static String getCurrentCompanyName(HttpServletRequest p_request)
    {
        HttpSession session = null;
        String isApplet = p_request.getParameter(WebAppConstants.APPLET);
        if (isApplet == null)
        {
            // attempt to access the session object
            session = p_request.getSession(false);
        }
        else
        {
            String rand = p_request
                    .getParameter(WebAppConstants.APPLET_DIRECTORY_SESSION_NAME_RANDOM);
            AppletDirectory directory = AppletDirectory.getInstance();
            session = directory.getSession(rand);
        }

        String companyName = null;
        try
        {
            String companyId = p_request
                    .getParameter(CompanyWrapper.CURRENT_COMPANY_ID);
            if (companyId == null)
                throw new NumberFormatException();
            Integer.parseInt(companyId);
            companyName = CompanyWrapper.getCompanyNameById(companyId);
        }
        catch (NumberFormatException ne)
        {
            companyName = p_request
                    .getParameter(CompanyWrapper.CURRENT_COMPANY_ID);
            if (companyName == null)
            {
                companyName = p_request
                        .getParameter(UserLdapHelper.LDAP_ATTR_COMPANY);
            }
        }

        if ((companyName == null || "".equals(companyName.trim()))
                && session != null)
        {
            companyName = (String) session
                    .getAttribute(WebAppConstants.SELECTED_COMPANY_NAME_FOR_SUPER_PM);
            if (UserUtil.isBlank(companyName))
            {
                companyName = (String) session
                        .getAttribute(UserLdapHelper.LDAP_ATTR_COMPANY);
            }
        }

        return companyName;
    }

    static StringBuffer createCompanyHTML(SessionManager sessionMgr,
            CreateUserWrapper wrapper, String selectedCompanyId)
            throws EnvoyServletException
    {
        StringBuffer allRoleCompanyNamesBuf = new StringBuffer();
        if (isSuperCompany(sessionMgr, wrapper))
        {
            // get All company Maps
            HashMap companyMap = getAllCompanyMap(sessionMgr);

            // Fix for GBS-1693
            ArrayList<Company> companies = new ArrayList<Company>();

            for (Iterator it = companyMap.keySet().iterator(); it.hasNext();)
            {
                String curCompanyId = (String) it.next();
                String curCompanyName = (String) companyMap.get(curCompanyId);
                Company company = new Company();
                company.setId(Long.valueOf(curCompanyId));
                company.setName(curCompanyName);
                companies.add(company);
            }
            SortUtil.sort(companies, new CompanyComparator(Locale.getDefault()));

            allRoleCompanyNamesBuf.append("<SELECT NAME=");
            allRoleCompanyNamesBuf.append(WebAppConstants.SELECTED_COMPANY_ID);
            allRoleCompanyNamesBuf
                    .append(" SIZE=\"1\" onChange=\"setSources(selectedIndex);\">");
            for (Company company : companies)
            {
                String curCompanyId = String.valueOf(company.getId());
                String curCompanyName = company.getName();
                allRoleCompanyNamesBuf.append("<option value=\"");
                allRoleCompanyNamesBuf.append(curCompanyId).append("\" ");
                if (curCompanyId.equals(selectedCompanyId))
                {
                    allRoleCompanyNamesBuf.append(" selected ");
                }
                else if (selectedCompanyId == null
                        && Integer.parseInt(curCompanyId) == 1)
                {
                    allRoleCompanyNamesBuf.append(" selected ");
                }
                allRoleCompanyNamesBuf.append(">");
                allRoleCompanyNamesBuf.append(curCompanyName);
                allRoleCompanyNamesBuf.append("</option>");
            }
            allRoleCompanyNamesBuf.append("</SELECT>");
        }
        else
        {
            allRoleCompanyNamesBuf.append(createCompanyHTMLText(wrapper
                    .getCompanyName()));
        }
        return allRoleCompanyNamesBuf;

    }

    public static String createCompanyHTMLText(String companyName)
    {
        return "<B>" + companyName + "</B></br>";
    }

    private static void createRateSelectionHTML(StringBuffer p_buffer,
            Locale uilocale, String p_sourceLocale, String p_targetLocale,
            Activity p_activity, int costType, long p_rateId)
            throws EnvoyServletException
    {
        ResourceBundle bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(
                        ResourceBundleConstants.LOCALE_RESOURCE_NAME, uilocale);
        String activityName = p_activity.getActivityName();
        String name = "";
        name = activityName + "_expense";
        p_buffer.append("<SELECT NAME='" + name + "'>\n");
        p_buffer.append("<OPTION ");
        if (p_rateId == -1)
        {
            p_buffer.append(" SELECTED ");
        }
        p_buffer.append(" VALUE='-1'>" + bundle.getString("lb_no_rate")
                + "</OPTION>");
        if ((p_sourceLocale != null) && (p_targetLocale != null))
        {
            List activityRates = null;
            try
            {
                GlobalSightLocale sourceLocale = getLocale(p_sourceLocale);
                GlobalSightLocale targetLocale = getLocale(p_targetLocale);
                activityRates = (List) ServerProxy.getCostingEngine().getRates(
                        p_activity, sourceLocale, targetLocale);
                SortUtil.sort(activityRates, new RateComparator(
                        RateComparator.NAME, Locale.getDefault()));
                Iterator it = activityRates.iterator();

                while (it.hasNext())
                {
                    Rate rate = (Rate) it.next();
                    long rateId = rate.getId();
                    if (rateId == p_rateId)
                    {
                        p_buffer.append("<OPTION SELECTED VALUE='");
                    }
                    else
                    {
                        p_buffer.append("<OPTION VALUE='");
                    }
                    p_buffer.append(rateId);
                    p_buffer.append("'>");
                    p_buffer.append(rate.getName());
                    p_buffer.append("</OPTION>");
                }
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

        p_buffer.append("</SELECT>\n");
    }

    public static GlobalSightLocale getLocale(String p_locale)
            throws EnvoyServletException
    {
        try
        {
            LocaleManager manager = ServerProxy.getLocaleManager();
            return manager.getLocaleByString(p_locale);
        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex.getExceptionId(), ex);
        }
        catch (RemoteException ex)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, ex);
        }
    }

    public static boolean isJobCostingEnabled()
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
            // Error getting system parameter
        }
        return isCostingEnabled;
    }

    public static boolean isSuperAdmin(String p_userId) throws RuntimeException
    {
        try
        {
            return ServerProxy.getUserManager().containsPermissionGroup(
                    p_userId, WebAppConstants.SUPER_ADMINISTRATOR_NAME);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static boolean isSuperPM(String p_userId) throws RuntimeException
    {
        try
        {
            return ServerProxy.getUserManager().containsPermissionGroup(
                    p_userId, WebAppConstants.SUPER_PM_NAME);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static User getUserById(String userId) throws RuntimeException
    {
        try
        {
            return getUserManager().getUser(userId);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validate if user is super localization participant
     * 
     * @param p_userId
     *            User ID
     * @return boolean If user is a super localization participant, then reutrn
     *         true
     * @throws RuntimeException
     */
    public static boolean isSuperLP(String p_userId) throws RuntimeException
    {
        if (isBlank(p_userId))
            return false;
        try
        {
            User user = getUserManager().getUser(p_userId);
            boolean isLP = getUserManager().containsPermissionGroup(p_userId,
                    WebAppConstants.LOCALIZATION_PARTICIPANT);
            if (CompanyWrapper.getSuperCompanyName().equals(
                    user.getCompanyName())
                    && isLP)
                return true;
            else
                return false;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Validate if user is in the specified permission group
     * 
     * @param p_userId
     *            User's ID
     * @param p_group
     *            Name of permission groups. For example,
     *            Permission.GROUP_ADMINISTRATOR
     * @return
     * @throws RuntimeException
     */
    public static boolean isInPermissionGroup(String p_userId, String p_group)
            throws RuntimeException
    {
        if (isBlank(p_userId) || isBlank(p_group))
            return false;
        try
        {
            ArrayList pers = new ArrayList(Permission.getPermissionManager()
                    .getAllPermissionGroupNamesForUser(p_userId));
            return pers.contains(p_group);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Validate if user is in specified project
     * 
     * @param p_userId
     *            User's ID
     * @param p_projectId
     *            ID of project
     * @return 'true' - If user is in the specified project or he is set up to
     *         be in all projects
     * @throws RuntimeException
     */
    @SuppressWarnings("unchecked")
    public static boolean isInProject(String p_userId, String p_projectId)
            throws RuntimeException
    {
        if (isBlank(p_userId) || isBlank(p_projectId))
            return false;
        try
        {
            User user = getUserManager().getUser(p_userId);
            Project project = ServerProxy.getProjectHandler().getProjectById(
                    Long.parseLong(p_projectId));
            String userId = user.getUserId();
            String userCompanyId = CompanyWrapper.getCompanyIdByName(user
                    .getCompanyName());

            if (!String.valueOf(project.getCompanyId()).equals(userCompanyId))
            {
                // Current user is not in the company of project
                if (!isSuperAdmin(userId) && !isSuperLP(userId)
                        && !isSuperPM(userId))
                    return false;
            }

            Set users = new TreeSet(project.getUserIds());
            if (user.isInAllProjects() || users.contains(p_userId))
                return true;
            else
                return false;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static boolean isBlank(String target)
    {
        if (target == null || target.trim().length() == 0)
        {
            return true;
        }
        return false;
    }

    public static String getProperLocale(String locale)
    {
        int index = locale.indexOf("=");
        if (index < 0)
        {
            return locale;
        }
        return locale.substring(0, index);
    }

    /**
     * Deletes user id and name mapping from table USER_ID_USER_NAME.
     */
    public static void removeUserFromUserIdUserName(String userId)
    {
        Connection connection = null;
        PreparedStatement ps = null;
        try
        {
            connection = DbUtil.getConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(SQL_REMOVE_USER_ID_USER_NAME);
            ps.setString(1, userId);
            ps.execute();
            connection.commit();
        }
        catch (Exception e)
        {
            logger.error("Error when removeUserFromUserIdUserName()."
                    + e.getMessage());
        }
        finally
        {
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }
    }

    /**
     * Creates a new user id and name mapping in table USER_ID_USER_NAME.
     */
    public static String newUserId(String userName)
    {
        Connection connection = null;
        PreparedStatement querypPs = null;
        PreparedStatement insertPs = null;
        ResultSet rs = null;
        String userId = userName;
        try
        {
            connection = DbUtil.getConnection();
            connection.setAutoCommit(false);
            querypPs = connection
                    .prepareStatement(SQL_QUERY_USER_ID_USER_NAME_USER_NAME);
            querypPs.setString(1, userName);
            rs = querypPs.executeQuery();
            if (rs.next())
            {
                // found duplicate user id, then make a new unique user id with
                // random number
                userId = userName + (int) (Math.random() * 10000);
            }
            insertPs = connection
                    .prepareStatement(SQL_INSERT_USER_ID_USER_NAME);
            insertPs.setString(1, userId);
            insertPs.setString(2, userName);
            insertPs.execute();
            connection.commit();
        }
        catch (Exception e)
        {
            logger.error("Error when newUserId()." + e.getMessage());
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(insertPs);
            DbUtil.silentClose(querypPs);
            DbUtil.silentReturnConnection(connection);
        }

        return userId;
    }

    /**
     * Updates user name in table USER_ID_USER_NAME.
     */
    public static void updateUserIdUserName(String userId, String userName)
    {
        Connection connection = null;
        PreparedStatement ps = null;
        try
        {
            connection = DbUtil.getConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(SQL_UPDATE_USER_ID_USER_NAME);
            ps.setString(1, userName);
            ps.setString(2, userId);
            ps.execute();
            connection.commit();
        }
        catch (Exception e)
        {
            logger.error("Error when updateUserIdUserName().", e);
        }
        finally
        {
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }
        // update user id and name mapping
        String oldName = getUserNameById(userId);
        userNameIdMap.remove(oldName);
        userNameIdMap.remove(oldName.trim().toLowerCase());
        userNameIdMap.put(userName.trim().toLowerCase(), userId);
        userIdNameMap.put(userId.trim().toLowerCase(), userName);

    }

    /**
     * Gets user name by user id.
     */
    public static String getUserNameById(String p_userId)
    {
        if (p_userId == null)
        {
            return null;
        }
        String userId = p_userId.trim().toLowerCase();
        if (!userIdNameMap.containsKey(userId))
        {
            String userName = getUserNameByIdFromMapping(userId);
            if (userName == null)
            {
                return p_userId;
            }
            userIdNameMap.put(userId, userName);
        }

        return userIdNameMap.get(userId);
    }

    /**
     * Gets user id by user name.
     */
    public static String getUserIdByName(String p_userName)
    {
        if (p_userName == null)
        {
            return null;
        }
        String userName = p_userName.trim().toLowerCase();
        if (!userNameIdMap.containsKey(userName))
        {
            String userId = getUserIdByNameFromMapping(userName);
            if (userId != null)
            {
                userNameIdMap.put(userName, userId);
            }
        }

        return userNameIdMap.get(userName);
    }

    public static String getUserIdsByNames(String p_userNames)
    {
        if (WorkflowTask.DEFAULT_ROLE_NAME.equals(p_userNames))
        {
            return p_userNames;
        }
        StringBuilder userIds = new StringBuilder();
        StringTokenizer st = new StringTokenizer(p_userNames, ",");
        while (st.hasMoreElements())
        {
            String userName = st.nextToken();
            userIds.append(getUserIdByName(userName));
            userIds.append(",");
        }
        userIds.deleteCharAt(userIds.length() - 1);

        return userIds.toString();
    }

    public static String getUserNamesByIds(String p_userIds)
    {
        if (WorkflowTask.DEFAULT_ROLE_NAME.equals(p_userIds))
        {
            return p_userIds;
        }
        StringBuilder userNames = new StringBuilder();
        StringTokenizer st = new StringTokenizer(p_userIds, ",");
        while (st.hasMoreElements())
        {
            String userId = st.nextToken();
            userNames.append(getUserNameById(userId));
            userNames.append(",");
        }
        userNames.deleteCharAt(userNames.length() - 1);

        return userNames.toString();
    }

    public static String[] getAllUserNames()
    {
        try
        {
            return ServerProxy.getUserManager().getUserNamesFromAllCompanies();
        }
        catch (Exception e)
        {
            logger.error("Error when getAllUserNames().", e);
        }

        return null;
    }

    public static String[] convertUserNamesToUserIds(String[] p_userNames)
    {
        if (p_userNames == null)
        {
            return null;
        }

        String[] userIds = new String[p_userNames.length];

        for (int i = 0; i < p_userNames.length; i++)
        {
            userIds[i] = getUserIdByName(p_userNames[i]);
        }

        return userIds;
    }

    public static Object[] convertUserIdsToUserNames(Object[] p_userIds)
    {
        if (p_userIds == null)
        {
            return null;
        }

        Object[] userNames = new Object[p_userIds.length];

        for (int i = 0; i < p_userIds.length; i++)
        {
            userNames[i] = getUserNameById((String) p_userIds[i]);
        }

        return userNames;
    }

    public static String[] convertUserIdsToUserNamesInRoles(String[] p_roles)
    {
        String[] newRoles = new String[p_roles.length];
        for (int i = 0; i < p_roles.length; i++)
        {
            String role = p_roles[i];
            String[] items = role.split(" ");
            if (items.length > 4)
            {
                // indicate this is an user role
                StringBuilder newRole = new StringBuilder();
                newRole.append(items[0]);
                newRole.append(" ");
                newRole.append(items[1]);
                newRole.append(" ");
                newRole.append(items[2]);
                newRole.append(" ");
                newRole.append(items[3]);
                newRole.append(" ");
                newRole.append(getUserNameById(items[4]));
                newRoles[i] = newRole.toString();
            }
            else
            {
                newRoles[i] = role;
            }
        }
        return newRoles;
    }

    /**
     * Gets user name by user id from table USER_ID_USER_NAME.
     */
    private static String getUserNameByIdFromMapping(String p_userId)
    {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            connection = DbUtil.getConnection();
            ps = connection
                    .prepareStatement(SQL_QUERY_USER_ID_USER_NAME_USER_NAME);
            ps.setString(1, p_userId);
            rs = ps.executeQuery();
            if (rs.next())
            {
                return rs.getString(1);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getUserNameByIdFromMapping().", e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }

        return null;
    }

    /**
     * Gets user id by user name from table USER_ID_USER_NAME.
     */
    private static String getUserIdByNameFromMapping(String p_userName)
    {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            connection = DbUtil.getConnection();
            ps = connection
                    .prepareStatement(SQL_QUERY_USER_ID_USER_NAME_USER_ID);
            ps.setString(1, p_userName);
            rs = ps.executeQuery();
            if (rs.next())
            {
                return rs.getString(1);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getUserIdByNameFromMapping().", e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }

        return null;
    }
}
