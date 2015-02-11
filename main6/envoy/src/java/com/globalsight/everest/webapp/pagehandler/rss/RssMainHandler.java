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
package com.globalsight.everest.webapp.pagehandler.rss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.SortUtil;

/**
 * This page handler produces the index.jsp page in login module.
 */
public class RssMainHandler extends PageHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(RssMainHandler.class);

    /**
     * Invokes this PageHandler.
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        ResourceBundle bundle = getBundle(session);
        RSSPersistenceManager rssManager = ServerProxy
                .getRSSPersistenceManager();

        String returnMsg = "";

        String action = p_request.getParameter("action");
        String rssUrl = p_request.getParameter("rssUrlName");

        // subscribe a new RSS
        if (action != null && "subscribe".equals(action))
        {
            if (rssUrl != null && !"".equals(rssUrl.trim()))
            {
                rssUrl = rssUrl.trim();

                Feed feed = RSSUtil.getFeedByURL(rssUrl);
                if (feed != null)
                {
                    int result = rssManager.subscribe(feed);
                    if (result == 1)
                    {
                        returnMsg = bundle.getString("msg_rss_add_succ");
                    }
                    else if (result == -1)
                    {
                        returnMsg = bundle.getString("msg_rss_add_fail");
                    }
                    else
                    {
                        returnMsg = bundle.getString("msg_rss_add_exist");
                    }
                }
                else
                    returnMsg = bundle.getString("msg_rss_add_fail");
            }
        }
        else if (action != null && "unsubscribe".equals(action))
        {
            String id = p_request.getParameter("id");
            Feed feed = rssManager.getFeed(Long.parseLong(id));
            rssManager.unsubscribe(feed.getRssUrl());
        }
        else if (action != null && "refresh".equals(action))
        {
            String id = p_request.getParameter("id");
            Feed feed = rssManager.getFeed(Long.parseLong(id));
            ArrayList<String> translated = RSSUtil.getTranslatedItems(feed);
            rssUrl = feed.getRssUrl();
            rssManager.unsubscribe(rssUrl);
            feed = RSSUtil.getFeedByURL(rssUrl);
            if (feed != null)
            {
                RSSUtil.refreshFeed(feed, translated);
                int result = rssManager.subscribe(feed);
                if (result == 1)
                {
                    returnMsg = bundle.getString("msg_rss_refresh_succ");
                }
                else if (result == -1)
                {
                    returnMsg = bundle.getString("msg_rss_refresh_fail");
                }
            }
        }
        // return all feed URLs in List.
        List allFeedsList = null;
        if (action != null && "search".equals(action))
        {
            allFeedsList = rssManager.getAllRSSFeeds(rssUrl);
        }
        else
            allFeedsList = rssManager.getAllRSSFeeds();
        SortUtil.sort(allFeedsList, new RSSFeedComparator(uiLocale));
        sessionMgr.setAttribute(RSSConstants.ALL_FEED, allFeedsList);
        sessionMgr.setAttribute(RSSConstants.RETURN_MESSAGE, returnMsg);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }
}
