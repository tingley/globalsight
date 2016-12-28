/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.webapp.pagehandler.login;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.SSOUserMapping;
import com.globalsight.everest.foundation.SSOUserUtil;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.sso.SSOParameter;
import com.globalsight.everest.foundation.sso.SSOResponse;
import com.globalsight.everest.foundation.sso.SSOSPHelper;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.securitymgr.SecurityManagerException;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.*;
import com.globalsight.everest.usermgr.LoggedUser;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.usermgr.UserLdapHelper;
import com.globalsight.everest.util.netegrity.Netegrity;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.log.ActivityLog;
import com.globalsight.util.GeneralException;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.webservices.AmbassadorUtil;
import jodd.util.StringUtil;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

/**
 * EntryPageControlFlowHelper, A page flow helper which does the 'login
 * authentication' and some system initialization and return the real link for
 * next page based on the operation result.
 */
public class EntryPageControlFlowHelper implements ControlFlowHelper,
        WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(EntryPageControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;

    private HttpServletResponse m_response = null;

    public EntryPageControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    // returns the name of the link to follow
    public String determineLinkToFollow() throws EnvoyServletException
    {
        // GBS-3991, return as a failure if the IP is blocked
        if (LoginAttemptController.isIpBlocked(m_request))
        {
            return loginFailed(null);
        }

        if (Netegrity.isNetegrityEnabled())
        {
            // check the HTTP referer host, and if the machine is Netegrity,
            // then allow the access otherwise don't
            String referer = (String) m_request.getHeader("Referer");
            String ipAddress = parseIpAddressFromUrl(referer);
            if (Netegrity.getNetegrityServerIpAddress().equals(ipAddress))
            {
                CATEGORY.debug("Came in from right Netegrity host: "
                        + ipAddress);
            }
            else
            {
                CATEGORY.error("INVALID LOGIN ATTEMPT FROM: " + ipAddress
                        + "\r\nUsers must come in through Netegrity.");
                return loginFailed(null);
            }
        }

        // convert to unicode from utf-8
        // to pass on to LDAP for authentication
        String userName = ServletUtil.getValue(m_request, WebAppConstants.LOGIN_NAME_FIELD);
        if (userName != null)
        {
            userName = EditUtil.utf8ToUnicode(userName).trim();
        }

        String userId = UserUtil.getUserIdByName(userName);
        if (userId == null)
        {
            return loginFailed(null);
        }
        String userPassword;
        if (Netegrity.isNetegrityEnabled())
        {
            userPassword = Netegrity.getNetegrityGsPassword();
            CATEGORY.info("netegrity enabled, using password: '" + userPassword
                    + "'");
        }
        else
        {
            userPassword = ServletUtil.get(m_request, WebAppConstants.PASSWORD_NAME_FIELD);
            if ("........".equals(userPassword))
            {
                // auto login
                String cookieUsername = CookieUtil.getCookieValue(m_request, "autoLogin");
                String[] data = StringUtil.split(cookieUsername, "|");
                if (data != null && data.length > 1 && data[0].equals(userName))
                {
                    try
                    {
                        userPassword = AmbassadorUtil.getDecryptionString(data[1]);
                    }
                    catch (Exception e)
                    {
                        CATEGORY.error("Cannot get user password correctly.", e);
                    }
                }
            }
        }

        // SSO user
        String isSSO = m_request.getParameter("isSSO");
        boolean isSsoUser ="on".equalsIgnoreCase(isSSO);
        if (isSsoUser)
        {
            String requestUrl = m_request.getRequestURL().toString();
            String backToUrl = requestUrl;

            if (backToUrl != null
                    && backToUrl.toLowerCase().contains("/globalsight"))
            {
                int index = backToUrl.indexOf("/globalsight");
                backToUrl = backToUrl.substring(0, index)
                        + "/globalsight/ControlServlet";
            }

            String ssoIdpUrl = m_request.getParameter("ssoIdpUrlField");
            String ssoUserId = userId;
            String ssoUserPwd = userPassword;

            // get user mapping
            List<SSOUserMapping> userMap = SSOUserUtil
                    .getUserMappingBySSOUser(ssoUserId);
            SSOUserMapping ssoUser = null;

            if (userMap == null || userMap.size() == 0)
            {
                ssoUser = null;
            }

            if (userMap.size() == 1)
            {
                ssoUser = userMap.get(0);
            }

            if (userMap.size() > 1)
            {
                for (SSOUserMapping ssoUserMapping : userMap)
                {
                    long companyId = ssoUserMapping.getCompanyId();
                    Company c = CompanyWrapper.getCompanyById(companyId + "");
                    if (c.getEnableSSOLogin()
                            && c.getSsoIdpUrl().equalsIgnoreCase(ssoIdpUrl))
                    {
                        ssoUser = ssoUserMapping;
                        break;
                    }
                }
            }

            if (ssoUser == null)
            {
                return loginFailed(null);
            }
            else
            {
                long companyId = ssoUser.getCompanyId();
                Company c = CompanyWrapper.getCompanyById(companyId + "");
                String companyName = c.getCompanyName();
                String idpUrl = c.getSsoIdpUrl();
                m_request.setAttribute("sso_back_to", backToUrl);
                m_request.setAttribute("sso_username", ssoUserId);
                m_request.setAttribute("sso_password", ssoUserPwd);
                m_request.setAttribute("sso_idp_url", idpUrl);
                m_request.setAttribute("sso_company_name", companyName);

                return "ssologon";
            }
        }

        // SSO response
        String ssoResponseData = m_request.getParameter("ssoResponseData");
        if (ssoResponseData != null)
        {
            SSOResponse resp = null;
            try
            {
                boolean useSaml = !ssoResponseData.contains("|");
                SSOSPHelper helper = SSOSPHelper.createInstance(useSaml);
                resp = helper.handleSSOResponse(ssoResponseData);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }

            if (resp != null && resp.isLoginSucess())
            {
                String ssoUserId = resp.getUserId();
                String companyName = resp.getCompanyName();
                String inResponseTo = resp.getInResponseTo();

                SSOParameter para = SSOSPHelper.getParameter(inResponseTo);
                if (para != null)
                {
                    companyName = para.getParameter(SSOParameter.COMPANY_NAME);
                    ssoUserId = para.getParameter(SSOParameter.SSO_USER_NAME);
                }

                Company c = CompanyWrapper.getCompanyByName(companyName);
                long companyId = c.getId();

                SSOUserMapping userMapping = SSOUserUtil
                        .getUserMappingBySSOUser(companyId, ssoUserId);
                userId = userMapping.getUserId();
                User user = UserUtil.getUserById(userId);
                userPassword = user.getPassword();
            }
            else
            {
                return loginFailed(null);
            }
        }

        // validate user name and password
        if (userName != null && userPassword != null && userName.length() > 0
                && userPassword.length() > 0)
        {
            // invoke the login confirmation page (welcome page)
            return loginUser(userId, userPassword);
        }
        else
        {
            return loginFailed(null);
        }
    }

    /**
     * Login the user.
     */
    private String loginUser(String p_userId, String p_password)
            throws EnvoyServletException
    {
        // create a session
        HttpSession session = m_request.getSession(true);

        // this is only true when sharing the same JVM from two
        // browsers or when a session has been timed out but not
        // invalidated (a WebServer bug?)
        if (session.getAttribute(WebAppConstants.SESSION_MANAGER) != null)
        {
            session.removeAttribute(WebAppConstants.SESSION_MANAGER);
        }

        User user = null;

        try
        {
            // do authentication...
            user = performLogin(p_userId, p_password, session.getId());

            session.setAttribute(WebAppConstants.USER_NAME, p_userId);
            session.setAttribute(UserLdapHelper.LDAP_ATTR_COMPANY,
                    user.getCompanyName());

            CompanyThreadLocal.getInstance().setValue(user.getCompanyName());
            boolean isSuperAdmin = UserUtil.isSuperAdmin(p_userId);
            session.setAttribute(WebAppConstants.IS_SUPER_ADMIN,
                    Boolean.valueOf(isSuperAdmin));
        }
        catch (EnvoyServletException ese)
        {
            return loginFailed(ese);
        }

        // Load the user parameters and store them in the session.
        HashMap params = loadUserParameters(p_userId);

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
            perms = Permission.getPermissionManager().getPermissionSetForUser(
                    p_userId);
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Total user permissions are: "
                        + perms.toString());
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(
                    "Failed to get permissions for user " + user.getUserName(),
                    e);
            throw new EnvoyServletException(e);
        }
        session.setAttribute(WebAppConstants.PERMISSIONS, perms);

        // put the User object in the session manager
        sessionManager.setAttribute(WebAppConstants.USER, user);

        // set the UI locale (in the session, not a managed object)
        // If user default language is the same as the login language, use
        // use user default UI locale
        // Get the login locale and use it as the UI locale (put it in session)
        String loginLocale = ServletUtil.getValue(m_request, WebAppConstants.UILOCALE);
        Locale uiLocale = null;
        try
        {
            uiLocale = LocaleUtils.toLocale(loginLocale);
        }
        catch (IllegalArgumentException le)
        {
            CATEGORY.error("Invalid user UI locale parameter value. [" + loginLocale + "]", le);
            uiLocale = null;
        }
        if (uiLocale == null)
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
        loadAdditionalUserInfo(session, p_userId);

        // set the most recently used job id's for this user in the session
        CookieUtil.loadJobIds(session, p_userId, JobSearchConstants.MRU_JOBS_COOKIE,
                JobSearchConstants.MRU_JOBS, m_request);
        CookieUtil.loadJobIds(session, p_userId, JobSearchConstants.MRU_TASKS_COOKIE,
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
        session.setAttribute(LOGIN_PORT, loginPort);
        session.setAttribute(LOGIN_PROTOCOL, protocol);
        session.setAttribute(LOGIN_SERVER, loginServer);

        // Adds auto login cookie.
        CookieUtil.addAutoLoginCookie(p_userId, p_password, m_response, protocol.equals(PROTOCOL_HTTPS));

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

        // GBS-3991 clean up the failed login attempt associated with the IP
        // address while login successfully
        LoginAttemptController.cleanFailedLoginAttempt(m_request);

        return WebAppConstants.LOGIN_PASS;
    }

    /**
     * User login process: 1. SecurityManager perform authentication and return
     * the User object 2. Login to IFlow
     */
    private User performLogin(String p_userId, String p_password,
            String p_sessionId) throws EnvoyServletException
    {
        User user = null;
        try
        {
            // first do authentication
            user = ServerProxy.getSecurityManager().authenticateUser(p_userId,
                    p_password);

            if (user.getResetPasswordTimes() == 0) {
                //User do NOT change his weak password in 3 times, then knock him out.
                String[] msgArgs = { "Failed to log in with weak password in 3 times against the Company Security Policy. Please using Forget Password to retrieve new password." };

                CATEGORY.warn("Invalid login attempt for user '"
                        + UserUtil.getUserNameById(p_userId)
                        + "' because the user do NOT change his weak password.");
                throw new EnvoyServletException(
                        EnvoyServletException.MSG_FAILED_TO_LOGIN, msgArgs, null);
            }
        }
        catch (GeneralException ge)
        {
            String[] msgArgs =
            { "Failed to authenticate the user" };

            // TODO distinguish the different error cases
            CATEGORY.warn("Invalid login attempt for user '"
                    + UserUtil.getUserNameById(p_userId)
                    + "' possibly due to the wrong password.");
            throw new EnvoyServletException(
                    EnvoyServletException.MSG_FAILED_TO_LOGIN, msgArgs, ge);
        }
        catch (RemoteException e)
        {
            CATEGORY.warn(e.getMessage(), e);

            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }
        Map<Object, Object> activityArgs = new HashMap<Object, Object>();
        activityArgs.put("userIP",
                LoginAttemptController.getIpAddress(m_request));
        activityArgs.put("user", user.getUserName());
        ActivityLog.Start activityStart = ActivityLog.start(
                EntryPageControlFlowHelper.class, "performLogin", activityArgs);
        try
        {
            // calculate the users that logged in the system.
            ServerProxy.getUserManager().loggedInUsers(user.getUserId(),
                    p_sessionId);
        }
        catch (Exception e)
        {
            CATEGORY.debug(e);

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
     * Loads a user's User Parameters.
     */
    private HashMap loadUserParameters(String p_userId)
    {
        HashMap result = null;

        try
        {
            result = ServerProxy.getUserParameterManager().getUserParameterMap(
                    p_userId);
        }
        catch (Throwable ex)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("can't load user parameters", ex);
            }
        }

        return result;
    }

    /**
     * Return the login failed link. Also make sure to set the header attribute
     * to "fail" to distingush the link from JSP text to be displayed.
     */
    private String loginFailed(EnvoyServletException ese)
    {
        // GBS-3991, record failed login attempt
        LoginAttemptController.recordFailedLoginAttempt(m_request);

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
     * Returns the IP Address of the host that serves the given URL
     * 
     * @param p_url
     *            URL
     * @return String like "10.0.0.17"
     * @exception Exception
     */
    private String parseIpAddressFromUrl(String p_url)
            throws EnvoyServletException
    {
        try
        {
            URL u = new URL(p_url);
            InetAddress ip = InetAddress.getByName(u.getHost());
            return ip.getHostAddress();
        }
        catch (Exception e)
        {
            CATEGORY.error("Could not determine IPAddress from url " + p_url);
            throw new EnvoyServletException(e);
        }
    }


    /*
     * Make sure the user's time zone is loaded and stored in the session. This
     * value is used for displaying date/time in the appropriate time zone for
     * each user.
     */
    private void loadAdditionalUserInfo(HttpSession p_session, String p_userId)
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
    }
}
