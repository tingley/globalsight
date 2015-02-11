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

import com.globalsight.everest.glossaries.GlossaryFile;
import com.globalsight.everest.glossaries.GlossaryException;
import com.globalsight.everest.glossaries.GlossaryUpload;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.LinkHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;


import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.GeneralException;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;

import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.rmi.RemoteException;

/**
 * <p>Glossary MainHandler is responsible for:</p>
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
public class MainHandler
    extends PageHandler
    implements GlossaryConstants
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            MainHandler.class.getName());

    private static final String UPLOAD_LINK = "upload";

    //
    // Private Members
    //
    private GlossaryState m_state = null;

    //
    // Constructor
    //
    public MainHandler()
    {
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException,
               IOException,
               EnvoyServletException
    {
        String value;
        HttpSession session = p_request.getSession();
        Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

        m_state = (GlossaryState)session.getAttribute(
            WebAppConstants.GLOSSARYSTATE);

        if (m_state == null)
        {
            m_state = new GlossaryState ();

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
        value = (String)p_request.getParameter(GlossaryConstants.DELETE);
        if (value != null)
        {
            deleteGlossaries(p_request.getParameterValues(
                GlossaryConstants.FILE_CHECKBOXES));
        }

        // NIY
        value = (String)p_request.getParameter(GlossaryConstants.UPDATE);
        if (value != null)
        {
            // update file information (category)
        }

        // Now reload the list and, optionally, re-sort it.
        refreshGlossaries(m_state.getSortColumn(), uiLocale);

        value = (String)p_request.getParameter(GlossaryConstants.SORT);
        if (value != null)
        {
            int column = Integer.parseInt(value);
            m_state.setSortColumn(column);
            sortFiles(column, uiLocale);
        }

        dispatchJSP(p_pageDescriptor, p_request, p_response, p_context);
        super.invokePageHandler(p_pageDescriptor, p_request, 
                                p_response, p_context);
    }

    //
    // Private Methods
    //

    // Invoke the correct JSP for this page
    private void dispatchJSP(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException,
               IOException,
               EnvoyServletException
    {
        String link = p_request.getParameter(LinkHelper.LINK_NAME);
        if (link != null && link.equals(UPLOAD_LINK))
        {
            // When opening the upload dialog, add required source and
            // target locale info to the state object.
	    Locale uiLocale = (Locale) p_request.getSession().getAttribute(WebAppConstants.UILOCALE);
            Collection sources = getAllSourceLocales(uiLocale);
            Collection targets = getAllTargetLocales(uiLocale);

            m_state.setAllSourceLocales(sources);
            m_state.setAllTargetLocales(targets);
        }        
    }

    /**
     * Calls the remote server to refresh glossary data
     */
    private void refreshGlossaries(int p_col, Locale p_locale)
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

        m_state.setGlossaries(glossaries);

        sortFiles(p_col, p_locale);
    }

    /**
     * Calls deleteGlossary() for each glossary file whose id is
     * specified in the array of strings.
     */
    private void deleteGlossaries(String[] p_ids)
        throws EnvoyServletException
    {
        for (int i = 0; i < p_ids.length; ++i)
        {
            deleteGlossary(Integer.parseInt(p_ids[i]));
        }
    }

    /**
     * Calls the remote server to delete a glossary file.
     */
    private void deleteGlossary(int p_id)
        throws EnvoyServletException
    {
        try
        {
            GlossaryFile item = (GlossaryFile)m_state.getGlossaries().get(p_id);
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
     * Helper function to read all source locales defined in the
     * system as GlobalSightLocale objects.
     */
    private Vector getAllSourceLocales(Locale p_locale)
        throws EnvoyServletException
    {
        try
        {
	    ArrayList al = new ArrayList(ServerProxy.getLocaleManager().getAllSourceLocales());
	    GlobalSightLocaleComparator comp =
		new GlobalSightLocaleComparator(GlobalSightLocaleComparator.DISPLAYNAME,
						p_locale);
	    Collections.sort(al,comp);
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
     * Helper function to read all target locales defined in teh
     * system as GlobalSightLocale objects.
     */
    private Vector getAllTargetLocales(Locale p_locale)
        throws EnvoyServletException
    {
        try
        {
	    ArrayList al = new ArrayList(ServerProxy.getLocaleManager().getAllTargetLocales());
	    GlobalSightLocaleComparator comp =
		new GlobalSightLocaleComparator(GlobalSightLocaleComparator.DISPLAYNAME,
						p_locale);
	    Collections.sort(al,comp);
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
     * Sorts the list of glossary files in the m_state object by
     * column. Column 1 = source locale, column 2 = target locale,
     * column 3 = category (not implemented yet), column 4 =
     * filename. The sorting is done according to the user's ui
     * locale. The result is again stored in the m_state object.
     */
    private void sortFiles(int p_column, final Locale p_locale)
    {
        if (m_state.getGlossaries().size() <= 1)
        {
            return;
        }

        Object[] files = m_state.getGlossaries().toArray();
        ArrayList sorted = new ArrayList(files.length);

        switch (p_column)
        {
        case 1:
            Arrays.sort(files, new Comparator()
                {
                    public int compare(Object o1, Object o2)
                    {
                        GlossaryFile f1 = (GlossaryFile)o1;
                        GlossaryFile f2 = (GlossaryFile)o2;

                        Collator c = Collator.getInstance(p_locale);
                                                
                        String f1_locale = f1.isForAnySourceLocale() ? 
                            GlossaryUpload.KEY_ANY_SOURCE_LOCALE : 
                            f1.getSourceLocale().toString();
                        String f2_locale = f2.isForAnySourceLocale() ? 
                            GlossaryUpload.KEY_ANY_SOURCE_LOCALE : 
                            f2.getSourceLocale().toString();
                            
                        //return c.compare(f1.getSourceLocale().toString(),
                        //    f2.getSourceLocale().toString());
                        return c.compare(f1_locale, f2_locale);
                    }

                    public boolean equals(Object obj) { return this == obj; }
                });
            break;
        case 2:
            Arrays.sort(files, new Comparator()
                {
                    public int compare(Object o1, Object o2)
                    {
                        GlossaryFile f1 = (GlossaryFile)o1;
                        GlossaryFile f2 = (GlossaryFile)o2;

                        Collator c = Collator.getInstance(p_locale);

                        String f1_locale = f1.isForAnyTargetLocale() ? 
                            GlossaryUpload.KEY_ANY_TARGET_LOCALE : 
                            f1.getTargetLocale().toString();
                        String f2_locale = f2.isForAnyTargetLocale() ? 
                            GlossaryUpload.KEY_ANY_TARGET_LOCALE :
                            f2.getTargetLocale().toString();

                        //return c.compare(f1.getTargetLocale().toString(),
                        //    f2.getTargetLocale().toString());
                        return c.compare(f1_locale, f2_locale);                            
                    }

                    public boolean equals(Object obj) { return this == obj; }
                });
            break;
        case 3:
            Arrays.sort(files, new Comparator()
                {
                    public int compare(Object o1, Object o2)
                    {
                        GlossaryFile f1 = (GlossaryFile)o1;
                        GlossaryFile f2 = (GlossaryFile)o2;

                        Collator c = Collator.getInstance(p_locale);

                        return c.compare(f1.getCategory(),
                            f2.getCategory());
                    }

                   public boolean equals(Object obj) { return this == obj; }
                });
        case 4:
            Arrays.sort(files, new Comparator()
                {
                    public int compare(Object o1, Object o2)
                    {
                        GlossaryFile f1 = (GlossaryFile)o1;
                        GlossaryFile f2 = (GlossaryFile)o2;

                        Collator c = Collator.getInstance(p_locale);

                        return c.compare(f1.getFilename(),
                            f2.getFilename());
                    }

                   public boolean equals(Object obj) { return this == obj; }
                });
            break;
        default:
            // do nothing
            break;
        }

        for (int i = 0; i < files.length; ++i)
        {
            sorted.add(files[i]);
        }

        m_state.setGlossaries(sorted);
    }
}

