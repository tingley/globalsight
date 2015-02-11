package com.globalsight.everest.webapp.pagehandler.administration.gsedition;

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.everest.gsedition.*;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.webservices.client.Ambassador;
import com.globalsight.webservices.client.WebServiceClientHelper;

public class GSEditionActionsBasicHandler extends PageHandler{
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context)
            throws ServletException, IOException, EnvoyServletException
        {
            String action = p_request.getParameter("action");
            GSEditionActivityManagerLocal manager = new GSEditionActivityManagerLocal();
            GSEditionManagerLocal gsEditionManager = new GSEditionManagerLocal();
            GSEdition edition = new GSEdition();
            
            try
            {
                ArrayList allGSEdition = (ArrayList)gsEditionManager.getAllGSEdition();
                p_request.setAttribute("allGSEdition",  allGSEdition);
                
                if (action != null && action.equals("modify")) {
                    long id = Long.parseLong((String)p_request.getParameter("id"));
                                
                    GSEditionActivity activity = (GSEditionActivity)manager.getGSEditionActivityByID(id);
                    edition = activity.getGsEdition();
                    
                    p_request.setAttribute("gsEditionActivity", activity);
                    p_request.setAttribute("edit", "true");
                }
                
                Collection allActions = manager.getAllActions();
                ArrayList allNames = new ArrayList();
                
                if(allActions != null) {
                    Iterator itr = allActions.iterator();
                    
                    while(itr.hasNext()) {
                        GSEditionActivity element = (GSEditionActivity)itr.next();
                        allNames.add(element.getName());
                    }
                }
                
                p_request.setAttribute("allGSEditionNames", allNames);
                
                
            }
            catch (Exception ge)
            {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
            }
            
            super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
        }
}
