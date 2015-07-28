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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

import com.globalsight.cxe.entity.customAttribute.TMAttribute;
import com.globalsight.cxe.entity.customAttribute.TMAttributeManager;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.gsedition.GSEdition;
import com.globalsight.everest.gsedition.GSEditionManagerLocal;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.Project;
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
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.log.OperationLog;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
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
    private static final Logger logger = Logger.getLogger(TmMainHandler.class);

    // Static Members
    static private ProjectHandler projectHandler = null;
    static private int NUM_PER_PAGE = 20;

    // Constructor
    public TmMainHandler()
    {
        super();

        if (projectHandler == null)
        {
            try
            {
                projectHandler = ServerProxy.getProjectHandler();
            }
            catch (Exception ignore)
            {
                logger.error("Error found in generating ProjectHandler.", 
                        ignore);
            }
        }
    }

    // Interface Methods: PageHandler

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

        handleProcessStatus(sessionMgr);

        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);

        String userId = getUser(session).getUserId();
        boolean isAdmin = UserUtil.isInPermissionGroup(userId,
                Permission.GROUP_ADMINISTRATOR);
        boolean isSuperAdmin = UserUtil.isSuperAdmin(userId);

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company currentCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTMAccessControl = currentCompany
                .getEnableTMAccessControl();

        sessionMgr.setAttribute("isAdmin", isAdmin);
        sessionMgr.setAttribute("enableTMAccessControl", enableTMAccessControl);

        String action = (String) p_request.getParameter(ACTION_STRING);
        String filterSearch = handleFilters(p_request, sessionMgr, action);
        setNumberOfPerPage(p_request);

        String[] tmIds = p_request.getParameterValues(RADIO_TM_ID);
        String tmId = "";
        if (tmIds != null && tmIds.length == 1)
            tmId = tmIds[0];

        String tmName = null;
        List<ProjectTM> tms = new ArrayList<ProjectTM>();

        try
        {
            List<TMAttribute> tmas = null;
            List<String> allAtt = null;
            if (!StringUtil.isEmpty(tmId))
            {
                ProjectTM tm = projectHandler.getProjectTMById(
                        Long.parseLong(tmId), false);
                tmName = tm.getName();

                sessionMgr.setAttribute(TM_TM_ID, tmId);
                sessionMgr.setAttribute(TM_TM_NAME, tmName);

                tmas = tm.getAllTMAttributes();
                allAtt = TMAttributeManager.getAvailableAttributenames(tm);
            }
            else
            {
                tmas = new ArrayList<TMAttribute>();
                allAtt = TMAttributeManager.getAvailableAttributenames();
            }
            p_request.setAttribute(TM_AVAILABLE_ATTS,
                    TMAttributeManager.toOneStr(allAtt));
            p_request.setAttribute(TM_TM_ATTS, TMAttributeManager.toOne(tmas));

            // Handle actions
            if (action == null)
            {
                Tm3ConvertProcess tm3ConvertProcess = Tm3ConvertProcess
                        .getInstance();
                String tm3Status = tm3ConvertProcess.getStatus();
                if ("".equals(tm3Status) || "Cancelled".equals(tm3Status))
                    tm3ConvertProcess.setStatus("null");
            }
            else if (action.equals(TM_ACTION_CANCEL_VALIDATION))
            {
                cancelValidation(sessionMgr);
            }
            else if (action.equals(TM_ACTION_NEW))
            {
                // Generate an empty TM defination
                createTM(p_request, sessionMgr);
            }
            else if (action.equals(TM_ACTION_MODIFY))
            {
                tmId = p_request.getParameter("TMId");
                ProjectTM projectTm = projectHandler.getProjectTMById(
                        Long.parseLong(tmId), false);
                sessionMgr.setAttribute("modifyProjectTM", projectTm);

                // load existing definition and display for modification
                String definition = getDefination(projectTm, false);
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
                if (isFromHttpGetWithoutTmId(p_request, p_response, tmId))
                    return;

                ProjectTM projectTm = projectHandler.getProjectTMById(
                        Long.parseLong(tmId), false);
                sessionMgr.setAttribute("modifyProjectTM", projectTm);
                // load existing definition and display for cloning
                String definition = getDefination(projectTm, true);
                sessionMgr.setAttribute(TM_DEFINITION, definition);

                tms = (List) projectHandler.getAllProjectTMs();
                sessionMgr.setAttribute(TM_EXIST_NAMES, getExistTMNames(tms));
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
                String indexTarget = EditUtil.utf8ToUnicode((String) p_request
                        .getParameter("indexTarget"));
                String isRemoteTm = EditUtil.utf8ToUnicode((String) p_request
                        .getParameter(TM_TM_REMOTE_TM));
                String tmAttributes = (String) p_request
                        .getParameter("tmAttributes");

                try
                {
                    ProjectTM tm = new ProjectTM();

                    tm.setName(newname);
                    tm.setDomain(domain);
                    tm.setOrganization(organization);
                    tm.setDescription(description);
                    tm.setIndexTarget("on".equals(indexTarget) ? true : false);
                    tm.setCreationUser(userId);
                    tm.setCreationDate(new Date());
                    tm.setCompanyId(Long.parseLong(CompanyThreadLocal
                            .getInstance().getValue()));
                    TMAttributeManager.setTMAttributes(tm, tmAttributes);
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
                        projectHandler.modifyProjectTM(tm);
                        OperationLog.log(userId, OperationLog.EVENT_EDIT, OperationLog.COMPONET_TM,
                                tm.getName());
                    }
                    else
                    {
                        if (FormUtil.isNotDuplicateSubmisson(p_request,
                                FormUtil.Forms.NEW_TRANSLATION_MEMORY))
                        {
                            // create new
                            projectHandler.createProjectTM(tm);
                            OperationLog.log(userId, OperationLog.EVENT_ADD, OperationLog.COMPONET_TM,
                                    tm.getName());
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
                catch (Exception e)
                {
                    logger.error("Error found in saving project TM.", e);
                }
            }
            else if (action.equals(TM_ACTION_DELETE))
            {
                try
                {
                    HashSet<GlobalSightLocale> TMLocales = new HashSet<GlobalSightLocale>();
                    ProjectTM tm = null;
                    Set<GlobalSightLocale> locales = null;
                    String tmIdString = "";
                    if (tmIds != null)
                    {
                        for (int i = 0; i < tmIds.length; i++)
                        {
                            tmId = tmIds[i];
                            tm = projectHandler.getProjectTMById(
                                    Long.valueOf(tmId), false);
                            tms.add(tm);
                            locales = LingServerProxy.getTmCoreManager()
                                    .getTmLocales(tm);
                            TMLocales.addAll(locales);
                            tmIdString += tmId + ",";
                        }
                    }
                    else
                    {
                        ArrayList<ProjectTM> allTms = new ArrayList<ProjectTM>(
                                projectHandler.getAllProjectTMs(isSuperAdmin));
                        for (int i = 0; i < allTms.size(); i++)
                        {
                            tm = allTms.get(i);
                            if (tm.getLastTUId() > -1
                                    || WebAppConstants.TM_STATUS_CONVERTING
                                            .equals(tm.getStatus()))
                                continue;
                            tmId = String.valueOf(tm.getId());
                            tms.add(tm);
                            locales = LingServerProxy.getTmCoreManager()
                                    .getTmLocales(tm);
                            TMLocales.addAll(locales);
                            tmIdString += tmId + ",";
                        }
                    }
                    if (!tmIdString.equals(""))
                        tmIdString = tmIdString.substring(0,
                                tmIdString.length() - 1);

                    // sessionMgr.setAttribute(TM_DEFINITION, definition);
                    sessionMgr.setAttribute("tmLocales", TMLocales);
                    sessionMgr.setAttribute(TM_TM_ID, tmIdString);
                    sessionMgr.setAttribute("projectTms", tms);
                }
                catch (Exception e)
                {
                    logger.error("Error found in deleting project TM.", e);
                }
            }
            else if (action.equals(TM_ACTION_IMPORT))
            {
                if (isFromHttpGetWithoutTmId(p_request, p_response, tmId))
                    return;
                String definition = getDefination(
                        projectHandler.getProjectTMById(Long.parseLong(tmId),
                                false), false);

                sessionMgr.setAttribute(TM_DEFINITION, definition);
            }
            else if (action.equals(TM_ACTION_EXPORT))
            {
                if (isFromHttpGetWithoutTmId(p_request, p_response, tmId))
                    return;
                String definition = getDefination(
                        projectHandler.getProjectTMById(Long.parseLong(tmId),
                                false), false);

                sessionMgr.setAttribute(TM_DEFINITION, definition);
            }
            else if (action.equals(TM_ACTION_STATISTICS))
            {
                Tm tm = projectHandler.getProjectTMById(Long.parseLong(tmId),
                        false);
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
            }
            else if (action.equals(TM_ACTION_CONVERT))
            {
                // Convert selected TM from TM2 to TM3
                if (isFromHttpGetWithoutTmId(p_request, p_response, tmId))
                    return;

                sessionMgr.setAttribute(TM_TM_NAME, tmName);
                sessionMgr.setAttribute(TM_TM_ID, tmId);

                Tm3ConvertProcess convertProcess = Tm3ConvertProcess
                        .getInstance();
                long tm2Id = Long.parseLong(tmId);
                try
                {
                    ProjectTM oldTm = (ProjectTM) HibernateUtil.get(
                            ProjectTM.class, tm2Id);
                    if (oldTm != null)
                    {
                        long companyId = oldTm.getCompanyId();

                        Tm3ConvertHelper tm3Convert = new Tm3ConvertHelper(
                                companyId, oldTm);

                        convertProcess.setConvertHelper(tm3Convert);
                        convertProcess.setTm2Id(oldTm.getId());
                        convertProcess.setTm2Name(oldTm.getName());
                        if (oldTm.getLastTUId() == -1)
                        {
                            convertProcess.setTm3Id(-1);
                            convertProcess
                                    .setStatus(WebAppConstants.TM_STATUS_DEFAULT);
                        }

                        session.setAttribute("tm3Convert", tm3Convert);
                        tm3Convert.convert();
                    }
                }
                catch (Exception e)
                {
                    logger.error("Error found in tm conversion.", e);
                }

                long tmpId = convertProcess.getTm3Id();
                while (true)
                {
                    if (isNewProjectTmCreated(tmpId))
                        break;
                    Thread.sleep(1000);
                    tmpId = convertProcess.getTm3Id();
                }
            }
            else if (action.equals(TM_ACTION_CONVERT_CANCEL))
            {
                try
                {
                    logger.info("Run into cancel operation.");
                    Tm3ConvertHelper tm3ConvertHelper = (Tm3ConvertHelper) session
                            .getAttribute("tm3Convert");
                    logger.info("TM3ConvertHelper object is "
                            + tm3ConvertHelper);
                    if (tm3ConvertHelper != null)
                        tm3ConvertHelper.cancel();

                    Tm3ConvertProcess convertProcess = Tm3ConvertProcess
                            .getInstance();
                    convertProcess.setConvertedRate(5);
                }
                catch (Exception e)
                {
                    logger.error("Error in canceling conversion"
                            + e.getMessage());
                }
            }
        }
        catch (Throwable ex)
        {
            logger.error("Error found in " + action, ex);
            sessionMgr.setAttribute(TM_ERROR, ex.toString());
        }

        tms = getTMs(userId, filterSearch);
        setTableNavigation(p_request, session, tms, new ProjectTMComparator(
                uiLocale), NUM_PER_PAGE, TM_LIST, TM_KEY);

        List<Long> tmIdList = new ArrayList<Long>();
        ArrayList<ProjectTM> proceedTms = new ArrayList<ProjectTM>(tms);
        String tm3Tms = "", convertingTms = "", remoteTms = "";
        String tmpTmId = "";
        for (ProjectTM tm : proceedTms)
        {
            tmIdList.add(tm.getIdAsLong());
            tmpTmId = String.valueOf(tm.getId());
            if (tm.getTm3Id() != null)
                tm3Tms += tmpTmId + ",";
            if (tm.getLastTUId() > 0
                    || WebAppConstants.TM_STATUS_CONVERTING.equals(tm
                            .getStatus()))
                convertingTms += tmpTmId + ",";
            if (tm.getIsRemoteTm())
                remoteTms = tmpTmId + ",";
        }
        sessionMgr.setAttribute("tm3Tms", tm3Tms);
        sessionMgr.setAttribute("convertingTms", convertingTms);
        sessionMgr.setAttribute("remoteTms", remoteTms);

        HashMap<Long, String> tmIdStatusMap = getTmIdStatusMap(tmIdList);
        sessionMgr.setAttribute("tmIdStatusMap", tmIdStatusMap);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void createTM(HttpServletRequest p_request,
            SessionManager sessionMgr) throws RemoteException
    {
        List<ProjectTM> tms;
        String definition = getDefination(null, false);
        sessionMgr.setAttribute(TM_DEFINITION, definition);

        tms = new ArrayList<ProjectTM>(projectHandler.getAllProjectTMs());
        sessionMgr.setAttribute(TM_EXIST_NAMES, getExistTMNames(tms));

        FormUtil.addSubmitToken(p_request,
                FormUtil.Forms.NEW_TRANSLATION_MEMORY);

        GSEditionManagerLocal gsEditionManager = new GSEditionManagerLocal();
        Collection allGSEdition = gsEditionManager.getAllGSEdition();
        sessionMgr.setAttribute(GS_EDITION_ALL, allGSEdition);
    }

    private void cancelValidation(SessionManager sessionMgr)
    {
        TmProcessStatus tmStatus = (TmProcessStatus) sessionMgr
                .getAttribute(TM_UPLOAD_STATUS);
        if (tmStatus != null)
            tmStatus.setCanceled(true);

        sessionMgr.setAttribute(TM_UPLOAD_STATUS, null);
    }

    private void handleProcessStatus(SessionManager sessionMgr)
    {
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
    }

    private String handleFilters(HttpServletRequest p_request,
            SessionManager sessionMgr, String action)
    {
        String name = (String) p_request.getParameter("tmNameFilter");
        String company = (String) p_request.getParameter("tmCompanyFilter");

        if (!FILTER_SEARCH.equals(action)
                || p_request.getMethod().equalsIgnoreCase(
                        WebAppConstants.REQUEST_METHOD_GET))
        {
            name = (String) sessionMgr.getAttribute("tmNameFilter");
            company = (String) sessionMgr.getAttribute("tmCompanyFilter");
        }
        sessionMgr.setAttribute("tmNameFilter", name == null ? "" : name);
        sessionMgr.setAttribute("tmCompanyFilter", company == null ? ""
                : company);

        String condition = "";
        if (StringUtil.isNotEmpty(name))
        {
            name = fixQueryString(name);
            condition = "pt.name like '%" + name + "%'";
        }
        if (StringUtil.isNotEmpty(company))
        {
            company = fixQueryString(company);
            if (!StringUtil.isEmpty(condition))
                condition += " and ";
            condition += "pt.companyId in (select c.id from Company c where c.name like '%"
                    + company + "%')";
        }
        if (FILTER_SEARCH.equals(action))
        {
            // Go to page #1 if current action is filter searching.
            sessionMgr.setAttribute(TM_KEY + TableConstants.LAST_PAGE_NUM,
                    Integer.valueOf(1));
        }

        return condition;
    }

    private String fixQueryString(String s)
    {
        return s.replace("_", "\\_");
    }

    private boolean isFromHttpGetWithoutTmId(HttpServletRequest p_request,
            HttpServletResponse p_response, String tmId) throws IOException
    {
        if (StringUtil.isEmpty(tmId)
                || p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
        {
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=tm");
            return true;
        }
        else
            return false;
    }

    private String getDefination(ProjectTM p_tm, boolean p_clone)
    {
        StringBuffer result = new StringBuffer(200);

        if (p_tm == null)
        {
            result.append("<tm>")
                    .append("<name></name>")
                    .append("<domain></domain>")
                    .append("<organization></organization>")
                    .append("<description></description>")
                    .append("<indexTarget>false</indexTarget>")
                    .append("<isRemoteTm>false</isRemoteTm>")
                    .append("<gsEditionId></gsEditionId>")
                    .append("<remoteTmProfileId></remoteTmProfileId>")
                    .append("<remoteTmProfileName></remoteTmProfileName>")
                    .append("</tm>");
        }
        else
        {
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
            // for bug GBS-2547,by fan
            String desc1 = EditUtil.encodeXmlEntities(p_tm.getDescription())
                    .replaceAll("\r\n", "&lt;br/&gt;"); // for windows
            String desc2 = desc1.replaceAll("\n", "&lt;br/&gt;"); // for unix
            result.append(desc2);
            result.append("</description>");
            result.append("<indexTarget>");
            result.append(EditUtil.encodeXmlEntities(p_tm
                    .isIndexTarget() == true ? "true" : "false"));
            result.append("</indexTarget>");
            result.append("<isRemoteTm>");
            result.append(EditUtil
                    .encodeXmlEntities(p_tm.getIsRemoteTm() == true ? "true"
                            : "false"));
            result.append("</isRemoteTm>");
            result.append("<gsEditionId>");
            result.append(EditUtil
                    .encodeXmlEntities(p_tm.getGsEditionId() == -1 ? ""
                            : String.valueOf(p_tm.getGsEditionId())));
            result.append("</gsEditionId>");
            result.append("<remoteTmProfileId>");
            result.append(EditUtil.encodeXmlEntities(p_tm
                    .getRemoteTmProfileId() == -1 ? "" : String.valueOf(p_tm
                    .getRemoteTmProfileId())));
            result.append("</remoteTmProfileId>");
            result.append("<remoteTmProfileName>");
            result.append(EditUtil.encodeXmlEntities(p_tm
                    .getRemoteTmProfileName() == null ? "" : p_tm
                    .getRemoteTmProfileName()));
            result.append("</remoteTmProfileName>");
            result.append("</tm>");
        }

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
    private List<ProjectTM> getTMs(String userId, String cond)
            throws ProjectHandlerException, RemoteException
    {
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company curremtCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        // Enable TM Access Control of current company
        boolean enableTMAccessControl = curremtCompany
                .getEnableTMAccessControl();
        List<ProjectTM> tms = new ArrayList<ProjectTM>();
        boolean isSuperPM = UserUtil.isSuperPM(userId);
        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");
        boolean isSuperLP = UserUtil.isSuperLP(userId);

        ProjectHandler projectHandler;
        Collection<ProjectTM> allTMs = null;
        ArrayList<Long> tmsIds = new ArrayList<Long>();
        try
        {
            projectHandler = ServerProxy.getProjectHandler();
            allTMs = projectHandler.getAllProjectTMs(cond);
            for (ProjectTM ptm : allTMs)
            {
                tmsIds.add(ptm.getIdAsLong());
            }
        }
        catch (Exception e)
        {
            logger.error("Error found in TmMainHandler.getTMs().", e);
        }

        if ("1".equals(currentCompanyId))
        {
            List<String> companies = new ArrayList<String>();
            if (isSuperLP)
            {
                // Get all the companies the super translator worked for
                List projectList = null;
                try
                {
                    projectList = ServerProxy.getProjectHandler()
                            .getProjectsByUser(userId);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                for (Iterator it = projectList.iterator(); it.hasNext();)
                {
                    Project pj = (Project) it.next();
                    String companyId = String.valueOf(pj.getCompanyId());
                    if (!companies.contains(companyId))
                    {
                        companies.add(companyId);
                    }
                }
                for (Iterator it = allTMs.iterator(); it.hasNext();)
                {
                    ProjectTM tm = (ProjectTM) it.next();
                    String companyId = String.valueOf(tm.getCompanyId());
                    if (companies.contains(companyId))
                    {
                        tms.add(tm);
                    }
                }

            }
            else
            {
                // Super admin
                tms.addAll(allTMs);
            }
        }
        else
        {
            if (enableTMAccessControl && !isAdmin)
            {

                ProjectTMTBUsers projectTMTBUsers = new ProjectTMTBUsers();
                List tmIdList = projectTMTBUsers.getTList(userId, "TM");
                Iterator it = tmIdList.iterator();
                while (it.hasNext())
                {
                    ProjectTM tm = null;
                    try
                    {
                        tm = ServerProxy.getProjectHandler().getProjectTMById(
                                ((BigInteger) it.next()).longValue(), false);
                    }
                    catch (Exception e)
                    {
                        throw new EnvoyServletException(e);
                    }
                    if (isSuperPM)
                    {
                        if (String.valueOf(tm.getCompanyId()).equals(
                                currentCompanyId)
                                && tmsIds.contains(tm.getIdAsLong()))
                        {
                            tms.add(tm);
                        }
                    }
                    else
                    {
                        if (tmsIds.contains(tm.getIdAsLong()))
                            tms.add(tm);
                    }
                }
            }
            else
            {
                tms.addAll(allTMs);
            }
        }

        SortUtil.sort(tms, new ProjectTMComparator(Locale.getDefault()));
        return tms;
    }

    /**
     * Set exist TM names into request.
     * 
     * @param request
     * @throws ProjectHandlerException
     * @throws RemoteException
     */
    private String getExistTMNames(List<ProjectTM> tms)
            throws ProjectHandlerException, RemoteException
    {
        StringBuilder existTMNames = new StringBuilder(",");
        if (tms != null)
        {
            Iterator<ProjectTM> iterator = tms.iterator();
            while (iterator.hasNext())
            {
                existTMNames.append(iterator.next().getName() + ",");
            }
        }

        return existTMNames.toString();
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
                    edition.getHostName(), edition.getHostPort(),
                    edition.getUserName(), edition.getPassword(),
                    edition.getEnableHttps());
            String fullAccessToken = ambassador.login(edition.getUserName(),
                    edition.getPassword());
            String realAccessToken = WebServiceClientHelper
                    .getRealAccessToken(fullAccessToken);

            String strAllTmProfiles = ambassador
                    .getAllTMProfiles(realAccessToken);
            logger.debug("allTmProfiles :: " + strAllTmProfiles);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(
                    strAllTmProfiles.getBytes("UTF-8"));
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
                            logger.debug("nodeName :: " + nodeName
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
            logger.error("Fail to get file profile Id-Names by gsEditionId : "
                    + p_gsEditionId);
        }

        return results;
    }

    private void setNumberOfPerPage(HttpServletRequest req)
    {
        String pageSize = (String) req.getParameter("numOfPageSize");

        if (!StringUtil.isEmpty(pageSize))
        {
            try
            {
                NUM_PER_PAGE = Integer.parseInt(pageSize);
            }
            catch (Exception e)
            {
                NUM_PER_PAGE = Integer.MAX_VALUE;
            }
        }
    }

    /**
     * Check if the specified project TM has been existed in DB.
     * @param newProjectTmId
     * @return
     */
    private boolean isNewProjectTmCreated(long newProjectTmId)
    {
        if (newProjectTmId == -1) return false;

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            connection = DbUtil.getConnection();
            String sql = "select count(id) from project_tm where id = ?";
            ps = connection.prepareStatement(sql);
            ps.setLong(1, newProjectTmId);
            rs = ps.executeQuery();
            if (rs.next() && rs.getLong(1) > 0) {
                return true;
            }
        }
        catch (Exception ex)
        {
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }

        return false;
    }

    /**
     * Store tmId-status info into map.
     * <P>
     * As conversion related operations are in another hibernate session, to
     * avoid the session synchronization problem, use JDBC to get latest status.
     * </P>
     * 
     * @param tmIds
     * @return
     */
    public static HashMap<Long, String> getTmIdStatusMap(List<Long> tmIds)
    {
        HashMap<Long, String> result = new HashMap<Long, String>();
        if (tmIds == null || tmIds.size() == 0)
            return result;

        StringBuilder sql = new StringBuilder();
        StringBuilder tmIdList = new StringBuilder();
        for (Long tmId : tmIds)
        {
            tmIdList.append(tmId).append(",");
        }
        String in = tmIdList.toString().substring(0, tmIdList.toString().length()-1);

        sql.append("SELECT id, STATUS FROM project_tm ");
        sql.append("WHERE STATUS IS NOT NULL ");
        sql.append("AND STATUS != '' ");
        sql.append("AND id IN (").append(in).append(");");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            ps = conn.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            while (rs.next())
            {
                long id = rs.getLong(1);
                String status = rs.getString(2);
                result.put(id, status);
            }
        }
        catch (Exception e)
        {

        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }

        return result;
    }
}
