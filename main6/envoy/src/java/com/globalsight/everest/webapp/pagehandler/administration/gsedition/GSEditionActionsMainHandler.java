package com.globalsight.everest.webapp.pagehandler.administration.gsedition;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.gsedition.*;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.util.comparator.GSEditionActivityComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.webservices.client.Ambassador;
import com.globalsight.webservices.client.WebServiceClientHelper;

public class GSEditionActionsMainHandler extends PageHandler{
    private GSEditionManagerLocal gsEditionManager = new GSEditionManagerLocal();
    private GSEditionActivityManagerLocal gsActivityManager = new GSEditionActivityManagerLocal();
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
            if(action != null) {
                if (action.equals("create"))
                {
                    createGSEditionActivity(p_request);
                }
                else if (action.equals("modify"))
                {
                    editGSEditionActivity(p_request);
                }
                
                else if (action.equals("remove"))
                {
                    removeGSEditionActivity(p_request); 
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

    private void dataForTable(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        //long GSEditionID = Integer.parseInt(p_request.getParameter("GSEditionID"));
        //GSEdition edition = gsEditionManager.getGSEditionByID(GSEditionID);
        Vector activities = 
            vectorizedCollection(gsActivityManager.getAllActions());
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, activities,
                           new GSEditionActivityComparator(uiLocale),
                           10, "GSEditionActionList", "GSEditonActionKey");
    }
    
    private void createGSEditionActivity(HttpServletRequest p_request) 
        throws GSEditionException, RemoteException {
        long GSEditionID = Integer.parseInt(p_request.getParameter("GSEdition"));
        GSEdition edition = gsEditionManager.getGSEditionByID(GSEditionID);
        Ambassador ambassador = null;
        String realAccessToken = null;
        
        try{
            ambassador = WebServiceClientHelper.getClientAmbassador(
                edition.getHostName(), 
                edition.getHostPort(), 
                edition.getUserName(),
                edition.getPassword(),
                edition.getEnableHttps());
                
            String fullAccessToken = 
                ambassador.login(edition.getUserName(), edition.getPassword());
            realAccessToken = WebServiceClientHelper.getRealAccessToken(fullAccessToken);
        }catch(Exception e) {
           String msg = e.getMessage();
           String errorInfo = null;
           if (msg != null && (msg.indexOf("Connection timed out") > -1 
                   || msg.indexOf("UnknownHostException") > -1
                   || msg.indexOf("java.net.ConnectException") > -1))
           {
               errorInfo = "Can not connect to server. Please check host name or host port.";
           } 
           else if (msg != null && msg.indexOf("Illegal web service access attempt from IP address") > -1) 
           {
               errorInfo = "Incorrect user name or password, or web service access is not allowed for IP filter is enabled on server.";
           }
           else if (msg != null && msg.indexOf("The username or password may be incorrect") > -1)
           {
               errorInfo = "Unable to login user to GlobalSight. The username or password may be incorrect.";
           }
           else
           {
               errorInfo = msg;
           }
           p_request.setAttribute("wsErrorInfo", errorInfo);
           return;
       }

       try {
            ambassador.getServerVersion(realAccessToken);
       }
       catch(Exception e) {
            p_request.setAttribute("noGSEditionVersion", "true");
            return;
       }

        String name = p_request.getParameter("name");
        long fileProfile = Long.parseLong(p_request.getParameter("fileProfile"));
        int sourceFileReference = Integer.parseInt(p_request.getParameter("sourceFileReference"));
        String description = p_request.getParameter("description");
        
        GSEditionActivity activity = new GSEditionActivity();
        
        if (!gsActivityManager.isActionExist(name, edition)) {
            activity.setName(name);
            activity.setFileProfile(fileProfile);
            activity.setFileProfileName(p_request.getParameter("fileprofileName"));
            activity.setSourceFileReference(sourceFileReference);
            activity.setGsEdition(edition);
            activity.setDescription(description);
        }

        gsActivityManager.createAction(activity);
        
        edition.getGsEditionActivities().add(activity);
    }
    
    private void editGSEditionActivity(HttpServletRequest p_request) 
        throws GSEditionException, RemoteException {
        if(p_request.getParameter("gsEditionActivityID") != null) {
            long id = Integer.parseInt(p_request.getParameter("gsEditionActivityID"));
            GSEditionActivity activity = gsActivityManager.getGSEditionActivityByID(id);
            
            long gsEditionID = Long.parseLong(p_request.getParameter("GSEdition"));
            GSEdition edition = gsEditionManager.getGSEditionByID(gsEditionID);
            
            Ambassador ambassador = null;
            String realAccessToken = null;
            try{
                ambassador = WebServiceClientHelper.getClientAmbassador(
                    edition.getHostName(), 
                    edition.getHostPort(), 
                    edition.getUserName(),
                    edition.getPassword(),
                    edition.getEnableHttps());
                    
                String fullAccessToken = 
                    ambassador.login(edition.getUserName(), edition.getPassword());
                realAccessToken = WebServiceClientHelper.getRealAccessToken(fullAccessToken);
            }catch(Exception e) {
               String msg = e.getMessage();
               String errorInfo = null;
               if (msg != null && (msg.indexOf("Connection timed out") > -1 
                       || msg.indexOf("UnknownHostException") > -1
                       || msg.indexOf("java.net.ConnectException") > -1))
               {
                   errorInfo = "Can not connect to server. Please check host name or host port.";
               } 
               else if (msg != null && msg.indexOf("Illegal web service access attempt from IP address") > -1) 
               {
                   errorInfo = "Incorrect user name or password, or web service access is not allowed for IP filter is enabled on server.";
               }
               else if (msg != null && msg.indexOf("The username or password may be incorrect") > -1)
               {
                   errorInfo = "Unable to login user to GlobalSight. The username or password may be incorrect.";
               }
               else
               {
                   errorInfo = msg;
               }
               p_request.setAttribute("wsErrorInfo", errorInfo);
               return;
           }

           try {
                ambassador.getServerVersion(realAccessToken);
           }
           catch(Exception e) {
                p_request.setAttribute("noGSEditionVersion", "true");
                return;
           }
       
            try {
                if(p_request.getParameter("name") !=null) {
                    activity.setName(p_request.getParameter("name"));
                }
                
                activity.setGsEdition(edition);
                
                long fileProfile = Long.parseLong(p_request.getParameter("fileProfile"));
                activity.setFileProfile(fileProfile);
                
                activity.setFileProfileName(p_request.getParameter("fileprofileName"));
                
                int sourcefile = Integer.parseInt(p_request.getParameter("sourceFileReference"));
                activity.setSourceFileReference(sourcefile);
                
                activity.setDescription(p_request.getParameter("description"));
                gsActivityManager.updateGSEditionActivity(activity);
            } catch (Exception e) {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
            }
        }
    }
    
    /*
     * remove the auto action.
     */
    private void removeGSEditionActivity(HttpServletRequest p_request) 
        throws GSEditionException, RemoteException {
        
        if(p_request.getParameter("id") != null) {
            long id = Integer.parseInt(p_request.getParameter("id"));
      
            try {
                Collection acitivities = gsActivityManager.getActivitiesByActionID
                                         (p_request.getParameter("id"));
                
                if(acitivities != null && acitivities.size() > 0) {
                    p_request.setAttribute("canBeRemoved", "false");
                }
                else {
                    gsActivityManager.removeAction(id);
                }
            } catch (Exception e) {
                throw new 
                    EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
            }
        }
    }
}
