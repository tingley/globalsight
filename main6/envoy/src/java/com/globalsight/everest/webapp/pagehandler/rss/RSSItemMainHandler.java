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

import org.apache.log4j.Logger;

// Envoy packages
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This page handler produces the index.jsp page in login module.
 */
public class RSSItemMainHandler extends PageHandler
{
	    private static final Logger CATEGORY =
	        Logger.getLogger(RSSItemMainHandler.class);

	    /**
	     * Invokes this PageHandler.
	     */
	    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
	        HttpServletRequest p_request, HttpServletResponse p_response,
	        ServletContext p_context)
	        throws ServletException, IOException, EnvoyServletException
	    {
	        HttpSession session = p_request.getSession();
	        SessionManager sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);
	        ResourceBundle bundle = getBundle(session);
	        RSSPersistenceManager rssManager = ServerProxy.getRSSPersistenceManager();
	        
	        String returnMsg = "";
	        String action = p_request.getParameter("action");
	        String channelId = p_request.getParameter("channelId");
	        if (channelId == null)
	        	channelId = (String)sessionMgr.getAttribute("RSS_CHANNEL_ID");
	        else
	        	sessionMgr.setAttribute("RSS_CHANNEL_ID", channelId);
	        String sCurrentPageNumber = p_request.getParameter("page");
	        int currentPageNum = 1;
	        try {
				currentPageNum = Integer.parseInt(sCurrentPageNumber);
			} catch (NumberFormatException nfe) {
				currentPageNum = 1;
			}
	        Feed feed = rssManager.getFeed(Long.parseLong(channelId));
	        if (action != null && "refresh".equals(action)) {
	    		String rssUrl = feed.getRssUrl();
	        	Feed newFeed = RSSUtil.getFeedByURL(rssUrl);
	        	if (newFeed != null) {
			        ArrayList<String> translated = RSSUtil.getTranslatedItems(feed);
		    		rssManager.unsubscribe(rssUrl);
		    		
	    			RSSUtil.refreshFeed(newFeed, translated);
	   				int result = rssManager.subscribe(newFeed); 
	   				if (result == 1) {
	   					returnMsg = bundle.getString("msg_rss_refresh_succ");
	   				} else if (result == -1 ) {
	   					returnMsg = bundle.getString("msg_rss_refresh_fail");
	   				}
	   				feed = newFeed;
	        	} else {
	        		returnMsg = bundle.getString("msg_rss_refresh_fail");
	        	}
	        }
	        sessionMgr.setAttribute("RSS_CHANNEL_ID", String.valueOf(feed.getId()));
	        sessionMgr.setAttribute(RSSConstants.FEED, feed);
	        
	        sessionMgr.setAttribute("jobName", null);
        	sessionMgr.setAttribute(WebAppConstants.PROJECT_ID, null);
        	sessionMgr.setAttribute("notes", null);
	        
	        //return all feed URLs in List.
	        ArrayList result = rssManager.getItemsForPage(feed.getRssUrl(), currentPageNum, 20);
	        List allItems = (List)result.get(0);
	        int[] params = (int[])result.get(1);
	        sessionMgr.setAttribute(RSSConstants.ALL_ITEM, allItems);
	        sessionMgr.setAttribute("Page_Params", params);
	        sessionMgr.setAttribute(RSSConstants.RETURN_MESSAGE, returnMsg);
    		
	        //Call parent invokePageHandler() to set link beans and invoke JSP
	        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
	    }
}
