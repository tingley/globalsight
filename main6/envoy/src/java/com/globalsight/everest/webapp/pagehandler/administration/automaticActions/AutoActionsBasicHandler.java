package com.globalsight.everest.webapp.pagehandler.administration.automaticActions;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.globalsight.everest.autoactions.*;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import javax.servlet.http.HttpSession;

public class AutoActionsBasicHandler extends PageHandler {
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
        AutoActionManagerLocal manager = new AutoActionManagerLocal();
        try
        {
            if (action != null && action.equals("modify")) {
                String id = (String)p_request.getParameter("id");
                
                
                AutoAction autoAction = (AutoAction)manager.getActionByID(Integer.parseInt(id));
                
                p_request.setAttribute("autoAction", autoAction);
                p_request.setAttribute("edit", "true");
            }
            
            ArrayList<AutoAction> allActions = (ArrayList)manager.getAllActions();
            ArrayList allNames = new ArrayList();
            
            if(allActions != null) {
                for(int i = 0; i < allActions.size(); i++) {
                    allNames.add(((AutoAction)allActions.get(i)).getName());
                }
            }
            
            p_request.setAttribute("allActionNames", allNames);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
        
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }
}
