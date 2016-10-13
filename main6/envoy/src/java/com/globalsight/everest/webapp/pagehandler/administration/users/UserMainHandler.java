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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.config.UserParameterImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.SSOUserUtil;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionManager;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.UserSecureFields;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.modules.Modules;

/*
 * Page handler for display list of Users.
 */
public class UserMainHandler extends PageHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(UserMainHandler.class);

    public static final String CREATE_USER_WRAPPER = "createUserWrapper";
    public static final String MODIFY_USER_WRAPPER = "modifyUserWrapper";
    public static final String ADD_ANOTHER = "addAnother";
    public static final String SEARCH_PARAMS = "searchParams";

    private static int NUM_PER_PAGE = 10;

    /**
     * Invokes this PageHandler.
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
            HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String action = request.getParameter("action");
        UserSearchParams params = (UserSearchParams) sessionMgr
                .getAttribute("fromSearch");

        if (action != null)
        {
            if (action.equals(USER_ACTION_CREATE_USER))
            {
                createUser(request);
                response.sendRedirect("/globalsight/ControlServlet?activityName=users");
                return;
            }
            else if (action.equals(USER_ACTION_MODIFY_USER))
            {
                modifyUser(request, false);
                response.sendRedirect("/globalsight/ControlServlet?activityName=users");
                return;
            }
            else if (action.equals(USER_ACTION_MODIFY2_USER))
            {
                modifyUser(request, true);
                response.sendRedirect("/globalsight/ControlServlet?activityName=users");
                return;

            }
            else if (action.equals("remove"))
            {
                removeUser(request);
            }
            else if (action.equals("search"))
            {
                // params = searchUsers(request);
                handleFilters(params, request, sessionMgr, action);
            }
            else if (action.equals(USER_ACTION_EXPORT))
            {
                exportUsers(request, response, sessionMgr);
                return;
            }
        }
        else
        {
            checkPreReqData(request, session);
        }

        try
        {
            PermissionSet perms = (PermissionSet) session
                    .getAttribute(WebAppConstants.PERMISSIONS);
            User thisUser = (User) sessionMgr
                    .getAttribute(WebAppConstants.USER);
            if (params == null
                    || (request.getParameter("linkName") != null && !request
                            .getParameter("linkName").startsWith("se")))
            {
                params = new UserSearchParams();
                sessionMgr.clear();
            }
            params.setPermissionSetOfSearcher(perms);
            params.setCompanyOfSearcher(thisUser.getCompanyName());

            dataForTable(request, request.getSession(), params);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /**
     * Perform create user action
     */
    private void createUser(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        // Get the session manager.
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        // Get the user wrapper off the session manager.
        CreateUserWrapper wrapper = (CreateUserWrapper) sessionMgr
                .getAttribute(CREATE_USER_WRAPPER);

        if (wrapper != null)
        {
            UserFluxCalendar cal;

            // Get the data from the last page (permissions page)
            UserUtil.extractPermissionData(p_request);
            try
            {
                if (Modules.isCalendaringInstalled())
                {
                    // Create the user's calendar
                    cal = (UserFluxCalendar) sessionMgr
                            .getAttribute(CalendarConstants.CALENDAR);
                }
                else
                {
                    // Create a user calendar based on the system calendar.
                    // FluxCalendar baseCal =
                    // CalendarHelper.getDefaultCalendar();
                    String companyId = CompanyWrapper
                            .getCompanyIdByName(wrapper.getCompanyName());
                    FluxCalendar baseCal = CalendarHelper
                            .getDefaultCalendar(companyId);
                    cal = new UserFluxCalendar(baseCal.getId(),
                            wrapper.getUserId(), baseCal.getTimeZoneId());
                    CalendarHelper.updateUserCalFieldsFromBase(baseCal, cal);
                }
            }
            catch (EnvoyServletException e)
            {
                // Don't create the user if calendar can't be created.
                throw e;
            }

            wrapper.setUserId(UserUtil.newUserId(wrapper.getUserName()));
            if (cal.getOwnerUserId() == null)
            {
                cal.setOwnerUserId(wrapper.getUserId());
            }
            wrapper.setCalendar(cal);

            // Now commit the wrapper
            wrapper.commitWrapper();

            // Add permissions groups is necessary
            addPermissionGroups(wrapper, sessionMgr);

            // save sso user mapping
            updateSSOUserMapping(wrapper);
        }
        clearSessionExceptTableInfo(session, UserConstants.USER_KEY);
    }

    /**
     * Perform modify user action.
     * 
     * @param getUserData
     *            - true if the user hit save from the first page. Need to get
     *            the data from the request.
     */
    private void modifyUser(HttpServletRequest p_request, boolean getUserData)
            throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        // Get the user wrapper off the session manager.
        ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
                .getAttribute(MODIFY_USER_WRAPPER);

        if (getUserData)
        {
            UserUtil.extractUserData(p_request, wrapper, false);
        }

        UserUtil.updateUserIdUserName(wrapper.getUserId(),
                wrapper.getUserName());
        // Commit the wrapper
        wrapper.commitWrapper(session);

        // Check for changes in Permissiong Groups
        updatePermissionGroups(wrapper, sessionMgr);

        // save sso user mapping
        updateSSOUserMapping(wrapper);

        clearSessionExceptTableInfo(session, UserConstants.USER_KEY);

        // If modify the current user, also need reset the session.
        String currentUserID = ((User) sessionMgr
                .getAttribute(WebAppConstants.USER)).getUserId();
        if (currentUserID != null
                && currentUserID.equalsIgnoreCase(wrapper.getUserId()))
        {
            try
            {
                User user = ServerProxy.getUserManager().getUser(currentUserID);
                sessionMgr.setAttribute(WebAppConstants.USER, user);
            }
            catch (Exception e)
            {
            }
        }
    }

    private void exportUsers(HttpServletRequest request,
            HttpServletResponse response, SessionManager sessionMgr)
    {
        FileOutputStream outStream = null;
        File exportedXmlFile = null;
        try
        {
            Element root = new Element("UserInfo");
            Document Doc = new Document(root);

            String[] userIds = request.getParameterValues("radioBtn");
            for (String userId : userIds)
            {
                User user = ServerProxy.getUserManager().getUser(userId);
                Element userNode = new Element("User");
                // ==========================basic info=======================
                Element basicInfoNode = new Element("BasicInfo");
                basicInfoNode.addContent(new Element("UserID").setText(user
                        .getUserId()));
                basicInfoNode.addContent(new Element("UserName").setText(user
                        .getUserName()));
                basicInfoNode.addContent(new Element("FirstName").setText(user
                        .getFirstName()));
                basicInfoNode.addContent(new Element("LastName").setText(user
                        .getLastName()));
                basicInfoNode.addContent(new Element("Password").setText(user
                        .getPassword()));
                basicInfoNode.addContent(new Element("Title").setText(user
                        .getTitle() == null ? "" : user.getTitle()));
                basicInfoNode.addContent(new Element("CompanyName")
                        .setText(user.getCompanyName()));
                userNode.addContent(basicInfoNode);
                // ======================== contact info =====================
                Element contactInfoNode = new Element("ContactInfo");
                contactInfoNode.addContent(new Element("Address").setText(user
                        .getAddress() == null ? "" : user.getAddress()));
				contactInfoNode.addContent(new Element("HomePhone")
						.setText(user.getHomePhoneNumber() == null ? "" : user
								.getHomePhoneNumber()));
				contactInfoNode.addContent(new Element("WorkPhone")
						.setText(user.getOfficePhoneNumber() == null ? ""
								: user.getOfficePhoneNumber()));
				contactInfoNode.addContent(new Element("CellPhone")
						.setText(user.getCellPhoneNumber() == null ? "" : user
								.getCellPhoneNumber()));
				contactInfoNode.addContent(new Element("Fax").setText(user
						.getFaxPhoneNumber() == null ? "" : user
						.getFaxPhoneNumber()));
                contactInfoNode
                        .addContent(new Element("EmailAddress").setText(user
                                .getEmail() == null ? "" : user.getEmail()));
                contactInfoNode.addContent(new Element("CCEmailAddress")
                        .setText(user.getCCEmail() == null ? "" : user
                                .getCCEmail()));
                contactInfoNode.addContent(new Element("BCCEmailAddress")
                        .setText(user.getBCCEmail() == null ? "" : user
                                .getBCCEmail()));
                contactInfoNode.addContent(new Element("EmailLanguage")
                        .setText(user.getDefaultUILocale()));
                userNode.addContent(contactInfoNode);
                // ======================= default roles
                // ==========================
                List<UserDefaultRole> defaultRoles = SetDefaultRoleUtil
                        .getDefaultRolesByUser(userId);
                if (defaultRoles != null && defaultRoles.size() > 0)
                {
                    HashMap<Long, String> allLocalesMap =  getAllLocales();
                    Element defaultRolesNode = new Element("DefaultRoles");
                    for (UserDefaultRole userDefaultRole : defaultRoles)
                    {
                        Element defaultRoleNode = new Element("DefaultRole");
                        defaultRoleNode.addContent(new Element("SourceLocale")
                                .setText(allLocalesMap.get(userDefaultRole
                                        .getSourceLocaleId())));
                        defaultRoleNode.addContent(new Element("TargetLocale")
                                .setText(allLocalesMap.get(userDefaultRole
                                        .getTargetLocaleId())));

                        Set activitys = userDefaultRole.getActivities();
                        for (Iterator iterator = activitys.iterator(); iterator
                                .hasNext();)
                        {
                            UserDefaultActivity defaultActivity = (UserDefaultActivity) iterator
                                    .next();
                            defaultRoleNode.addContent(new Element(
                                    "ActivityName").setText(defaultActivity
                                    .getActivityName()));
                        }
                        defaultRolesNode.addContent(defaultRoleNode);
                    }
                    userNode.addContent(defaultRolesNode);
                }
                // ======================= roles ==========================
                Element rolesNode = new Element("Roles");
                Collection userRoles = ServerProxy.getUserManager()
                        .getUserRoles(user);
                if (userRoles != null)
                {
                    for (Iterator it = userRoles.iterator(); it.hasNext();)
                    {
                        UserRoleImpl userRole = (UserRoleImpl) it.next();
                        Element activityNode = new Element("Activity");
                        Activity activity = userRole.getActivity();
                        activityNode.addContent(new Element("CompanyName")
                                .setText(CompanyWrapper
                                        .getCompanyNameById(activity
                                                .getCompanyId())));
                        activityNode.addContent(new Element("SourceLocale")
                                .setText(userRole.getSourceLocale()));
                        activityNode.addContent(new Element("TargetLocale")
                                .setText(userRole.getTargetLocale()));
                        String activityName = activity.getActivityName();
                        activityNode.addContent(new Element("ActivityName")
                                .setText(activityName.substring(0,
                                        activityName.lastIndexOf("_"))));
                        activityNode.addContent(new Element("Rate")
                                .setText(userRole.getRate()));
                        rolesNode.addContent(activityNode);
                    }
                    userNode.addContent(rolesNode);
                }
                // ========================== projects
                // ============================
                Element projectsNode = new Element("Projects");
                try
                {
                    projectsNode.addContent(new Element("IsInAllProjects")
                            .setText(String.valueOf(user.isInAllProjects())));
                    List<?> projects = ServerProxy.getProjectHandler()
                            .getProjectsByUser(userId);
                    for (int i = 0; i < projects.size(); i++)
                    {
                        Project project = (Project) projects.get(i);
                        Element projectNode = new Element("Project");
                        projectNode.addContent(new Element("ProjectId")
                                .setText(String.valueOf(project.getId())));
                        projectNode.addContent(new Element("ProjectName")
                                .setText(project.getName()));
                        projectNode
                                .addContent(new Element("ProjectCompanyName")
                                        .setText(CompanyWrapper
                                                .getCompanyNameById(project
                                                        .getCompanyId())));
                        projectsNode.addContent(projectNode);
                    }
                }
                catch (NamingException e)
                {
                    throw new EnvoyServletException(
                            EnvoyServletException.EX_GENERAL, e);
                }
                userNode.addContent(projectsNode);
                // ========================= security ==========================
                Element securityNode = new Element("Security");
                User operator = (User) sessionMgr
                        .getAttribute(WebAppConstants.USER);
                FieldSecurity fs = UserHandlerHelper.getSecurity(user,
                        operator, false);
                securityNode.addContent(new Element("AccessLevel").setText(fs
                        .get(UserSecureFields.ACCESS_GROUPS)));
                securityNode.addContent(new Element("Address").setText(fs
                        .get(UserSecureFields.ADDRESS)));
                securityNode.addContent(new Element("Security").setText(fs
                        .get(UserSecureFields.SECURITY)));
                securityNode.addContent(new Element("Calendar").setText(fs
                        .get(UserSecureFields.CALENDAR)));
                securityNode.addContent(new Element("CellPhone").setText(fs
                        .get(UserSecureFields.CELL_PHONE)));
                securityNode.addContent(new Element("CompanyName").setText(fs
                        .get(UserSecureFields.COMPANY)));
                securityNode.addContent(new Element("Country").setText(fs
                        .get(UserSecureFields.COUNTRY)));
                securityNode.addContent(new Element("EmailAddress").setText(fs
                        .get(UserSecureFields.EMAIL_ADDRESS)));
                securityNode.addContent(new Element("CCEmailAddress")
                        .setText(fs.get(UserSecureFields.CC_EMAIL_ADDRESS)));
                securityNode.addContent(new Element("BCCEmailAddress")
                        .setText(fs.get(UserSecureFields.BCC_EMAIL_ADDRESS)));
                securityNode.addContent(new Element("EmailLanguage").setText(fs
                        .get(UserSecureFields.EMAIL_LANGUAGE)));
                securityNode.addContent(new Element("Fax").setText(fs
                        .get(UserSecureFields.FAX)));
                securityNode.addContent(new Element("FirstName").setText(fs
                        .get(UserSecureFields.FIRST_NAME)));
                securityNode.addContent(new Element("HomePhone").setText(fs
                        .get(UserSecureFields.HOME_PHONE)));
                securityNode.addContent(new Element("LastName").setText(fs
                        .get(UserSecureFields.LAST_NAME)));
                securityNode.addContent(new Element("Password").setText(fs
                        .get(UserSecureFields.PASSWORD)));
                securityNode.addContent(new Element("Projects").setText(fs
                        .get(UserSecureFields.PROJECTS)));
                securityNode.addContent(new Element("Roles").setText(fs
                        .get(UserSecureFields.ROLES)));
                securityNode.addContent(new Element("Status").setText(fs
                        .get(UserSecureFields.STATUS)));
                securityNode.addContent(new Element("Title").setText(fs
                        .get(UserSecureFields.TITLE)));
                securityNode.addContent(new Element("WorkPhone").setText(fs
                        .get(UserSecureFields.WORK_PHONE)));
                userNode.addContent(securityNode);
                // ======================= permission =========================
                Element permissionGroupsNode = new Element("PermissionGroups");
                Collection permList = PermissionHelper
                        .getAllPermissionGroupsForUser(userId);
                for (Iterator iterator = permList.iterator(); iterator
                        .hasNext();)
                {
                    Element permissionGroupNode = new Element("PermissionGroup");
                    PermissionGroup pg = (PermissionGroup) iterator.next();
                    permissionGroupNode.addContent(new Element("CompanyName")
                            .setText(CompanyWrapper.getCompanyNameById(pg
                                    .getCompanyId())));
                    permissionGroupNode.addContent(new Element(
                            "PermissionGroupId").setText(String.valueOf(pg
                            .getId())));
                    permissionGroupNode.addContent(new Element(
                            "PermissionGroupName").setText(pg.getName()));
                    permissionGroupsNode.addContent(permissionGroupNode);
                }
                userNode.addContent(permissionGroupsNode);
                // ======================= user parameter
                // =========================
                Element userParametersNode = new Element("UserParameters");
                Collection userConfig = ServerProxy.getUserParameterManager()
                        .getUserParameters(userId);
                for (Iterator it = userConfig.iterator(); it.hasNext();)
                {
                    UserParameterImpl up = (UserParameterImpl) it.next();

                    Element userParameterNode = new Element("UserParameter");
                    userParameterNode.addContent(new Element("Name").setText(up
                            .getName()));
                    userParameterNode.addContent(new Element("Value")
                            .setText(up.getValue()));
                    userParametersNode.addContent(userParameterNode);
                }
                userNode.addContent(userParametersNode);

                // add to root
                root.addContent(userNode);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String fileName = "User_information_" + sdf.format(new Date())
                    + ".xml";
            XMLOutputter XMLOut = new XMLOutputter();
            String filePath = AmbFileStoragePathUtils.getFileStorageDirPath()
                    + File.separator + "tmp";
            exportedXmlFile = new File(filePath, fileName);
            exportedXmlFile.getParentFile().mkdirs();
            outStream = new FileOutputStream(exportedXmlFile);
            XMLOut.output(Doc, outStream);

            ExportUtil.writeToResponse(response, exportedXmlFile, fileName);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }
        finally
        {
            try
            {
                if (outStream != null)
                    outStream.close();
            }
            catch (IOException e)
            {

            }

            FileUtil.deleteFile(exportedXmlFile);
        }
    }

    /**
     * Get a locale ID to LANG_COUNTRY code map for performance purpose.
     */
    private HashMap<Long, String> getAllLocales() throws Exception
    {
        HashMap<Long, String> result = new HashMap<Long, String>();
        Vector allLocales = ServerProxy.getLocaleManager()
                .getAvailableLocales();
        for (Iterator it = allLocales.iterator(); it.hasNext();)
        {
            GlobalSightLocale gsl = (GlobalSightLocale) it.next();
            result.put(gsl.getIdAsLong(), gsl.toString());
        }
        return result;
    }

    /**
     * For sso user mapping
     * 
     * @param wrapper
     */
    private void updateSSOUserMapping(CreateUserWrapper wrapper)
    {
        String ssoUserId = wrapper.getSsoUserId();

        if (ssoUserId != null)
        {
            String companyName = wrapper.getCompanyName();
            long companyId = CompanyWrapper.getCompanyByName(companyName)
                    .getId();
            String userId = wrapper.getUserId();
            SSOUserUtil.saveUserMapping(companyId, userId, ssoUserId);
        }
    }

    /**
     * Remove a user.
     */
    private void removeUser(HttpServletRequest request)
            throws EnvoyServletException
    {
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        User loggedInUser = (User) sessionMgr
                .getAttribute(WebAppConstants.USER);

        String[] userIds = request.getParameterValues("radioBtn");
        if (userIds == null || request.getMethod().equalsIgnoreCase("get"))
        {
            return;
        }

        for (String userId : userIds)
        {
            if (loggedInUser.getUserId().equals(userId))
            {
                CATEGORY.warn(loggedInUser.getUserName()
                        + " is trying to delete himself, which is not allowed in the system");
                continue;
            }
            String deps = UserHandlerHelper.checkForDependencies(userId,
                    session);
            if (deps == null)
            {
                // removes the user
                UserHandlerHelper.removeUser(loggedInUser, userId);
                SetDefaultRoleUtil.removeDefaultRoles(userId);
                ProjectTMTBUsers ptu = new ProjectTMTBUsers();
                ptu.deleteAllTMTB(userId);
                try
                {
                    Collection<?> userParameters = ServerProxy
                            .getUserParameterManager()
                            .getUserParameters(userId);
                    HibernateUtil.delete(userParameters);
                }
                catch (Exception e)
                {
                    CATEGORY.error("Failed to remove user parameters.", e);
                }
                
                try 
                {
        			String hql = "from UserRoleImpl a where a.user=:userId";
        			HashMap map = new HashMap();
        			map.put("userId", userId);
        			List roles = HibernateUtil.search(hql, map);
        			HibernateUtil.delete(roles);
        			roles = new ArrayList(HibernateUtil.search(hql, map));
        		} 
                catch (Exception e) 
                {
        			CATEGORY.error(e.getMessage(), e);
        		}
            }
            else
            {
                CATEGORY.warn("Cannot delete user " + userId
                        + " because of the following dependencies:\r\n" + deps);
                request.setAttribute(UserConstants.DEPENDENCIES, deps);
            }
        }
    }

    /**
     * Search for users with certain criteria.
     */
    private UserSearchParams searchUsers(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        String buf = p_request.getParameter("nameTypeOptions");
        UserSearchParams params = new UserSearchParams();
        params.setNameType(Integer.parseInt(buf));
        buf = p_request.getParameter("nameOptions");
        params.setNameFilter(Integer.parseInt(buf));
        params.setNameParam(p_request.getParameter("nameField"));
        params.setSourceLocaleParam(p_request.getParameter("srcLocale"));
        params.setTargetLocaleParam(p_request.getParameter("targLocale"));
        params.setPermissionGroupParam(p_request
                .getParameter("permissionGroup"));
        return params;
    }

    /**
     * Before being able to create a User, certain objects must exist. Check
     * that here.
     */
    private void checkPreReqData(HttpServletRequest p_request,
            HttpSession p_session) throws EnvoyServletException
    {
        String userId = (String) p_session
                .getAttribute(WebAppConstants.USER_NAME);
        boolean isSuperAdmin = false;
        try
        {
            isSuperAdmin = UserUtil.isSuperAdmin(userId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        if (isSuperAdmin)
        {
            return;
        }

        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        Vector allSourceLocales = UserHandlerHelper.getAllSourceLocales();
        Vector allActivities = UserHandlerHelper.getAllActivities(uiLocale);

        if (allActivities == null || allActivities.size() < 1
                || allSourceLocales == null || allSourceLocales.size() < 1)
        {
            ResourceBundle bundle = getBundle(p_session);
            StringBuffer message = new StringBuffer();
            boolean addcomma = false;
            message.append(bundle.getString("msg_prereq_warning_1"));
            message.append(":  ");
            if (allActivities == null || allActivities.size() < 1)
            {
                message.append(bundle.getString("lb_activity_types"));
                addcomma = true;
            }
            if (allSourceLocales == null || allSourceLocales.size() < 1)
            {
                if (addcomma)
                    message.append(", ");
                message.append(bundle.getString("lb_locale_pairs"));
            }
            message.append(".  ");
            message.append(bundle.getString("msg_prereq_warning_2"));

            p_request.setAttribute("preReqData", message.toString());
        }
    }

    /**
     * Get list of all users, sorted appropriately
     */
    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session, UserSearchParams params)
            throws RemoteException, NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);
        StringBuffer condition = new StringBuffer();
        String[][] array = new String[][] {{"uNameFilter","u.userName"},
        { "ufNameFilter", "u.firstName" },
        { "ulNameFilter", "u.lastName" },
        { "uEmailFilter", "u.email" } };
               
        for (int i = 0; i < array.length; i++)
        {
            makeCondition(sessionMgr, condition, array[i][0], array[i][1]);
        }
        
        Vector users = ServerProxy.getUserManager().getUsers(
                condition.toString());

        filtrateSuperAdmin(p_session, params, users);

        filtrateUsers(users, sessionMgr);
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        String numOfPerPage = p_request.getParameter("numOfPageSize");
        if (StringUtil.isNotEmpty(numOfPerPage))
        {
            try
            {
                NUM_PER_PAGE = Integer.parseInt(numOfPerPage);
            }
            catch (Exception e)
            {
                NUM_PER_PAGE = Integer.MAX_VALUE;
            }
        }

        setTableNavigation(p_request, p_session, users, new UserComparator(
                uiLocale, getBundle(p_session)), NUM_PER_PAGE,
                UserConstants.USER_LIST, UserConstants.USER_KEY);
        User loggedInUser = (User) sessionMgr
                .getAttribute(WebAppConstants.USER);

        // for GBS-1155.
        if (!CompanyThreadLocal.getInstance().fromSuperCompany())
        {
            p_request.setAttribute("securities",
                    UserHandlerHelper.getSecurities(users, loggedInUser));
        }

        sessionMgr.setAttribute("fromSearch", params);
    }

    private void filtrateSuperAdmin(HttpSession p_session,
            UserSearchParams params, Vector users) throws RemoteException,
            NamingException
    {
        String userId = (String) p_session
                .getAttribute(WebAppConstants.USER_NAME);
        boolean isSuperAdmin = false;
        boolean isSuperPM = false;
        try
        {
            isSuperAdmin = UserUtil.isSuperAdmin(userId);
            if (!isSuperAdmin)
            {
                isSuperPM = UserUtil.isSuperPM(userId);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        if (!isSuperAdmin)
        {
            String companyName = null;
            if (isSuperPM)
            {
                companyName = CompanyWrapper
                        .getCompanyNameById(CompanyThreadLocal.getInstance()
                                .getValue());
            }
            else
            {
                companyName = params.getCompanyOfSearcher();
            }

            for (Iterator iter = users.iterator(); iter.hasNext();)
            {
                User user = (User) iter.next();
                if (!companyName.equals(user.getCompanyName()))
                {
                    iter.remove();
                }
            }
        }
    }

    private void makeCondition(SessionManager sessionMgr,
            StringBuffer condition, String par, String sqlparam)
    {
        String uNameFilter = (String) sessionMgr.getAttribute(par);
        if (StringUtils.isNotBlank(uNameFilter))
        {
            condition.append(" and  " + sqlparam + " LIKE '%"
                    + StringUtil.transactSQLInjection(uNameFilter.trim())
                    + "%'");
        }
    }

    private void filtrateUsers(Vector users, SessionManager sessionMgr)
    {
        String uProjectFilter = (String) sessionMgr
                .getAttribute("uProjectFilter");
        String uPermissionFilter = (String) sessionMgr
                .getAttribute("uPermissionFilter");
        String uCompanyFilter = (String) sessionMgr
                .getAttribute("uCompanyFilter");
        HashMap<String, String> ProjectNameMap = UserHandlerHelper
                .getAllPerAndProNameForUser(UserHandlerHelper.PROJECT);
        HashMap<String, String> PermissionGroupNamesMap = UserHandlerHelper
                .getAllPerAndProNameForUser(UserHandlerHelper.PERMISSIONGROUP);
        for (Iterator iter = users.iterator(); iter.hasNext();)
        {
            User user = (User) iter.next();
            String pName = user.getCompanyName();
            if (matchCondition(uCompanyFilter, pName))
            {
                iter.remove();
                continue;
            }

            pName = ProjectNameMap.get(user.getUserId());

            if (matchCondition(uProjectFilter, pName))
            {
                iter.remove();
                continue;
            }
            user.setProjectNames(pName);
            pName = PermissionGroupNamesMap.get(user.getUserId());

            if (matchCondition(uPermissionFilter, pName))
            {
                iter.remove();
                continue;
            }

            user.setPermissiongNames(pName);
        }
    }

    private boolean matchCondition(String uCompanyFilter, String pName)
    {
        return StringUtils.isNotEmpty(uCompanyFilter)
                && !StringUtils
                        .containsIgnoreCase(pName, uCompanyFilter.trim());
    }

    /**
     * If there have been changes to the Permission Groups for a user, do the
     * update.
     */
    private void updatePermissionGroups(ModifyUserWrapper p_wrapper,
            SessionManager p_sessionMgr) throws EnvoyServletException
    {
        ArrayList changed = (ArrayList) p_sessionMgr.getAttribute("userPerms");
        if (changed == null)
            return;
        ArrayList existing = (ArrayList) PermissionHelper
                .getAllPermissionGroupsForUser(p_wrapper.getUserId());
        if (existing == null && changed.size() == 0)
            return;

        User user = p_wrapper.getUser();
        ArrayList list = new ArrayList(1);
        list.add(user.getUserId());
        try
        {
            PermissionManager manager = Permission.getPermissionManager();
            if (existing == null)
            {
                // just adding new perm groups
                for (int i = 0; i < changed.size(); i++)
                {
                    PermissionGroup pg = (PermissionGroup) changed.get(i);
                    manager.mapUsersToPermissionGroup(list, pg);
                }
            }
            else
            {
                // need to determine what to add and what to remove.
                // Loop thru old list and see if perm is in new list. If not,
                // remove it.
                for (int i = 0; i < existing.size(); i++)
                {
                    PermissionGroup pg = (PermissionGroup) existing.get(i);
                    boolean found = false;
                    for (int j = 0; j < changed.size(); j++)
                    {
                        PermissionGroup cpg = (PermissionGroup) changed.get(j);
                        if (pg.getId() == cpg.getId())
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        manager.unMapUsersFromPermissionGroup(list, pg);
                }

                // Loop thru new list and see if perm is in old list. If not,
                // add it.
                for (int i = 0; i < changed.size(); i++)
                {
                    boolean found = false;
                    PermissionGroup pg = (PermissionGroup) changed.get(i);
                    for (int j = 0; j < existing.size(); j++)
                    {
                        PermissionGroup cpg = (PermissionGroup) existing.get(j);
                        if (pg.getId() == cpg.getId())
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        manager.mapUsersToPermissionGroup(list, pg);
                }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Add Permission Groups to new user.
     */
    private void addPermissionGroups(CreateUserWrapper p_wrapper,
            SessionManager p_sessionMgr) throws EnvoyServletException
    {
        ArrayList userPerms = (ArrayList) p_sessionMgr
                .getAttribute("userPerms");
        if (userPerms == null && userPerms.size() == 0)
            return;
        User user = p_wrapper.getUser();
        ArrayList list = new ArrayList(1);
        list.add(user.getUserId());
        try
        {
            PermissionManager manager = Permission.getPermissionManager();
            for (int i = 0; i < userPerms.size(); i++)
            {
                PermissionGroup pg = (PermissionGroup) userPerms.get(i);
                manager.mapUsersToPermissionGroup(list, pg);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private void handleFilters(UserSearchParams params,
            HttpServletRequest p_request, SessionManager sessionMgr,
            String action)
    {
        String uNameFilter = (String) p_request.getParameter("uNameFilter");
        String ufNameFilter = (String) p_request.getParameter("ufNameFilter");
        String ulNameFilter = (String) p_request.getParameter("ulNameFilter");
        String uEmailFilter = (String) p_request.getParameter("uEmailFilter");
        String uCompanyFilter = (String) p_request
                .getParameter("uCompanyFilter");
        String uProjectFilter = (String) p_request
                .getParameter("uProjectFilter");
        String uPermissionFilter = (String) p_request
                .getParameter("uPermissionFilter");
        if (!"search".equals(action)
                || p_request.getMethod().equalsIgnoreCase(
                        WebAppConstants.REQUEST_METHOD_GET))
        {
            uNameFilter = (String) sessionMgr.getAttribute("uNameFilter");
            ufNameFilter = (String) sessionMgr.getAttribute("ufNameFilter");
            ulNameFilter = (String) sessionMgr.getAttribute("ulNameFilter");
            uEmailFilter = (String) sessionMgr.getAttribute("uEmailFilter");
            uCompanyFilter = (String) sessionMgr.getAttribute("uCompanyFilter");
            uProjectFilter = (String) sessionMgr.getAttribute("uProjectFilter");
            uPermissionFilter = (String) sessionMgr
                    .getAttribute("uPermissionFilter");
        }
        // sessionMgr.setAttribute("tmNameFilter", name == null ? "" : name);
        // sessionMgr.setAttribute("tmCompanyFilter", company == null ? "" :
        // company);
        sessionMgr.setAttribute("uNameFilter", uNameFilter == null ? ""
                : uNameFilter);
        params.setIdName(uNameFilter);
        sessionMgr.setAttribute("ufNameFilter", ufNameFilter == null ? ""
                : ufNameFilter);
        params.setFirstName(ufNameFilter);
        sessionMgr.setAttribute("ulNameFilter", ulNameFilter == null ? ""
                : ulNameFilter);
        params.setLastName(ulNameFilter);
        sessionMgr.setAttribute("uEmailFilter", uEmailFilter == null ? ""
                : uEmailFilter);
        params.setEmail(uEmailFilter);
        sessionMgr.setAttribute("uCompanyFilter", uCompanyFilter == null ? ""
                : uCompanyFilter);
        // params.setCompany((uCompanyFilter));
        sessionMgr.setAttribute("uProjectFilter", uProjectFilter == null ? ""
                : uProjectFilter);
        sessionMgr.setAttribute("uPermissionFilter",
                uPermissionFilter == null ? "" : uPermissionFilter);
    }

}
