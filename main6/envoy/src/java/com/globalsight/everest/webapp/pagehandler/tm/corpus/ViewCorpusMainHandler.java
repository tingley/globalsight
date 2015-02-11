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
package com.globalsight.everest.webapp.pagehandler.tm.corpus;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.corpus.CorpusManagerWLRemote;
import com.globalsight.everest.corpus.CorpusContext;
import com.globalsight.everest.corpus.CorpusDoc;
import com.globalsight.util.modules.Modules;
import com.globalsight.everest.corpus.CorpusContextHolder;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;

import com.globalsight.everest.webapp.pagehandler.tm.corpus.CorpusViewBean;
import com.globalsight.everest.webapp.pagehandler.tm.corpus.CorpusXsltHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>PageHandler is responsible for helping processing corpus views.</p>
 */
public class ViewCorpusMainHandler extends PageHandler
{
    private static final Logger c_logger =
        Logger.getLogger(ViewCorpusMainHandler.class);

    public static final String TUV_ID = "tuvId";
    public static final String LOCALE_DBID = "localeDbId";
    public static final String CONTEXT_BEANS = "contextBeans";
    public static final String XSLT_HELPER = "xsltHelper";

    //
    // Constructor
    //
    public ViewCorpusMainHandler()
    {
        super();
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invoke this PageHandler.
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        if (!Modules.isCorpusInstalled())
            throw new EnvoyServletException(new Exception("CorpusTm is not installed"));

        try {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
//        Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
            Long tuvId = new Long((String)p_request.getParameter(TUV_ID));
            Long localeDbId = new Long((String)p_request.getParameter(LOCALE_DBID));
            CorpusManagerWLRemote corpusManager = ServerProxy.getCorpusManager();
            ArrayList context = corpusManager.getCorpusContextsForSegment(tuvId.longValue(),localeDbId.longValue());
            c_logger.debug("got " + context.size() + " context holders.");
            Iterator iter = context.iterator();
            ArrayList contextBeans = new ArrayList();
            while (iter.hasNext())
            {
                CorpusContextHolder holder = (CorpusContextHolder) iter.next();
                CorpusContext src = holder.getSourceContext();
                CorpusContext target = holder.getTargetContext();
                CorpusDoc srcDoc = corpusManager.getCorpusDoc(src.getCuvId());
                CorpusDoc targetDoc = corpusManager.getCorpusDoc(target.getCuvId());
                contextBeans.add(new CorpusViewBean(src,target,srcDoc,targetDoc));
            }
            CorpusXsltHelper xsltHelper = new CorpusXsltHelper(p_request);
            sessionMgr.setAttribute(CONTEXT_BEANS,contextBeans);
            sessionMgr.setAttribute(XSLT_HELPER, xsltHelper);
            super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
        }
        catch (EnvoyServletException ese)
        {
            throw ese;
        }
        catch (ServletException se)
        {
            throw se;
        }
        catch (IOException ioe)
        {
            throw ioe;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }
}

