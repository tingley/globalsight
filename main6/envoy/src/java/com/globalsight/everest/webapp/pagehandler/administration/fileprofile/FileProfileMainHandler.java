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
package com.globalsight.everest.webapp.pagehandler.administration.fileprofile;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Transaction;

import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.fileprofile.FileprofileVo;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.QAFilterManager;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.entity.knownformattype.KnownFormatTypeImpl;
import com.globalsight.cxe.entity.xmldtd.XmlDtdImpl;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManagerWLRemote;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.cvsconfig.CVSFileProfile;
import com.globalsight.everest.cvsconfig.CVSFileProfileManagerLocal;
import com.globalsight.everest.projecthandler.FileProfileSearchParameters;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.FileProfileComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.ling.docproc.extractor.xml.XmlFilterHelper;
import com.globalsight.log.OperationLog;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

/**
 * FileProfileMainHandler, A page handler to produce the entry page (index.jsp)
 * for DataSources management.
 * 
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */
public class FileProfileMainHandler extends PageHandler
{
    static private final Logger logger = Logger
            .getLogger(FileProfileMainHandler.class);
    private static int NUM_PER_PAGE = 10;
    String m_userId;

    /**
     * Invokes this PageHandler
     * 
     * @param pageDescriptor
     *            the page desciptor
     * @param request
     *            the original request sent from the browser
     * @param response
     *            the original response object
     * @param context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String action = p_request.getParameter("action");

        try
        {
            if (FileProfileConstants.CANCEL.equals(action))
            {
                clearSessionExceptTableInfo(session,
                        FileProfileConstants.FILEPROFILE_KEY);
                // sessionMgr.setAttribute("searchParams", params);
            }
            else if (FileProfileConstants.CREATE.equals(action))
            {
                if (FormUtil.isNotDuplicateSubmisson(p_request,
                        FormUtil.Forms.NEW_FILE_PROFILE))
                {
                    createFileProfile(p_request);
                }
            }
            else if (FileProfileConstants.EDIT.equals(action))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=fileprofiles");
                    return;
                }
                modifyFileProfile(p_request);
                clearSessionExceptTableInfo(session,
                        FileProfileConstants.FILEPROFILE_KEY);
            }
            else if (FileProfileConstants.REMOVE.equals(action))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=fileprofiles");
                    return;
                }
                removeFileProfile(p_request, session);
            }
            if ((p_request.getParameter("linkName") != null && !p_request
                    .getParameter("linkName").startsWith("self")))
            {
                sessionMgr.clear();
            }
            handleFilters(p_request, sessionMgr, action);
            dataForTable(p_request, session);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Before being able to create a File Profile, certain objects must exist.
     * Check that here.
     */
    private void checkPreReqData(HttpServletRequest p_request,
            HttpSession p_session, Hashtable p_l10nProfilePairs)
            throws EnvoyServletException
    {
        if (p_l10nProfilePairs == null || p_l10nProfilePairs.size() < 1)
        {
            ResourceBundle bundle = getBundle(p_session);
            StringBuffer message = new StringBuffer();
            message.append(bundle.getString("msg_prereq_warning_1"));
            message.append(":  ");
            message.append(bundle.getString("lb_loc_profile"));
            message.append(".  ");
            message.append(bundle.getString("msg_prereq_warning_2"));

            p_request.setAttribute("preReqData", message.toString());
        }
    }

    private void createFileProfile(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        FileProfileImpl fp = new FileProfileImpl();

        setRequestParams(p_request, fp);

        try
        {
            // If the known format is XLZ, it need to process more
            if (fp.getKnownFormatTypeId() == 48)
                processXLZFormat(p_request, fp);

            ServerProxy.getFileProfilePersistenceManager()
                    .createFileProfile(fp);
            OperationLog.log(m_userId, OperationLog.EVENT_ADD,
                    OperationLog.COMPONET_FILE_PROFILE, fp.getName());

            updateXslPath(p_request, fp);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private void updateXslPath(HttpServletRequest p_request, FileProfileImpl fp)
            throws Exception
    {
        String xslPath = p_request.getParameter("tmpXslPath");
        StringBuffer updatedPath = new StringBuffer("");

        if (xslPath != null && !xslPath.equals(""))
        {
            File file = new File(xslPath);

            updatedPath
                    .append(xslPath.substring(0, xslPath.lastIndexOf("~TMP")));
            updatedPath.append(fp.getId());

            File updatedFile = new File(updatedPath.toString());
            if (updatedFile.exists())
            {
                File[] files = updatedFile.listFiles();
                for (int i = 0; i < files.length; i++)
                    files[i].delete();
                updatedFile.delete();
            }

            if (!file.getParentFile().renameTo(updatedFile))
            {
                logger.error("Failed to rename XSL temporary path.");
            }

        }

    }

    private void removeRelevantXslFile(String id) throws Exception
    {

        String xslPath = AmbFileStoragePathUtils.getXslDir().getPath() + "/"
                + id;
        File xslFiles = new File(xslPath);
        if (xslFiles.exists())
        {
            File[] files = xslFiles.listFiles();
            for (int i = 0; i < files.length; i++)
                files[i].delete();
            xslFiles.delete();
        }

    }

    private void modifyFileProfile(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager) p_request.getSession()
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        FileProfileImpl fp = (FileProfileImpl) sessionMgr
                .getAttribute("fileprofile");
        FileProfileImpl newFp = new FileProfileImpl();

        Transaction tx = HibernateUtil.getTransaction();
        try
        {
            fp = HibernateUtil.get(FileProfileImpl.class, fp.getId());
            fp.setIsActive(false);

            setRequestParams(p_request, newFp);
            if (newFp.getKnownFormatTypeId() == 48)
                processXLZFormat(p_request, newFp);

            HibernateUtil.saveOrUpdate(newFp);
            fp.setNewId(newFp.getId());
            String oldName = fp.getName();
            String newName = newFp.getName();
            fp.setName(newName);
            HibernateUtil.saveOrUpdate(fp);

            if (!newName.equals(oldName))
            {
                String hql = "from FileProfileImpl f where f.newId = :oId";
                Map map = new HashMap();
                map.put("oId", fp.getId());

                List<FileProfileImpl> fps = (List<FileProfileImpl>) HibernateUtil
                        .search(hql, map);
                for (FileProfileImpl f : fps)
                {
                    f.setName(newName);
                    f.setNewId(newFp.getId());
                }

                HibernateUtil.saveOrUpdate(fps);
            }

            HibernateUtil.commit(tx);
            OperationLog.log(m_userId, OperationLog.EVENT_EDIT,
                    OperationLog.COMPONET_FILE_PROFILE, newName);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            HibernateUtil.rollback(tx);
            throw new EnvoyServletException(e);
        }

        boolean isRmoveXsl = "true".equals(p_request
                .getParameter("removeXslFile"));

        if (!isRmoveXsl)
        {
            String xslPath = AmbFileStoragePathUtils.getXslDir().getPath()
                    + "/" + fp.getId();
            String newXslPath = AmbFileStoragePathUtils.getXslDir().getPath()
                    + "/" + newFp.getId();
            File xslFiles = new File(xslPath);

            try
            {
                if (xslFiles.exists())
                {
                    File newXslFile = new File(newXslPath);
                    FileUtil.copyFolder(xslFiles, newXslFile);
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
                throw new EnvoyServletException(e);
            }
        }

        try
        {
            updateXslPath(p_request, newFp);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new EnvoyServletException(e);
        }
    }

    private void processXLZFormat(HttpServletRequest p_request,
            FileProfileImpl p_fp)
    {
        FileProfileImpl xlzRefFp = null;
        try
        {
            FileProfilePersistenceManagerWLRemote fpManager = ServerProxy
                    .getFileProfilePersistenceManager();

            // XLZ file profile
            xlzRefFp = new FileProfileImpl();
            setRequestParams(p_request, xlzRefFp);

            String fpName = xlzRefFp.getName();
            xlzRefFp.setName(fpName + "_RFP");
            xlzRefFp.setIsActive(false);
            xlzRefFp.setKnownFormatTypeId(39);

            // Set file extensions
            ArrayList exts = new ArrayList(fpManager.getAllFileExtensions());
            Vector tmpExts = new Vector();
            FileExtensionImpl ext = null;
            for (int i = 0; i < exts.size(); i++)
            {
                ext = (FileExtensionImpl) exts.get(i);
                if ("xlf".equalsIgnoreCase(ext.getName())
                        || "xliff".equalsIgnoreCase(ext.getName()))
                {
                    tmpExts.add(ext.getIdAsLong());
                }
            }
            xlzRefFp.setFileExtensionIds(tmpExts);
            fpManager.createFileProfile(xlzRefFp);
            p_fp.setReferenceFP(xlzRefFp.getId());
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    private void removeFileProfile(HttpServletRequest p_request,
            HttpSession p_session) throws EnvoyServletException
    {
        try
        {
            String idString = (String) p_request.getParameter(RADIO_BUTTON);
            if (idString != null)
            {
                String[] idarray = idString.split(" ");
                for(int i=0;i<idarray.length;i++){
                String id=idarray[i].split(",")[0];
				FileProfileImpl fp = (FileProfileImpl) ServerProxy
						.getFileProfilePersistenceManager().getFileProfileById(
								Long.parseLong(id), true);

                // CVSFileProfileManagerLocal cvsFPManager = new
                // CVSFileProfileManagerLocal();
                // cvsFPManager.removeByFileProfileId(fp.getId());
                if (fp.isActive())
                {
                    ServerProxy.getFileProfilePersistenceManager()
                            .deleteFileProfile(fp);
                    OperationLog.log(m_userId, OperationLog.EVENT_DELETE,
                            OperationLog.COMPONET_FILE_PROFILE, fp.getName());
                }

                // Don't really remove
                // removeRelevantXslFile(id);
            }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get the request params and set them in the FileProfile.
     */
    private void setRequestParams(HttpServletRequest p_request, FileProfile p_fp)
    {
        p_fp.setName(p_request.getParameter("fpName"));
        String filterInfo = p_request.getParameter("filterInfo");
        if (filterInfo != null)
        {
            String[] filterInfoArray = filterInfo.split(",");
            long filterId = Long.parseLong(filterInfoArray[0]);
            p_fp.setFilterId(filterId);
            if (filterInfoArray.length == 2)
            {
                String filterTableName = filterInfoArray[1];
                p_fp.setFilterTableName(filterTableName);
            }
            else
                p_fp.setFilterTableName(null);
        }
        String qaFilterInfo = p_request.getParameter("qaFilterInfo");
        if (qaFilterInfo != null)
        {
            long qaFilterId = Long.parseLong(qaFilterInfo);
            p_fp.setQaFilter(QAFilterManager.getQAFilterById(qaFilterId));
        }

        // for bug GBS-2590, by fan
        char[] xmlEncodeChar =
        { '<', '>', '&', '"' };
        String desc = XmlFilterHelper.encodeSpecifiedEntities(
                p_request.getParameter("desc"), xmlEncodeChar);
        p_fp.setDescription(desc);

        p_fp.setCompanyId(Long.parseLong(CompanyThreadLocal.getInstance()
                .getValue()));
        // p_fp.setSupportSid(p_request.getParameter("supportSid") != null);
        // p_fp.setUnicodeEscape(p_request.getParameter("unicodeEscape") !=
        // null);
        // p_fp.setHeaderTranslate(p_request.getParameter("headerTranslate") !=
        // null);
        p_fp.setL10nProfileId(Long.parseLong(p_request
                .getParameter("locProfileId")));
        p_fp.setScriptOnImport(p_request.getParameter("scriptOnImport"));
        p_fp.setScriptOnExport(p_request.getParameter("scriptOnExport"));
        String formatInfo = p_request.getParameter("formatInfo");
        int idx = formatInfo.indexOf(",");
        formatInfo = formatInfo.substring(0, idx);
        p_fp.setKnownFormatTypeId(Long.parseLong(formatInfo));
        p_fp.setCodeSet(p_request.getParameter("codeSet"));

        // String rule = p_request.getParameter("rule");
        // if (rule != null && !rule.startsWith("-1"))
        // {
        // p_fp.setXmlRuleFileId(Long.parseLong(rule));
        // }
        // else
        // {
        // p_fp.setXmlRuleFileId(0);
        // }

        KnownFormatTypeImpl knownFormat = HibernateUtil.get(
                KnownFormatTypeImpl.class, Long.parseLong(formatInfo));
        if (knownFormat != null
                && !KnownFormatType.XML.equals(knownFormat.getName()))
        {
            p_fp.setXmlDtd(null);
        }
        else
        {
            String dtdIds = p_request.getParameter("dtdIds");
            if (dtdIds != null && !"-1".equals(dtdIds))
            {
                p_fp.setXmlDtd(HibernateUtil.get(XmlDtdImpl.class,
                        Long.parseLong(dtdIds)));
            }
            else
            {
                p_fp.setXmlDtd(null);
            }
        }

        if ("0".equals(p_request.getParameter("extGroup")))
        {
            Vector extensionIds = new Vector();
            String ids = p_request.getParameter("extensions");
            StringTokenizer tok = new StringTokenizer(ids, ",");
            while (tok.hasMoreTokens())
            {
                extensionIds.addElement(new Long(tok.nextToken()));
            }
            p_fp.setFileExtensionIds(extensionIds);
        }
        else
        {
            p_fp.setFileExtensionIds(null);
        }

        // default export (primary vs. secondary target files)
        if ("1".equals(p_request.getParameter("exportFiles")))
        {
            p_fp.byDefaultExportStf(false);
        }
        else
        {
            p_fp.byDefaultExportStf(true);
        }

        // String jsFilter = p_request.getParameter("jsFilter");
        // if (jsFilter == null || jsFilter.trim().length() == 0)
        // {
        // jsFilter = null;
        // }
        // p_fp.setJsFilterRegex(jsFilter);

        String terminologyApproval = p_request.getParameter("terminologyRadio");
        if (terminologyApproval != null)
        {
            p_fp.setTerminologyApproval(Integer.parseInt(terminologyApproval));
        }

        String BOMType = p_request.getParameter("bomType");
        p_fp.setBOMType(Integer.parseInt(BOMType));

        String xlfSourceAsUnTranslatedTarget = p_request
                .getParameter("xlfSrcAsTargetRadio");
        if (xlfSourceAsUnTranslatedTarget != null)
        {
            p_fp.setXlfSourceAsUnTranslatedTarget(Integer
                    .parseInt(xlfSourceAsUnTranslatedTarget));
        }

        /**
         * String referenceFPId = p_request.getParameter("xlfFp"); if
         * (referenceFPId == null || referenceFPId == "-1")
         * p_fp.setReferenceFP(0); else
         * p_fp.setReferenceFP(Long.parseLong(referenceFPId));
         */
    }

    /**
     * Get data for main table.
     */
    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session) throws RemoteException, NamingException,
            GeneralException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);
        StringBuffer condition = new StringBuffer();
        String[][] array = new String[][]
        {
        { "uNameFilter", "f.name" },
        { "uLPFilter", "lp.name" },
        { "uSourceFileFormatFilter", "kft.name" },
        { "uCompanyFilter", "c.name" } };
        // filterTableName
        String uFNFilter = (String) sessionMgr.getAttribute("uFNFilter");
        boolean needRemove = false;
        Map<String, String> filres = new HashMap<String, String>();
        if (StringUtils.isNotBlank(uFNFilter))
        {
            condition.append(" and  f.filterTableName IS NOT null");
            filres = FilterHelper.getallFiltersLikeName(StringUtil
                    .transactSQLInjection(uFNFilter.trim()));
            needRemove = true;

        }
        for (int i = 0; i < array.length; i++)
        {
            makeCondition(sessionMgr, condition, array[i][0], array[i][1]);
        }
        Collection fileprofiles = null;
        try
        {
            fileprofiles = ServerProxy.getFileProfilePersistenceManager()
                    .getAllFileProfilesByCondition(condition.toString());
            if (needRemove)
            {
                if (filres.size() > 0)
                {
                    LOOP: for (Iterator iter = fileprofiles.iterator(); iter
                            .hasNext();)
                    {
                        FileprofileVo fileprofilevo = (FileprofileVo) iter
                                .next();
                        FileProfile FileProfile = fileprofilevo
                                .getFileProfile();
                        String filterName = filres.get(FileProfile
                                .getFilterTableName()
                                + FileProfile.getFilterId());
                        if (filterName == null)
                        {
                            iter.remove();
                        }
                    }
                }
                else
                {
                    fileprofiles.clear();
                }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        String numOfPerPage = p_request.getParameter("numOfPageSize");
        if (StringUtil.isNotEmpty(numOfPerPage))
        {
            try
            {
                NUM_PER_PAGE = Integer.parseInt(numOfPerPage);
            }
            catch (Exception e)
            {
                NUM_PER_PAGE = Integer.MAX_VALUE;
            }
        }
        HashMap<Long, String> idViewExtensions = ServerProxy
                .getFileProfilePersistenceManager().getIdViewFileExtensions();
        setTableNavigation(p_request, p_session, (List) fileprofiles,
                new FileProfileComparator(uiLocale, idViewExtensions),
                NUM_PER_PAGE, FileProfileConstants.FILEPROFILE_LIST,
                FileProfileConstants.FILEPROFILE_KEY);

        CVSFileProfileManagerLocal cvsFPManager = new CVSFileProfileManagerLocal();
        ArrayList<CVSFileProfile> cvsfps = (ArrayList<CVSFileProfile>) cvsFPManager
                .getAllCVSFileProfiles();
        ArrayList<String> existCVSFPs = new ArrayList<String>();
        ;
        if (cvsfps != null)
        {
            for (CVSFileProfile f : cvsfps)
            {
                existCVSFPs.add(String.valueOf(f.getFileProfile().getId()));
            }
        }
        p_request.setAttribute("existCVSFPs", existCVSFPs);
        p_request.setAttribute("idViewExtensions", idViewExtensions);
        Hashtable l10nprofiles = ServerProxy.getProjectHandler()
                .getAllL10nProfileNames();
        checkPreReqData(p_request, p_session, l10nprofiles);
    }

    private void makeCondition(SessionManager sessionMgr,
            StringBuffer condition, String par, String sqlparam)
    {
        String uNameFilter = (String) sessionMgr.getAttribute(par);
        if (StringUtils.isNotBlank(uNameFilter))
        {
            condition.append(" and  " + sqlparam + " LIKE '%"
                    + StringUtil.transactSQLInjection(uNameFilter.trim())
                    + "%'");
        }
    }

    /**
     * Search for fileprofiles with certain criteria.
     */
    private FileProfileSearchParameters getSearchCriteria(
            HttpServletRequest p_request, boolean advSearch)
            throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        FileProfileSearchParameters params = new FileProfileSearchParameters();
        String buf = p_request.getParameter("nameOptions");
        params.setFileProfileCondition(buf);
        params.setFileProfileName(p_request.getParameter("nameField"));
        if (advSearch)
        {
            try
            {
                buf = p_request.getParameter("srcFormat");
                if (!buf.equals("-1"))
                {
                    params.setSourceFileFormat(Long.valueOf(buf));
                }
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
            String localizationprofiles = p_request.getParameter("locprofiles");
            if (!localizationprofiles.equals("-1"))

                params.setLocProfilesId(Long.valueOf(localizationprofiles));

        }
        sessionMgr.setAttribute("searchParams", params);
        return params;
    }

    private void handleFilters(HttpServletRequest p_request,
            SessionManager sessionMgr, String action)
    {
        String uNameFilter = (String) p_request.getParameter("uNameFilter");
        String uLPFilter = (String) p_request.getParameter("uLPFilter");
        String uFNFilter = (String) p_request.getParameter("uFNFilter");
        String uSourceFileFormatFilter = (String) p_request
                .getParameter("uSourceFileFormatFilter");
        String uCompanyFilter = (String) p_request
                .getParameter("uCompanyFilter");

        if (p_request.getMethod().equalsIgnoreCase(
                WebAppConstants.REQUEST_METHOD_GET))
        {
            uNameFilter = (String) sessionMgr.getAttribute("uNameFilter");
            uLPFilter = (String) sessionMgr.getAttribute("uLPFilter");
            uFNFilter = (String) sessionMgr.getAttribute("uFNFilter");
            uSourceFileFormatFilter = (String) sessionMgr
                    .getAttribute("uSourceFileFormatFilter");
            uCompanyFilter = (String) sessionMgr.getAttribute("uCompanyFilter");
        }
        sessionMgr.setAttribute("uNameFilter", uNameFilter);
        sessionMgr.setAttribute("uLPFilter", uLPFilter);
        sessionMgr.setAttribute("uFNFilter", uFNFilter);
        sessionMgr.setAttribute("uSourceFileFormatFilter",
                uSourceFileFormatFilter);
        sessionMgr.setAttribute("uCompanyFilter", uCompanyFilter);
    }
}
