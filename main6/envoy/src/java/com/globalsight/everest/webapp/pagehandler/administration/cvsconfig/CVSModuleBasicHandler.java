package com.globalsight.everest.webapp.pagehandler.administration.cvsconfig;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.globalsight.everest.cvsconfig.*;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.util.GeneralException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.NamingException;
import javax.servlet.http.HttpSession;

public class CVSModuleBasicHandler extends PageHandler 
                                       implements CVSConfigConstants {
    /**
     * Invokes this PageHandler
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
            if (action.equals(CVSConfigConstants.UPDATE)) {
                String id = (String)p_request.getParameter("id");
                SessionManager sessionMgr =
                    (SessionManager)session.getAttribute(SESSION_MANAGER);
                CVSServerManagerLocal manager = new CVSServerManagerLocal();
                
                CVSModule module = (CVSModule)manager.getModule(Integer.parseInt(id));
                
                sessionMgr.setAttribute(CVSConfigConstants.CVS_MODULE, module);
                p_request.setAttribute("edit", "true");
            }
            
            setCVSModule(p_request);
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
     * Get list of all company names.  Needed in jsp to determine duplicate names.
     */
    private void setCVSModule(HttpServletRequest p_request)
        throws RemoteException, NamingException, GeneralException
    {
        CVSServerManagerLocal manager = new CVSServerManagerLocal();
        p_request.setAttribute(CVSConfigConstants.CVS_SERVER_LIST, manager.getAllServer());
    }
}
