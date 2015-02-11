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

package com.globalsight.everest.webapp.pagehandler.aligner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.fileextension.FileExtension;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile;
import com.globalsight.everest.aligner.AlignerManager;
import com.globalsight.everest.aligner.AlignerPackageOptions;
import com.globalsight.everest.aligner.AlignerPackageOptions.FilePair;
import com.globalsight.everest.aligner.AlignmentStatus;
import com.globalsight.everest.localemgr.CodeSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.LocaleComparator;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * This page handler is responsible for creating aligner packages.
 */

public class AlignerPackagePageHandler extends PageHandler implements
        WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(AlignerPackagePageHandler.class);

    //
    // Static Members
    //
    static private AlignerManager s_manager = null;

    //
    // Constructor
    //
    public AlignerPackagePageHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getAlignerManager();
            }
            catch (GeneralException ex)
            {
                // ignore.
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
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        String action = (String) p_request.getParameter(GAP_ACTION);
        String options = (String) p_request.getParameter(GAP_OPTIONS);

        if (options != null)
        {
            // options are posted as UTF-8 string
            options = EditUtil.utf8ToUnicode(options);
        }

        try
        {
            AlignerPackageOptions gapOptions = (AlignerPackageOptions) sessionMgr
                    .getAttribute(GAP_OPTIONS);

            if (gapOptions != null)
            {
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("INITIALIZING ALIGNER OPTIONS " + options);
                }

                if (options != null)
                {
                    gapOptions.init(options);
                }
            }
            else
            {
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("INITIALIZING ALIGNER PACKAGE CREATION");
                }

                gapOptions = new AlignerPackageOptions();

                sessionMgr.setAttribute(GAP_OPTIONS, gapOptions);
            }

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Action = " + action + "\n"
                        + gapOptions.getXml());
            }

            if (action == null || action.equals(GAP_ACTION_NEWPACKAGE))
            {
                // Fetch format types, xml rules, locales, encodings,
                // extensions; keep a cache in session manager.

                if (sessionMgr.getAttribute(GAP_FORMATTYPES) == null)
                {
                    ArrayList formatTypes = getFormatTypes();
                    ArrayList rules = getXmlRules();
                    ArrayList locales = getLocales(uiLocale);
                    ArrayList encodings = getEncodings();
                    ArrayList extensions = getExtensions();
                    SortUtil.sort(extensions,
                            new StringComparator(Locale.getDefault()));
                    List gapPackages = s_manager.getAllPackages();
                    List packageNames = new ArrayList();
                    for (int i = 0; i < gapPackages.size(); i++)
                    {
                        AlignmentStatus status = (AlignmentStatus) gapPackages
                                .get(i);
                        packageNames.add(status.getPackageName().replaceAll(
                                "&amp;", "&"));
                    }
                    sessionMgr.setAttribute(GAP_FORMATTYPES, formatTypes);
                    sessionMgr.setAttribute(GAP_RULES, rules);
                    sessionMgr.setAttribute(GAP_LOCALES, locales);
                    sessionMgr.setAttribute(GAP_ENCODINGS, encodings);
                    sessionMgr.setAttribute(GAP_EXTENSIONS, extensions);
                    sessionMgr.setAttribute(GAP_PACKAGE_NAMES, packageNames);
                }
            }
            else if (action.equals(GAP_ACTION_SELECTFILES))
            {
                prepareFileList(p_request, gapOptions.getExtensions(),
                        sessionMgr);

                gapOptions.clearFilePairs();
                gapOptions.addAllFilePairs(getFileList(sessionMgr));

                String temp;
                temp = p_request.getParameter(GAP_CURRENTFOLDERSRC);
                temp = EditUtil.utf8ToUnicode(temp);
                if (temp != null)
                {
                    session.setAttribute(GAP_CURRENTFOLDERSRC, temp);
                }

                temp = p_request.getParameter(GAP_CURRENTFOLDERTRG);
                temp = EditUtil.utf8ToUnicode(temp);
                if (temp != null)
                {
                    session.setAttribute(GAP_CURRENTFOLDERTRG, temp);
                }

                // turn off cache. do both. "pragma" for the older browsers.
                p_response.setHeader("Pragma", "no-cache");
                p_response.setHeader("Cache-Control", "no-cache");
                p_response.addHeader("Cache-Control", "no-store");
                p_response.addHeader("Cache-Control", "max-age=0");
            }
            else if (action.equals(GAP_ACTION_ALIGNOPTIONS))
            {
                // do nothing
            }
            else if (action.equals(GAP_ACTION_CREATEPACKAGE))
            {
                // start package creation in background

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Creating aligner package with options "
                            + gapOptions.getXml());
                }

                s_manager.batchAlign(gapOptions, getUser(session));
            }

            // clean up performed in AlignerPageHandler.
        }
        catch (Exception ex)
        {
            CATEGORY.error("aligner package error", ex);

            // JSP needs to clear this.
            sessionMgr.setAttribute(GAP_ERROR, ex.toString());
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    //
    // Private Methods
    //

    private ArrayList getFormatTypes() throws Exception
    {
        ArrayList result = new ArrayList();

        // result is an ordered collection of KnownFormatType objects.
        Collection tmp = ServerProxy.getFileProfilePersistenceManager()
                .getAllKnownFormatTypes();

        for (Iterator it = tmp.iterator(); it.hasNext();)
        {
            KnownFormatType type = (KnownFormatType) it.next();

            result.add(type);
        }

        return result;
    }

    private ArrayList getXmlRules() throws Exception
    {
        ArrayList result = new ArrayList();

        // result is an ordered collection of XmlRuleFile objects
        Collection tmp = ServerProxy.getXmlRuleFilePersistenceManager()
                .getAllXmlRuleFiles();

        for (Iterator it = tmp.iterator(); it.hasNext();)
        {
            XmlRuleFile rule = (XmlRuleFile) it.next();

            result.add(rule.getName());
        }

        return result;
    }

    private ArrayList getLocales(Locale p_uiLocale) throws Exception
    {
        ArrayList result = new ArrayList();

        // result is an unordered collection of GlobalSightLocale objects
        Vector locales = ServerProxy.getLocaleManager().getAvailableLocales();

        SortUtil.sort(locales, new LocaleComparator(2, p_uiLocale));

        // Result contains the GlobalSightLocale objects.
        for (int i = 0, max = locales.size(); i < max; i++)
        {
            GlobalSightLocale locale = (GlobalSightLocale) locales.get(i);

            result.add(locale);
        }

        return result;
    }

    private ArrayList getEncodings() throws Exception
    {
        ArrayList result = new ArrayList();

        // result is an un-ordered collection of strings, needs to be
        // sorted with win-1252 and utf-8 in front.
        Collection tmp = ServerProxy.getLocaleManager().getAllCodeSets();

        for (Iterator it = tmp.iterator(); it.hasNext();)
        {
            CodeSet enc = (CodeSet) it.next();

            result.add(enc.getCodeSet());
        }

        return result;
    }

    private ArrayList getExtensions() throws Exception
    {
        ArrayList result = new ArrayList();

        // result is an ordered collection of FileExtension objects
        Collection tmp = ServerProxy.getFileProfilePersistenceManager()
                .getAllFileExtensions();

        for (Iterator it = tmp.iterator(); it.hasNext();)
        {
            FileExtension ext = (FileExtension) it.next();

            result.add(ext.getName());
        }

        return result;
    }

    static private File s_cxeBaseDir = null;

    // static public File getCXEBaseDir()
    // {
    // if (s_cxeBaseDir == null)
    // {
    // try
    // {
    // SystemConfiguration config = SystemConfiguration.getInstance();
    // s_cxeBaseDir = new File(config.getStringParameter(
    // SystemConfigParamNames.CXE_DOCS_DIR));
    // }
    // catch (Exception e)
    // {
    // CATEGORY.error(e.getMessage(), e);
    // throw new RuntimeException(e.getMessage());
    // }
    // }
    //
    // return s_cxeBaseDir;
    // }

    static public String getAbsolutePath(String p_absolute)
    {
        // return getCXEBaseDir().getPath() + File.separator + p_absolute;
        return AmbFileStoragePathUtils.getCxeDocDirPath() + File.separator
                + p_absolute;
    }

    static public String getRelativePath(File p_parent, File p_absolute)
    {
        String parent;

        if (p_parent.getPath().endsWith(File.separator))
        {
            parent = p_parent.getPath();
        }
        else
        {
            parent = p_parent.getPath() + File.separator;
        }

        String absolute = p_absolute.getPath();

        return absolute.substring(parent.length());
    }

    /**
     * Obtains the file list and adds or removes the files selected from the
     * selected list.
     */
    private void prepareFileList(HttpServletRequest p_request,
            ArrayList p_extensions, SessionManager p_sessionMgr)
    {
        // Must be first, initializes GAP_FILELIST.
        ArrayList fileList = getFileList(p_sessionMgr);

        String fileAction = (String) p_request.getParameter("fileAction");
        if (fileAction == null)
        {
            return;
        }

        String temp = (String) p_request.getParameter("filePair");
        temp = EditUtil.utf8ToUnicode(temp);
        String[] pairs = temp.split("\\|");

        for (int i = 0, max = pairs.length; i < max; i += 2)
        {
            String srcFile = pairs[i];
            String trgFile = pairs[i + 1];

            FilePair pair = new FilePair(srcFile, trgFile);

            if (fileAction.equals("add"))
            {
                fileList.add(pair);
            }
            else if (fileAction.equals("remove"))
            {
                fileList.remove(pair);
            }
        }
    }

    private ArrayList getFileList(SessionManager p_sessionMgr)
    {
        ArrayList result = (ArrayList) p_sessionMgr.getAttribute(GAP_FILELIST);

        if (result == null)
        {
            result = new ArrayList();
            p_sessionMgr.setAttribute(GAP_FILELIST, result);
        }

        return result;
    }
}
