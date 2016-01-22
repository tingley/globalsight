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
package com.globalsight.connector.eloqua;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.globalsight.connector.eloqua.form.CreateEloquaForm;
import com.globalsight.connector.eloqua.form.EloquaFileFilter;
import com.globalsight.connector.eloqua.models.Alls;
import com.globalsight.connector.eloqua.models.Email;
import com.globalsight.connector.eloqua.models.LandingPage;
import com.globalsight.connector.eloqua.util.EloquaHelper;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.cxe.entity.eloqua.EloquaConnector;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.cxe.entity.filterconfiguration.ValidateException;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.EloquaObjectComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.FileUploader;
import com.globalsight.everest.webapp.pagehandler.administration.createJobs.CreateJobsMainHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

/**
 * XmldtdFilePageHandler, A page handler to produce the entry page (index.jsp)
 * for XmldtdFile management.
 */
public class EloquaCreateJobHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(EloquaCreateJobHandler.class);
    // public static final String TMP_FOLDER_NAME = "createEloquaJob_tmp";
    private final static int EMAIL = 1;
    private final static int LANDING_PAGE = 2;

    private final static String EMAIL_SET = "emailSet";
    private final static String EMAIL_SET_TOTAL = "emailSetTotal";

    private final static String LANDING_PAGE_SET = "landingPageSet";
    private final static String LANDING_PAGE_SET_TOTAL = "landingPageSetTotal";

    private final static Map<String, String> l10NToTargetLocalesMap = new HashMap<String, String>();

    @ActionHandler(action = "updateTargetLocales", formClass = "")
    public void updateTargetLocales(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        SessionManager sessionMgr = (SessionManager) request.getSession()
                .getAttribute(SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        if (user == null)
        {
            String userName = request.getParameter("userName");
            if (userName != null && !"".equals(userName))
            {
                user = ServerProxy.getUserManager().getUserByName(userName);
                sessionMgr.setAttribute(WebAppConstants.USER, user);
            }
        }
        queryTargetLocales(request, response, user);
        pageReturn();
    }

    @ActionHandler(action = "queryJobAttributes", formClass = "")
    public void queryJobAttributes(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        PrintWriter writer = response.getWriter();
        try
        {
            String l10Nid = request.getParameter("l10Nid");
            String hasAttribute = "false";
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(
                    Long.valueOf(l10Nid));
            Project p = lp.getProject();
            AttributeSet attributeSet = p.getAttributeSet();

            if (attributeSet != null)
            {
                List<Attribute> attributeList = attributeSet
                        .getAttributeAsList();
                for (Attribute attribute : attributeList)
                {
                    if (attribute.isRequired())
                    {
                        hasAttribute = "required";
                        break;
                    }
                    else
                    {
                        hasAttribute = "true";
                    }
                }
            }
            response.setContentType("text/html;charset=UTF-8");
            writer.write(hasAttribute);
        }
        catch (Exception e)
        {
            logger.error("Failed to query job attributes of project.", e);
        }
        finally
        {
            writer.close();
        }
        pageReturn();
    }

    private List subPage(List ls, int page, int perPage)
    {
        int total = ls.size();
        int start = (page - 1) * perPage;
        int end = page * perPage;
        end = end > total ? total : end;
        return ls.subList(start, end);
    }

    @ActionHandler(action = "getLandingPages", formClass = "")
    public void getLandingPages(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        int perPage = getPerPage(request);
        String type = (String) request.getParameter("type");
        HttpSession session = request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        int page = (Integer) sessionManager.getAttribute("landingpage_page");
        List<LandingPage> ps = (List<LandingPage>) sessionManager
                .getAttribute("eloquaLandingPages");
        int pageTotal = ps.size();

        switch (Integer.parseInt(type))
        {
        case 1: // first
            page = 1;
            break;
        case 2: // previous
            page--;
            break;
        case 3: // next
            page++;
            break;
        case 4: // last
            page = pageTotal / perPage;
            if (pageTotal % perPage > 0)
                page++;
            break;
        case 5: // last
            page = Integer.parseInt(request.getParameter("page"));
            break;
        case 6: // filter
            page = 1;
            EloquaFileFilter filter = getFilter(request);

            ps = loadLandingPageFromSession(sessionManager);
            ps = filter.filter(ps);
            sessionManager.setAttribute("eloquaLandingPages", ps);
            sessionManager.setAttribute("eloquaLandingPageFilter", filter);
            break;
        case 7: // update the page size
            page = 1;
            break;
        case 8: // update the page set
            page = 1;
            EloquaFileFilter filter2 = (EloquaFileFilter) sessionManager
                    .getAttribute("eloquaLandingPageFilter");
            ps = reloadLandingPages(request).getElements();
            if (filter2 != null)
            {
                ps = filter2.filter(ps);
            }

            sessionManager.setAttribute("eloquaLandingPages", ps);
            break;
        case 9: // init the page.
            page = 1;
            ps = loadLandingPageFromSession(sessionManager);
            if (ps == null)
            {
                ps = reloadLandingPages(request).getElements();
            }
            sessionManager.setAttribute("eloquaLandingPages", ps);
        default:
            break;
        }

        List<LandingPage> p1 = subPage(ps, page, perPage);

        String pageSet = getPageSetNavString("LandingPage",
                getEloquaPageSet(request, LANDING_PAGE_SET),
                getEloquaPageSet(request, LANDING_PAGE_SET_TOTAL), request);

        sessionManager.setAttribute("landingpage_page", page);
        String nav = getNavString("LandingPage", ps.size(), page, request, 1);
        String nav2 = getNavString("LandingPage", ps.size(), page, request, 2);

        JSONObject result = new JSONObject();
        result.put("nav", nav);
        result.put("nav2", nav2 + pageSet);

        JSONArray files = new JSONArray();
        for (LandingPage e : p1)
        {
            JSONObject em = new JSONObject();
            em.put("displayId", e.getDisplayId());
            em.put("name", e.getDisplayName());
            em.put("createdBy", e.getCreateBy());
            em.put("createdAt", e.getCreatedAt());
            em.put("status", e.getStatus());
            files.put(em);
        }

        result.put("files", files);
        PrintWriter writer = response.getWriter();
        writer.write(result.toString());
        writer.close();

        pageReturn();
    }

    private Alls reloadLandingPages(HttpServletRequest request)
    {
        int page = getEloquaPageSet(request, LANDING_PAGE_SET);

        HttpSession session = request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        EloquaConnector conn = (EloquaConnector) sessionManager
                .getAttribute("EloquaConnector");
        EloquaHelper helper = new EloquaHelper(conn);
        Alls all = helper.getPages(page, 1000);
        List<LandingPage> ps = all.getElements();
        EloquaObjectComparator c = new EloquaObjectComparator(
                EloquaObjectComparator.NAME, uiLocale);
        Collections.sort(ps, c);

        sessionManager.setAttribute(LANDING_PAGE_SET_TOTAL,
                all.getTotal() / 1000 + 1);

        sessionManager.setAttribute("eloquaLandingPagesNoFilter", ps);
        return all;
    }

    private Alls reloadEmails(HttpServletRequest request)
    {
        int page = getEloquaPageSet(request, EMAIL_SET);
        HttpSession session = request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        EloquaConnector conn = (EloquaConnector) sessionManager
                .getAttribute("EloquaConnector");
        EloquaHelper helper = new EloquaHelper(conn);
        Alls all = helper.getEmails(page, 1000);
        List<Email> es = (List<Email>) all.getElements();
        EloquaObjectComparator c = new EloquaObjectComparator(
                EloquaObjectComparator.NAME, uiLocale);
        Collections.sort(es, c);

        sessionManager.setAttribute(EMAIL_SET_TOTAL, all.getTotal() / 1000 + 1);
        
        sessionManager.setAttribute("eloquaEmailsNoFilter", es);

        return all;
    }

    private EloquaFileFilter getFilter(HttpServletRequest request)
    {
        EloquaFileFilter filter = new EloquaFileFilter();
        filter.setNameFilter((String) request.getParameter("nameFilter"));
        filter.setCreatedAtFilter((String) request
                .getParameter("createdAtFilter"));
        filter.setCreatedByFilter((String) request
                .getParameter("createdByFilter"));
        filter.setStatusFilter((String) request.getParameter("statusFilter"));

        return filter;
    }

    @ActionHandler(action = "getEmails", formClass = "")
    public void getEmails(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        int perPage = getPerPage(request);
        String type = (String) request.getParameter("type");
        HttpSession session = request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Integer pageInteger = (Integer) sessionManager.getAttribute("email_page");
        int page = pageInteger == null ? 0 : pageInteger;

        List<Email> es = (List<Email>) sessionManager
                .getAttribute("eloquaEmails");
        int pageTotal = es.size();

        switch (Integer.parseInt(type))
        {
        case 1: // first
            page = 1;
            break;
        case 2: // previous
            page--;
            break;
        case 3: // next
            page++;
            break;
        case 4: // last
            page = pageTotal / perPage;
            if (pageTotal % perPage > 0)
                page++;
            break;
        case 5: // last
            page = Integer.parseInt(request.getParameter("page"));
            break;
        case 6: // filter
            page = 1;
            EloquaFileFilter filter = getFilter(request);
//            Alls alls = reloadEmails(request);
            es = loadEmailFromSession(sessionManager);
            es = filter.filter(es);
            sessionManager.setAttribute("eloquaEmails", es);
            sessionManager.setAttribute("eloquaEmailFilter", filter);

            break;
        case 7: // update the page size
            page = 1;

            break;
        case 8: // update the page set
            page = 1;
            EloquaFileFilter filter2 = (EloquaFileFilter) sessionManager
                    .getAttribute("eloquaEmailFilter");
            Alls alls2 = reloadEmails(request);
            es = alls2.getElements();
            if (filter2 != null)
            {
                es = filter2.filter(es);
            }
            sessionManager.setAttribute("eloquaEmails", es);
            break;
        default:
            break;
        }

        List<Email> e1 = subPage(es, page, perPage);

        sessionManager.setAttribute("email_page", page);
        String nav = getNavString("Email", es.size(), page, request, 1);
        String nav2 = getNavString("Email", es.size(), page, request, 2);

        String pageSet = getPageSetNavString("Email",
                getEloquaPageSet(request, EMAIL_SET),
                getEloquaPageSet(request, EMAIL_SET_TOTAL), request);

        JSONObject result = new JSONObject();
        result.put("nav", nav);
        result.put("nav2", nav2 + pageSet);

        JSONArray emails = new JSONArray();
        for (Email e : e1)
        {
            JSONObject em = new JSONObject();
            em.put("displayId", e.getDisplayId());
            em.put("name", e.getDisplayName());
            em.put("createdBy", e.getCreateBy());
            em.put("createdAt", e.getCreatedAt());
            em.put("status", e.getStatus());
            emails.put(em);
        }

        result.put("emails", emails);
        // response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(result.toString());
        writer.close();

        pageReturn();
    }

    @ActionHandler(action = "createEloquaJob", formClass = "com.globalsight.connector.eloqua.form.CreateEloquaForm")
    public void createEloquaJob(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        CreateEloquaForm eForm = (CreateEloquaForm) form;
        SessionManager sessionMgr = (SessionManager) request.getSession()
                .getAttribute(SESSION_MANAGER);
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        if (user == null)
        {
            String userName = request.getParameter("userName");
            if (userName != null && !"".equals(userName))
            {
                user = ServerProxy.getUserManager().getUserByName(userName);
                sessionMgr.setAttribute(WebAppConstants.USER, user);
            }
        }

        EloquaConnector conn = (EloquaConnector) sessionMgr
                .getAttribute("EloquaConnector");
        File file = (File) sessionMgr.getAttribute("uploadAttachment");
        String[] targetLocales = request.getParameterValues("targetLocale");
        String attachmentName = request.getParameter("attachment");
        String attribute = request.getParameter("attributeString");
        String uuid = sessionMgr.getAttribute("uuid") == null ? JobImpl
                .createUuid() : (String) sessionMgr.getAttribute("uuid");
        sessionMgr.removeElement("uuid");

        CreateJobThread runnable = new CreateJobThread(user, currentCompanyId,
                conn, file, eForm, targetLocales, attachmentName, attribute,
                uuid);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();

        request.setAttribute("isCreate", true);
    }

    /**
     * Called by ajax, search target locales for files, and init the target
     * locales checkbox on the jsp
     * 
     * @param request
     * @param response
     * @param user
     * @throws IOException
     */
    private void queryTargetLocales(HttpServletRequest request,
            HttpServletResponse response, User user) throws IOException
    {
        String l10Nid = request.getParameter("l10Nid");
        if (StringUtils.isNotEmpty(l10Nid))
        {
            String targetLocalesString = l10NToTargetLocalesMap.get(l10Nid);
            if (targetLocalesString == null)
            {
                String hsql = "select wti.targetLocale from "
                        + "L10nProfileWFTemplateInfo as ltp, WorkflowTemplateInfo wti "
                        + "where wti.id = ltp.key.wfTemplateId and ltp.key.l10nProfileId = "
                        + l10Nid
                        + " and ltp.isActive = 'Y' and wti.isActive = 'Y' "
                        + "order by wti.targetLocale.language";
                List<?> localeList = HibernateUtil.search(hsql);

                if (localeList != null)
                {
                    targetLocalesString = this.initTargetLocaleSelect(
                            localeList, user);
                    l10NToTargetLocalesMap.put(l10Nid, targetLocalesString);
                }
                else
                {
                    targetLocalesString = "";
                }
            }

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(targetLocalesString);
            writer.close();
        }
    }

    /**
     * Init the checkbox of target locales. The pattern is fr_FR(French_France).
     * 
     * @param localeList
     * @param user
     * @param checked
     */
    private String initTargetLocaleSelect(List<?> localeList, User user)
    {
        StringBuffer sb = new StringBuffer();
        Locale locale = this.getUserLocale(user);
        for (int i = 0; i < localeList.size(); i++)
        {
            GlobalSightLocale gsl = (GlobalSightLocale) localeList.get(i);
            sb.append("<div class='locale'>");
            sb.append("<input type='checkbox' name='targetLocale' value='"
                    + gsl.getId() + "' checked='true'>&nbsp;");
            sb.append(gsl.getLanguage() + "_" + gsl.getCountry() + " ("
                    + gsl.getDisplayLanguage(locale) + "_"
                    + gsl.getDisplayCountry(locale) + ")");
            sb.append("</div>");
        }
        return sb.toString();
    }

    /**
     * Get default UI locale information for specified user
     * 
     * @param user
     *            User information
     * @return Locale Default UI locale for the specified user
     */
    private Locale getUserLocale(User user)
    {
        String dl = null;
        if (user != null)
        {
            dl = user.getDefaultUILocale();
        }
        if (dl == null)
            return new Locale("en", "US");
        else
        {
            try
            {
                String language = dl.substring(0, dl.indexOf("_"));
                String country = dl.substring(dl.indexOf("_") + 1);
                country = (country == null) ? "" : country;

                return new Locale(language, country);
            }
            catch (Exception e)
            {
                return new Locale("en", "US");
            }
        }
    }

    @ActionHandler(action = "uploadAttachment", formClass = "")
    public void uploadAttachment(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Upload attachment...");
        FileUploader uploader = new FileUploader();
        File file = uploader.upload(request);

        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute("uploadAttachment", file);

        PrintWriter writer = response.getWriter();
        writer.write("<script type='text/javascript'>window.parent.addAttachment(' ')</script>;");
        pageReturn();

        logger.debug("Upload attachment finished.");
    }

    @ActionHandler(action = "removeAttachment", formClass = "")
    public void removeAttachment(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Remove attachment...");
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        File file = (File) sessionMgr.getAttribute("uploadAttachment");
        if (file != null)
        {
            file.delete();
            sessionMgr.setAttribute("uploadAttachment", null);
        }

        logger.debug("Remove attachment finished.");
    }

    @ActionHandler(action = "getFileProfile", formClass = "")
    public void getFileProfile(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ServletOutputStream out = response.getOutputStream();
        try
        {
            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            HttpSession session = request.getSession(false);
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(SESSION_MANAGER);
            User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
            if (user == null)
            {
                String userName = request.getParameter("userName");
                if (userName != null && !"".equals(userName))
                {
                    user = ServerProxy.getUserManager().getUserByName(userName);
                    sessionMgr.setAttribute(WebAppConstants.USER, user);
                }
            }

            ArrayList<FileProfileImpl> fileProfileListOfUser = new ArrayList<FileProfileImpl>();
            List<String> extensionList = new ArrayList<String>();
            extensionList.add("html");
            List<FileProfileImpl> fileProfileListOfCompany = (List) ServerProxy
                    .getFileProfilePersistenceManager()
                    .getFileProfilesByExtension(extensionList,
                            Long.valueOf(currentCompanyId));
            SortUtil.sort(fileProfileListOfCompany, new Comparator<Object>()
            {
                public int compare(Object arg0, Object arg1)
                {
                    FileProfileImpl a0 = (FileProfileImpl) arg0;
                    FileProfileImpl a1 = (FileProfileImpl) arg1;
                    return a0.getName().compareToIgnoreCase(a1.getName());
                }
            });

            List projectsOfCurrentUser = ServerProxy.getProjectHandler()
                    .getProjectsByUser(user.getUserId());

            for (FileProfileImpl fp : fileProfileListOfCompany)
            {
                Project fpProj = getProject(fp);
                // get the project and check if it is in the group of
                // user's projects
                if (projectsOfCurrentUser.contains(fpProj))
                {
                    fileProfileListOfUser.add(fp);
                }
            }

            List l = new ArrayList();
            for (FileProfileImpl fp : fileProfileListOfUser)
            {
                Map m = new HashMap();
                m.put("id", fp.getId());
                m.put("name", fp.getName());
                m.put("lid", fp.getL10nProfileId());
                l.add(m);
            }

            out.write(JsonUtil.toJson(l).getBytes("UTF-8"));
        }
        catch (ValidateException ve)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            String s = "({\"error\" : "
                    + JsonUtil.toJson(ve.getMessage(bundle)) + "})";
            out.write(s.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            String s = "({\"error\" : " + JsonUtil.toObjectJson(e.getMessage())
                    + "})";
            out.write(s.getBytes("UTF-8"));
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }

    /**
     * Get the project that the file profile is associated with.
     * 
     * @param p_fp
     *            File profile information
     * @return Project Project information which is associated with specified
     *         file profile
     */
    private Project getProject(FileProfile p_fp)
    {
        Project p = null;
        try
        {
            long l10nProfileId = p_fp.getL10nProfileId();
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(
                    l10nProfileId);
            p = lp.getProject();
        }
        catch (Exception e)
        {
            logger.error(
                    "Failed to get the project that file profile "
                            + p_fp.toString() + " is associated with.", e);
        }
        return p;
    }

    private ArrayList<FileProfileImpl> getAllFileProfile(
            HttpServletRequest request) throws Exception
    {
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        if (user == null)
        {
            String userName = request.getParameter("userName");
            if (userName != null && !"".equals(userName))
            {
                user = ServerProxy.getUserManager().getUserByName(userName);
                sessionMgr.setAttribute(WebAppConstants.USER, user);
            }
        }

        ArrayList<FileProfileImpl> fileProfileListOfUser = new ArrayList<FileProfileImpl>();
        List<String> extensionList = new ArrayList<String>();
        extensionList.add("html");
        List<FileProfileImpl> fileProfileListOfCompany = (List) ServerProxy
                .getFileProfilePersistenceManager().getFileProfilesByExtension(
                        extensionList, Long.valueOf(currentCompanyId));
        SortUtil.sort(fileProfileListOfCompany, new Comparator<Object>()
        {
            public int compare(Object arg0, Object arg1)
            {
                FileProfileImpl a0 = (FileProfileImpl) arg0;
                FileProfileImpl a1 = (FileProfileImpl) arg1;
                return a0.getName().compareToIgnoreCase(a1.getName());
            }
        });

        List projectsOfCurrentUser = ServerProxy.getProjectHandler()
                .getProjectsByUser(user.getUserId());

        for (FileProfileImpl fp : fileProfileListOfCompany)
        {
            Project fpProj = getProject(fp);
            // get the project and check if it is in the group of
            // user's projects
            if (projectsOfCurrentUser.contains(fpProj))
            {
                fileProfileListOfUser.add(fp);
            }
        }

        return fileProfileListOfUser;
    }
    
    private List<Email> loadEmailFromSession(SessionManager sessionManager)
    {
        return (List<Email>) sessionManager.getAttribute("eloquaEmailsNoFilter");
    }
    
    private List<LandingPage> loadLandingPageFromSession(SessionManager sessionManager)
    {
        return (List<LandingPage>) sessionManager.getAttribute("eloquaLandingPagesNoFilter");
    }

    /**
     * Get list of all rules.
     */
    private void dataForTable(HttpServletRequest request)
            throws GeneralException
    {
        HttpSession session = request.getSession();
        int perPage = getPerPage(request);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        
        List<Email> es;
        List<LandingPage> ps;
        
        // is after create job.
        if (request.getAttribute("isCreate") != null || request.getParameter("isCancel") != null)
        {
            es = loadEmailFromSession(sessionManager);
            ps = loadLandingPageFromSession(sessionManager);
            
            if (ps == null)
                ps = new ArrayList();
        }
        else
        {
            Alls alls = reloadEmails(request);
            es = alls.getElements();
            
            int n = alls.getTotal() / 1000 + 1;
            sessionManager.setAttribute(EMAIL_SET_TOTAL, n);
            sessionManager.setAttribute(EMAIL_SET, 1);
            
            Alls pageAlls = new Alls();
            ps = new ArrayList();
            int n2 = pageAlls.getTotal() / 1000 + 1;
            sessionManager.setAttribute(LANDING_PAGE_SET_TOTAL, n2);
            sessionManager.setAttribute(LANDING_PAGE_SET, 1);
        }
        
        sessionManager.setAttribute("eloquaLandingPageFilter", null);
        sessionManager.setAttribute("eloquaEmailFilter", null);

        sessionManager.setAttribute("eloquaEmails", es);
        sessionManager.setAttribute("eloquaLandingPages", ps);

        sessionManager.setAttribute("email_page", 1);
        request.setAttribute("email_page", 1);
        sessionManager.setAttribute("landingpage_page", 1);
        request.setAttribute("landingpage_page", 1);

        List<Email> e1 = es.size() > perPage ? es.subList(0, perPage) : es;
        List<LandingPage> p1 = ps.size() > perPage ? ps.subList(0, perPage)
                : ps;

        String pageSet = getPageSetNavString("Email",
                getEloquaPageSet(request, EMAIL_SET),
                getEloquaPageSet(request, EMAIL_SET_TOTAL), request);

        String pageSet2 = getPageSetNavString("LandingPage",
                getEloquaPageSet(request, LANDING_PAGE_SET),
                getEloquaPageSet(request, LANDING_PAGE_SET_TOTAL), request);

        request.setAttribute("email_nav",
                getNavString("Email", es.size(), 1, request, 1));
        request.setAttribute("email_nav2",
                getNavString("Email", es.size(), 1, request, 2) + pageSet);
        request.setAttribute("email_list", e1);

        request.setAttribute("page_nav",
                getNavString("LandingPage", ps.size(), 1, request, 1));
        request.setAttribute("page_nav2",
                getNavString("LandingPage", ps.size(), 1, request, 2)
                        + pageSet2);
        request.setAttribute("page_list", p1);

        Integer creatingJobsNum = getCreatingJobsNum();
        request.setAttribute("creatingJobsNum", creatingJobsNum);
    }

    public Integer getCreatingJobsNum()
    {
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Integer creatingJobsNum = null;
        try
        {
            String sql = CreateJobsMainHandler.CREATING_JOBS_NUM_SQL;
            boolean isSuperCompany = CompanyWrapper
                    .isSuperCompany(currentCompanyId);
            if (isSuperCompany)
            {
                creatingJobsNum = HibernateUtil.count(sql);
            }
            else
            {
                creatingJobsNum = HibernateUtil.count(sql
                        + " and COMPANY_ID = " + currentCompanyId);
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to get createingJobsNum.", e);
            // not blocking the following processes.
        }
        return creatingJobsNum;
    }

    private int getEloquaPageSet(HttpServletRequest request, String key)
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        Integer orgSize = (Integer) sessionManager.getAttribute(key);
        int size = orgSize == null ? 1 : orgSize;
        String numOfPerPage = request.getParameter(key);
        if (StringUtil.isNotEmpty(numOfPerPage))
        {
            try
            {
                size = Integer.parseInt(numOfPerPage);
            }
            catch (Exception e)
            {
                size = 1;
            }

            sessionManager.setAttribute(key, size);
        }

        return size;
    }

    private int getPerPage(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        Integer orgSize = (Integer) session.getAttribute("eloquaFilePageSize");
        int size = orgSize == null ? 10 : orgSize;
        String numOfPerPage = request.getParameter("eloquaFilePageSize");
        if (StringUtil.isNotEmpty(numOfPerPage))
        {
            try
            {
                size = Integer.parseInt(numOfPerPage);
            }
            catch (Exception e)
            {
                size = Integer.MAX_VALUE;
            }

            session.setAttribute("eloquaFilePageSize", size);
        }

        return size;
    }

    private void addDisplayString(String type, StringBuffer sb, int perPage)
    {
        List<Integer> os = new ArrayList<Integer>();
        os.add(10);
        os.add(20);
        os.add(50);

        sb.append("Display #: ");
        sb.append("<select id='numOfPageSize' onchange='changePageSize(\""
                + type + "\", this.value);'>");

        boolean found = false;
        for (Integer o : os)
        {
            if (o.intValue() == perPage)
            {
                sb.append("<option value='" + o + "' selected>" + o
                        + "</option>");
                found = true;
            }
            else
            {
                sb.append("<option value='" + o + "'>" + o + "</option>");
            }
        }

        if (!found)
        {
            sb.append("<option value='" + Integer.MAX_VALUE
                    + "' selected>All</option>");
        }
        else
        {
            sb.append("<option value='" + Integer.MAX_VALUE + "'>All</option>");
        }

        sb.append("</select>&nbsp;&nbsp;");
    }

    private String getPageSetNavString(String type, int set, int setTotal,
            HttpServletRequest request)
    {
        if (setTotal <= 1)
            return "";

        ResourceBundle bundle = PageHandler.getBundle(request.getSession());

        StringBuilder url = new StringBuilder();
        url.append("<div style=\"float:right; padding-top:15px;\">");

        for (int i = 1; i <= setTotal; i++)
        {
            int start = (i - 1) * 1000 + 1;
            int end = i * 1000;

            String startString = start == 1 ? "0001" : "" + start;

            if (i == set)
            {
                url.append(bundle.getString("lb_asset")).append(" ");
                url.append(startString).append(" - ").append(end)
                        .append("<br><br> ");
            }
            else
            {
                url.append(
                        "<a href=\"#\" onclick=\"toPage" + type + "Set(" + i
                                + ")\">").append(bundle.getString("lb_asset"))
                        .append(" ").append(startString).append(" - ")
                        .append(end).append("</a> <br><br> ");
            }
        }

        url.append("</div>");
        return url.toString();
    }

    /**
     * Get the nav string
     * 
     * @param type
     * @param total
     * @param pageNum
     * @param request
     * @param flag
     *            1. Displaying 1 - 2 of 2 First | Previous | 1 | Next | Last 2.
     *            Display #: [10, 20, 50, All] First | Previous | 1 | Next |
     *            Last
     * @return
     */
    private String getNavString(String type, int total, int pageNum,
            HttpServletRequest request, int flag)
    {
        int PAGES_EACH_SIDE = 3;
        StringBuffer sb = new StringBuffer();
        try
        {
            int perPage = getPerPage(request);
            int numPages = total / perPage;
            if (total % perPage > 0)
            {
                numPages++;
            }

            int possibleTo = pageNum * perPage;
            int to = possibleTo > total ? total : possibleTo;
            int from = ((pageNum - 1) * perPage) + 1;

            // DIV start
            sb.append("<div align='right'>");

            ResourceBundle bundle = PageHandler.getBundle(request.getSession());

            if (flag == 1)
            {
                // Print "Displaying x to y of z"
                Object[] args = { new Integer(from), new Integer(to),
                        new Integer(total) };
                sb.append(MessageFormat.format(
                        bundle.getString("lb_displaying_records"), args));
                sb.append("&nbsp;&nbsp;");
            }
            else if (flag == 2)
            {
                addDisplayString(type, sb, perPage);
            }

            // The "First" and "Previous" links
            if (pageNum == 1)
            {
                sb.append(bundle.getString("lb_first"));
                sb.append(" | ");
                sb.append(bundle.getString("lb_previous"));
            }
            else
            {
                StringBuilder url = new StringBuilder();
                url.append("<a href=\"#\" onclick=\"get" + type + "s(1)\">")
                        .append(bundle.getString("lb_first")).append("</a> | ");
                url.append("<a href=\"#\" onclick=\"get" + type + "s(2)\">")
                        .append(bundle.getString("lb_previous")).append("</a>");
                sb.append(url.toString());
            }
            sb.append(" | ");

            // Print out the paging numbers
            for (int i = 1; i <= numPages; i++)
            {
                if (((pageNum <= PAGES_EACH_SIDE) && (i <= PAGES_EACH_SIDE * 2))
                        || (((numPages - pageNum) <= PAGES_EACH_SIDE) && (i > (numPages - PAGES_EACH_SIDE * 2)))
                        || ((i <= (pageNum + PAGES_EACH_SIDE)) && (i >= (pageNum - PAGES_EACH_SIDE))))
                {
                    // Don't hyperlink the page you're on
                    if (i == pageNum)
                    {
                        sb.append("<b>" + i + "</b>");
                    }
                    // Hyperlink the other pages
                    else
                    {
                        StringBuilder url = new StringBuilder();
                        url.append(
                                "<a class=standardHREF href=\"#\" onclick=\"toPage"
                                        + type + "(" + i + ")\">").append(i)
                                .append("</a>");
                        sb.append(url.toString());
                    }
                    sb.append(" ");
                }
            }
            sb.append("| ");

            // The "Next" and "Last" links
            if (to >= total)
            {
                sb.append(bundle.getString("lb_next"));
                sb.append(" | ");
                sb.append(bundle.getString("lb_last"));
            }
            else
            {
                StringBuilder url = new StringBuilder();
                url.append("<a href=\"#\" onclick=\"get" + type + "s(3)\">")
                        .append(bundle.getString("lb_next")).append("</a> | ");
                url.append("<a href=\"#\" onclick=\"get" + type + "s(4)\">")
                        .append(bundle.getString("lb_last")).append("</a>");
                sb.append(url.toString());
            }

            // DIV end
            sb.append("</div>");
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        return sb.toString();
    }

    @ActionHandler(action = "preview", formClass = "")
    public void preview(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        response.setCharacterEncoding("utf-8");
        String id = request.getParameter("id");
        if (id != null)
        {
            SessionManager sessionMgr = (SessionManager) request.getSession()
                    .getAttribute(SESSION_MANAGER);
            EloquaConnector conn = (EloquaConnector) sessionMgr
                    .getAttribute("EloquaConnector");
            EloquaHelper helper = new EloquaHelper(conn);

            if (id.startsWith("e"))
            {
                Email e = helper.getEmail(id.substring(1));

                JSONObject result = new JSONObject();
                result.put("subject", e.getSubject());
                result.put("from", e.getString("senderEmail"));
                result.put("name", e.getDisplayName());
                result.put("html", e.getHtml());

                String createAt = "--";
                if (e.getJson().has("createdAt"))
                {
                    String d = e.getString("createdAt");
                    DateFormat dateFormat = new SimpleDateFormat(
                            "MM/dd/yyyy HH:mm:ss");
                    long t = Long.parseLong(d);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(t * 1000);

                    createAt = dateFormat.format(calendar.getTime());
                }

                result.put("createAt", createAt);

                PrintWriter writer = response.getWriter();
                writer.write(result.toString());
                writer.close();
            }
            else if (id.startsWith("p"))
            {
                LandingPage e = helper.getLandingPage(id.substring(1));

                JSONObject result = new JSONObject();
                result.put("name", e.getDisplayName());
                result.put("html", e.getHtml());

                String createAt = "--";
                if (e.getJson().has("createdAt"))
                {
                    String d = e.getString("createdAt");
                    DateFormat dateFormat = new SimpleDateFormat(
                            "MM/dd/yyyy HH:mm:ss");

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(d));

                    createAt = dateFormat.format(calendar.getTime());
                }

                result.put("createAt", createAt);

                PrintWriter writer = response.getWriter();
                writer.write(result.toString());
                writer.close();
            }
        }

        pageReturn();
    }

    @ActionHandler(action = "preview2", formClass = "")
    public void preview2(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        response.setCharacterEncoding("utf-8");
        String id = request.getParameter("id");
        if (id != null)
        {
            SessionManager sessionMgr = (SessionManager) request.getSession()
                    .getAttribute(SESSION_MANAGER);
            EloquaConnector conn = (EloquaConnector) sessionMgr
                    .getAttribute("EloquaConnector");
            EloquaHelper helper = new EloquaHelper(conn);

            StringBuffer sb = new StringBuffer();
            String html = "";
            if (id.startsWith("e"))
            {
                Email e = helper.getEmail(id.substring(1));
                html = e.getHtml();

                String createAt = "--";
                if (e.getJson().has("createdAt"))
                {
                    String d = e.getString("createdAt");
                    DateFormat dateFormat = new SimpleDateFormat(
                            "MM/dd/yyyy HH:mm:ss");
                    long t = Long.parseLong(d);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(t * 1000);

                    createAt = dateFormat.format(calendar.getTime());
                }

                String subject = e.getSubject();
                subject = subject == null ? "" : subject;
                sb.append("<div id=\"previewAttribute\" style=\"width:750px;background-color:rgb(240, 248, 240) ;border-bottom: 1px dashed #d7d7d7; border-left: 0px; border-right: 0px; border-top: opx;\">");
                sb.append("<table class=standardText cellpadding=\"5\">");
                sb.append("<tr style=\"font-size:150%\">");
                sb.append("<td colspan=\"2\" id=\"previewSubject\">" + subject
                        + "</td>");
                sb.append("</tr>");
                sb.append("<tr style=\"color:#999;\">");
                sb.append("<td>" + bundle.getString("lb_from_address")
                        + " : </td>");
                sb.append("<td id=\"previewFrom\">"
                        + e.getString("senderEmail") + "</td>");
                sb.append("</tr>");
                sb.append("<tr style=\"color:#999;\">");
                sb.append("<td>" + bundle.getString("lb_create_at")
                        + " : </td>");
                sb.append("<td id=\"previewCreateAt\">" + createAt + "</td>");
                sb.append("</tr>");
                sb.append("</table>");
                sb.append("</div>");
            }
            else if (id.startsWith("p"))
            {
                LandingPage e = helper.getLandingPage(id.substring(1));
                html = e.getHtml();
            }

            sb.append("<div id=\"previewContent\" style=\"width:750px\">");
            sb.append(html);
            sb.append("</div>");
            sb.append("<script type='text/javascript'>window.parent.doPreview(' ')</script>;");
            PrintWriter writer = response.getWriter();
            writer.write(sb.toString());
            writer.close();
        }

        pageReturn();
    }

    /**
     * Set languages on the page according to locales
     * 
     * @param request
     * @param bundle
     */
    private void setLable(HttpServletRequest request, ResourceBundle bundle)
    {
        setLableToJsp(request, bundle, "lb_name");// name
        setLableToJsp(request, bundle, "lb_status");// status
        setLableToJsp(request, bundle, "lb_size");// size
        setLableToJsp(request, bundle, "lb_file_profile");// file profile
        setLableToJsp(request, bundle, "lb_target_locales");// target locales
        setLableToJsp(request, bundle, "lb_create_job");// create job
        setLableToJsp(request, bundle, "lb_create_job_without_java");// create
                                                                     // job(zip
                                                                     // only)
        setLableToJsp(request, bundle, "lb_add_files");// add files
        setLableToJsp(request, bundle, "lb_browse");// Browse
        setLableToJsp(request, bundle, "lb_cancel");// Cancel
        setLableToJsp(request, bundle, "jsmsg_customer_job_name");
        setLableToJsp(request, bundle, "jsmsg_invalid_job_name_1");
        setLableToJsp(request, bundle,
                "jsmsg_choose_file_profiles_for_all_files");
        setLableToJsp(request, bundle, "lb_import_select_target_locale");
        setLableToJsp(request, bundle, "jsmsg_customer_job_name");
        setLableToJsp(request, bundle, "jsmsg_customer_comment");
        setLableToJsp(request, bundle, "jsmsg_comment_must_be_less");
        setLableToJsp(request, bundle, "lb_total");// Total
        setLableToJsp(request, bundle, "lb_uploaded");
        setLableToJsp(request, bundle, "msg_failed");
        setLableToJsp(request, bundle, "msg_job_add_files");
        setLableToJsp(request, bundle, "msg_job_folder_confirm");
        setLableToJsp(request, bundle, "help_create_job");
        setLableToJsp(request, bundle, "msg_job_create_empty_file");
        setLableToJsp(request, bundle, "msg_job_create_exist");
        setLableToJsp(request, bundle, "msg_job_create_large_file");
        setLableToJsp(request, bundle, "highest");
        setLableToJsp(request, bundle, "major");
        setLableToJsp(request, bundle, "normal");
        setLableToJsp(request, bundle, "lower");
        setLableToJsp(request, bundle, "lowest");
        setLableToJsp(request, bundle, "lb_attachment");
        setLableToJsp(request, bundle, "lb_reference_file");
        setLableToJsp(request, bundle, "lb_uploaded_files");
        setLableToJsp(request, bundle, "lb_clear_profile");
        setLableToJsp(request, bundle, "msg_job_attachment_uploading");
        setLableToJsp(request, bundle, "lb_create_job_uploaded_files_tip");
        setLableToJsp(request, bundle, "lb_create_job_clean_map_tip");
        setLableToJsp(request, bundle, "lb_create_job_add_file_tip");
        setLableToJsp(request, bundle, "lb_create_job_browse_tip");
        setLableToJsp(request, bundle, "lb_job_creating");
        setLableToJsp(request, bundle, "lb_jobs_creating");
        setLableToJsp(request, bundle, "lb_job_attributes");
    }

    private void setLableToJsp(HttpServletRequest request,
            ResourceBundle bundle, String msg)
    {
        String label = bundle.getString(msg);
        request.setAttribute(msg, label);
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        this.setLable(request, bundle);
        dataForTable(request);

        try
        {
            ArrayList<FileProfileImpl> fps = getAllFileProfile(request);
            StringBuffer s = new StringBuffer();
            if (fps.size() != 1)
            {
                s.append("<option class=\"-1\" value=\"-1\"></option>");
            }

            for (FileProfileImpl fp : fps)
            {
                s.append("<option class=\"").append(fp.getL10nProfileId())
                        .append("\" value=\"").append(fp.getId()).append("\"");

                if (fps.size() == 1)
                    s.append(" selected=\"selected\" ");

                s.append(">");
                s.append(fp.getName()).append("</option>");
            }
            request.setAttribute("fps", s.toString());
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        response.setCharacterEncoding("utf-8");
    }
}
