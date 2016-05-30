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
package com.globalsight.everest.webapp.pagehandler.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.mail.MailerLocal;
import com.globalsight.util.resourcebundle.SystemResourceBundle;
import com.globalsight.webservices.AmbassadorUtil;

/**
 * Page handler for adding/removing email notification settings to a user.
 */
@SuppressWarnings("unchecked")
public class AccountNotificationHandler extends PageHandler
{
    private static final Logger logger = Logger.getLogger(AccountNotificationHandler.class
            .getName());

    private static List s_adminNotifications = new ArrayList();
    private static List s_pmAndWfmNotifications = new ArrayList();
    private static List s_generalNotifications = new ArrayList();
    public static String RESOURCE_LOCATION = "/com/globalsight/resources/messages/";

    // populate the notification lists based on access group
    static
    {
        // Admin
        s_adminNotifications.add(UserParamNames.NOTIFY_INITIAL_IMPORT_FAILURE);
        // PM and WFM
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_JOB_DISCARD_FAILURE);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_READY_TO_DISPATCH);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_DISPATCH_FAILURE);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_IMPORT_FAILURE);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_IMPORT_CORRECTION);
        s_pmAndWfmNotifications
                .add(UserParamNames.NOTIFY_DELAYED_REIMPORT_FAILURE);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_TASK_ACCEPTANCE);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_TASK_COMPLETION);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_WFL_COMPLETION);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_JOB_COMPLETION);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_TASK_REJECTION);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_PM_CHANGE_IN_PROJECT);
        s_pmAndWfmNotifications
                .add(UserParamNames.NOTIFY_NO_AVAILABLE_RESOURCE);
        s_pmAndWfmNotifications
                .add(UserParamNames.NOTIFY_EXPORT_SOURCE_FAILURE);
        s_pmAndWfmNotifications
                .add(UserParamNames.NOTIFY_ESTIMATED_EXCEEDS_PLANNED_DATE);
        s_pmAndWfmNotifications
                .add(UserParamNames.NOTIFY_ADD_WORKFLOW_TO_JOB_FAILURE);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_STF_CREATION_FAILURE);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_SCHEDULING_FAILURE);
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_ACTIVITY_DEADLINE);
        s_pmAndWfmNotifications
                .add(UserParamNames.NOTIFY_BATCH_ALIGNMENT_SUCCESS);
        s_pmAndWfmNotifications
                .add(UserParamNames.NOTIFY_BATCH_ALIGNMENT_FAILURE);
        s_pmAndWfmNotifications
                .add(UserParamNames.NOTIFY_ALIGNMENT_UPLOAD_SUCCESS);
        s_pmAndWfmNotifications
                .add(UserParamNames.NOTIFY_ALIGNMENT_UPLOAD_FAILURE);

        // Adds parameter for notification pm overdue
        s_pmAndWfmNotifications.add(UserParamNames.NOTIFY_OVERDUE_PM);

        // LP, LM, and etc...
        s_generalNotifications.add(UserParamNames.NOTIFY_DELAYED_REIMPORT);
        s_generalNotifications.add(UserParamNames.NOTIFY_NEWLY_ASSIGNED_TASK);
        s_generalNotifications.add(UserParamNames.NOTIFY_EXPORT_FOR_UPDATE);
        s_generalNotifications.add(UserParamNames.NOTIFY_EXPORT_COMPLETION);
        s_generalNotifications.add(UserParamNames.NOTIFY_SUCCESSFUL_UPLOAD);
        s_generalNotifications.add(UserParamNames.NOTIFY_WORKFLOW_DISCARD);
        s_generalNotifications
                .add(UserParamNames.NOTIFY_SAVING_SEGMENTS_FAILURE);
        // Adds parameter for notification user overdue
        s_generalNotifications.add(UserParamNames.NOTIFY_OVERDUE_USER);
        // Adds parameter for notification quote person
        s_generalNotifications.add(UserParamNames.NOTIFY_QUOTE_PERSON);
    }

    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     * @throws IOException 
     * @throws ServletException 
     * @throws EnvoyServletException 
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor, HttpServletRequest request,
            HttpServletResponse response, ServletContext context) throws EnvoyServletException,
            ServletException, IOException
    {
        HttpSession session = request.getSession(false);

        String action = request.getParameter("action");
        if ("notification".equals(action))
        {
            preparePageInfo(request, session);
        }
        else if ("save".equals(action))
        {
            saveEditEmailTemplate(request, session, response);
            return;
        }
        else if ("edit".equals(action))
        {
            editEmailTemplate(request, session, response);
            return;
        }
        else if ("reset".equals(action))
        {
            resetEmailTemplate(request, session, response);
            return;
        }

        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Resets email content template to the most origin template.
     */
    private void resetEmailTemplate(HttpServletRequest p_request, HttpSession p_session,
            HttpServletResponse response)
    {
        try
        {
            PrintWriter writer = response.getWriter();
            String subjectKey = p_request.getParameter("subjectKey");
            String messageKey = p_request.getParameter("messageKey");
            Locale uiLocale = (Locale) p_session.getAttribute(WebAppConstants.UILOCALE);
            ResourceBundle resetBundle = SystemResourceBundle.getInstance().getResourceBundle(
                    MailerLocal.DEFAULT_RESOURCE_NAME, uiLocale);

            String subjectReset = resetBundle.getString(subjectKey);
            JSONObject json = new JSONObject();
            json.put("subjectKey", subjectKey);
            json.put("messageKey", messageKey);
            json.put("subjectText", subjectReset);
            json.put("messageText", resetBundle.getString(messageKey));

            writer.write(json.toString());
            writer.flush();
            writer.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    /**
     * Edits email content template.
     */
    private void editEmailTemplate(HttpServletRequest p_request, HttpSession p_session,
            HttpServletResponse response)
    {
        try
        {
            String companyName = getCompanyName(p_session);
            Locale uiLocale = (Locale) p_session.getAttribute(WebAppConstants.UILOCALE);
            ResourceBundle emailBundle = SystemResourceBundle.getInstance().getEmailResourceBundle(
                    MailerLocal.DEFAULT_RESOURCE_NAME, uiLocale, companyName);

            String selectFromValue = p_request.getParameter("selectFromValue");
            String selectToValue = p_request.getParameter("selectToValue");
            String selectValue = (!"".equals(selectFromValue)) ? selectFromValue : selectToValue;
            String subjectKey = MailerConstants.getEmailSubject(selectValue);
            String messageKey = MailerConstants.getEmailMessage(selectValue);
            String subjectText = emailBundle.getString(subjectKey);
            String messageText = emailBundle.getString(messageKey);

            JSONObject json = new JSONObject();
            json.put("subjectKey", subjectKey);
            json.put("messageKey", messageKey);
            json.put("subjectText", subjectText);
            json.put("messageText", messageText);

            PrintWriter writer = response.getWriter();
            writer.write(json.toString());
            writer.flush();
            writer.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    /**
     * Checks apply pattern of subject or message.
     * @param content
     * @return
     */
    private boolean checkApplyPattern(String content)
    {
        try
        {
            new MessageFormat(content);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    /**
     * Save content that user edit email template
     * 
     * @return
     * @throws IOException
     * @throws ServletException
     */
    private void saveEditEmailTemplate(HttpServletRequest p_request, HttpSession p_session,
            HttpServletResponse response)
    {
        PrintWriter writer = null;
        try
        {
            writer = response.getWriter();

            String companyName = getCompanyName(p_session);
            Locale uiLocale = (Locale) p_session.getAttribute(WebAppConstants.UILOCALE);
            String key = MailerLocal.DEFAULT_RESOURCE_NAME + "_" + companyName + "_" + uiLocale;
            ResourceBundle resourceBundle = SystemResourceBundle.getInstance().getResourceBundle(
                    MailerLocal.DEFAULT_RESOURCE_NAME, uiLocale);

            String subjectKey = p_request.getParameter("subjectKey");
            String messageKey = p_request.getParameter("messageKey");
            String subjectEdited = p_request.getParameter("subjectText");
            String messageEdited = p_request.getParameter("messageText");

            // check subject and message pattern
            if (!checkApplyPattern(subjectEdited))
            {
                writer.write("Subject fails to apply pattern: unmatched braces.");
                return;
            }
            if (!checkApplyPattern(messageEdited))
            {
                writer.write("Message fails to apply pattern: unmatched braces.");
                return;
            }

            subjectEdited = keepBsSubject(subjectEdited);
            messageEdited = handleMessage(messageEdited);
            String subjectOri = resourceBundle.getString(subjectKey);
            String messageOri = resourceBundle.getString(messageKey);
            // check place-holders
            HashSet<String> list = checkPlaceHolders(subjectOri, messageOri, subjectEdited,
                    messageEdited);
            if (list.size() != 0)
            {
                StringBuffer result = new StringBuffer();
                String addErrorPlaceHold = AmbassadorUtil.listToString(list);
                result.append("The following placeholders cannot be added : " + addErrorPlaceHold
                        + "\r\n");

                writer.write(result.toString());
                writer.flush();
            }
            else
            {
                String oriLocaleFilePath = getClass().getResource(
                        RESOURCE_LOCATION + "EmailMessageResource_" + uiLocale + ".properties")
                        .getFile();
                oriLocaleFilePath = oriLocaleFilePath.substring(0,
                        oriLocaleFilePath.lastIndexOf("/"));
                String newEmailTemplateFileName = "EmailMessageResource_" + companyName + "_"
                        + uiLocale + ".properties";
                File newFile = new File(oriLocaleFilePath, newEmailTemplateFileName);

                FileInputStream fis = null;
                if (newFile.exists())
                {
                    fis = (FileInputStream) getClass().getResourceAsStream(
                            RESOURCE_LOCATION + "EmailMessageResource_" + companyName + "_" + uiLocale
                                    + ".properties");
                }
                else
                {
                    newFile.getParentFile().mkdirs();
                    fis = (FileInputStream) getClass().getResourceAsStream(
                            RESOURCE_LOCATION + "EmailMessageResource_" + uiLocale + ".properties");
                }

                String content = FileUtil.readFile(fis, "utf-8");
                StringBuffer document = new StringBuffer();

                String contentPartOne = content.substring(0, content.indexOf(subjectKey));
                document.append(contentPartOne);
                document.append(subjectKey).append("=").append(subjectEdited).append("\r\n");
                document.append(messageKey).append("=").append(messageEdited);

                String contentPartOpt = content.substring(content.indexOf(messageKey));
                String contentPartTwo = contentPartOpt.substring(contentPartOpt.indexOf("##########"));
                document.append("\r\n" + contentPartTwo);

                FileUtil.writeFile(newFile, document.toString());

                SystemResourceBundle.getInstance().removeResourceBundleKey(key);

                writer.write("Save successfully.");
                writer.flush();
            }
        }
        catch (Exception e)
        {
            logger.error("Error when save email template.", e);
        }
        finally
        {
            if (writer != null)
            {
                writer.close();                
            }
        }
    }

    private String handleMessage(String messageEdited)
    {
        messageEdited = keepBsMessage(messageEdited);

        messageEdited = StringUtil.replace(messageEdited, "\n", "\\r\\n\\\r\n");

        return ignoreLastBackSlash(messageEdited);
    }

    /**
     * Keeps the backslash of the email template content.
     */
    private String keepBsSubject(String content)
    {
        StringBuffer keepBackslash = new StringBuffer();
        for (int i = 0; i < content.length(); i++)
        {
            char c = content.charAt(i);
            if (c == '\\')
            {
                keepBackslash.append("\\\\\\\\");
            }
            else
            {
                keepBackslash.append(c);
            }
        }
        return keepBackslash.toString();
    }

    /**
     * Keeps the backslash of the email template content.
     */
    private String keepBsMessage(String content)
    {
        StringBuffer keepBackslash = new StringBuffer();
        for (int i = 0; i < content.length(); i++)
        {
            char c = content.charAt(i);
            if (c == '\\')
            {
                keepBackslash.append("\\\\");
            }
            else
            {
                keepBackslash.append(c);
            }
        }
        return keepBackslash.toString();
    }

    /**
     * Remove the last "\" in message body to keep consistent with that in original properties.
     */
    private String ignoreLastBackSlash(String content)
    {
        if (content.indexOf("\\") != -1 && content.endsWith("\\\r\n"))
        {
            content = content.substring(0, content.lastIndexOf("\\"));
        }
        return content;
    }

    /**
     * Checks if email template content that user edited is valid or not.
     */
    private HashSet<String> checkPlaceHolders(String subjectOri, String messageOri,
            String subjectEdited, String messageEdited)
    {
        HashSet<String> addedErrorPlaceHolds = new HashSet<String>();
        HashSet<String> placeHoldersOri = new HashSet<String>();
        HashSet<String> subPlaceHoldersEdited = new HashSet<String>();
        HashSet<String> mesPlaceHoldersEdited = new HashSet<String>();

        getPlaceHolders(subjectOri, placeHoldersOri);
        getPlaceHolders(messageOri, placeHoldersOri);
        getPlaceHolders(subjectEdited, subPlaceHoldersEdited);
        getPlaceHolders(messageEdited, mesPlaceHoldersEdited);

        addErrorPlaceHoldes(placeHoldersOri, subPlaceHoldersEdited, addedErrorPlaceHolds);
        addErrorPlaceHoldes(placeHoldersOri, mesPlaceHoldersEdited, addedErrorPlaceHolds);

        return addedErrorPlaceHolds;
    }

    /**
     * Adds invalid placeholders into arraylist.
     */
    private void addErrorPlaceHoldes(HashSet<String> placeHoldersOri,
            HashSet<String> placeHoldersEdited, HashSet<String> addedErrorPlaceHolds)
    {
        for (String ph : placeHoldersEdited)
        {
            if (!placeHoldersOri.contains(ph))
            {
                addedErrorPlaceHolds.add(ph);
            }
        }
    }

    /**
     * Gets place holders of email template content.
     */
    private void getPlaceHolders(String text, HashSet<String> placeholdersOri)
    {
        Pattern p = Pattern.compile("\\{\\d+\\}");
        Matcher m = p.matcher(text);
        while (m.find())
        {
            placeholdersOri.add(m.group());
        }
    }

    /**
     * Get the notification flag value. The value will be retrieved from the
     * options hash if it has been updated (but not saved yet).
     */
    private String notificationValue(SessionManager sessionMgr,
            HttpSession p_session)
    {
        String isEnabled = null;
        HashMap optionsHash = (HashMap) sessionMgr.getAttribute("optionsHash");

        if (optionsHash != null)
        {
            isEnabled = (String) optionsHash
                    .get(UserParamNames.NOTIFICATION_ENABLED);
        }

        if (isEnabled == null)
        {
            isEnabled = PageHandler.getUserParameter(p_session,
                    UserParamNames.NOTIFICATION_ENABLED).getValue();
        }

        return isEnabled;
    }

    /**
     * Prepare the info that should be displayed to the user. Also store the
     * info from the previous page.
     */
    private void preparePageInfo(HttpServletRequest p_request,
            HttpSession p_session) throws EnvoyServletException
    {
        // Save data from previous page
        TaskHelper.saveBasicInformation(p_session, p_request);

        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);

        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        StringComparator comparator = new StringComparator(uiLocale);

        // Get the available notification options
        List availableOptions = (List) sessionMgr
                .getAttribute(WebAppConstants.AVAILABLE_NOTIFICATION_OPTIONS);

        if (availableOptions == null)
        {
            availableOptions = populateAvailableOptions(p_request);
            sessionMgr.setAttribute(
                    WebAppConstants.AVAILABLE_NOTIFICATION_OPTIONS,
                    availableOptions);
        }

        SortUtil.sort(availableOptions, comparator);

        // Get the selected notification options
        List addedOptions = (List) sessionMgr
                .getAttribute(WebAppConstants.ADDED_NOTIFICATION_OPTIONS);

        if (addedOptions == null)
        {
            addedOptions = populateAddedOptions(availableOptions, p_session);
            sessionMgr.setAttribute(WebAppConstants.ADDED_NOTIFICATION_OPTIONS,
                    addedOptions);
        }

        SortUtil.sort(addedOptions, comparator);

        p_request.setAttribute(UserParamNames.NOTIFICATION_ENABLED,
                notificationValue(sessionMgr, p_session));
    }

    /*
     * Populate the list of notification options that are selected.
     */
    private List populateAddedOptions(List p_availableOptions,
            HttpSession p_session)
    {
        ArrayList addedOptions = new ArrayList();

        HashMap params = (HashMap) p_session
                .getAttribute(WebAppConstants.USER_PARAMS);

        int size = p_availableOptions.size();
        for (int i = 0; i < size; i++)
        {
            String option = (String) p_availableOptions.get(i);
            UserParameter param = (UserParameter) params.get(option);

            if (param != null && param.getBooleanValue())
            {
                addedOptions.add(option);
            }
        }

        return addedOptions;
    }

    /*
     * Populate the list of available options based on user's access group.
     */
    private List populateAvailableOptions(HttpServletRequest p_request)
    {
        ArrayList availableOptions = new ArrayList();
        HttpSession session = p_request.getSession(false);
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);

        if (perms.getPermissionFor(Permission.ACCOUNT_NOTIFICATION_SYSTEM))
            availableOptions.addAll(s_adminNotifications);

        if (perms.getPermissionFor(Permission.ACCOUNT_NOTIFICATION_WFMGMT))
            availableOptions.addAll(s_pmAndWfmNotifications);

        if (perms.getPermissionFor(Permission.ACCOUNT_NOTIFICATION_GENERAL))
            availableOptions.addAll(s_generalNotifications);

        return availableOptions;
    }

    private String getCompanyName(HttpSession p_session) throws UserManagerException,
            RemoteException, GeneralException
    {
        String companyName = null;
        String userId = (String) p_session.getAttribute(WebAppConstants.USER_NAME);
        User user = ServerProxy.getUserManager().getUser(userId);
        if (UserUtil.isSuperPM(userId))
        {
            companyName = (String) p_session
                    .getAttribute(WebAppConstants.SELECTED_COMPANY_NAME_FOR_SUPER_PM);
        }
        else
        {
            companyName = user.getCompanyName();
        }
        return companyName;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////
}
