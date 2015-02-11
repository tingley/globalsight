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

package com.globalsight.everest.webapp.pagehandler.administration.glossaries;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.glossaries.GlossaryFile;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.util.comparator.GlossaryFileComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.LinkHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * <p>
 * Glossary MainHandler is responsible for:
 * </p>
 * <ol>
 * <li>Displaying the list of available glossaries.</li>
 * <li>Sorting the list of glossaries.</li>
 * <li>Deleting (and updating) existing glossary files.</li>
 * </ol>
 * 
 * For uploading glossaries, see UploadHandler.
 * 
 * @see UploadHandler
 */
public class MainHandler extends PageHandler implements GlossaryConstants
{
    private static final Logger CATEGORY = Logger.getLogger(MainHandler.class
            .getName());

    private static final String UPLOAD_LINK = "upload";

    // Private Members
    private GlossaryState m_state = null;

    // Constructor
    public MainHandler()
    {
    }

    /**
     * Invokes this PageHandler
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
        String value;
        HttpSession session = p_request.getSession();
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        m_state = (GlossaryState) session
                .getAttribute(WebAppConstants.GLOSSARYSTATE);

        if (m_state == null)
        {
            m_state = new GlossaryState();

            session.setAttribute(WebAppConstants.GLOSSARYSTATE, m_state);
        }
        else
        {
            // If object has been set previously in editor screens,
            // including source & target locales, clear them out here
            // so admin/PM sees all files for all locales.
            m_state.setSourceLocale(null);
            m_state.setTargetLocale(null);
        }

        // Evaluate parameters and commands

        // First handle commands that use indexes into the current
        // file list and are thus position-dependent.
        value = (String) p_request.getParameter(GlossaryConstants.DELETE);
        if (value != null)
        {
            deleteGlossaries(p_request
                    .getParameterValues(GlossaryConstants.FILE_CHECKBOXES));
        }

        // NIY
        value = (String) p_request.getParameter(GlossaryConstants.UPDATE);
        if (value != null)
        {
            // update file information (category)
        }

        // Get the comparator, fix for GBS-1693
        GlossaryFileComparator comparator = (GlossaryFileComparator) session
                .getAttribute("Comparator");
        if (comparator == null)
        {
            comparator = new GlossaryFileComparator(Locale.getDefault());
        }

        // Get all the glossaries
        ArrayList glossaries = refreshGlossaries(m_state.getSortColumn(),
                uiLocale);

        value = (String) p_request.getParameter(GlossaryConstants.SORT);
        int column = GlossaryFileComparator.M_SOURCE_LOCALE;
        if (value != null)
        {
            column = Integer.parseInt(value);
            switch (column)
            {
                default:
                case GlossaryFileComparator.M_SOURCE_LOCALE:
                    comparator.setType(GlossaryFileComparator.M_SOURCE_LOCALE);
                    comparator.setAsc_source_locale(!comparator
                            .isAsc_source_locale());
                    break;
                case GlossaryFileComparator.M_TARGET_LOCALE:
                    comparator.setType(GlossaryFileComparator.M_TARGET_LOCALE);
                    comparator.setAsc_target_locale(!comparator
                            .isAsc_target_locale());
                    break;
                case GlossaryFileComparator.M_FILENAME:
                    comparator.setType(GlossaryFileComparator.M_FILENAME);
                    comparator.setAsc_fileName(!comparator.isAsc_fileName());
                    break;
            }
        }

        SortUtil.sort(glossaries, comparator);
        m_state.setGlossaries(glossaries);
        m_state.setComparator(comparator);

        dispatchJSP(p_pageDescriptor, p_request, p_response, p_context);
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    //
    // Private Methods
    //

    // Invoke the correct JSP for this page
    private void dispatchJSP(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        String link = p_request.getParameter(LinkHelper.LINK_NAME);
        if (link != null && link.equals(UPLOAD_LINK))
        {
            // When opening the upload dialog, add required source and
            // target locale info to the state object.
            Locale uiLocale = (Locale) p_request.getSession().getAttribute(
                    WebAppConstants.UILOCALE);
            Collection sources = getAllSourceLocales(uiLocale);
            Collection targets = getAllTargetLocales(uiLocale);

            m_state.setAllSourceLocales(sources);
            m_state.setAllTargetLocales(targets);
        }
    }

    /**
     * Calls the remote server to refresh glossary data
     */
    private ArrayList refreshGlossaries(int p_col, Locale p_locale)
            throws EnvoyServletException
    {
        ArrayList glossaries = null;

        try
        {
            glossaries = ServerProxy.getGlossaryManager().getGlossaries(
                    m_state.getSourceLocale(), m_state.getTargetLocale(),
                    m_state.getCategory(), null);
        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex.getExceptionId(), ex);
        }
        catch (RemoteException ex)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, ex);
        }

        return glossaries;
    }

    /**
     * Calls deleteGlossary() for each glossary file whose id is specified in
     * the array of strings.
     */
    private void deleteGlossaries(String[] p_ids) throws EnvoyServletException
    {
        for (int i = 0; i < p_ids.length; ++i)
        {
            deleteGlossary(Integer.parseInt(p_ids[i]));
        }
    }

    /**
     * Calls the remote server to delete a glossary file.
     */
    private void deleteGlossary(int p_id) throws EnvoyServletException
    {
        try
        {
            GlossaryFile item = (GlossaryFile) m_state.getGlossaries()
                    .get(p_id);
            ServerProxy.getGlossaryManager().deleteGlossary(item);
        }
        catch (Exception ex)
        {
            // Well, as long as the file does not exist now we're happy.
            // If we couldn't delete it it'll show up again in the list.
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Could not delete glossary file: " + ex);
            }
        }
    }

    /**
     * Helper function to get a hold of GlobalSightLocale objects.
     */
    private GlobalSightLocale getLocale(String p_locale)
            throws EnvoyServletException
    {
        try
        {
            LocaleManager manager = ServerProxy.getLocaleManager();
            return manager.getLocaleByString(p_locale);
        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex.getExceptionId(), ex);
        }
        catch (RemoteException ex)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, ex);
        }
    }

    /**
     * Helper function to read all source locales defined in the system as
     * GlobalSightLocale objects.
     */
    private Vector getAllSourceLocales(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            ArrayList al = new ArrayList(ServerProxy.getLocaleManager()
                    .getAllSourceLocales());
            GlobalSightLocaleComparator comp = new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.DISPLAYNAME, p_locale);
            SortUtil.sort(al, comp);
            return new Vector(al);

        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex.getExceptionId(), ex);
        }
        catch (RemoteException ex)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, ex);
        }
    }

    /**
     * Helper function to read all target locales defined in teh system as
     * GlobalSightLocale objects.
     */
    private Vector getAllTargetLocales(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            ArrayList al = new ArrayList(ServerProxy.getLocaleManager()
                    .getAllTargetLocales());
            GlobalSightLocaleComparator comp = new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.DISPLAYNAME, p_locale);
            SortUtil.sort(al, comp);
            return new Vector(al);

        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex.getExceptionId(), ex);
        }
        catch (RemoteException ex)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, ex);
        }
    }
}
