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
package com.globalsight.everest.webapp.pagehandler.login;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.usermgr.UserManagerWLRemote;
import com.globalsight.everest.util.netegrity.Netegrity;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.RegexUtil;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.mail.MailerException;
import com.globalsight.util.resourcebundle.LocaleWrapper;

public class RetrieveUsernamesPassHandler extends PageHandler
{

    private static Logger s_logger = Logger
            .getLogger(RetrieveUsernamesPassHandler.class.getName());
    public static final int password_length = 8;
    public static final String blackClass = "standardTextBoldLarge";
    public static final String redClass = "warningText";
    public static String serverURL = null;
    public static String message;
    private static ResourceBundle bundle;

    public RetrieveUsernamesPassHandler()
    {
    }

    /**
     * Invokes this PageHandler
     * 
     * @param jspURL
     *            the URL of the JSP to be invoked
     * @param the
     *            original request sent from the browser
     * @param the
     *            original response object
     * @param context
     *            the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException
    {
        // In case the "logout" link is clicked, invalidate the session
        HttpSession session = p_request.getSession(true);
        session.invalidate();

        // turn off cache. do both. "pragma" for the older browsers.
        p_response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        p_response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
        p_response.addHeader("Cache-Control", "no-store"); // tell proxy not to
                                                           // cache
        p_response.addHeader("Cache-Control", "max-age=0"); // stale right away

        String[] supportedLocales = (String[]) p_request
                .getAttribute(SystemConfigParamNames.UI_LOCALES);
        String defaultLocale = (String) p_request
                .getAttribute(SystemConfigParamNames.DEFAULT_UI_LOCALE);
        // get last uilocale from cookie
        String cookieUiLocale = "";
        Cookie[] cookies = p_request.getCookies();
        if (cookies != null)
        {
            for (int i = 0; i < cookies.length; i++)
            {
                Cookie cookie = cookies[i];
                if ("localelang".equals(cookie.getName()))
                    cookieUiLocale = cookie.getValue();
            }
        }
        if (supportedLocales != null
                && !Arrays.asList(supportedLocales).contains(cookieUiLocale))
            cookieUiLocale = defaultLocale;
        if (cookieUiLocale.equals(""))
            cookieUiLocale = defaultLocale;

        bundle = PageHandler.getBundleByLocale(cookieUiLocale);

        if (Netegrity.isNetegrityEnabled())
        {
            // see if this is a login or logout
            if (p_request.getParameter("activityName") != null)
            {
                s_logger.info("Redirecting to Netegrity Logout URL.");
                p_response.sendRedirect(Netegrity.getNetegrityLogoutUrl());
            }
            else
            {
                s_logger.info("Redirecting to Netegrity Login URL. All Logins must come through Netegrity.");
                p_response.sendRedirect(Netegrity.getNetegrityLoginUrl());
            }
        }
        else
        {
            dispatchJSP(p_pageDescriptor, p_request, p_response, p_context);
        }
    }

    /**
     * Invoke the correct JSP for this page
     */
    protected void dispatchJSP(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException
    {
        // create the java beans and pass them to the request. use
        // dummy link, real link will be determined after the user
        // navigates out of the page
        NavigationBean bean = new NavigationBean(DUMMY_LINK,
                p_pageDescriptor.getPageName());
        p_request.setAttribute(DUMMY_NAVIGATION_BEAN_NAME, bean);
        boolean isResetPass = false;
        boolean success = false;
        String messageStyle = redClass;
        message = "";
        String dispatchPage = null;
        serverURL = getServerURL(p_request);

        // invoke JSP
        RequestDispatcher dispatcher;
        String linkName = p_request.getParameter("linkName");
        String userName, email;
        userName = p_request.getParameter(WebAppConstants.LOGIN_NAME_FIELD);
        email = p_request.getParameter(WebAppConstants.LOGIN_EMAIL_FIELD);
        if (userName != null)
        {
            userName = EditUtil.utf8ToUnicode(userName).trim();
        }
        if (email != null)
        {
            email = EditUtil.utf8ToUnicode(email).trim();
        }

        if (null != linkName && linkName.equals("resetPass"))
        {
            if (RegexUtil.validEmail(email))
            {
                String remoteHost = p_request.getRemoteHost();
                success = resetPass(userName, email, remoteHost);
            }
            else
            {
                message = bundle.getString(LoginConstants.msg_invalidEmail);
            }
            isResetPass = true;
            p_request
                    .setAttribute(LoginConstants.form_action,
                            "/globalsight/ControlServlet?linkName=resetPass&pageName=retrieve");
        }
        else if (null != linkName && linkName.equals("retrieveUsername"))
        {
            if (RegexUtil.validEmail(email))
            {
                success = retrieveUsername(email);
            }
            else
            {
                message = bundle.getString(LoginConstants.msg_invalidEmail);
            }
            isResetPass = false;
            p_request
                    .setAttribute(LoginConstants.form_action,
                            "/globalsight/ControlServlet?linkName=retrieveUsername&pageName=retrieve");
        }

        if (success)
        {
            messageStyle = blackClass;
            dispatchPage = p_pageDescriptor.getJspURL();
        }
        else
        {
            messageStyle = redClass;
            if (isResetPass)
            {
                dispatchPage = "/envoy/login/resetPass.jsp";
            }
            else
            {
                dispatchPage = "/envoy/login/retrieveUsername.jsp";
            }
        }

        p_request.setAttribute("messStyle", messageStyle);
        p_request.setAttribute("mess", message);
        dispatcher = p_context.getRequestDispatcher(dispatchPage);
        dispatcher.forward(p_request, p_response);
    }

    /**
     * This method is used for reseting password.
     * 
     * @param p_username
     *            username
     * @param p_email
     *            userEmail
     * @param p_remoteHost
     *            the remote host for reseting password
     */
    @SuppressWarnings("unchecked")
    protected boolean resetPass(String p_userName, String p_email,
            String p_remoteHost)
    {
        boolean result = false;

        User user = null;
        UserManagerWLRemote m_userManger = ServerProxy.getUserManager();

        // get user from username
        try
        {
            user = m_userManger.getUserByName(p_userName);
        }
        catch (Exception e)
        {
            s_logger.info("Failed to get user.");
        }

        if (user == null)
        {
            message = bundle.getString(LoginConstants.msg_resetPass_noUsername);
            return false;
        }
        else
        {
            // validate email, and reset password
            String m_email = user.getEmail();
            String password = null;
            if (m_email != null && m_email.equals(p_email))
            {
                password = new Password().generater(password_length);
                user.setPasswordSet(true);
                user.setPassword(password);
                s_logger.info("The password of "
                        + user.getSpecialNameForEmail() + " has been reset by "
                        + p_remoteHost);

                try
                {
                    m_userManger.modifyUser(user, user, null, null, null);
                    result = true;
                }
                catch (Exception e)
                {
                    s_logger.info("Failed to modify user.");
                }
            }
            else
            {
                message = bundle
                        .getString(LoginConstants.msg_resetPass_noEmail);
                return false;
            }

            // Send Email
            if (result)
            {

                result = false;
                StringBuffer strBuffer = new StringBuffer();
                strBuffer.append(",").append(password); // 0
                strBuffer.append(",").append(user.getUserName()); // 1
                strBuffer.append(",").append(user.getEmail()); // 2
                strBuffer.append(",").append(
                        user.getDisplayName(LocaleWrapper.getLocale(user
                                .getDefaultUILocale()))); // 3
                strBuffer.append(",").append(serverURL); // 4

                String[] messageArguments = strBuffer.toString().substring(1)
                        .split(",");

                result = sendEmail(user, messageArguments,
                        MailerConstants.LOGIN_RESET_PASSWORD_SUBJECT,
                        MailerConstants.LOGIN_RESET_PASSWORD_MESSAGE);

                if (result)
                {
                    message = bundle
                            .getString(LoginConstants.msg_resetPass_success);
                }
            }
        }

        return result;
    }

    /**
     * This methord is used for retriving username from email.
     * 
     * @param p_email
     *            email address of user
     */
    @SuppressWarnings("unchecked")
    protected boolean retrieveUsername(String p_email)
    {

        List<User> users = null;
        message = null;
        boolean result = false;

        try
        {
            users = ServerProxy.getUserManager().getUsersByEmail(p_email);
        }
        catch (UserManagerException e)
        {

        }
        catch (RemoteException e)
        {

        }
        catch (GeneralException e)
        {

        }

        if (null == users || users.size() == 0)
        {
            message = bundle
                    .getString(LoginConstants.msg_retrieveUsernames_noUsername);
        }
        else
        {

            result = false;

            // Get usernames and sort
            List<String> names = new ArrayList<String>();
            User user = null;
            Iterator<User> it = users.iterator();
            while (it.hasNext())
            {
                user = (User) it.next();
                names.add(user.getUserName());
            }
            SortUtil.sort(names);
            StringBuffer namesStr = new StringBuffer();
            String lineBreakTag = "\r\n";
            for (int i = 0; i < names.size(); i++)
            {
                namesStr.append(names.get(i)).append(lineBreakTag);
            }

            StringBuffer strBuffer = new StringBuffer();
            strBuffer.append(",").append(namesStr); // 0
            strBuffer.append(",").append(serverURL); // 1

            String[] messageArguments = strBuffer.toString().substring(1)
                    .split(",");

            result = sendEmail(user, messageArguments,
                    MailerConstants.LOGIN_RETRIEVE_UESRNAME_SUBJECT,
                    MailerConstants.LOGIN_RETRIEVE_UESRNAME_MESSAGE);
            if (result)
            {
                message = bundle
                        .getString(LoginConstants.msg_retrieveUsernames_success);
            }
        }
        return result;
    }

    /**
     * This method is used for sending email
     */
    protected boolean sendEmail(User p_user, String[] p_arguments,
            String p_subject, String p_message)
    {

        boolean result = false;

        try
        {
            boolean isSystemNotificationEnabled = ServerProxy.getMailer()
                    .isSystemNotificationEnabled();
            if (!isSystemNotificationEnabled)
            {
                message = bundle.getString(LoginConstants.msg_noEmailServer);
                return result;
            }

            String companyIdStr = CompanyWrapper.getCompanyIdByName(p_user
                    .getCompanyName());
            ServerProxy.getMailer().sendMailFromAdmin(p_user, p_arguments,
                    p_subject, p_message, companyIdStr);

            result = true;
        }
        catch (MailerException e)
        {
        }
        catch (RemoteException e)
        {
        }
        catch (GeneralException e)
        {
        }

        return result;
    }

    /**
     * Get Server URL
     */
    public String getServerURL(HttpServletRequest p_request)
    {

        if (null == serverURL || serverURL.length() == 0)
        {
            String url = p_request.getRequestURL().toString();
            serverURL = url.substring(0, url.lastIndexOf("/") + 1);
        }

        return serverURL;
    }
}
