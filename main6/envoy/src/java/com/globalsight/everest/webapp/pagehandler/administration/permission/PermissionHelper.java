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
package com.globalsight.everest.webapp.pagehandler.administration.permission;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionManager;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.util.edit.EditUtil;

/**
 * A bunch of helper methods for operating on Permission Groups.
 */
public class PermissionHelper
{

    /**
     * Gets all the PermissionGroups that this user can assign. If the user has
     * the permission to assign all permission groups, then he gets all. If he
     * doesn't, then he only gets permissiongroups which are a subset of his own
     * permission set.
     * 
     * @return Collection
     */
    public static Collection getAllPermissionGroupsUserCanAssign(
            String p_companyId, PermissionSet p_userPermissionSet)
            throws EnvoyServletException
    {
        try
        {
            ArrayList assignablePermGroups = new ArrayList();
            Collection allPermGroups = Permission.getPermissionManager()
                    .getAllPermissionGroupsByCompanyId(p_companyId);
            if (p_userPermissionSet
                    .getPermissionFor(Permission.USERS_EDIT_ASSIGN_ANY_PERMGROUPS))
                return allPermGroups;

            Iterator iter = allPermGroups.iterator();
            while (iter.hasNext())
            {
                PermissionGroup permGroup = (PermissionGroup) iter.next();
                PermissionSet permSet = permGroup.getPermissionSet();
                if (permSet.isSubSet(p_userPermissionSet))
                {
                    assignablePermGroups.add(permGroup);
                }
            }
            return assignablePermGroups;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get all permission groups.
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Collection getAllPermissionGroups()
            throws EnvoyServletException
    {
        try
        {
            return Permission.getPermissionManager().getAllPermissionGroups();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Gets permission ids of super admin and super pm
     * 
     * @return permission ids of super admin and super pm
     * @throws EnvoyServletException
     */
    public static long[] getSuperPermissionGroupIds()
            throws EnvoyServletException
    {
        try
        {
            Collection pGroups = Permission.getPermissionManager()
                    .getAllPermissionGroupsByCompanyId(
                            CompanyWrapper.SUPER_COMPANY_ID);

            long[] ids = new long[pGroups.size()];
            int index = 0;
            Iterator iter = pGroups.iterator();
            while (iter.hasNext())
            {
                PermissionGroup pGroup = (PermissionGroup) iter.next();
                String name = pGroup.getName();
                if (!WebAppConstants.SUPER_ADMINISTRATOR_NAME.equals(name)
                        && !WebAppConstants.SUPER_PM_NAME.equals(name))
                    continue;
                ids[index++] = pGroup.getId();
            }

            return ids;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get all permission groups for a user.
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Collection getAllPermissionGroupsForUser(String p_userId)
            throws EnvoyServletException
    {
        try
        {
            return Permission.getPermissionManager()
                    .getAllPermissionGroupsForUser(p_userId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get all users for a permission group.
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Collection getAllUsersForPermissionGroup(long p_id)
            throws EnvoyServletException
    {
        try
        {
            ArrayList userIds = (ArrayList) Permission.getPermissionManager()
                    .getAllUsersForPermissionGroup(p_id);
            ArrayList users = new ArrayList(userIds.size());
            // Loop through and fetch User's because we want the user's name
            // too.
            for (int i = 0; i < userIds.size(); i++)
            {
                String userid = (String) userIds.get(i);
                users.add(ServerProxy.getUserManager().getUser(userid));
            }
            return users;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Save the request parameters from the Basic Info page
     */
    public static void saveBasicInfo(PermissionGroup permGroup,
            HttpServletRequest request) throws EnvoyServletException
    {
        String buf = (String) request.getParameter("nameField");
        if (buf != null)
            permGroup.setName(buf);
        buf = (String) request.getParameter("descField");
        if (buf != null)
            permGroup.setDescription(buf);

        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (permGroup.getCompanyId() < 0)
            permGroup.setCompanyId(Long.parseLong(companyId));
    }

    /**
     * Save the request parameters from the Permission Set page
     */
    public static void savePermissionSet(PermissionGroup permGroup,
            HttpServletRequest request) throws EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        boolean hasActivityDashboardViewPermissionBefore = false;
        boolean hasActivityDashboardViewPermissionAfter = false;
        PermissionSet set = permGroup.getPermissionSet();

        if (set != null)
        {
            hasActivityDashboardViewPermissionBefore = set
                    .getPermissionFor(Permission.ACTIVITY_DASHBOARD_VIEW);
        }

        set = new PermissionSet();

        Enumeration enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements())
        {
            String name = (String) enumeration.nextElement();
            if (name.startsWith("perm."))
            {
                // strip off "perm."
                String permission = name.substring(5);
                set.setPermissionFor(permission, true);
                if (Permission.ACTIVITY_DASHBOARD_VIEW.equals(permission))
                {
                    hasActivityDashboardViewPermissionAfter = true;
                }
            }
        }
        permGroup.setPermissionSet(set.toString());

        if (hasActivityDashboardViewPermissionBefore
                && !hasActivityDashboardViewPermissionAfter)
        {
            session.setAttribute(Permission.ACTIVITY_DASHBOARD_VIEW,
                    "unchecked");
        }
        if (!hasActivityDashboardViewPermissionBefore
                && hasActivityDashboardViewPermissionAfter)
        {
            session.setAttribute(Permission.ACTIVITY_DASHBOARD_VIEW, "checked");
        }
    }

    /**
     * Save the request parameters from the users page.
     */
    public static void saveUsers(PermissionGroup permGroup,
            HttpServletRequest p_request) throws EnvoyServletException
    {
        if (p_request == null)
            return;

        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String toField = (String) p_request.getParameter("toField");
        ArrayList users = new ArrayList();
        try
        {
            if (toField != null && !toField.equals(""))
            {
                String[] user = toField.split(",");
                for (int i = 0; i < user.length; i++)
                {
                    users.add(ServerProxy.getUserManager().getUser(user[i]));
                }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        sessionMgr.setAttribute("usersForGroup", users);
    }

    public static void updateUsers(PermissionGroup permGroup,
            HttpServletRequest request) throws EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        ArrayList changed = (ArrayList) sessionMgr
                .getAttribute("usersForGroup");
        if (changed == null)
            return;
        ArrayList existing = (ArrayList) PermissionHelper
                .getAllUsersForPermissionGroup(permGroup.getId());
        if (existing == null && changed.size() == 0)
            return;

        try
        {
            PermissionManager manager = Permission.getPermissionManager();
            if (existing == null)
            {
                manager.mapUsersToPermissionGroup(changed, permGroup);
            }
            else
            {
                // need to determine what to add and what to remove.
                // Loop thru old list and see if user is in new list. If not,
                // remove it.
                ArrayList remove = new ArrayList();
                for (int i = 0; i < existing.size(); i++)
                {
                    boolean found = false;
                    User user = (User) existing.get(i);
                    for (int j = 0; j < changed.size(); j++)
                    {
                        User cuser = (User) changed.get(j);
                        if (user.getUserId().equals(cuser.getUserId()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        remove.add(user.getUserId());
                }
                if (remove.size() > 0)
                    manager.unMapUsersFromPermissionGroup(remove, permGroup);

                // Loop thru new list and see if user is in old list. If not,
                // add it.
                ArrayList add = new ArrayList();
                for (int i = 0; i < changed.size(); i++)
                {
                    boolean found = false;
                    User user = (User) changed.get(i);
                    for (int j = 0; j < existing.size(); j++)
                    {
                        User cuser = (User) existing.get(j);
                        if (user.getUserId().equals(cuser.getUserId()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        add.add(user.getUserId());
                }
                if (add.size() > 0)
                    manager.mapUsersToPermissionGroup(add, permGroup);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

    }

    /*
     * Parse the xml and create a new xml file and store it in the session. The
     * reason to create a new xml file is that the jsp needs to know localized
     * labels and if the permission is set or not. So it'll take something like:
     * 
     * <permission id="localePairs.remove"/> and generate: <permission
     * id="localePairs.remove" label="Remove" set="false"/>
     */
    public static void setPermissionXmlInSession(SessionManager sessionMgr,
            HttpSession session, PermissionSet permSet, String permissionXml)
    {
        ResourceBundle bundle = PageHandler.getBundle(session);

        // Run xml through parser to look up the strings in the resource bundle
        Document doc = null;
        try
        {
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.parse(new InputSource(new StringReader(permissionXml)));
            doc = parser.getDocument();

            Element root = doc.getDocumentElement();
            NodeList categories = root.getElementsByTagName("category");
            for (int i = 0; i < categories.getLength(); i++)
            {
                Element category = (Element) categories.item(i);
                String label = category.getAttributes().item(0).getNodeValue();
                category.setAttribute("label", bundle.getString(label));
            }
            NodeList perms = root.getElementsByTagName("permission");
            for (int i = 0; i < perms.getLength(); i++)
            {
                Element perm = (Element) perms.item(i);
                String id = perm.getAttributes().item(0).getNodeValue();
                perm.setAttribute("label", bundle.getString("permission." + id));
                perm.setAttribute(
                        "set",
                        permSet == null ? "false" : String.valueOf(permSet
                                .getPermissionFor(id)));
            }
        }
        catch (org.xml.sax.SAXNotRecognizedException e)
        {
            return;
        }
        catch (org.xml.sax.SAXException e1)
        {
            return;
        }
        catch (java.io.IOException e2)
        {
            return;
        }
        // Convert back to a string to send to the jsp
        try
        {
            Source source = new DOMSource((Element) doc.getDocumentElement());
            StringWriter out = new StringWriter();
            StreamResult result = new StreamResult(out);
            Transformer xformer = TransformerFactory.newInstance()
                    .newTransformer();
            xformer.setOutputProperty("encoding", "iso-8859-1");
            xformer.setOutputProperty("indent", "no");
            xformer.transform(source, result);
            sessionMgr.setAttribute("permissionXML",
                    EditUtil.toJavascript(result.getWriter().toString()));
        }
        catch (Exception e3)
        {
            return;
        }
    }
}
