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
import java.util.ArrayList;
import java.util.HashMap;
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
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.FileUtil;
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
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
            HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws EnvoyServletException, ServletException, IOException
    {
        HttpSession session = request.getSession(false);

        preparePageInfo(request, session);

        String action = request.getParameter("action");

        if ("save".equals(action))
        {
            try
            {
                saveEditEmailTemplate(request, session, response);
            }
            catch (Exception e)
            {
                logger.error(e);
            }
            return;
        }
        
        else if ("edit".equals(action))
        {
            try
            {
                editEmailTemplate(request, session, response);
            }
            catch (Exception e)
            {
                logger.error(e);
            }
            return;
        }
        else if ("reset".equals(action))
        {
            try
            {
                resetEmailTemplate(request, session, response);
            }
            catch (Exception e)
            {
                logger.error(e);
            }
            return;
        }
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Resets email content template to the most origin template.
     */
    private void resetEmailTemplate(HttpServletRequest p_request, HttpSession p_session,
            HttpServletResponse response) throws Exception
    {
        PrintWriter writer = response.getWriter();
        String subjectKey = p_request.getParameter("subjectKey");
        String messageKey = p_request.getParameter("messageKey");

        String userId = (String) p_session.getAttribute(WebAppConstants.USER_NAME);
        User user = ServerProxy.getUserManager().getUser(userId);
        Locale uiLocale = (Locale) p_session.getAttribute(WebAppConstants.UILOCALE);
        ResourceBundle resetBundle = SystemResourceBundle.getInstance().getResourceBundle(
                MailerLocal.DEFAULT_RESOURCE_NAME, uiLocale);
        ResourceBundle emailBundle = SystemResourceBundle.getInstance().getEmailResourceBundle(
                MailerLocal.DEFAULT_RESOURCE_NAME, uiLocale, user.getCompanyName());

        String subjectReset = resetBundle.getString(subjectKey);
        String messageReset = keepEscapeCharacter(StringUtil.replace(
                resetBundle.getString(messageKey), "\r\n", "\\r\\n\\\r\n"));
        String messageCur = keepEscapeCharacter(StringUtil.replace(
                emailBundle.getString(messageKey), "\r\n", "\\r\\n\\\r\n"));

        String newFilepath = getClass().getResource(
                RESOURCE_LOCATION + "EmailMessageResource_" + uiLocale + ".properties").getFile();
        String editTemplatePath = newFilepath.substring(1, newFilepath.lastIndexOf("/"));
        File newFile = new File(editTemplatePath, "EmailMessageResource_" + user.getCompanyName()
                + "_" + uiLocale + ".properties");

        FileInputStream fis = null;
        if (newFile.exists())
        {
            fis = (FileInputStream) getClass().getResourceAsStream(
                    RESOURCE_LOCATION + "EmailMessageResource_" + user.getCompanyName() + "_"
                            + uiLocale + ".properties");
        }
        else
        {
            newFile.getParentFile().mkdirs();
            fis = (FileInputStream) getClass().getResourceAsStream(
                    RESOURCE_LOCATION + "EmailMessageResource_" + uiLocale + ".properties");
        }

        String content = FileUtil.readFile(fis, "utf-8");
        content = content.replaceFirst(subjectKey + "\\s*=([^\r\n]*)", subjectKey + "="
                + subjectReset);
        content = StringUtil.replace(content, messageCur, messageReset);
        FileUtil.writeFile(newFile, content);
        String key = MailerLocal.DEFAULT_RESOURCE_NAME + "_" + user.getCompanyName() + "_"
                + uiLocale;
        SystemResourceBundle.getInstance().removeResourceBundleKey(key);
        JSONObject json = new JSONObject();
        json.put("subjectKey", subjectKey);
        json.put("messageKey", messageKey);
        json.put("subjectText", subjectReset);
        json.put("messageText", messageReset);

        writer.write(json.toString());
        writer.flush();
        writer.close();
    }

    /**
     * Edits email content template.
     */
    private void editEmailTemplate(HttpServletRequest p_request, HttpSession p_session,
            HttpServletResponse response) throws Exception
    {
        PrintWriter writer = response.getWriter();
        String userId = (String) p_session.getAttribute(WebAppConstants.USER_NAME);
        User user = ServerProxy.getUserManager().getUser(userId);
        Locale uiLocale = (Locale) p_session.getAttribute(WebAppConstants.UILOCALE);
        ResourceBundle emailBundle = SystemResourceBundle.getInstance().getEmailResourceBundle(
                MailerLocal.DEFAULT_RESOURCE_NAME, uiLocale, user.getCompanyName());

        String selectFromValue = p_request.getParameter("selectFromValue");
        String selectToValue = p_request.getParameter("selectToValue");
        String selectValue = (!selectFromValue.equals("")) ? selectFromValue : selectToValue;
        String subjectKey = MailerConstants.getEmailSubject(selectValue);
        String messageKey = MailerConstants.getEmailMessage(selectValue);
        String subjectText = emailBundle.getString(subjectKey);
        String messageText = emailBundle.getString(messageKey);

        JSONObject json = new JSONObject();
        json.put("subjectKey", subjectKey);
        json.put("messageKey", messageKey);
        json.put("subjectText", subjectText);
        json.put("messageText", messageText);
        writer.write(json.toString());
        writer.flush();
        writer.close();
        
        
    }
    
    /**
     * Save content that user edit email template
     * 
     * @return
     * @throws IOException
     * @throws ServletException
     */
    private void saveEditEmailTemplate(HttpServletRequest p_request, HttpSession p_session,
            HttpServletResponse response) throws Exception
    {
        StringBuffer result = new StringBuffer();
        PrintWriter writer = response.getWriter();

        String userId = (String) p_session.getAttribute(WebAppConstants.USER_NAME);
        User user = ServerProxy.getUserManager().getUser(userId);
        Locale uiLocale = (Locale) p_session.getAttribute(WebAppConstants.UILOCALE);
        String key = MailerLocal.DEFAULT_RESOURCE_NAME + "_" + user.getCompanyName() + "_"
                + uiLocale;
        ResourceBundle emailBundle = SystemResourceBundle.getInstance().getEmailResourceBundle(
                MailerLocal.DEFAULT_RESOURCE_NAME, uiLocale, user.getCompanyName());

        String subjectKey = p_request.getParameter("subjectKey");
        String messageKey = p_request.getParameter("messageKey");
        String subjectEdited = p_request.getParameter("subjectText");
        String messageEdited = keepEscapeCharacter(StringUtil.replace(
                p_request.getParameter("messageText"), "\n", "\\r\\n\\\r\n"));

        String subjectOri = emailBundle.getString(subjectKey);
        String messageOri = keepEscapeCharacter(StringUtil.replace(
                emailBundle.getString(messageKey), "\r\n", "\\r\\n\\\r\n"));

        List<ArrayList<String>> list = checkEmailTemplateContent(subjectOri, messageOri,
                subjectEdited, messageEdited);
        if (list.size() != 0)
        {
            if (list.get(0).size() != 0)
            {
                String addErrorPlaceHold = AmbassadorUtil.listToString(list.get(0));
                result.append("The follow placeholders can not be added : " + addErrorPlaceHold
                        + "\r\n");
            }
            if (list.get(1).size() != 0)
            {
                String missingPlaceHold = AmbassadorUtil.listToString(list.get(1));
                result.append("The follow placeholders are missing : " + missingPlaceHold);
            }
            writer.write(result.toString());
            writer.flush();
        }
        else
        {
            String newFilepath = getClass().getResource(
                    RESOURCE_LOCATION + "EmailMessageResource_" + uiLocale + ".properties")
                    .getFile();
            String editTemplatePath = newFilepath.substring(1, newFilepath.lastIndexOf("/"));
            File newFile = new File(editTemplatePath, "EmailMessageResource_"
                    + user.getCompanyName() + "_" + uiLocale + ".properties");

            FileInputStream fis = null;
            if (newFile.exists())
            {
                fis = (FileInputStream) getClass().getResourceAsStream(
                        RESOURCE_LOCATION + "EmailMessageResource_" + user.getCompanyName() + "_"
                                + uiLocale + ".properties");
            }
            else
            {
                newFile.getParentFile().mkdirs();
                fis = (FileInputStream) getClass().getResourceAsStream(
                        RESOURCE_LOCATION + "EmailMessageResource_" + uiLocale + ".properties");
            }

            String content = FileUtil.readFile(fis, "utf-8");
            content = content.replaceFirst(subjectKey + "\\s*=([^\r\n]*)", subjectKey + "="
                    + subjectEdited);
            content = StringUtil.replace(content, messageOri, messageEdited);
            FileUtil.writeFile(newFile, content);
            SystemResourceBundle.getInstance().removeResourceBundleKey(key);
            writer.write("Save successful");
            writer.flush();
        }
        writer.close();
    }
    
    /**
     * Keeps the escape character of string .
     */
    private String keepEscapeCharacter(String content)
    {
        return content.substring(0,content.lastIndexOf("\\"));
    }

    /**
     * Checks if email template content that user edited is valid or not.
     */
    private List<ArrayList<String>> checkEmailTemplateContent(String subjectOri, String messageOri,
            String subjectEdited, String messageEdited)
    {
        ArrayList<String> addedErrorPlaceHolds = new ArrayList<String>();
        ArrayList<String> missingPlaceHolds = new ArrayList<String>();
        List<ArrayList<String>> errorOption = new ArrayList<ArrayList<String>>();

        ArrayList<String> subPlaceHoldersOri = getPlaceHolders(subjectOri);
        ArrayList<String> subPlaceHoldersEdited = getPlaceHolders(subjectEdited);
        ArrayList<String> msgPlaceHoldersOri = getPlaceHolders(messageOri);
        ArrayList<String> mesPlaceHoldersEdited = getPlaceHolders(messageEdited);

        addErrorPlaceHoldes(subPlaceHoldersOri, subPlaceHoldersEdited, addedErrorPlaceHolds,
                missingPlaceHolds);
        addErrorPlaceHoldes(msgPlaceHoldersOri, mesPlaceHoldersEdited, addedErrorPlaceHolds,
                missingPlaceHolds);

        if (addedErrorPlaceHolds.size() != 0 || missingPlaceHolds.size() != 0)
        {
            errorOption.add(addedErrorPlaceHolds);
            errorOption.add(missingPlaceHolds);
        }
        return errorOption;
    }
    
    /**
     * Adds missing placeholders or invalid placeholders into arraylist.
     */
    private void addErrorPlaceHoldes(ArrayList<String> placeHoldersOri,
            ArrayList<String> placeHoldersEdited, ArrayList<String> addErrorPlaceHoldes,
            ArrayList<String> missingPlaceHolds)
    {
        for (String ph : placeHoldersEdited)
        {
            if (!placeHoldersOri.contains(ph))
            {
                addErrorPlaceHoldes.add(ph);
            }
        }

        for (String ph : placeHoldersOri)
        {
            if (!placeHoldersEdited.contains(ph))
            {
                missingPlaceHolds.add(ph);
            }
        }
    }
    
    /**
     * Gets place holders of email template content. 
     */
    private ArrayList<String> getPlaceHolders(String text)
    {
        Pattern p = Pattern.compile("\\{\\d+\\}");
        Matcher m = p.matcher(text);
        ArrayList<String> placeHolders = new ArrayList<String>();
        while(m.find())
        {
            placeHolders.add(m.group());
        }
        return placeHolders;
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
    // ////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////
}
