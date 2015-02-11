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

import org.hibernate.Transaction;

import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
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
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;

/**
 * FileProfileMainHandler, A page handler to produce the entry page (index.jsp)
 * for DataSources management.
 * 
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */
public class FileProfileMainHandler extends PageHandler
{
    static private final GlobalSightCategory logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger(FileProfileMainHandler.class); 
    
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
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        FileProfileSearchParameters params = (FileProfileSearchParameters) sessionMgr
                .getAttribute("searchParams");

        String action = p_request.getParameter("action");

        try
        {
            if (FileProfileConstants.CANCEL.equals(action))
            {
                clearSessionExceptTableInfo(session,
                        FileProfileConstants.FILEPROFILE_KEY);
                sessionMgr.setAttribute("searchParams", params);
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
            else if (FileProfileConstants.SEARCH_ACTION.equals(action))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=fileprofiles");
                    return;
                }
                params = getSearchCriteria(p_request, false);
            }
            else if (FileProfileConstants.ADV_SEARCH_ACTION.equals(action))
            {
                params = getSearchCriteria(p_request, true);
            }
            dataForTable(p_request, session, params);
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

            updateXslPath(p_request, fp);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private void updateXslPath(HttpServletRequest p_request, FileProfileImpl fp) throws Exception
    {
        String xslPath = p_request.getParameter("tmpXslPath");
        StringBuffer updatedPath = new StringBuffer("");
        logger.info("temporary xsl path: " + xslPath);
        
        if (xslPath != null && !xslPath.equals(""))
        {
            File file = new File(xslPath);
            
            updatedPath.append(xslPath.substring(0, xslPath.lastIndexOf("~TMP")));
            updatedPath.append(fp.getId());
            logger.info("updated xsl path: " + updatedPath.toString());
            
            File updatedFile = new File(updatedPath.toString());
            if (updatedFile.exists())
            {
                File[] files = updatedFile.listFiles();
                for (int i = 0;i < files.length;i++)
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
    
        String xslPath = AmbFileStoragePathUtils.getXslDir().getPath() + "/" + id;
        File xslFiles = new File(xslPath);
        if (xslFiles.exists())
        {
            File[] files = xslFiles.listFiles();
            for (int i = 0;i < files.length;i++)
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
                
                List<FileProfileImpl> fps = (List<FileProfileImpl>)HibernateUtil.search(hql, map);
                for (FileProfileImpl f : fps)
                {
                    f.setName(newName);
                    f.setNewId(newFp.getId());
                }
                
                HibernateUtil.saveOrUpdate(fps);
            }
            
            HibernateUtil.commit(tx);
        }
        catch (Exception e)
        {
            logger.error(e);
            HibernateUtil.rollback(tx);
            throw new EnvoyServletException(e);
        }
        
        String xslPath = AmbFileStoragePathUtils.getXslDir().getPath() + "/" + fp.getId();
        String newXslPath = AmbFileStoragePathUtils.getXslDir().getPath() + "/" + newFp.getId();
        File xslFiles = new File(xslPath);
        if (xslFiles.exists())
        {
            File newXslFile = new File(newXslPath);
            xslFiles.renameTo(newXslFile);
            if (!newXslFile.exists())
            {
                logger.error("Failed to rename XSL temporary path.");
            }
        }
        
        try
        {
            updateXslPath(p_request, newFp);
        } 
        catch (Exception e)
        {
           logger.error(e);
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
            logger.error(e);
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
                String id = idString.split(",")[0];
                FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                        .getFileProfileById(Long.parseLong(id), true);

                // CVSFileProfileManagerLocal cvsFPManager = new
                // CVSFileProfileManagerLocal();
                // cvsFPManager.removeByFileProfileId(fp.getId());

                ServerProxy.getFileProfilePersistenceManager()
                        .deleteFileProfile(fp);

                // Don't really remove
                //removeRelevantXslFile(id);
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
        if(filterInfo != null)
        {
            String[] filterInfoArray = filterInfo.split(",");
            long filterId = Long.parseLong(filterInfoArray[0]);
            p_fp.setFilterId(filterId);
            if(filterInfoArray.length == 2)
            {
                String filterTableName = filterInfoArray[1];
                p_fp.setFilterTableName(filterTableName);
            } else
                p_fp.setFilterTableName(null);
        }
//        int filterId = Integer.parseInt((p_request.getParameter("filterInfo")!=null)? (String)p_request.getParameter("filterInfo") : "-1");
//        p_fp.setFilterId(filterId);
        p_fp.setDescription(p_request.getParameter("desc"));
        p_fp.setCompanyId(CompanyThreadLocal.getInstance().getValue());
        p_fp.setSupportSid(p_request.getParameter("supportSid") != null);
        p_fp.setUnicodeEscape(p_request.getParameter("unicodeEscape") != null);
        p_fp.setHeaderTranslate(p_request.getParameter("headerTranslate") != null);
        p_fp.setL10nProfileId(Long.parseLong(p_request
                .getParameter("locProfileId")));
        p_fp.setScriptOnImport(p_request.getParameter("scriptOnImport"));
        p_fp.setScriptOnExport(p_request.getParameter("scriptOnExport"));
        String formatInfo = p_request.getParameter("formatInfo");
        int idx = formatInfo.indexOf(",");
        formatInfo = formatInfo.substring(0, idx);
        p_fp.setKnownFormatTypeId(Long.parseLong(formatInfo));
        p_fp.setCodeSet(p_request.getParameter("codeSet"));

        String rule = p_request.getParameter("rule");
        if (rule != null && !rule.startsWith("-1"))
        {
            p_fp.setXmlRuleFileId(Long.parseLong(rule));
        }
        else
        {
            p_fp.setXmlRuleFileId(0);
        }
        
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
                p_fp.setXmlDtd(HibernateUtil.get(XmlDtdImpl.class, Long.parseLong(dtdIds)));
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
        
        String jsFilter = p_request.getParameter("jsFilter");
        if (jsFilter == null || jsFilter.trim().length() == 0)
        {
            jsFilter = null;
        }
        p_fp.setJsFilterRegex(jsFilter);
        
        String terminologyApproval = p_request.getParameter("terminologyRadio");
        
        if(terminologyApproval != null) {
            p_fp.setTerminologyApproval(Integer.parseInt(terminologyApproval));
        }
        
        /**
        String referenceFPId = p_request.getParameter("xlfFp");
        if (referenceFPId == null || referenceFPId == "-1")
            p_fp.setReferenceFP(0);
        else
            p_fp.setReferenceFP(Long.parseLong(referenceFPId));
        */
    }

    /**
     * Get data for main table.
     */
    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session, FileProfileSearchParameters p_params)
            throws RemoteException, NamingException, GeneralException
    {
        Collection fileprofiles = null;
        try
        {
            if (p_params == null)
            {
                fileprofiles = ServerProxy.getFileProfilePersistenceManager()
                        .getAllFileProfiles();
            }
            else
            {
                fileprofiles = ServerProxy.getProjectHandler()
                        .findFileProfileTemplates(p_params);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        Hashtable l10nprofiles = ServerProxy.getProjectHandler()
                .getAllL10nProfileNames();

        setTableNavigation(p_request, p_session, (List) fileprofiles,
                new FileProfileComparator(uiLocale, l10nprofiles), 10,
                FileProfileConstants.FILEPROFILE_LIST,
                FileProfileConstants.FILEPROFILE_KEY);

        p_request.setAttribute("l10nprofiles", l10nprofiles);
        CVSFileProfileManagerLocal cvsFPManager = new CVSFileProfileManagerLocal();
        ArrayList<CVSFileProfile> cvsfps = (ArrayList<CVSFileProfile>)cvsFPManager.getAllCVSFileProfiles();
        ArrayList<String> existCVSFPs = new ArrayList<String>();;
        if (cvsfps != null) {
            for (CVSFileProfile f : cvsfps) {
                existCVSFPs.add(String.valueOf(f.getFileProfile().getId()));
            }
        }
        p_request.setAttribute("existCVSFPs", existCVSFPs);
        
        checkPreReqData(p_request, p_session, l10nprofiles);
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
            String localizationprofiles = p_request
                    .getParameter("locprofiles");
            if (!localizationprofiles.equals("-1"))

                params.setLocProfilesId(Long.valueOf(localizationprofiles));

        }
        sessionMgr.setAttribute("searchParams", params);
        return params;
    }
}
