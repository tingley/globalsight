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

package com.globalsight.everest.webapp.pagehandler.tm.management;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.gsedition.GSEdition;
import com.globalsight.everest.gsedition.GSEditionManagerLocal;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.searchreplace.SearchReplaceManager;
import com.globalsight.everest.util.comparator.ProjectTMComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.FormUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProcessStatus;
import com.globalsight.util.progress.TmProcessStatus;
import com.globalsight.webservices.client.Ambassador;
import com.globalsight.webservices.client.WebServiceClientHelper;

/**
 * PageHandler is responsible for creating, deleting and modifying TMs.
 */
public class TmMainHandler extends PageHandler implements WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(TmMainHandler.class);

    //
    // Static Members
    //
    static private ProjectHandler s_manager = null;
    static private int NUM_PER_PAGE = 10;

    //
    // Constructor
    //
    public TmMainHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getProjectHandler();
            }
            catch (Exception ignore)
            {
            }
        }
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invoke this PageHandler.
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        ProcessStatus status = (ProcessStatus) sessionMgr
                .getAttribute(TM_TM_STATUS);

        if (status != null)
        {
            SearchReplaceManager manager = (SearchReplaceManager) sessionMgr
                    .getAttribute(TM_CONCORDANCE_MANAGER);
            if (manager != null)
                manager.detachListener((IProcessStatusListener) status);

            sessionMgr.removeElement(TM_TM_STATUS);
            sessionMgr.removeElement(TM_CONCORDANCE_MANAGER);
            sessionMgr.removeElement(TM_CONCORDANCE_SEARCH_RESULTS);
        }

        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);

        String userId = getUser(session).getUserId();
        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");
        boolean isSuperAdmin = UserUtil.isSuperAdmin(userId);
        
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company curremtCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTMAccessControl = curremtCompany
                .getEnableTMAccessControl();
        

        String action = (String) p_request.getParameter(TM_ACTION);
        String tmId = (String) p_request.getParameter(RADIO_TM_ID);
        String name = null;

        try
        {
            if (tmId != null && tmId.length() > 0)
            {
                /*
                 * name = s_manager.getTmName(Long.parseLong(tmid));
                 */
                ProjectTM tm = s_manager.getProjectTMById(Long.parseLong(tmId),
                        false);
                name = tm.getName();

                sessionMgr.setAttribute(TM_TM_ID, tmId);
                sessionMgr.setAttribute(TM_TM_NAME, name);
            }

            if (action == null)
            {
                // show main screen with list of TMs
                /*
                 * String names = s_manager.getDescriptions(uiLocale); String
                 * names = getDescriptions(s_manager.getAllProjectTMs(),
                 * uiLocale, session); sessionMgr.setAttribute(TM_NAMELIST,
                 * names);
                 */
                sessionMgr.setAttribute("isAdmin", isAdmin);
                sessionMgr.setAttribute("enableTMAccessControl", enableTMAccessControl);
                List tms = getTMs(userId);
                setTableNavigation(p_request, session, tms,
                        new ProjectTMComparator(uiLocale), NUM_PER_PAGE,
                        TM_LIST, TM_KEY);

            }
            else if (action.equals(TM_ACTION_CANCEL_VALIDATION))
            {
                TmProcessStatus tmStatus = (TmProcessStatus) sessionMgr
                        .getAttribute(TM_UPLOAD_STATUS);
                if (tmStatus != null)
                {
                    tmStatus.setCanceled(true);
                }

                sessionMgr.setAttribute(TM_UPLOAD_STATUS, null);
                sessionMgr.setAttribute("isAdmin", isAdmin);
                sessionMgr.setAttribute("enableTMAccessControl", enableTMAccessControl);
                List tms = getTMs(userId);
                setTableNavigation(p_request, session, tms,
                        new ProjectTMComparator(uiLocale), NUM_PER_PAGE,
                        TM_LIST, TM_KEY);
            }
            else if (action.equals(TM_ACTION_NEW))
            {
                // show screen to create new TM: pass an empty (or, default)
                // definition.
                String definition = "<tm>"
                        + "<name></name><domain></domain><organization></organization><description></description>"
                        + "<isRemoteTm>false</isRemoteTm><gsEditionId></gsEditionId>"
                        + "<remoteTmProfileId></remoteTmProfileId><remoteTmProfileName></remoteTmProfileName>"
                        + "</tm>";
                sessionMgr.setAttribute(TM_DEFINITION, definition);
                List tms = (List) s_manager.getAllProjectTMs();
                setTMNames(p_request, tms);

                FormUtil.addSubmitToken(p_request,
                        FormUtil.Forms.NEW_TRANSLATION_MEMORY);

                GSEditionManagerLocal gsEditionManager = new GSEditionManagerLocal();
                Collection allGSEdition = gsEditionManager.getAllGSEdition();
                sessionMgr.setAttribute("allGSEdition", allGSEdition);
            }
            else if (action.equals(TM_ACTION_MODIFY))
            {
                if (tmId == null
                        || p_request.getMethod().equalsIgnoreCase(
                                REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=tm");
                    return;
                }
                ProjectTM projectTm = s_manager.getProjectTMById(Long
                        .parseLong(tmId), false);
                sessionMgr.setAttribute("modifyProjectTM", projectTm);

                // load existing definition and display for modification
                String definition = getDescription(projectTm, false);
                sessionMgr.setAttribute(TM_DEFINITION, definition);

                GSEditionManagerLocal gsEditionManager = new GSEditionManagerLocal();
                Collection allGSEdition = gsEditionManager.getAllGSEdition();
                sessionMgr.setAttribute("allGSEdition", allGSEdition);

                long gsEditionId = projectTm.getGsEditionId();
                if (gsEditionId != -1)
                {
                    Map remoteFileProfileIdName = getRemoteFPIdName(gsEditionId);
                    sessionMgr.setAttribute("remoteFpIdNames",
                            remoteFileProfileIdName);
                }
            }
            else if (action.equals(TM_ACTION_CLONE))
            {
                if (tmId == null
                        || p_request.getMethod().equalsIgnoreCase(
                                REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=tm");
                    return;
                }
                ProjectTM projectTm = s_manager.getProjectTMById(Long
                        .parseLong(tmId), false);
                sessionMgr.setAttribute("modifyProjectTM", projectTm);
                // load existing definition and display for cloning
                String definition = getDescription(projectTm, true);
                sessionMgr.setAttribute(TM_DEFINITION, definition);

                List tms = getTMs(userId);
                setTMNames(p_request, tms);
                FormUtil.addSubmitToken(p_request,
                        FormUtil.Forms.NEW_TRANSLATION_MEMORY);

                GSEditionManagerLocal gsEditionManager = new GSEditionManagerLocal();
                Collection allGSEdition = gsEditionManager.getAllGSEdition();
                sessionMgr.setAttribute("allGSEdition", allGSEdition);
            }
            else if (action.equals(TM_ACTION_SAVE))
            {
                String id = EditUtil.utf8ToUnicode((String) p_request
                        .getParameter(TM_TM_ID));
                String newname = EditUtil.utf8ToUnicode((String) p_request
                        .getParameter(TM_TM_NAME));
                String domain = EditUtil.utf8ToUnicode((String) p_request
                        .getParameter(TM_TM_DOMAIN));
                String organization = EditUtil.utf8ToUnicode((String) p_request
                        .getParameter(TM_TM_ORGANIZATION));
                String description = EditUtil.utf8ToUnicode((String) p_request
                        .getParameter(TM_TM_DESCRIPTION));
                String isRemoteTm = EditUtil.utf8ToUnicode((String) p_request
                        .getParameter(TM_TM_REMOTE_TM));

                try
                {
                    /*
                     * if (id != null && id.length() > 0) { // update existing
                     * TM s_manager.updateTm(Long.parseLong(id), newname,
                     * domain, organization, description); } else { // create
                     * new s_manager.createTm( newname, domain, organization,
                     * description); }
                     */

                    ProjectTM tm = new ProjectTM();

                    tm.setName(newname);
                    tm.setDomain(domain);
                    tm.setOrganization(organization);
                    tm.setDescription(description);
                    tm.setCreationUser(userId);
                    tm.setCreationDate(new Date());
                    tm
                            .setCompanyId(CompanyThreadLocal.getInstance()
                                    .getValue());
                    if (isRemoteTm != null && "on".equals(isRemoteTm))
                    {
                        tm.setIsRemoteTm(true);
                        // set gsEditionId
                        String gsEditionId = EditUtil
                                .utf8ToUnicode((String) p_request
                                        .getParameter(TM_TM_GS_EDITON));
                        tm.setGsEditionId(Long.parseLong(gsEditionId));
                        // set remoteTmProfileId and remoteTmProfileName
                        // (tmprofileId_tmprofileName)
                        String remoteTmProfileIdName = EditUtil
                                .utf8ToUnicode((String) p_request
                                        .getParameter(TM_TM_REMOTE_TM_PROFILE));
                        if (remoteTmProfileIdName != null
                                && !"".equals(remoteTmProfileIdName))
                        {
                            int index = remoteTmProfileIdName.indexOf("_");
                            long tmProfileId = Long
                                    .parseLong(remoteTmProfileIdName.substring(
                                            0, index));
                            String tmProfileName = remoteTmProfileIdName
                                    .substring(index + 1);
                            tm.setRemoteTmProfileId(tmProfileId);
                            tm.setRemoteTmProfileName(tmProfileName);
                        }
                    }
                    else
                    {
                        tm.setIsRemoteTm(false);
                    }

                    if (id != null && id.length() > 0)
                    {
                        // update existing TM
                        tm.setId(Long.parseLong(id));
                        s_manager.modifyProjectTM(tm);
                    }
                    else
                    {
                        if (FormUtil.isNotDuplicateSubmisson(p_request,
                                FormUtil.Forms.NEW_TRANSLATION_MEMORY))
                        {
                            // create new
                            s_manager.createProjectTM(tm);
                            if (!isAdmin && !isSuperAdmin)
                            {
                                long tId = tm.getId();
                                ProjectTMTBUsers ptmUsers = new ProjectTMTBUsers();
                                ptmUsers.addUsers(userId, String.valueOf(tId),
                                        "TM");
                            }
                            FormUtil.removeSubmitToken(p_request,
                                    FormUtil.Forms.NEW_TRANSLATION_MEMORY);
                        }
                    }
                }
                finally
                {
                    /*
                     * String names = s_manager.getDescriptions(uiLocale);
                     * String names =
                     * getDescriptions(s_manager.getAllProjectTMs(), uiLocale,
                     * session);
                     * 
                     * sessionMgr.setAttribute(TM_NAMELIST, names);
                     */
                    List tms = getTMs(userId);
                    sessionMgr.setAttribute("isAdmin", isAdmin);
                    sessionMgr.setAttribute("enableTMAccessControl", enableTMAccessControl);
                    setTableNavigation(p_request, session, tms,
                            new ProjectTMComparator(uiLocale), NUM_PER_PAGE,
                            TM_LIST, TM_KEY);
                }
            }
            else if (action.equals(TM_ACTION_DELETE))
            {
                if (tmId == null
                        || p_request.getMethod().equalsIgnoreCase(
                                REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=tm");
                    return;
                }
                try
                {
                    /*
                     * s_manager.deleteTm(name);
                     */
                    // throw new
                    // Exception("YOU SHALL NOT DELETE A TM. GOD FORBIDS IT");
                    Tm tm = s_manager.getProjectTMById(Long.valueOf(tmId),
                            false);
                    String definition = LingServerProxy.getTmCoreManager()
                            .getTmStatistics(tm, uiLocale, false).asXML();
                    sessionMgr.setAttribute(TM_DEFINITION, definition);
                    p_request.setAttribute(TM_TM_ID, tmId);
                }
                finally
                {
                    /*
                     * String names = s_manager.getDescriptions(uiLocale);
                     * String names =
                     * getDescriptions(s_manager.getAllProjectTMs(), uiLocale,
                     * session);
                     * 
                     * sessionMgr.setAttribute(TM_NAMELIST, names);
                     */
                    List tms = getTMs(userId);
                    sessionMgr.setAttribute("isAdmin", isAdmin);
                    sessionMgr.setAttribute("enableTMAccessControl", enableTMAccessControl);
                    setTableNavigation(p_request, session, tms,
                            new ProjectTMComparator(uiLocale), NUM_PER_PAGE,
                            TM_LIST, TM_KEY);
                }
            }
            else if (action.equals(TM_ACTION_IMPORT))
            {
                if (tmId == null
                        || p_request.getMethod().equalsIgnoreCase(
                                REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=tm");
                    return;
                }
                // load existing definition and display for modification
                /*
                 * String definition = s_manager.getDefinition(name, false);
                 */
                String definition = getDescription(s_manager.getProjectTMById(
                        Long.parseLong(tmId), false), false);

                sessionMgr.setAttribute(TM_DEFINITION, definition);
            }
            else if (action.equals(TM_ACTION_EXPORT))
            {
                if (tmId == null
                        || p_request.getMethod().equalsIgnoreCase(
                                REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=tm");
                    return;
                }
                // load existing definition and display for modification
                /*
                 * String definition = s_manager.getDefinition(name, false);
                 */
                String definition = getDescription(s_manager.getProjectTMById(
                        Long.parseLong(tmId), false), false);

                sessionMgr.setAttribute(TM_DEFINITION, definition);
            }
            else if (action.equals(TM_ACTION_STATISTICS))
            {
                Tm tm = s_manager.getProjectTMById(Long.parseLong(tmId), false);
                String stats = LingServerProxy.getTmCoreManager()
                        .getTmStatistics(tm, uiLocale, false).asXML();

                // JSP needs to clear this after use.
                sessionMgr.setAttribute(TM_STATISTICS, stats);
                Long tm3id = tm.getTm3Id();
                sessionMgr.setAttribute(TM_TYPE, tm3id == null ? "TM2" : "TM3");
            }
            else if (action.equals(TM_ACTION_SAVEUSERS))
            {
                String selectedField = (String) p_request
                        .getParameter("toField");

                ProjectTMTBUsers projectTMTBUsers = new ProjectTMTBUsers();
                projectTMTBUsers.updateUsers(tmId, "TM", selectedField);

                List tms = getTMs(userId);
                sessionMgr.setAttribute("isAdmin", isAdmin);
                sessionMgr.setAttribute("enableTMAccessControl", enableTMAccessControl);
                setTableNavigation(p_request, session, tms,
                        new ProjectTMComparator(uiLocale), NUM_PER_PAGE,
                        TM_LIST, TM_KEY);
            }
        }
        catch (Throwable ex)
        {
            CATEGORY.error(action, ex);

            // JSP needs to clear this.
            sessionMgr.setAttribute(TM_ERROR, ex.toString());
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private String getDescription(ProjectTM p_tm, boolean p_clone)
    {
        StringBuffer result = new StringBuffer();

        if (!p_clone)
        {
            result.append("<tm id='");
            result.append(p_tm.getId());
            result.append("'>");
        }
        else
        {
            result.append("<tm>");
        }
        result.append("<name>");
        if (!p_clone)
        {
            result.append(EditUtil.encodeXmlEntities(p_tm.getName()));
        }
        result.append("</name>");
        result.append("<domain>");
        result.append(EditUtil.encodeXmlEntities(p_tm.getDomain()));
        result.append("</domain>");
        result.append("<organization>");
        result.append(EditUtil.encodeXmlEntities(p_tm.getOrganization()));
        result.append("</organization>");
        result.append("<description>");
        result.append(EditUtil.encodeXmlEntities(p_tm.getDescription()));
        result.append("</description>");
        result.append("<isRemoteTm>");
        result
                .append(EditUtil
                        .encodeXmlEntities(p_tm.getIsRemoteTm() == true ? "true" : "false"));
        result.append("</isRemoteTm>");
        result.append("<gsEditionId>");
        result.append(EditUtil
                .encodeXmlEntities(p_tm.getGsEditionId() == -1 ? "" : String
                        .valueOf(p_tm.getGsEditionId())));
        result.append("</gsEditionId>");
        result.append("<remoteTmProfileId>");
        result
                .append(EditUtil
                        .encodeXmlEntities(p_tm.getRemoteTmProfileId() == -1 ? "" : String
                                .valueOf(p_tm.getRemoteTmProfileId())));
        result.append("</remoteTmProfileId>");
        result.append("<remoteTmProfileName>");
        result
                .append(EditUtil.encodeXmlEntities(p_tm
                        .getRemoteTmProfileName() == null ? "" : p_tm
                        .getRemoteTmProfileName()));
        result.append("</remoteTmProfileName>");
        result.append("</tm>");

        return result.toString();
    }

    /**
     * Get TMs for user
     * 
     * @param userId
     * @return
     * @throws ProjectHandlerException
     * @throws RemoteException
     */
    private List getTMs(String userId) throws ProjectHandlerException,
            RemoteException
    {
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company curremtCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        //Enable TM Access Control of current company
        boolean enableTMAccessControl = curremtCompany
                .getEnableTMAccessControl();
        List tms = new ArrayList();
        if (enableTMAccessControl)
        {
            boolean isAdmin = UserUtil.isInPermissionGroup(userId,
                    "Administrator");
            boolean isSuperAdmin = UserUtil.isSuperAdmin(userId);
            boolean isSuperPM = UserUtil.isSuperPM(userId);
            // for admin or superadmin, should get all the TM
            if (isAdmin || isSuperAdmin)
            {
                tms = (List) s_manager.getAllProjectTMs();
            }
            // for superPM, should get the TMs he can access in the company
            else if (isSuperPM)
            {
                String companyId = CompanyThreadLocal.getInstance().getValue();
                ProjectTMTBUsers ptmUsers = new ProjectTMTBUsers();
                List tmIds = ptmUsers.getTList(userId, "TM");
                Iterator it = tmIds.iterator();
                while (it.hasNext())
                {
                    ProjectTM projectTM;
                    projectTM = s_manager.getProjectTMById(((BigInteger) it
                            .next()).longValue(), false);
                    if (projectTM.getCompanyId().equals(companyId))
                    {
                        tms.add(projectTM);
                    }
                }
            }
            else
            {
                ProjectTMTBUsers ptmUsers = new ProjectTMTBUsers();
                List tmIds = ptmUsers.getTList(userId, "TM");
                Iterator it = tmIds.iterator();
                while (it.hasNext())
                {
                    ProjectTM projectTM;
                    projectTM = s_manager.getProjectTMById(((BigInteger) it
                            .next()).longValue(), false);
                    tms.add(projectTM);
                }
            }
        }
        else
        {
            tms = (List) s_manager.getAllProjectTMs();
        }
        return tms;
    }

    /**
     * Set exist TM names into request.
     * 
     * @param request
     * @throws ProjectHandlerException
     * @throws RemoteException
     */
    private void setTMNames(HttpServletRequest request, List tms)
            throws ProjectHandlerException, RemoteException
    {
        List<String> tmNames = new ArrayList<String>();
        Iterator iterator = tms.iterator();
        while (iterator.hasNext())
        {
            tmNames.add(((ProjectTM) iterator.next()).getName());
        }

        request.setAttribute(TM_EXIST_NAMES, tmNames);
    }

    /**
     * Get file profile Id-Names map from remote server specified by
     * gsEditionId.
     * 
     * @param p_gsEditionId
     * @return
     */
    private Map getRemoteFPIdName(long p_gsEditionId)
    {
        Map results = new HashMap();
        try
        {
            GSEditionManagerLocal gsEditionManager = new GSEditionManagerLocal();
            GSEdition edition = gsEditionManager
                    .getGSEditionByID(p_gsEditionId);
            Ambassador ambassador = WebServiceClientHelper.getClientAmbassador(
                    edition.getHostName(), edition.getHostPort(), edition
                            .getUserName(), edition.getPassword(), edition
                            .getEnableHttps());
            String fullAccessToken = ambassador.login(edition.getUserName(),
                    edition.getPassword());
            String realAccessToken = WebServiceClientHelper
                    .getRealAccessToken(fullAccessToken);

            String strAllTmProfiles = ambassador
                    .getAllTMProfiles(realAccessToken);
            CATEGORY.debug("allTmProfiles :: " + strAllTmProfiles);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(strAllTmProfiles
                    .getBytes("UTF-8"));
            org.w3c.dom.Document doc = db.parse(stream);
            Element root = doc.getDocumentElement();

            NodeList TMProfileNL = root.getElementsByTagName("TMProfile");
            for (int i = 0; i < TMProfileNL.getLength(); i++)
            {
                String id = null;
                String name = null;

                Node subNode = TMProfileNL.item(i);
                if (subNode instanceof Element)
                {
                    NodeList childNodeList = subNode.getChildNodes();
                    for (int j = 0; j < childNodeList.getLength(); j++)
                    {
                        if (childNodeList.item(j) instanceof Element)
                        {
                            String nodeName = childNodeList.item(j)
                                    .getNodeName();
                            NodeList subNodeList = childNodeList.item(j)
                                    .getChildNodes();
                            String nodeValue = null;
                            if (subNodeList != null
                                    && subNodeList.getLength() > 0)
                            {
                                nodeValue = subNodeList.item(0).getNodeValue();
                            }
                            CATEGORY.debug("nodeName :: " + nodeName
                                    + "; nodeValue :: " + nodeValue);

                            if ("id".equals(nodeName.toLowerCase()))
                            {
                                id = nodeValue;
                            }
                            else if ("name".equals(nodeName.toLowerCase()))
                            {
                                name = nodeValue;
                            }
                        }
                    }
                }
                results.put(new Long(id), name);
            }
        }
        catch (Exception ex)
        {
            CATEGORY
                    .error("Fail to get file profile Id-Names by gsEditionId : "
                            + p_gsEditionId);
        }

        return results;
    }

}
