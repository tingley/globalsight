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
package com.globalsight.everest.webapp.pagehandler.administration.config.fileextension;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.FileExtensionComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.fileprofile.FileProfileConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.dependencychecking.FileExtensionDependencyChecker;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;

/**
 * FileExtensionPageHandler, A page handler to produce the entry page(index.jsp)
 * for FileExtension management.
 * <p>
 * 
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */
public class FileExtensionMainHandler extends PageHandler
{
    public static final String EXTENSION_KEY = "extension";
    public static final String EXTENSION_LIST = "extensions";

    /**
     * Invokes this PageHandler
     * <p>
     * 
     * @param p_thePageDescriptor
     *            the page descriptor
     * @param p_theRequest
     *            the original request sent from the browser
     * @param p_theResponse
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");

        try
        {
            if ("cancel".equals(action))
            {
                clearSessionExceptTableInfo(session, EXTENSION_KEY);
            }
            else if ("create".equals(action))
            {
                if (FormUtil.isNotDuplicateSubmisson(p_request,
                        FormUtil.Forms.NEW_FILE_EXTENSION))
                {
                    createFileExtension(p_request);
                }
            }
            else if (FileProfileConstants.REMOVE.equals(action))
            {
                removeFileExtension(p_request);
            }

            // checkExtention();

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

    private void removeFileExtension(HttpServletRequest p_request)
    {
        String id = p_request.getParameter("id");
        try
        {
            FileExtensionImpl fileExtension = ServerProxy
                    .getFileProfilePersistenceManager().getFileExtension(
                            Long.valueOf(id));
            String deps = checkDependencies(fileExtension,
                    p_request.getSession());
            if (deps == null)
            {
                ServerProxy.getFileProfilePersistenceManager()
                        .deleteFileExtension(fileExtension);
            }
            else
            {
                SessionManager sessionMgr = (SessionManager) p_request
                        .getSession().getAttribute(
                                WebAppConstants.SESSION_MANAGER);
                sessionMgr.setAttribute("dependencies", deps);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private String checkDependencies(FileExtensionImpl fileExtension,
            HttpSession session)
    {
        ResourceBundle bundle = PageHandler.getBundle(session);
        FileExtensionDependencyChecker depChecker = new FileExtensionDependencyChecker();

        Hashtable catDeps = depChecker.categorizeDependencies(fileExtension);

        StringBuffer deps = new StringBuffer();
        if (catDeps.size() == 0)
        {
            return null;
        }

        deps.append("<span class=\"errorMsg\">");
        Object[] args =
        { bundle.getString("lb_file_extension") };
        deps.append(MessageFormat.format(bundle.getString("msg_dependency"),
                args));

        for (Enumeration e = catDeps.keys(); e.hasMoreElements();)
        {
            String key = (String) e.nextElement();
            deps.append("<p>*** " + bundle.getString(key) + " ***<br>");
            Vector values = (Vector) catDeps.get(key);
            for (int i = 0; i < values.size(); i++)
            {
                deps.append((String) values.get(i));
                deps.append("<br>");
            }
        }
        deps.append("</span>");
        return deps.toString();
    }

    private void createFileExtension(HttpServletRequest p_request)
            throws RemoteException, NamingException, GeneralException,
            FileProfileEntityException
    {
        String ext = p_request.getParameter("nameField").toLowerCase();
        String companyId = CompanyThreadLocal.getInstance().getValue();
        FileExtensionImpl fe = new FileExtensionImpl(ext, companyId);

        ServerProxy.getFileProfilePersistenceManager().createFileExtension(fe);
    }

    /**
     * Get data for main table.
     */
    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session) throws RemoteException, NamingException,
            GeneralException
    {
        Collection fileextensions = ServerProxy
                .getFileProfilePersistenceManager().getAllFileExtensions();
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, (ArrayList) fileextensions,
                new FileExtensionComparator(uiLocale), 10, EXTENSION_LIST,
                EXTENSION_KEY);
    }

    /*
     * Every company must have the 30 type extentions, check the company is
     * short of every extention, and add it to the company.
     */
    private void checkExtention()
    {
        String[] extensions =
        { "htm", "html", "shtml", "jhtml", "txt", "css", "js", "properties",
                "cfm", "cfml", "asp", "jsp", "xml", "doc", "xls", "ppt", "fm",
                "qxd", "cpp", "java", "pdf", "indd", "ai", "docx", "xlsx",
                "pptx", "rtf", "inx", "xlf", "xliff", "rc" };
        String currentId = CompanyThreadLocal.getInstance().getValue();
        long companyId = Long.parseLong(currentId);
        String sql = "from FileExtensionImpl f where f.companyId=:companyId";
        Map map = new HashMap();
        map.put("companyId", companyId);

        List extentionList = HibernateUtil.search(sql, map);
        ArrayList nameList = new ArrayList();

        for (int i = 0; i < extentionList.size(); i++)
        {
            nameList.add(((FileExtensionImpl) extentionList.get(i)).getName());
        }

        ArrayList exCol = new ArrayList();

        for (int i = 0; i < extensions.length; i++)
        {
            if (!nameList.contains(extensions[i]))
            {
                FileExtensionImpl fe = new FileExtensionImpl();
                fe.setCompanyId(companyId);
                fe.setName(extensions[i]);
                exCol.add(fe);
            }
        }

        try
        {
            HibernateUtil.save(exCol);
        }
        catch (Exception e)
        {
        }
        ;
    }
}
