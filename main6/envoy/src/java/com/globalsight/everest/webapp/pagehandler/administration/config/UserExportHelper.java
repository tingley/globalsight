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
package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.naming.NamingException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.globalsight.config.UserParameterImpl;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.UserSecureFields;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.SetDefaultRoleUtil;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserDefaultActivity;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserDefaultRole;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GlobalSightLocale;

/**
 * Exports system user info.
 *
 */
public class UserExportHelper implements ConfigConstants
{
    public static File exportUsers(File userPropertyFile, Element root, Document Doc,
            User operator, String userId)
    {
        FileOutputStream outStream = null;
        Element userNode = new Element("User");
        try
        {
            User user = ServerProxy.getUserManager().getUser(userId);
            // ==========================basic info=======================
            Element basicInfoNode = new Element("BasicInfo");
            basicInfoNode.addContent(new Element("UserID").setText(user.getUserId()));
            basicInfoNode.addContent(new Element("UserName").setText(user.getUserName()));
            basicInfoNode.addContent(new Element("FirstName").setText(user.getFirstName()));
            basicInfoNode.addContent(new Element("LastName").setText(user.getLastName()));
            basicInfoNode.addContent(new Element("Password").setText(user.getPassword()));
            basicInfoNode.addContent(new Element("Title").setText(user.getTitle() == null ? ""
                    : user.getTitle()));
            basicInfoNode.addContent(new Element("CompanyName").setText(user.getCompanyName()));
            userNode.addContent(basicInfoNode);
            // ======================== contact info =====================
            Element contactInfoNode = new Element("ContactInfo");
            contactInfoNode
                    .addContent(new Element("Address").setText(user.getAddress() == null ? ""
                            : user.getAddress()));
            contactInfoNode
                    .addContent(new Element("HomePhone").setText(user.getHomePhoneNumber() == null ? ""
                            : user.getHomePhoneNumber()));
            contactInfoNode
                    .addContent(new Element("WorkPhone")
                            .setText(user.getOfficePhoneNumber() == null ? "" : user
                                    .getOfficePhoneNumber()));
            contactInfoNode
                    .addContent(new Element("CellPhone").setText(user.getCellPhoneNumber() == null ? ""
                            : user.getCellPhoneNumber()));
            contactInfoNode
                    .addContent(new Element("Fax").setText(user.getFaxPhoneNumber() == null ? ""
                            : user.getFaxPhoneNumber()));
            contactInfoNode
                    .addContent(new Element("EmailAddress").setText(user.getEmail() == null ? ""
                            : user.getEmail()));
            contactInfoNode
                    .addContent(new Element("CCEmailAddress").setText(user.getCCEmail() == null ? ""
                            : user.getCCEmail()));
            contactInfoNode
                    .addContent(new Element("BCCEmailAddress").setText(user.getBCCEmail() == null ? ""
                            : user.getBCCEmail()));
            contactInfoNode.addContent(new Element("EmailLanguage").setText(user
                    .getDefaultUILocale()));
            userNode.addContent(contactInfoNode);
            // ======================= default roles==========================
            List<UserDefaultRole> defaultRoles = SetDefaultRoleUtil.getDefaultRolesByUser(userId);
            if (defaultRoles != null && defaultRoles.size() > 0)
            {
                HashMap<Long, String> allLocalesMap = getAllLocales();
                Element defaultRolesNode = new Element("DefaultRoles");
                for (UserDefaultRole userDefaultRole : defaultRoles)
                {
                    Element defaultRoleNode = new Element("DefaultRole");
                    defaultRoleNode.addContent(new Element("SourceLocale").setText(allLocalesMap
                            .get(userDefaultRole.getSourceLocaleId())));
                    defaultRoleNode.addContent(new Element("TargetLocale").setText(allLocalesMap
                            .get(userDefaultRole.getTargetLocaleId())));

                    Set activitys = userDefaultRole.getActivities();
                    for (Iterator iterator = activitys.iterator(); iterator.hasNext();)
                    {
                        UserDefaultActivity defaultActivity = (UserDefaultActivity) iterator.next();
                        defaultRoleNode.addContent(new Element("ActivityName")
                                .setText(defaultActivity.getActivityName()));
                    }
                    defaultRolesNode.addContent(defaultRoleNode);
                }
                userNode.addContent(defaultRolesNode);
            }
            // ======================= roles ==========================
            Element rolesNode = new Element("Roles");
            Collection userRoles = ServerProxy.getUserManager().getUserRoles(user);
            if (userRoles != null)
            {
                for (Iterator it = userRoles.iterator(); it.hasNext();)
                {
                    UserRoleImpl userRole = (UserRoleImpl) it.next();
                    Element activityNode = new Element("Activity");
                    Activity activity = userRole.getActivity();
                    activityNode.addContent(new Element("CompanyName").setText(CompanyWrapper
                            .getCompanyNameById(activity.getCompanyId())));
                    activityNode.addContent(new Element("SourceLocale").setText(userRole
                            .getSourceLocale()));
                    activityNode.addContent(new Element("TargetLocale").setText(userRole
                            .getTargetLocale()));
                    String activityName = activity.getActivityName();
                    activityNode.addContent(new Element("ActivityName").setText(activityName
                            .substring(0, activityName.lastIndexOf("_"))));
                    activityNode.addContent(new Element("Rate").setText(userRole.getRate()));
                    rolesNode.addContent(activityNode);
                }
                userNode.addContent(rolesNode);
            }
            // ========================== projects============================
            Element projectsNode = new Element("Projects");
            try
            {
                projectsNode.addContent(new Element("IsInAllProjects").setText(String.valueOf(user
                        .isInAllProjects())));
                List<?> projects = ServerProxy.getProjectHandler().getProjectsByUser(userId);
                for (int i = 0; i < projects.size(); i++)
                {
                    Project project = (Project) projects.get(i);
                    Element projectNode = new Element("Project");
                    projectNode.addContent(new Element("ProjectId").setText(String.valueOf(project
                            .getId())));
                    projectNode.addContent(new Element("ProjectName").setText(project.getName()));
                    projectNode.addContent(new Element("ProjectCompanyName").setText(CompanyWrapper
                            .getCompanyNameById(project.getCompanyId())));
                    projectsNode.addContent(projectNode);
                }
            }
            catch (NamingException e)
            {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
            }
            userNode.addContent(projectsNode);
            // ========================= security ==========================
            Element securityNode = new Element("Security");

            FieldSecurity fs = UserHandlerHelper.getSecurity(user, operator, false);
            securityNode.addContent(new Element("AccessLevel").setText(fs
                    .get(UserSecureFields.ACCESS_GROUPS)));
            securityNode
                    .addContent(new Element("Address").setText(fs.get(UserSecureFields.ADDRESS)));
            securityNode.addContent(new Element("Security").setText(fs
                    .get(UserSecureFields.SECURITY)));
            securityNode.addContent(new Element("Calendar").setText(fs
                    .get(UserSecureFields.CALENDAR)));
            securityNode.addContent(new Element("CellPhone").setText(fs
                    .get(UserSecureFields.CELL_PHONE)));
            securityNode.addContent(new Element("CompanyName").setText(fs
                    .get(UserSecureFields.COMPANY)));
            securityNode
                    .addContent(new Element("Country").setText(fs.get(UserSecureFields.COUNTRY)));
            securityNode.addContent(new Element("EmailAddress").setText(fs
                    .get(UserSecureFields.EMAIL_ADDRESS)));
            securityNode.addContent(new Element("CCEmailAddress").setText(fs
                    .get(UserSecureFields.CC_EMAIL_ADDRESS)));
            securityNode.addContent(new Element("BCCEmailAddress").setText(fs
                    .get(UserSecureFields.BCC_EMAIL_ADDRESS)));
            securityNode.addContent(new Element("EmailLanguage").setText(fs
                    .get(UserSecureFields.EMAIL_LANGUAGE)));
            securityNode.addContent(new Element("Fax").setText(fs.get(UserSecureFields.FAX)));
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
            securityNode.addContent(new Element("Roles").setText(fs.get(UserSecureFields.ROLES)));
            securityNode.addContent(new Element("Status").setText(fs.get(UserSecureFields.STATUS)));
            securityNode.addContent(new Element("Title").setText(fs.get(UserSecureFields.TITLE)));
            securityNode.addContent(new Element("WorkPhone").setText(fs
                    .get(UserSecureFields.WORK_PHONE)));
            userNode.addContent(securityNode);
            // ======================= permission =========================
            Element permissionGroupsNode = new Element("PermissionGroups");
            Collection permList = PermissionHelper.getAllPermissionGroupsForUser(userId);
            for (Iterator iterator = permList.iterator(); iterator.hasNext();)
            {
                Element permissionGroupNode = new Element("PermissionGroup");
                PermissionGroup pg = (PermissionGroup) iterator.next();
                permissionGroupNode.addContent(new Element("CompanyName").setText(CompanyWrapper
                        .getCompanyNameById(pg.getCompanyId())));
                permissionGroupNode.addContent(new Element("PermissionGroupId").setText(String
                        .valueOf(pg.getId())));
                permissionGroupNode.addContent(new Element("PermissionGroupName").setText(pg
                        .getName()));
                permissionGroupsNode.addContent(permissionGroupNode);
            }
            userNode.addContent(permissionGroupsNode);
            // ======================= user parameter =========================
            Element userParametersNode = new Element("UserParameters");
            Collection userConfig = ServerProxy.getUserParameterManager().getUserParameters(userId);
            for (Iterator it = userConfig.iterator(); it.hasNext();)
            {
                UserParameterImpl up = (UserParameterImpl) it.next();

                Element userParameterNode = new Element("UserParameter");
                userParameterNode.addContent(new Element("Name").setText(up.getName()));
                userParameterNode.addContent(new Element("Value").setText(up.getValue()));
                userParametersNode.addContent(userParameterNode);
            }
            userNode.addContent(userParametersNode);
            root.addContent(userNode);
            XMLOutputter XMLOut = new XMLOutputter();
            outStream = new FileOutputStream(userPropertyFile);
            XMLOut.output(Doc, outStream);
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
        }
        return userPropertyFile;
    }

    /**
     * Gets a locale ID to LANG_COUNTRY code map for performance purpose.
     */
    private static HashMap<Long, String> getAllLocales() throws Exception
    {
        HashMap<Long, String> result = new HashMap<Long, String>();
        Vector allLocales = ServerProxy.getLocaleManager().getAvailableLocales();
        for (Iterator it = allLocales.iterator(); it.hasNext();)
        {
            GlobalSightLocale gsl = (GlobalSightLocale) it.next();
            result.put(gsl.getIdAsLong(), gsl.toString());
        }
        return result;
    }

    public static File createPropertyfile(long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId)).append(File.separator)
                .append("GlobalSight").append(File.separator).append("config")
                .append(File.separator).append("export").append(File.separator).append("Users");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = USER_FILE_NAME + "information_" + sdf.format(new Date()) + ".xml";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }
}
