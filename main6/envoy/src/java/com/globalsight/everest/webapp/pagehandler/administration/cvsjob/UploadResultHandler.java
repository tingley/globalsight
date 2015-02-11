/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 */
package com.globalsight.everest.webapp.pagehandler.administration.cvsjob;


import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
// Servlet
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
// JDK
import java.io.IOException;


/**
 * UploadResultHandler is responsible for displaying the result of an upload.
 */

public class UploadResultHandler extends PageHandler        
{
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
    	HttpSession session = p_request.getSession();
    	
    	String selectFiles = (String)p_request.getParameter("selectFiles");
    	
    	SessionManager sessionMgr =
            (SessionManager)p_request.getSession().getAttribute(SESSION_MANAGER);
    	
    	sessionMgr.setAttribute("selectFiles", selectFiles);
    	sessionMgr.setAttribute("jobType", "cvsJob");
    	
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    
    //////////////////////////////////////////////////////////////////////
    //  End: Override Methods
    //////////////////////////////////////////////////////////////////////    
}
