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
package com.globalsight.everest.webapp.pagehandler.edit.inctxrv;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.securitymgr.SecurityManagerException;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.AppletDirectory;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.usermgr.LoggedUser;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.usermgr.UserLdapHelper;
import com.globalsight.everest.util.netegrity.Netegrity;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.login.EntryPageControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.ling.common.URLDecoder;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.log.ActivityLog;
import com.globalsight.mediasurface.CmsUserInfo;
import com.globalsight.util.Base64;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.modules.Modules;
import com.globalsight.webservices.AmbassadorUtil;

/**
 * Webservice API "getInContextReviewLink(..)" return a link, client can open
 * this link to open in context review window. User need not login, but the
 * logging in is required and executed background(in this class).
 * 
 * @author YorkJin
 * @version 8.6
 */
public class InContextReviewHelper implements WebAppConstants
{
    private static final Logger logger = Logger
            .getLogger(InContextReviewHelper.class.getName());

    /**
     * Login the user.
     * @throws Exception 
     */
    // From "EntryPageControlFlowHelper.java".
    public static String loginUser(HttpServletRequest m_request,
            HttpServletResponse p_response)
    {
        String secret = m_request.getParameter("secret");
        try
        {
            secret = AmbassadorUtil.getDecryptionString(secret);
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        HashMap paramsInSecret = parseSecret(secret);

        String userName = (String) paramsInSecret.get(WebAppConstants.LOGIN_NAME_FIELD);
        if (userName != null) {
            userName = EditUtil.utf8ToUnicode(userName).trim();
        }
        String userId = UserUtil.getUserIdByName(userName);
        if (userId == null) {
            return loginFailed(null, m_request);
        }

        User user = UserUtil.getUserById(userId);
        String encyptPassword = user.getPassword();

        // create a session
        HttpSession session = m_request.getSession(true);

        // this is only true when sharing the same JVM from two
        // browsers or when a session has been timed out but not
        // invalidated (a WebServer bug?)
        if (session.getAttribute(WebAppConstants.SESSION_MANAGER) != null)
        {
            session.removeAttribute(WebAppConstants.SESSION_MANAGER);
        }

        // Get the login locale and use it as the UI locale (put it in session)
        String loginLocale = m_request.getParameter(WebAppConstants.UILOCALE);

        try
        {
            // do authentication...
            user = performLogin(userId, encyptPassword, session.getId(), m_request,
                    p_response);

            session.setAttribute(WebAppConstants.USER_NAME, userId);
            session.setAttribute(UserLdapHelper.LDAP_ATTR_COMPANY,
                    user.getCompanyName());

            CompanyThreadLocal.getInstance().setValue(user.getCompanyName());
            boolean isSuperAdmin = UserUtil.isSuperAdmin(userId);
            session.setAttribute(WebAppConstants.IS_SUPER_ADMIN,
                    Boolean.valueOf(isSuperAdmin));
        }
        catch (EnvoyServletException ese)
        {
            return loginFailed(ese, m_request);
        }

        // Load the user parameters and store them in the session.
        HashMap params = loadUserParameters(userId);

        Company c = CompanyWrapper.getCompanyByName(user.getCompanyName());
        if (!StringUtil.isEmpty(c.getSessionTime())
                && c.getSessionTime().matches("\\d*"))
        {
            session.setMaxInactiveInterval(60 * Integer.parseInt(c
                    .getSessionTime()));
        }

        session.setAttribute(WebAppConstants.USER_PARAMS, params);

        // Get the applet directory and store the session in it (uid
        // generated in directory).
        AppletDirectory ad = AppletDirectory.getInstance();
        String uid = ad.setSession(session);
        session.setAttribute(WebAppConstants.UID + session.getId(), uid);

        // Create a SessionManager instance and put it in the session.
        SessionManager sessionManager = new SessionManager(uid, session);
        session.setAttribute(WebAppConstants.SESSION_MANAGER, sessionManager);

        // query out all the permissiongroups that the user is in
        PermissionSet perms = new PermissionSet();
        try
        {
            perms = Permission.getPermissionManager().getPermissionSetForUser(userId);
        }
        catch (Exception e)
        {
            logger.error("Failed to get permissions for user " + user.getUserName(), e);
            throw new EnvoyServletException(e);
        }
        session.setAttribute(WebAppConstants.PERMISSIONS, perms);

        // put the User object in the session manager
        sessionManager.setAttribute(WebAppConstants.USER, user);

        // set the UI locale (in the session, not a managed object)
        // If user default language is the same as the login language, use
        // use user default UI locale
        Locale uiLocale = null;
        if (loginLocale == null
                || PageHandler
                        .getUILocale(loginLocale)
                        .getDisplayLanguage()
                        .equals(PageHandler.getUILocale(
                                user.getDefaultUILocale()).getDisplayLanguage()))
        {
            // If user default language is the same as the login language, use
            // use user default UI locale
            uiLocale = PageHandler.getUILocale(user.getDefaultUILocale());
        }
        else
        {
            // use login locale
            uiLocale = PageHandler.getUILocale(loginLocale);
        }

        session.setAttribute(WebAppConstants.UILOCALE, uiLocale);
        loadAdditionalUserInfo(session, userId);

        // set the most recently used job id's for this user in the session
        loadJobIds(session, userId, JobSearchConstants.MRU_JOBS_COOKIE,
                JobSearchConstants.MRU_JOBS, m_request);
        loadJobIds(session, userId, JobSearchConstants.MRU_TASKS_COOKIE,
                JobSearchConstants.MRU_TASKS, m_request);

        // Get user's login protocol and port and put these info into session
        int loginPort = m_request.getServerPort();
        String loginServer = m_request.getServerName();

        String protocol = PROTOCOL_HTTPS;
        if (loginPort == HTTP_PORT)
        {
            protocol = PROTOCOL_HTTP;
        }

        // These two fields are supposed to be permanent, so not put
        // in SessionManager, but directly in session
        session.setAttribute(LOGIN_PORT, new Integer(loginPort));
        session.setAttribute(LOGIN_PROTOCOL, protocol);
        session.setAttribute(LOGIN_SERVER, loginServer);

        // Adds auto login cookie.
        addAutoLoginCookie(userId, encyptPassword, p_response);
        
        // store current logged user info
        try
        {
            UserInfo userInfo = ServerProxy.getUserManager().getUserInfo(
                    user.getUserId());
            LoggedUser.getInstance().setLoggedUserInfo(userInfo);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        
        return WebAppConstants.LOGIN_PASS;
    }

    /**
     * User login process: 1. SecurityManager perform authentication and return
     * the User object 2. Login to IFlow
     */
    private static User performLogin(String p_userId, String p_password,
            String p_sessionId, HttpServletRequest m_request,
            HttpServletResponse p_response) throws EnvoyServletException
    {
        User user = null;
        try
        {
            // first do authentication
            user = ServerProxy.getSecurityManager().authenticateUser(p_userId,
                    p_password);
        }
        catch (GeneralException ge)
        {
            String[] msgArgs =
            { "Failed to authenticate user!" };

            logger.warn("Invalid login attempt for user '"
                    + UserUtil.getUserNameById(p_userId)
                    + "' possibly due to the wrong password.");
            throw new EnvoyServletException(
                    EnvoyServletException.MSG_FAILED_TO_LOGIN, msgArgs, ge);
        }
        catch (RemoteException e)
        {
            logger.warn(e.getMessage(), e);

            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }
        Map<Object, Object> activityArgs = new HashMap<Object, Object>();
        activityArgs.put("userIP", getIpAddr(m_request));
        activityArgs.put("user", user.getUserName());
        ActivityLog.Start activityStart = ActivityLog.start(
                EntryPageControlFlowHelper.class, "_doPost", activityArgs);
        try
        {
            // calculate the users that logged in the system.
            ServerProxy.getUserManager().loggedInUsers(user.getUserId(),
                    p_sessionId);
        }
        catch (Exception e)
        {
            logger.debug(e);

            String[] msgArgs =
            { "Failed to calculate the logged users." };
            throw new EnvoyServletException(
                    EnvoyServletException.MSG_FAILED_TO_LOGIN, msgArgs, e);
        }
        finally
        {
            activityStart.end();
        }
        return user;
    }

    /**
     * Return the login failed link. Also make sure to set the header attribute
     * to "fail" to distingush the link from JSP text to be displayed.
     */
    private static String loginFailed(EnvoyServletException ese,
            HttpServletRequest m_request)
    {
        String failureString = m_request.getMethod().equals("POST") ? "generalFail"
                : "notALogin";

        if (ese != null)
        {
            // cast to a general exception since we know that
            // the security managerexception is a general exception
            GeneralException e = (GeneralException) ese
                    .containsNestedException(com.globalsight.everest.securitymgr.SecurityManagerException.class);

            if (e != null)
            {
                if (e.getMessageKey() == SecurityManagerException.MSG_FAILED_TO_AUTHENTICATE)
                {
                    failureString = "invalidUser";
                }
            }
            else
            {
                failureString = ese.getMessage();
            }
        }
        // cause "login try again" page to be invoked
        m_request.setAttribute("header", failureString);

        // if Netegrity is being used, then don't re-show the login form, use a
        // simpler failure page
        if (Netegrity.isNetegrityEnabled())
            return WebAppConstants.SIMPLE_LOGIN_FAIL;
        else
            return WebAppConstants.LOGIN_FAIL;
    }

    /**
     * Loads a user's User Parameters.
     */
    private static HashMap loadUserParameters(String p_userId)
    {
        HashMap result = null;

        try
        {
            result = ServerProxy.getUserParameterManager().getUserParameterMap(
                    p_userId);
        }
        catch (Throwable ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("can't load user parameters", ex);
            }
        }

        return result;
    }

    /*
     * Make sure the user's time zone is loaded and stored in the session. This
     * value is used for displaying date/time in the appropriate time zone for
     * each user.
     */
    private static void loadAdditionalUserInfo(HttpSession p_session, String p_userId)
    {
        try
        {
            TimeZone tz = ServerProxy.getCalendarManager().findUserTimeZone(
                    p_userId);
            if (tz != null)
            {
                p_session.setAttribute(WebAppConstants.USER_TIME_ZONE, tz);
            }
        }
        catch (Exception e)
        {
            // no time zone was loaded
        }

        // now load the CMS user info (if CMS is installed)
        try
        {
            if (Modules.isCmsAdapterInstalled())
            {
                CmsUserInfo cmsUserInfo = ServerProxy.getCmsUserManager()
                        .findCmsUserInfo(p_userId);

                // can be null and should be set from account info UI
                if (cmsUserInfo != null)
                {
                    p_session.setAttribute(WebAppConstants.CMS_USER_INFO,
                            cmsUserInfo);
                }
            }
        }
        catch (Exception e)
        {
            // do nothing (no cms user info was found)
        }
    }

    /*
     * Get the user's most recently used jobs and put in the session so the jobs
     * menu can show them.
     * 
     * @param p_session the http session @param p_userId the user id @param
     * p_cookieName the name of the cookie @param p_sessionConstant the session
     * attribute name to store the info
     */
    private static void loadJobIds(HttpSession p_session, String p_userId,
            String p_cookieName, String p_sessionConstant, HttpServletRequest m_request)
    {
        Cookie[] cookies = (Cookie[]) m_request.getCookies();
        if (cookies != null)
        {
            String cookieName = p_cookieName + p_userId.hashCode();
            for (int i = 0; i < cookies.length; i++)
            {
                Cookie cookie = (Cookie) cookies[i];
                if (cookie.getName().equals(cookieName))
                {
                    try
                    {
                        p_session.setAttribute(p_sessionConstant,
                                URLDecoder.decode(cookie.getValue()));
                    }
                    catch (Exception e)
                    {
                        // do nothing
                    }

                    break;
                }
            }
        }
    }

    // well,if the system need it will move to an class
    private static String getIpAddr(HttpServletRequest request)
    {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    // Adds auto login cookie.
    private static void addAutoLoginCookie(String p_userId, String p_pass, HttpServletResponse m_response)
    {
        String cookieName = "autoLogin";
        int expires = 60 * 60 * 24 * 14;
        String userName = UserUtil.getUserNameById(p_userId);
        String pass = Base64.encodeToString(p_pass);
        pass = URLEncoder.encode(pass);
        Cookie cookie = new Cookie(cookieName, userName + "|" + pass);
        cookie.setMaxAge(expires);
        cookie.setHttpOnly(true);
        m_response.addCookie(cookie);
    }

    /**
     * The parameter is like
     * "taskId=7498&sourcePageId=3789&targetPageId=8186&nameField=userName",
     * parse them into map.
     */
    public static HashMap parseSecret(String secret)
    {
        if (secret == null || "".equals(secret.trim()))
            return null;

        HashMap map = new HashMap();
        String[] params = secret.split("&");
        for (String param : params)
        {
            String[] par = param.split("=");
            if (par.length == 2)
            {
                String key = par[0].trim();
                String value = par[1].trim();
                map.put(key, value);
            }
        }

        return map;
    }
}
