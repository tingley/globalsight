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
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.config.UserParameterImpl;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.securitymgr.UserFieldSecurity;
import com.globalsight.everest.securitymgr.UserSecureFields;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.SetDefaultRoleUtil;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserDefaultActivity;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserDefaultRole;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * Imports user info into system.
 */
public class UserImport implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(UserImport.class);
    private User operator;
    private String sessionId;
    private Company company;
    private static final String SQL_INSERT_PERMISSIONGROUP_USER = 
            "INSERT INTO permissiongroup_user (permissiongroup_id, user_id) VALUES (?, ?)";

    public UserImport(String sessionId, User operator, String companyId, String importToCompId)
    {
        try
        {
            this.operator = operator;
            this.sessionId = sessionId;
            Company companyUserBelongTo;
            if (importToCompId != null && !importToCompId.equals("-1"))
            {
                companyUserBelongTo = ServerProxy.getJobHandler().getCompanyById(
                        Long.parseLong(importToCompId));
            }
            else
            {
                companyUserBelongTo = ServerProxy.getJobHandler().getCompanyById(
                        Long.parseLong(companyId));
            }
            CompanyThreadLocal.getInstance().setIdValue(companyUserBelongTo.getId());
            this.company = companyUserBelongTo;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
       
    }

    public void analysisAndImport(File uploadedFile)
    {
        try
        {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(uploadedFile);

            Element rootElm = document.getRootElement();
            List<?> userNodes = rootElm.elements("User");
            int size = userNodes.size();
            for (int i = 0; i < size; i++)
            {
                Element userNode = (Element) userNodes.get(i);
                if (validateData(userNode))
                {
                    addNewUser(userNode);
                }
            }
            addMessage("<b>Done importing Users.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import user info.", e);
            addToError(e.getMessage());
        }
    }

    /**
     * Adds a new user into system.
     */
    private void addNewUser(Element userNode) throws Exception
    {
        User user = new UserImpl();
        Element basicInfoNode = userNode.element("BasicInfo");

        // basic info node
        String userName = basicInfoNode.element("UserName").getText();
        String userNewName = getNewName(userName);
        String userID = UserUtil.newUserId(userNewName);
        user.setUserId(userID);
        user.setUserName(userNewName);
        initUserInfo(userNode, user);
        // default role node
        ArrayList<UserDefaultRole> defaultRolesList = initDefaultUserRoles(userNode, userID);
        // role node
        List<UserRoleImpl> rolesList = initUserRoles(userNode, user);
        // project node
        List<Long> projectIds = initProjectIds(userNode, user);
        // security node
        UserFieldSecurity fs = initSecurity(userNode);
        // permission node
        List<Long> permissionGroupIds = initPermissionGroups(userNode);
        // user parameters
        List<UserParameterImpl> userParameterList = initUserParameter(userNode, user.getUserId());
        // saves user
        saveUserInfo(user, projectIds, fs, rolesList, permissionGroupIds,
                userParameterList);
        SetDefaultRoleUtil.saveDefaultRoles(user.getUserId(), defaultRolesList);
        if (userName.equals(userNewName))
        {
            addMessage("<b>" + userNewName + "</b> imported successfully.");
        }
        else
        {
            addMessage(" User name <b>" + userName + "</b> already exists. <b>" + userNewName
                    + "</b> imported successfully.");
        }
    }

    private String getNewName(String userName)
    {
        String[] allUserNames = UserHandlerHelper.getAllUserNames();
        Set<String> allUserNameSet = new HashSet<String>();
        if (allUserNames != null)
        {
            allUserNameSet.addAll(Arrays.asList(allUserNames));
        }
        if (allUserNameSet != null && allUserNameSet.contains(userName))
        {
            for (int num = 1;; num++)
            {
                String returnStr = null;
                if (userName.contains("_import_"))
                {
                    returnStr = userName.substring(0, userName.lastIndexOf('_')) + "_" + num;
                }
                else
                {
                    returnStr = userName + "_import_" + num;
                }
                if (!allUserNameSet.contains(returnStr))
                {
                    return returnStr;
                }
            }
        }
        else
        {
            return userName;
        }
    }

    /**
     * Saves data to database.
     */
    private void saveUserInfo(User user, List<Long> projectIds,
            UserFieldSecurity fs, List<UserRoleImpl> rolesList, List<Long> permissionGroupIds,
            List<UserParameterImpl> userParameterList) throws Exception
    {
        String userId = user.getUserId();
        ServerProxy.getUserManager().addUser(operator, user, projectIds, fs, rolesList, false);
        saveNewPermissionGroupUser(permissionGroupIds, userId);

        FluxCalendar baseCal = CalendarHelper.getDefaultCalendar(String.valueOf(company.getId()));
        UserFluxCalendar cal = new UserFluxCalendar(baseCal.getId(), userId,
                baseCal.getTimeZoneId());
        CalendarHelper.updateUserCalFieldsFromBase(baseCal, cal);
        ServerProxy.getCalendarManager().createUserCalendar(cal, userId);

        HibernateUtil.saveOrUpdate(userParameterList);
    }

    /**
     * Init the user basic info.
     */
    private List<UserParameterImpl> initUserParameter(Element userNode, String userId)
    {
        List<UserParameterImpl> userParameterList = new ArrayList<UserParameterImpl>();

        List<?> userParameters = userNode.element("UserParameters").elements("UserParameter");
        for (int j = 0; j < userParameters.size(); j++)
        {
            Element up = (Element) userParameters.get(j);
            String name = up.element("Name").getText();
            String value = up.element("Value").getText();
            UserParameterImpl upi = new UserParameterImpl(userId, name, value);
            userParameterList.add(upi);
        }
        return userParameterList;
    }

    /**
     * Gets all permission groups from file.
     */
    @SuppressWarnings("rawtypes")
    private List<Long> initPermissionGroups(Element userNode)
    {
        List<Long> permissionGroupIds = new ArrayList<Long>();
        List<?> permissionGroups = userNode.element("PermissionGroups").elements("PermissionGroup");
        for (int j = 0; j < permissionGroups.size(); j++)
        {
            Element permissionGroup = (Element) permissionGroups.get(j);
            String permissionGroupName = permissionGroup.element("PermissionGroupName").getText();

            final String hsql = "from PermissionGroupImpl as a where a.name =:pgName and a.companyId =:cId";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("pgName", permissionGroupName);
            params.put("cId", company.getId());
            List res = HibernateUtil.search(hsql, params);
            if (res != null && res.size() > 0)
            {
                PermissionGroup pg = (PermissionGroup) res.get(0);
                permissionGroupIds.add(pg.getId());
            }
            else
            {
                String msg = "Cannot find proper permission group. Name: <b>" + permissionGroupName
                        + "</b> in Company <b>" + company.getName() + "</b>.";
                addToError(msg);
                continue;
            }
        }
        return permissionGroupIds;
    }

    /**
     * Init user security info.
     */
    private UserFieldSecurity initSecurity(Element userNode)
    {
        UserFieldSecurity fs = new UserFieldSecurity();
        Element securityNode = userNode.element("Security");
        fs.put(UserSecureFields.ACCESS_GROUPS, securityNode.element("AccessLevel").getText());
        fs.put(UserSecureFields.ADDRESS, securityNode.element("Address").getText());
        fs.put(UserSecureFields.CELL_PHONE, securityNode.element("CellPhone").getText());
        fs.put(UserSecureFields.COMPANY, securityNode.element("CompanyName").getText());
        fs.put(UserSecureFields.COUNTRY, securityNode.element("Country").getText());
        fs.put(UserSecureFields.EMAIL_ADDRESS, securityNode.element("EmailAddress").getText());
        fs.put(UserSecureFields.CC_EMAIL_ADDRESS, securityNode.element("CCEmailAddress").getText());
        fs.put(UserSecureFields.BCC_EMAIL_ADDRESS, securityNode.element("BCCEmailAddress")
                .getText());
        fs.put(UserSecureFields.EMAIL_LANGUAGE, securityNode.element("EmailLanguage").getText());
        fs.put(UserSecureFields.FAX, securityNode.element("Fax").getText());
        fs.put(UserSecureFields.SECURITY, securityNode.element("Security").getText());
        fs.put(UserSecureFields.CALENDAR, securityNode.element("Calendar").getText());
        fs.put(UserSecureFields.FIRST_NAME, securityNode.element("FirstName").getText());
        fs.put(UserSecureFields.HOME_PHONE, securityNode.element("HomePhone").getText());
        fs.put(UserSecureFields.LAST_NAME, securityNode.element("LastName").getText());
        fs.put(UserSecureFields.PASSWORD, securityNode.element("Password").getText());
        fs.put(UserSecureFields.PROJECTS, securityNode.element("Projects").getText());
        fs.put(UserSecureFields.ROLES, securityNode.element("Roles").getText());
        fs.put(UserSecureFields.STATUS, securityNode.element("Status").getText());
        fs.put(UserSecureFields.TITLE, securityNode.element("Title").getText());
        fs.put(UserSecureFields.WORK_PHONE, securityNode.element("WorkPhone").getText());
        return fs;
    }

    /**
     * Gets user project ids.
     */
    private List<Long> initProjectIds(Element userNode, User user) throws ProjectHandlerException,
            RemoteException, GeneralException, NamingException
    {
        List<Long> projectIds = new ArrayList<Long>();

        Element projectsNode = userNode.element("Projects");
        String inAllProjects = projectsNode.element("IsInAllProjects").getText();
        user.isInAllProjects(Boolean.valueOf(inAllProjects));

        if (inAllProjects != null && Boolean.parseBoolean(inAllProjects))
        {
            boolean isSuperPM = false;
            try
            {
                isSuperPM = UserUtil.isSuperPM(user.getUserId());
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }

            List<Project> projects = null;
            if (!isSuperPM)
            {
                projects = ServerProxy.getProjectHandler().getProjectsByCompanyId(company.getId());
                String companyId = String.valueOf(company.getId());
                if (!CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
                {
                    Project project = null;
                    for (Iterator<Project> iter = projects.iterator(); iter.hasNext();)
                    {
                        project = (Project) iter.next();
                        if (project.getCompanyId() != company.getId())
                        {
                            iter.remove();
                        }
                    }
                }
            }
            else
            {
                projects = (List<Project>) ProjectHandlerHelper.getAllProjects();
            }

            if (projects != null)
            {
                for (int i = 0; i < projects.size(); i++)
                {
                    Project project = (Project) projects.get(i);
                    projectIds.add(project.getIdAsLong());
                }
            }
        }
        else
        {
            List<?> projects = projectsNode.elements("Project");
            for (int j = 0; j < projects.size(); j++)
            {
                Element projectNode = (Element) projects.get(j);

                String projectName = projectNode.element("ProjectName").getText();
                Project project = ServerProxy.getProjectHandler().getProjectByNameAndCompanyId(
                        projectName, company.getId());
                if (project == null || project.getCompanyId() != company.getId())
                {
                    this.addToError("Cannot find project: <b>" + projectName
                            + "</b> in Company: <b>" + company + "</b>.");
                    continue;
                }

                projectIds.add(project.getIdAsLong());
            }
        }
        return projectIds;
    }

    private ArrayList<UserDefaultRole> initDefaultUserRoles(Element userNode, String userID)
            throws Exception
    {
        ArrayList<UserDefaultRole> list = new ArrayList<UserDefaultRole>();
        Element roleNode = userNode.element("DefaultRoles");
        if (roleNode != null)
        {
            List<?> defaultRoles = roleNode.elements("DefaultRole");
            for (int i = 0; i < defaultRoles.size(); i++)
            {
                Element defaultRoleNode = (Element) defaultRoles.get(i);
                String sourceLocale = defaultRoleNode.element("SourceLocale").getText();
                String targetLocale = defaultRoleNode.element("TargetLocale").getText();
                GlobalSightLocale srcLocale = null;
                GlobalSightLocale trgLocale = null;
                try
                {
                    srcLocale = ServerProxy.getLocaleManager().getLocaleByString(sourceLocale);
                }
                catch (Exception e)
                {
                    this.addToError("Cannot find locale by '<b>" + sourceLocale
                            + "</b>' in current system.");
                    continue;
                }
                try
                {
                    trgLocale = ServerProxy.getLocaleManager().getLocaleByString(targetLocale);
                }
                catch (Exception e)
                {
                    this.addToError("Cannot find locale by '<b>" + targetLocale
                            + "</b>' in current system.");
                    continue;
                }

                UserDefaultRole role = new UserDefaultRole();
                role.setUserId(userID);
                role.setSourceLocaleId(srcLocale.getId());
                role.setTargetLocaleId(trgLocale.getId());
                role.setSourceLocaleObject(srcLocale);
                role.setTargetLocaleObject(trgLocale);
                role.setStatus(UserDefaultRole.ADD);

                Set<UserDefaultActivity> activities = new HashSet<UserDefaultActivity>();
                List<?> acNodes = defaultRoleNode.elements("ActivityName");
                for (int j = 0; j < acNodes.size(); j++)
                {
                    Element acNode = (Element) acNodes.get(j);
                    UserDefaultActivity ac = new UserDefaultActivity();
                    // In "user_default_activities" table, it does not care
                    // company id. So if an activity does not exist in
                    // system, it does not matter, no need to check here.
                    ac.setActivityName(acNode.getText());
                    ac.setDefaultRole(role);
                    activities.add(ac);
                }
                role.setActivities(activities);
                list.add(role);
            }
        }

        return list;
    }

    private List<UserRoleImpl> initUserRoles(Element userNode, User user) throws Exception
    {
        List<UserRoleImpl> rolesList = new ArrayList<UserRoleImpl>();

        Element roleNode = userNode.element("Roles");
        if (roleNode != null)
        {
            List<?> activitys = roleNode.elements("Activity");
            for (int j = 0; j < activitys.size(); j++)
            {
                Element activityNode = (Element) activitys.get(j);
                String sourceLocale = activityNode.element("SourceLocale").getText();
                String targetLocale = activityNode.element("TargetLocale").getText();
                String activityName = activityNode.element("ActivityName").getText();
                LocalePair lp = ServerProxy.getLocaleManager()
                        .getLocalePairBySourceTargetAndCompanyStrings(sourceLocale, targetLocale,
                                company.getId());
                if (lp == null)
                {
                    String lpName = sourceLocale + " TO " + targetLocale;
                    this.addToError("Cannot find locale pair: <b>" + lpName
                            + "</b> in Company: <b>" + company.getName() + "</b>.");
                    continue;
                }

                Activity activity = null;
                activity = ServerProxy.getJobHandler().getActivityByCompanyId(
                        activityName + "_" + company.getId(), String.valueOf(company.getId()));
                if (activity == null)
                {
                    this.addToError("Cannot find activity: <b>" + activityName
                            + "</b> in Company: <b>" + company.getName() + "</b>.");
                    continue;
                }

                UserRoleImpl userRole = new UserRoleImpl();
                ((Role) userRole).setSourceLocale(sourceLocale);
                ((Role) userRole).setTargetLocale(targetLocale);
                userRole.setUser(user.getUserId());
                userRole.setActivity(activity);
                userRole.setRate("-1");

                rolesList.add(userRole);
            }
        }
        return rolesList;
    }

    private void initUserInfo(Element userNode, User user)
    {
        Element basicInfoNode = userNode.element("BasicInfo");

        user.setFirstName(basicInfoNode.element("FirstName").getText());
        user.setLastName(basicInfoNode.element("LastName").getText());
        user.setPassword(basicInfoNode.element("Password").getText());
        user.setTitle(basicInfoNode.element("Title").getText());
        user.setCompanyName(company.getName());

        // contact info node
        Element contactInfoNode = userNode.element("ContactInfo");
        user.setAddress(contactInfoNode.element("Address").getText());
        user.setHomePhoneNumber(contactInfoNode.element("HomePhone").getText());
        user.setOfficePhoneNumber(contactInfoNode.element("WorkPhone").getText());
        user.setCellPhoneNumber(contactInfoNode.element("CellPhone").getText());
        user.setFaxPhoneNumber(contactInfoNode.element("Fax").getText());
        user.setEmail(contactInfoNode.element("EmailAddress").getText());
        user.setCCEmail(contactInfoNode.element("CCEmailAddress").getText());
        user.setBCCEmail(contactInfoNode.element("BCCEmailAddress").getText());
        user.setDefaultUILocale(contactInfoNode.element("EmailLanguage").getText());
    }

    /**
     * Adds new data into "permissiongroup_user" table.
     */
    private synchronized void saveNewPermissionGroupUser(List<Long> permissionGroupIds,
            String userId)
    {
        Connection c = null;
        PreparedStatement stmt = null;
        try
        {
            c = ConnectionPool.getConnection();
            c.setAutoCommit(false);
            stmt = c.prepareStatement(SQL_INSERT_PERMISSIONGROUP_USER);
            for (Long id : permissionGroupIds)
            {
                stmt.setLong(1, id);
                stmt.setString(2, userId);
                stmt.addBatch();
            }
            stmt.executeBatch();
            c.commit();
        }
        catch (Exception e)
        {
            logger.error("Failed to insert permission group user.", e);
        }
        finally
        {
            ConnectionPool.silentClose(stmt);
            ConnectionPool.silentReturnConnection(c);
        }
    }

    /**
     * Checks data before import.
     */
    private boolean validateData(Element userNode) throws Exception
    {
        // check user existence
        String userName = userNode.element("BasicInfo").element("UserName").getText();
        String companyNameFromUserNode = userNode.element("BasicInfo").element("CompanyName")
                .getText();
        // checks email address
        String email = userNode.element("ContactInfo").element("EmailAddress").getText()
                .replaceAll(" ", "");
        String ccEmail = userNode.element("ContactInfo").element("CCEmailAddress").getText()
                .replaceAll(" ", "");
        String bccEmail = userNode.element("ContactInfo").element("BCCEmailAddress").getText()
                .replaceAll(" ", "");
        // checks company existence
        Company company = ServerProxy.getJobHandler().getCompany(companyNameFromUserNode);
        if (company == null)
        {
            String msg = "Skipped importing user " + userName + ". Cannot find a company named <b>"
                    + companyNameFromUserNode + "</b> in system.";
            logger.warn(msg);
            addToError(msg);
            return false;
        }

        String msgEmail = "Invalid Email Address : Only letters(a-z,A-Z), numbers(0-9), underscores(_),"
                + " hyphen(-) and dot (.) are allowed. No consecutive underscores, hyphens or dots"
                + " allowed, and not the first or last character.";
        String msgCCEmail = "Invalid CC Email Address : Only letters(a-z,A-Z), numbers(0-9), underscores(_),"
                + " hyphen(-) and dot (.) are allowed. No consecutive underscores, hyphens or dots"
                + " allowed, and not the first or last character.";
        String msgBCCEmail = "Invalid BCC Email Address : Only letters(a-z,A-Z), numbers(0-9), underscores(_),"
                + " hyphen(-) and dot (.) are allowed. No consecutive underscores, hyphens or dots"
                + " allowed, and not the first or last character.";

        String regm = "\\w+([-_.]\\w+)*@\\w+([-_.]\\w+)*\\.\\w+([-_.]\\w+)*";

        if ("@".equals(email))
        {
            return true;
        }
        else if (email.endsWith(",") || email.contains("__"))
        {
            logger.warn(msgEmail);
            addToError(msgEmail);
            return false;
        }
        else
        {
            for (String emailOne : email.split(","))
            {
                if (!emailOne.matches(regm) && emailOne != "")
                {
                    logger.warn(msgEmail);
                    addToError(msgEmail);
                    return false;
                }
            }
        }

        if (ccEmail.endsWith(",") || ccEmail.contains("__"))
        {
            logger.warn(msgCCEmail);
            addToError(msgCCEmail);
            return false;
        }
        else
        {
            for (String ccEmailOne : ccEmail.split(","))
            {
                if (!ccEmailOne.matches(regm) && ccEmailOne != "")
                {
                    logger.warn(msgCCEmail);
                    addToError(msgCCEmail);
                    return false;
                }
            }
        }

        if (bccEmail.endsWith(",") || bccEmail.contains("__"))
        {
            logger.warn(msgBCCEmail);
            addToError(msgBCCEmail);
            return false;
        }
        else
        {
            for (String bccEmailOne : bccEmail.split(","))
            {
                if (!bccEmailOne.matches(regm) && bccEmailOne != "")
                {
                    logger.warn(msgBCCEmail);
                    addToError(msgBCCEmail);
                    return false;
                }
            }
        }
        return true;
    }

    private void addToError(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p style='color:red'>" + msg);
    }

    private void addMessage(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p>" + msg);
    }
}
