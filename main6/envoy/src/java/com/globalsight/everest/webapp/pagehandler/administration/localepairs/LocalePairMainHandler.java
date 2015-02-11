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
package com.globalsight.everest.webapp.pagehandler.administration.localepairs;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.LocalePairComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.dependencychecking.LocalePairDependencyChecker;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

public class LocalePairMainHandler extends PageHandler
    implements LocalePairConstants, WebAppConstants
{
    /**
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");

        try
        {
            if (isPost(p_request))
            {
                if (LocalePairConstants.CANCEL.equals(action))
                {
                    clearSessionExceptTableInfo(session, 
                                                LocalePairConstants.LP_KEY);
                }
                else if (LocalePairConstants.CREATE.equals(action))
                {
                    createLocalePair(p_request, session);
                }
                else if (LocalePairConstants.CREATE_LOCALE.equals(action))
                {
                    createLocale(p_request, session);
                }
                else if (LocalePairConstants.REMOVE.equals(action))
                {
                    removeLocalePair(p_request, session);
                }
            }
            dataForTable(p_request, session);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }


    /**
     * Check if any objects have dependencies on this Locale Pair.
     * This should be called BEFORE attempting to remove a Locale Pair.
     * <p>
     * 
     */
    private String checkDependencies(LocalePair p_pair,
                                     HttpSession session)
        throws RemoteException, GeneralException
    {
        ResourceBundle bundle = PageHandler.getBundle(session);
        LocalePairDependencyChecker depChecker = new LocalePairDependencyChecker();

        Hashtable catDeps = depChecker.categorizeDependencies(p_pair);

        StringBuffer deps = new StringBuffer();
        if (catDeps.size() == 0)
        {
            return null;
        }

        deps.append("<span class=\"errorMsg\">");
        Object[] args = {bundle.getString("lb_locale_pair")};
        deps.append(MessageFormat.format(bundle.getString("msg_dependency"), args));

        for (Enumeration e = catDeps.keys(); e.hasMoreElements() ;)
        {
            String key = (String)e.nextElement();
            deps.append("<p>*** " + bundle.getString(key) + " ***<br>");
            Vector values = (Vector)catDeps.get(key);
            for (int i = 0 ; i < values.size() ; i++)
            {
                deps.append((String)values.get(i));
                deps.append("<br>");
            }
        }
        deps.append("</span>");
        return deps.toString();
    }
                                                                     

    /**
     * Removes a source/target locale pair from the database; also removes all
     * container roles relying on this pair.
     */
    private void removeLocalePair(HttpServletRequest p_request, HttpSession p_session)
    throws EnvoyServletException, RemoteException 
    {
        String id = (String)p_request.getParameter("id");
        try
        {
            LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
            LocalePair pair = localeMgr.getLocalePairById(Long.parseLong(id));

            // check dependencies first
            String deps = checkDependencies(pair, p_session);
            if (deps == null)
            {
                // removes the locale pair and all the roles associated with it
                localeMgr.removeSourceTargetLocalePair(pair);
            }
            else
            {
                SessionManager sessionMgr = (SessionManager)
                    p_session.getAttribute(WebAppConstants.SESSION_MANAGER);
                sessionMgr.setAttribute(DEPENDENCIES, deps);
            }

        } catch (Exception lme)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, lme);
        }
    }

    /**
     * Adds a source/target locale pair.
     */
    private void createLocalePair(HttpServletRequest p_request, HttpSession p_session)
        throws EnvoyServletException, RemoteException 
    {
        String srcId = (String)p_request.getParameter("sourceLocale");
        String targId = (String)p_request.getParameter("targetLocale");

        try
        {
            LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
            GlobalSightLocale srcLocale = localeMgr.getLocaleById(Long.parseLong(srcId));
            GlobalSightLocale targLocale = localeMgr.getLocaleById(Long.parseLong(targId));
            // all the roles are created by the locale manager
            localeMgr.addSourceTargetLocalePair(srcLocale, targLocale);
        }
        catch (Exception lme)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, lme);
        }
    }

    /**
     * Adds a locale.
     */
    private void createLocale(HttpServletRequest p_request, HttpSession p_session)
        throws EnvoyServletException, RemoteException 
    {
        String language = (String)p_request.getParameter("language");
        String country = (String)p_request.getParameter("country");

        try
        {
            LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
            GlobalSightLocale locale = new GlobalSightLocale(language, country, false);
            localeMgr.addLocale(locale);
        }
        catch (Exception lme)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, lme);
        }
    }

    private void dataForTable(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
        Vector lps = localeMgr.getSourceTargetLocalePairs();
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);


        setTableNavigation(p_request, p_session, lps,
                       new LocalePairComparator(uiLocale),
                       10,
                       LP_LIST, LP_KEY);
    }

}

