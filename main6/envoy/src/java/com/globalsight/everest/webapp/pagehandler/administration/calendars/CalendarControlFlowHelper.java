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
package com.globalsight.everest.webapp.pagehandler.administration.calendars;

import org.apache.log4j.Logger;


import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.calendar.CalendarManagerException;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Dispatches the user to the correct JSP.
 */
class CalendarControlFlowHelper
    implements ControlFlowHelper, WebAppConstants, CalendarConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            CalendarControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public CalendarControlFlowHelper(HttpServletRequest p_request,
        HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    /**
     * Does the processing then 
     * returns the name of the link to follow
     * 
     * @return 
     * @exception EnvoyServletException
     */
    public String determineLinkToFollow()
        throws EnvoyServletException
    {


        HttpSession p_session = m_request.getSession(false);
        String destinationPage = null;

        // action should only be null for paging purposes
        if (m_request.getParameter(ACTION) == null) 
        {
            destinationPage = "self";
        }
        else if (m_request.getParameter(ACTION).equals(USER_CALS_ACTION)) 
        {
            destinationPage = "userCals1";
        }
        else if (m_request.getParameter(ACTION).equals(HOLIDAYS_ACTION)) 
        {
            destinationPage = "holidays1";
        }
        else if (m_request.getParameter(ACTION).equals(SYS_CALS_ACTION)) 
        {
            destinationPage = "sysCals1";
        }
        else if (m_request.getParameter(ACTION).equals(NEW_ACTION)) 
        {
            destinationPage = "new1";
        }
        else if (m_request.getParameter(ACTION).equals(EDIT_ACTION)) 
        {
            destinationPage = "modify";
        }
        else if (m_request.getParameter(ACTION).equals(SAVE_ACTION)) 
        {
            destinationPage = "save";
        }
        else if (m_request.getParameter(ACTION).equals(MAKE_DEFAULT_ACTION))
        {  
            destinationPage = "default";
        }
        else if (m_request.getParameter(ACTION).equals(DUPLICATE_ACTION))
        {  
            destinationPage = "duplicate";
        }
        else if (m_request.getParameter(ACTION).equals(CANCEL_HOL_ACTION))
        {  
            destinationPage = "cancelHol";
        }
        else if (m_request.getParameter(ACTION).equals(REMOVE_HOL_ACTION))
        {  
            destinationPage = "removeHol";
        }
        else if (m_request.getParameter(ACTION).equals(REMOVE_ACTION))
        {  
            String id = m_request.getParameter("id");

            try
            {
                CalendarHelper.removeSysCalendar(Long.parseLong(id));
            }
            catch (CalendarManagerException ce)
            {
                SessionManager sessionMgr =
                    (SessionManager)p_session.getAttribute(SESSION_MANAGER);
                sessionMgr.setAttribute("dependencies", ce.getMessage());
                return("dependencies");
            }
            catch(GeneralException ge)
            {
                throw new EnvoyServletException(GeneralException.EX_GENERAL, ge);
            }
            catch(IOException ie)
            {
                throw new EnvoyServletException(ie);
            }
            destinationPage = "sysCals1";
        }
        return destinationPage;
    }
}

