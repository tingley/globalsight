package com.globalsight.everest.webapp.pagehandler.administration.gsedition;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.gsedition.GSEdition;
import com.globalsight.everest.gsedition.GSEditionManagerLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

public class GSEditionBasicHandler extends PageHandler{
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
        GSEditionManagerLocal manager = new GSEditionManagerLocal();
        try
        {
            if (action != null && action.equals("modify")) {
                String id = (String)p_request.getParameter("id");
                
                GSEdition gsEdition = (GSEdition)manager.getGSEditionByID(Integer.parseInt(id));
                
                p_request.setAttribute("gsEdition", gsEdition);
                p_request.setAttribute("edit", "true");
            }
            
            ArrayList<GSEdition> allActions = (ArrayList)manager.getAllGSEdition();
            ArrayList allNames = new ArrayList();
            
            if(allActions != null) {
                for(int i = 0; i < allActions.size(); i++) {
                    allNames.add(((GSEdition)allActions.get(i)).getName());
                }
            }
            
            p_request.setAttribute("allGSEditionNames", allNames);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
        
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }
}
